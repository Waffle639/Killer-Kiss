package org.example;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "partidas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KillerKiss {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "El nombre de la partida no puede estar vacío")
    @Column(nullable = false)
    private String nom;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "partida_participantes",
        joinColumns = @JoinColumn(name = "partida_id"),
        inverseJoinColumns = @JoinColumn(name = "persona_id")
    )
    private List<Persona> personas = new ArrayList<>();
    
    @Column(nullable = false)
    private boolean estat = false;
    
    @ManyToOne
    @JoinColumn(name = "ganador_id", nullable = true)
    private Persona ganador;
    
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
    
    @Column(name = "fecha_finalizacion")
    private LocalDateTime fechaFinalizacion;

    public KillerKiss(String nom, List<Persona> personas) {
        this.nom = nom;
        this.personas = new ArrayList<>(personas);
        this.fechaCreacion = LocalDateTime.now();
        this.estat = false;
    }

    public void afegirPersona(Persona persona) {
        if (!this.personas.contains(persona)) {
            this.personas.add(persona);
        }
    }

    public void eliminarPersona(Persona persona) {
        this.personas.remove(persona);
    }

    public void iniciarPartida() {
        this.estat = true;
    }

    public void acavarPartida(Persona guanyador) {
        this.estat = false;
        this.ganador = guanyador;
        this.fechaFinalizacion = LocalDateTime.now();
        if (guanyador != null) {
            guanyador.sumarVictoria();
        }
    }

    public void sortPersonas() {
        java.util.Collections.shuffle(this.personas);
    }

    // Getters y Setters (Lombok los genera automáticamente con @Data, pero los añadimos explícitamente)
    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public List<Persona> getPersonas() {
        return personas;
    }

    public boolean isEstat() {
        return estat;
    }

    public Persona getGanador() {
        return ganador;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaFinalizacion() {
        return fechaFinalizacion;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setPersonas(List<Persona> personas) {
        this.personas = personas;
    }

    public void setEstat(boolean estat) {
        this.estat = estat;
    }

    public void setGanador(Persona ganador) {
        this.ganador = ganador;
    }
}
