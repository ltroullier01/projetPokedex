/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lael.ia.projetpokedex.database;

/**
 * Classe utilitaire chargée de gérer la connexion à la base SQLite
 * et d'initialiser sa structure (tables et données de référence).
 * 
 * @author laelt
 */

// Importation des classes standard JDBC (Java Database Connectivity) pour dialoguer avec la base
import java.sql.Connection;        // Représente une session ouverte avec la base de données
import java.sql.DriverManager;     // Gère les pilotes JDBC et permet d'ouvrir la connexion
import java.sql.PreparedStatement; // Permet d'exécuter des requêtes SQL paramétrées (sécurisées et rapides)
import java.sql.ResultSet;         // Contient les résultats renvoyés par une requête SELECT
import java.sql.SQLException;      // Exception levée en cas d'erreur SQL ou de connexion
import java.sql.Statement;         // Permet d'exécuter des requêtes SQL simples sans paramètres

public class DatabaseManager {

    // Chaîne de connexion JDBC : indique qu'on utilise le pilote SQLite
    // et que le fichier "pokedex.db" sera créé à la racine du projet NetBeans.
    private static final String URL = "jdbc:sqlite:pokedex.db";

    /**
     * Ouvre et retourne une connexion active vers la base SQLite.
     * Active systématiquement la vérification des clés étrangères.
     * 
     * @return Un objet Connection prêt à être utilisé par les classes DAO.
     * @throws SQLException Si l'ouverture du fichier .db échoue.
     */
    public static Connection getConnection() throws SQLException {
        // Ouvre la connexion (crée le fichier pokedex.db s'il n'existe pas encore)
        Connection conn = DriverManager.getConnection(URL);
        
        // Le bloc "try-with-resources" ferme automatiquement le Statement à la fin du bloc
        try (Statement stmt = conn.createStatement()) {
            // Sous SQLite, les clés étrangères (FOREIGN KEY) sont désactivées par défaut
            // à chaque nouvelle connexion : cette commande force leur activation.
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    /**
     * Initialise la structure de la base de données au démarrage de l'application :
     * 1. Crée les tables "types" et "pokemons" si elles n'existent pas.
     * 2. Remplit la table "types" avec les 18 types officiels si elle est vide.
     */
    public static void initialiserBase() {
        // Requête SQL (Text Block Java) pour créer la table de référence des types
        String sqlCreateTypes = """
            CREATE TABLE IF NOT EXISTS types (
                id INTEGER PRIMARY KEY AUTOINCREMENT, -- Identifiant unique généré automatiquement
                nom TEXT NOT NULL UNIQUE,             -- Nom du type (obligatoire et sans doublon)
                couleur_hex TEXT NOT NULL             -- Code couleur CSS/Hexa pour l'affichage UI
            );
            """;

        // Requête SQL pour créer la table principale des Pokémon
        String sqlCreatePokemons = """
            CREATE TABLE IF NOT EXISTS pokemons (
                id INTEGER PRIMARY KEY AUTOINCREMENT, -- Clé primaire unique du Pokémon sauvegardé
                numero_pokedex INTEGER NOT NULL,      -- Numéro officiel dans le Pokédex
                nom TEXT NOT NULL,                    -- Nom de l'espèce (ex: Pikachu)
                surnom TEXT,                          -- Surnom optionnel (peut être NULL)
                type1_id INTEGER NOT NULL,            -- Référence obligatoire vers l'id du 1er type
                type2_id INTEGER,                     -- Référence optionnelle (NULL si mono-type)
                niveau INTEGER DEFAULT 1,             -- Valeur par défaut = 1 si non précisée
                pv INTEGER DEFAULT 50,                -- Points de vie par défaut
                attaque INTEGER DEFAULT 50,           -- Statistique d'attaque par défaut
                defense INTEGER DEFAULT 50,           -- Statistique de défense par défaut
                vitesse INTEGER DEFAULT 50,           -- Statistique de vitesse par défaut
                -- Définition des liens relationnels vers la table "types" :
                FOREIGN KEY (type1_id) REFERENCES types(id),
                FOREIGN KEY (type2_id) REFERENCES types(id)
            );
            """;

        // Ouvre la connexion et un Statement, puis les ferme automatiquement à la sortie du try
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Exécute les deux requêtes de création de tables
            stmt.execute(sqlCreateTypes);
            stmt.execute(sqlCreatePokemons);

            // Vérifie si la table "types" est vide pour éviter d'insérer les 18 types en double
            // à chaque lancement du programme
            if (estTableTypesVide(conn)) {
                peuplerTypesParDefaut(conn);
            }

            System.out.println("Base de données Pokédex initialisée avec succès.");

        } catch (SQLException e) {
            // Affiche l'erreur dans la console d'erreur si la création échoue
            System.err.println("Erreur critique lors de l'initialisation de la base : " + e.getMessage());
        }
    }

    /**
     * Vérifie si la table "types" contient déjà des enregistrements.
     * 
     * @param conn La connexion SQLite active.
     * @return true si la table contient 0 ligne, false sinon.
     * @throws SQLException En cas d'erreur lors de la requête COUNT.
     */
    private static boolean estTableTypesVide(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM types";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            // rs.next() place le curseur sur la première ligne du résultat
            // rs.getInt(1) récupère la valeur de la 1ère colonne (le résultat du COUNT)
            return rs.next() && rs.getInt(1) == 0;
        }
    }

    /**
     * Insère les 18 types Pokémon officiels et leur couleur associée dans la table "types".
     * Utilise une transaction SQL et un traitement par lot (Batch) pour optimiser les performances.
     * 
     * @param conn La connexion SQLite active.
     * @throws SQLException Si une erreur survient (déclenche un rollback complet).
     */
    private static void peuplerTypesParDefaut(Connection conn) throws SQLException {
        // Tableau à 2 dimensions associant chaque nom de type à son code couleur hexadécimal
        String[][] typesInitiaux = {
            {"Normal",   "#A8A77A"},
            {"Feu",      "#EE8130"},
            {"Eau",      "#6390F0"},
            {"Plante",   "#7AC74C"},
            {"Électrik", "#F7D02C"},
            {"Glace",    "#96D9D6"},
            {"Combat",   "#C22E28"},
            {"Poison",   "#A33EA1"},
            {"Sol",      "#E2BF65"},
            {"Vol",      "#A98FF3"},
            {"Psy",      "#F95587"},
            {"Insecte",  "#A6B91A"},
            {"Roche",    "#B6A136"},
            {"Spectre",  "#735797"},
            {"Dragon",   "#6F35FC"},
            {"Ténèbres", "#705746"},
            {"Acier",    "#B7B7CE"},
            {"Fée",      "#D685AD"}
        };

        // Requête préparée avec deux paramètres "?" (1 = nom, 2 = couleur_hex)
        String sqlInsert = "INSERT INTO types (nom, couleur_hex) VALUES (?, ?)";

        // Sauvegarde l'état actuel de l'auto-commit et le désactive :
        // cela permet de regrouper les 18 insertions en une seule transaction atomique.
        boolean autoCommitOriginal = conn.getAutoCommit();
        conn.setAutoCommit(false);

        try (PreparedStatement pstmt = conn.prepareStatement(sqlInsert)) {
            // Parcourt chaque paire {nom, couleur} du tableau
            for (String[] type : typesInitiaux) {
                pstmt.setString(1, type[0]); // Remplace le 1er "?" par le nom du type
                pstmt.setString(2, type[1]); // Remplace le 2e "?" par le code couleur
                pstmt.addBatch();            // Ajoute cette requête au lot d'exécution
            }
            
            pstmt.executeBatch(); // Envoie les 18 requêtes d'un seul coup à SQLite
            conn.commit();        // Valide définitivement la transaction sur le disque
            System.out.println("Les 18 types Pokémon ont été ajoutés à la base.");
            
        } catch (SQLException e) {
            // En cas de problème sur l'un des types, annule toutes les insertions du lot
            conn.rollback();
            throw e; // Propage l'erreur à la méthode appelante (initialiserBase)
            
        } finally {
            // Restaure le comportement auto-commit d'origine de la connexion quoi qu'il arrive
            conn.setAutoCommit(autoCommitOriginal);
        }
    }
}