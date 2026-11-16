/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package lael.ia.projetpokedex;

import java.util.List;
import lael.ia.projetpokedex.dao.PokemonDAO;
import lael.ia.projetpokedex.database.DatabaseManager;
import lael.ia.projetpokedex.dao.TypeDAO;
import lael.ia.projetpokedex.model.Pokemon;
import lael.ia.projetpokedex.model.TypePokemon;

/**
 *
 * @author laelt
 */
public class ProjetPokedex {

    public static void main(String[] args) {
        // 1. Initialise la base SQLite (crée pokedex.db, les tables et les 18 types si absents)
        // Toujours exécuter cette étape AVANT d'ouvrir l'interface graphique
        DatabaseManager.initialiserBase();

        TypeDAO typeDAO = new TypeDAO();
        PokemonDAO pokemonDAO = new PokemonDAO();

// Récupération des types depuis SQLite
        TypePokemon plante = typeDAO.trouverParNom("Plante");
        TypePokemon poison = typeDAO.trouverParNom("Poison");

// Ajout d'un Pokémon double-type
        Pokemon bulbizarre = new Pokemon(2, "Bulbizarre", "Bulbi", plante, poison, 5, 45, 49, 49, 45);
        pokemonDAO.ajouter(bulbizarre);

// Affichage de tous les Pokémon
        pokemonDAO.listerTous().forEach(System.out::println);
    }
}
