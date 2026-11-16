/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lael.ia.projetpokedex.model;

import java.util.Objects;

/**
 * Représente un type Pokémon (ex: Feu, Eau, Plante) tel que stocké dans la table "types".
 * 
 * @author laelt
 */
public class TypePokemon {

    private int id;
    private String nom;
    private String couleurHex;

    // Constructeur vide par défaut
    public TypePokemon() {
    }

    // Constructeur sans id (utile avant l'insertion en base)
    public TypePokemon(String nom, String couleurHex) {
        this.nom = nom;
        this.couleurHex = couleurHex;
    }

    // Constructeur complet (utilisé lors de la lecture depuis SQLite)
    public TypePokemon(int id, String nom, String couleurHex) {
        this.id = id;
        this.nom = nom;
        this.couleurHex = couleurHex;
    }

    // --- Getters et Setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getCouleurHex() {
        return couleurHex;
    }

    public void setCouleurHex(String couleurHex) {
        this.couleurHex = couleurHex;
    }

    /**
     * Permet aux composants graphiques (ex: JComboBox) d'afficher directement le nom du type.
     */
    @Override
    public String toString() {
        return nom;
    }
}