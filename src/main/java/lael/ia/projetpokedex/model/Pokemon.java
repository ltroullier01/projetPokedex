/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lael.ia.projetpokedex.model;

/**
 * Représente un Pokémon et ses statistiques, correspondant à la table "pokemons".
 * 
 * @author laelt
 */
public class Pokemon {

    private int id;
    private int numeroPokedex;
    private String nom;
    private String surnom;
    private TypePokemon type1;
    private TypePokemon type2; // Peut être null si le Pokémon n'a qu'un seul type
    private int niveau;
    private int pv;
    private int attaque;
    private int defense;
    private int vitesse;

    // Constructeur vide initialisant les valeurs par défaut définies dans la base SQLite
    public Pokemon() {
        this.niveau = 1;
        this.pv = 50;
        this.attaque = 50;
        this.defense = 50;
        this.vitesse = 50;
    }

    // Constructeur sans id (à utiliser dans l'UI pour créer un nouveau Pokémon avant sauvegarde)
    public Pokemon(int numeroPokedex, String nom, String surnom,
                   TypePokemon type1, TypePokemon type2,
                   int niveau, int pv, int attaque, int defense, int vitesse) {
        this.numeroPokedex = numeroPokedex;
        this.nom = nom;
        this.surnom = surnom;
        this.type1 = type1;
        this.type2 = type2;
        this.niveau = niveau;
        this.pv = pv;
        this.attaque = attaque;
        this.defense = defense;
        this.vitesse = vitesse;
    }

    // Constructeur complet avec id (utilisé par PokemonDAO lors de la lecture depuis SQLite)
    public Pokemon(int id, int numeroPokedex, String nom, String surnom,
                   TypePokemon type1, TypePokemon type2,
                   int niveau, int pv, int attaque, int defense, int vitesse) {
        this(numeroPokedex, nom, surnom, type1, type2, niveau, pv, attaque, defense, vitesse);
        this.id = id;
    }

    // --- Getters et Setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNumeroPokedex() {
        return numeroPokedex;
    }

    public void setNumeroPokedex(int numeroPokedex) {
        this.numeroPokedex = numeroPokedex;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getSurnom() {
        return surnom;
    }

    public void setSurnom(String surnom) {
        this.surnom = surnom;
    }

    public TypePokemon getType1() {
        return type1;
    }

    public void setType1(TypePokemon type1) {
        this.type1 = type1;
    }

    public TypePokemon getType2() {
        return type2;
    }

    public void setType2(TypePokemon type2) {
        this.type2 = type2;
    }

    public int getNiveau() {
        return niveau;
    }

    public void setNiveau(int niveau) {
        this.niveau = niveau;
    }

    public int getPv() {
        return pv;
    }

    public void setPv(int pv) {
        this.pv = pv;
    }

    public int getAttaque() {
        return attaque;
    }

    public void setAttaque(int attaque) {
        this.attaque = attaque;
    }

    public int getDefense() {
        return defense;
    }

    public void setDefense(int defense) {
        this.defense = defense;
    }

    public int getVitesse() {
        return vitesse;
    }

    public void setVitesse(int vitesse) {
        this.vitesse = vitesse;
    }

    /**
     * Retourne une représentation lisible du Pokémon (utile pour les logs ou listes simples).
     */
    @Override
    public String toString() {
        String affichageNom = (surnom != null && !surnom.isBlank())
                ? surnom + " (" + nom + ")"
                : nom;
        return String.format("#%03d %s - Niv. %d", numeroPokedex, affichageNom, niveau);
    }
}