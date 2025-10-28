package org.example;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "personas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Persona {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    
    @Column(nullable = false, unique = true)
    private String mail;
    
    @Column(nullable = false)
    private int victories = 0;

    public Persona(String nom, String mail) {
        this.nom = nom;
        this.mail = mail;
        this.victories = 0;
    }

    public void sumarVictoria() {
        this.victories++;
    }

    // Getters y Setters (Lombok los genera automáticamente con @Data, pero los añadimos explícitamente)
    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getMail() {
        return mail;
    }

    public int getVictories() {
        return victories;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public void setVictories(int victories) {
        this.victories = victories;
    }
}
