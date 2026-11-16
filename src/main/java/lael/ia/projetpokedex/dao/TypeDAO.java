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
import java.util.ArrayList;
import java.util.List;
import lael.ia.projetpokedex.database.DatabaseManager;
import lael.ia.projetpokedex.model.TypePokemon;

/**
 * DAO chargé de lire les types Pokémon depuis la table "types".
 * 
 * @author laelt
 */
public class TypeDAO {

    /**
     * Récupère tous les types disponibles triés par ordre alphabétique.
     * Idéal pour alimenter une JComboBox dans l'interface graphique.
     * 
     * @return La liste complète des objets TypePokemon.
     */
    public List<TypePokemon> listerTous() {
        List<TypePokemon> types = new ArrayList<>();
        String sql = "SELECT id, nom, couleur_hex FROM types ORDER BY nom ASC";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                types.add(mapperResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des types : " + e.getMessage());
        }

        return types;
    }

    /**
     * Recherche un type précis via son identifiant SQLite.
     * 
     * @param id L'identifiant du type.
     * @return L'objet TypePokemon correspondant, ou null s'il n'existe pas.
     */
    public TypePokemon trouverParId(int id) {
        String sql = "SELECT id, nom, couleur_hex FROM types WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapperResultSet(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche du type ID " + id + " : " + e.getMessage());
        }

        return null;
    }

    /**
     * Recherche un type précis via son nom exact (ex: "Feu", "Électrik").
     * 
     * @param nom Le nom du type recherché.
     * @return L'objet TypePokemon correspondant, ou null s'il n'existe pas.
     */
    public TypePokemon trouverParNom(String nom) {
        String sql = "SELECT id, nom, couleur_hex FROM types WHERE nom = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nom);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapperResultSet(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche du type '" + nom + "' : " + e.getMessage());
        }

        return null;
    }

    /**
     * Transforme la ligne courante d'un ResultSet en objet TypePokemon.
     */
    private TypePokemon mapperResultSet(ResultSet rs) throws SQLException {
        return new TypePokemon(
            rs.getInt("id"),
            rs.getString("nom"),
            rs.getString("couleur_hex")
        );
    }
}