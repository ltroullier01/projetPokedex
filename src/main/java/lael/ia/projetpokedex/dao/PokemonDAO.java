/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lael.ia.projetpokedex.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import lael.ia.projetpokedex.database.DatabaseManager;
import lael.ia.projetpokedex.model.Pokemon;
import lael.ia.projetpokedex.model.TypePokemon;

/**
 * DAO chargé de toutes les opérations SQLite sur la table "pokemons".
 * 
 * @author laelt
 */
public class PokemonDAO {

    // Requête SELECT de base avec jointures pour récupérer les détails des deux types en une seule fois :
    // - INNER JOIN sur t1 car type1_id est obligatoire
    // - LEFT JOIN sur t2 car type2_id peut être NULL (Pokémon mono-type)
    private static final String SELECT_BASE = """
        SELECT p.id, p.numero_pokedex, p.nom, p.surnom,
               p.niveau, p.pv, p.attaque, p.defense, p.vitesse,
               t1.id AS t1_id, t1.nom AS t1_nom, t1.couleur_hex AS t1_couleur,
               t2.id AS t2_id, t2.nom AS t2_nom, t2.couleur_hex AS t2_couleur
        FROM pokemons p
        INNER JOIN types t1 ON p.type1_id = t1.id
        LEFT JOIN types t2 ON p.type2_id = t2.id
        """;

    /**
     * Insère un nouveau Pokémon dans la base de données.
     * Met automatiquement à jour l'attribut "id" de l'objet Pokemon passé en paramètre.
     * 
     * @param p Le Pokémon à enregistrer.
     * @return true si l'insertion a réussi, false sinon.
     */
    public boolean ajouter(Pokemon p) {
        String sql = """
            INSERT INTO pokemons (numero_pokedex, nom, surnom, type1_id, type2_id, niveau, pv, attaque, defense, vitesse)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            remplirParametresPokemon(pstmt, p);

            int lignesAffectees = pstmt.executeUpdate();
            if (lignesAffectees > 0) {
                // Récupère l'ID auto-généré par SQLite et l'injecte dans l'objet Java
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        p.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du Pokémon '" + p.getNom() + "' : " + e.getMessage());
        }

        return false;
    }

    /**
     * Met à jour les informations d'un Pokémon existant (identifié par p.getId()).
     * 
     * @param p Le Pokémon contenant les nouvelles valeurs.
     * @return true si la mise à jour a réussi, false sinon.
     */
    public boolean modifier(Pokemon p) {
        String sql = """
            UPDATE pokemons
            SET numero_pokedex = ?, nom = ?, surnom = ?, type1_id = ?, type2_id = ?,
                niveau = ?, pv = ?, attaque = ?, defense = ?, vitesse = ?
            WHERE id = ?
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            remplirParametresPokemon(pstmt, p);
            pstmt.setInt(11, p.getId()); // 11e paramètre : la clause WHERE id = ?

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erreur lors de la modification du Pokémon ID " + p.getId() + " : " + e.getMessage());
        }

        return false;
    }

    /**
     * Supprime définitivement un Pokémon de la base via son identifiant.
     * 
     * @param id L'identifiant SQLite du Pokémon à supprimer.
     * @return true si la suppression a réussi, false sinon.
     */
    public boolean supprimer(int id) {
        String sql = "DELETE FROM pokemons WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du Pokémon ID " + id + " : " + e.getMessage());
        }

        return false;
    }

    /**
     * Récupère un Pokémon complet par son identifiant SQLite.
     * 
     * @param id L'identifiant du Pokémon.
     * @return L'objet Pokemon trouvé, ou null s'il n'existe pas.
     */
    public Pokemon trouverParId(int id) {
        String sql = SELECT_BASE + " WHERE p.id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapperResultSet(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche du Pokémon ID " + id + " : " + e.getMessage());
        }

        return null;
    }

    /**
     * Récupère tous les Pokémon sauvegardés, triés par numéro de Pokédex puis par nom.
     * 
     * @return La liste de tous les Pokémon.
     */
    public List<Pokemon> listerTous() {
        List<Pokemon> pokemons = new ArrayList<>();
        String sql = SELECT_BASE + " ORDER BY p.numero_pokedex ASC, p.nom ASC";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                pokemons.add(mapperResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors du chargement de la liste des Pokémon : " + e.getMessage());
        }

        return pokemons;
    }

    /**
     * Recherche dynamique combinant un filtre texte (nom ou surnom) et/ou un filtre par type.
     * Pratique pour brancher une barre de recherche en temps réel dans l'interface graphique.
     * 
     * @param texteRecherche Texte cherché dans le nom ou le surnom (peut être null ou vide).
     * @param typeId         ID du type filtré sur type1 ou type2 (peut être null pour "Tous les types").
     * @return La liste filtrée des Pokémon correspondants.
     */
    public List<Pokemon> rechercher(String texteRecherche, Integer typeId) {
        List<Pokemon> pokemons = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SELECT_BASE + " WHERE 1=1");
        List<Object> parametres = new ArrayList<>();

        // Ajout conditionnel du filtre texte (insensible à la casse sur ASCII avec LIKE)
        if (texteRecherche != null && !texteRecherche.isBlank()) {
            sql.append(" AND (p.nom LIKE ? OR p.surnom LIKE ?)");
            String motif = "%" + texteRecherche.trim() + "%";
            parametres.add(motif);
            parametres.add(motif);
        }

        // Ajout conditionnel du filtre par type (vérifie le type principal ET le type secondaire)
        if (typeId != null) {
            sql.append(" AND (p.type1_id = ? OR p.type2_id = ?)");
            parametres.add(typeId);
            parametres.add(typeId);
        }

        sql.append(" ORDER BY p.numero_pokedex ASC, p.nom ASC");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            // Injection dynamique des paramètres dans le PreparedStatement
            for (int i = 0; i < parametres.size(); i++) {
                pstmt.setObject(i + 1, parametres.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    pokemons.add(mapperResultSet(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche filtrée de Pokémon : " + e.getMessage());
        }

        return pokemons;
    }

    /**
     * Compte le nombre total de Pokémon enregistrés dans la base.
     * 
     * @return Le nombre total d'entrées dans la table "pokemons".
     */
    public int compterTotal() {
        String sql = "SELECT COUNT(*) FROM pokemons";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage des Pokémon : " + e.getMessage());
        }

        return 0;
    }

    // --- Méthodes utilitaires privées ---

    /**
     * Factorise le remplissage des 10 premiers paramètres communs aux requêtes INSERT et UPDATE.
     */
    private void remplirParametresPokemon(PreparedStatement pstmt, Pokemon p) throws SQLException {
        pstmt.setInt(1, p.getNumeroPokedex());
        pstmt.setString(2, p.getNom());

        // Gestion propre du surnom optionnel (NULL si vide)
        if (p.getSurnom() != null && !p.getSurnom().isBlank()) {
            pstmt.setString(3, p.getSurnom().trim());
        } else {
            pstmt.setNull(3, Types.VARCHAR);
        }

        pstmt.setInt(4, p.getType1().getId());

        // Gestion propre du second type optionnel (NULL si Pokémon mono-type)
        if (p.getType2() != null) {
            pstmt.setInt(5, p.getType2().getId());
        } else {
            pstmt.setNull(5, Types.INTEGER);
        }

        pstmt.setInt(6, p.getNiveau());
        pstmt.setInt(7, p.getPv());
        pstmt.setInt(8, p.getAttaque());
        pstmt.setInt(9, p.getDefense());
        pstmt.setInt(10, p.getVitesse());
    }

    /**
     * Reconstruit un objet Pokemon complet (avec ses objets TypePokemon) à partir d'une ligne SQL.
     */
    private Pokemon mapperResultSet(ResultSet rs) throws SQLException {
        TypePokemon type1 = new TypePokemon(
            rs.getInt("t1_id"),
            rs.getString("t1_nom"),
            rs.getString("t1_couleur")
        );

        TypePokemon type2 = null;
        int t2Id = rs.getInt("t2_id");
        // rs.wasNull() vérifie si la dernière colonne lue (t2_id) valait NULL dans SQLite
        if (!rs.wasNull()) {
            type2 = new TypePokemon(
                t2Id,
                rs.getString("t2_nom"),
                rs.getString("t2_couleur")
            );
        }

        return new Pokemon(
            rs.getInt("id"),
            rs.getInt("numero_pokedex"),
            rs.getString("nom"),
            rs.getString("surnom"),
            type1,
            type2,
            rs.getInt("niveau"),
            rs.getInt("pv"),
            rs.getInt("attaque"),
            rs.getInt("defense"),
            rs.getInt("vitesse")
        );
    }
}
