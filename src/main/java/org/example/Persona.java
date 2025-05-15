package org.example;

public class Persona {
    private String nom;
    private String seccio;
    private String mail;
    private int victories = 0;

    public Persona(String nom, String seccio, String mail) {
        this.nom = nom;
        this.seccio = seccio;
        this.mail = mail;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getseccio() {
        return seccio;
    }

    public void setseccio(String seccio) {
        this.seccio = seccio;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public int getVictories() {
        return victories;
    }

    public void sumarVictoria() {
        this.victories++;
    }

    public String toString() {
        return "Nom='" + nom + '\'' +
                ", Seccio='" + seccio + '\'' +
                ", Gmail='" + mail
                ;
    }

    public boolean equals(Persona persona) {
        if(this.nom.equals(persona.getNom()) && this.seccio.equals(persona.getseccio()) && this.mail.equals(persona.getMail())) {
            return true;
        } else {
            return false;
        }
    }
}
