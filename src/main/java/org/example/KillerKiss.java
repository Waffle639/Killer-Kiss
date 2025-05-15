package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class KillerKiss {
    private String nom;
    private ArrayList<Persona> personas;
    private boolean estat;
    private static int totalPartides = 0;

    public KillerKiss(String nom,ArrayList<Persona> personas) {
        this.nom = nom;
        this.personas = new ArrayList<>(personas);
        totalPartides++;
    }

    public ArrayList<Persona> getPersonas() {
        return personas;
    }

    public void afegirPersona(Persona persona) {
        this.personas.add(persona);
    }

    public void eliminarPersona(Persona persona) {
        this.personas.remove(persona);
    }


    public String toString() {
        return "Partida " + this.getNom() + ": " + personas.toString();
    }

    public ArrayList<Persona> sortPersonas() {
        estat = true;
        Collections.shuffle(this.personas);
        return personas;
    }

    public void acavarPartida(Persona guanyador) {
        estat = false;
        guanyador.sumarVictoria();
    }

    public boolean getEstat() {
        return estat;
    }

    public String getNom() {
        return nom;
    }

    public static int  getTotalPartides() {
        return totalPartides;
    }

}
