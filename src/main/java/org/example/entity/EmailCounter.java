package org.example.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "email_counter")
@NoArgsConstructor
@AllArgsConstructor
public class EmailCounter {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private LocalDate fecha;
    
    @Column(nullable = false)
    private Integer emailsEnviados = 0;
    
    public EmailCounter(LocalDate fecha) {
        this.fecha = fecha;
        this.emailsEnviados = 0;
    }
    
    public void incrementar(int cantidad) {
        this.emailsEnviados += cantidad;
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public LocalDate getFecha() {
        return fecha;
    }
    
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
    
    public Integer getEmailsEnviados() {
        return emailsEnviados;
    }
    
    public void setEmailsEnviados(Integer emailsEnviados) {
        this.emailsEnviados = emailsEnviados;
    }
}
