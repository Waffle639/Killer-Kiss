package org.example.service;

import org.example.repository.EmailCounterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Componente para rastrear el número de emails enviados por día.
 * Persiste en base de datos para mantener el contador entre reinicios.
 * SendGrid Free permite 100 emails/día.
 */
@Component
public class EmailCounter {

    @Autowired
    private EmailCounterRepository repository;

    private static final int LIMITE_DIARIO = 100;

    /**
     * Incrementa el contador de emails enviados hoy.
     */
    public synchronized void incrementar() {
        incrementar(1);
    }

    /**
     * Incrementa el contador de emails enviados hoy en la cantidad especificada.
     */
    public synchronized void incrementar(int cantidad) {
        LocalDate hoy = LocalDate.now();
        org.example.entity.EmailCounter counter = repository.findByFecha(hoy)
                .orElseGet(() -> {
                    org.example.entity.EmailCounter nuevo = new org.example.entity.EmailCounter(hoy);
                    return repository.save(nuevo);
                });
        
        counter.incrementar(cantidad);
        counter = repository.save(counter);
        System.out.println("Email enviado. Total hoy: " + counter.getEmailsEnviados() + "/" + LIMITE_DIARIO);
    }

    /**
     * Obtiene el número de emails enviados hoy.
     */
    public synchronized int getEnviadosHoy() {
        LocalDate hoy = LocalDate.now();
        return repository.findByFecha(hoy)
                .map(c -> c.getEmailsEnviados())
                .orElse(0);
    }

    /**
     * Obtiene el límite diario de emails.
     */
    public int getLimiteDiario() {
        return LIMITE_DIARIO;
    }

    /**
     * Retorna el contador en formato "X/100"
     */
    public synchronized String getContadorFormateado() {
        return getEnviadosHoy() + "/" + LIMITE_DIARIO;
    }

    /**
     * Verifica si se ha alcanzado el límite diario.
     */
    public synchronized boolean limiteAlcanzado() {
        return getEnviadosHoy() >= LIMITE_DIARIO;
    }

    /**
     * Verifica si se pueden enviar la cantidad de emails especificada.
     */
    public synchronized boolean puedeEnviar(int cantidad) {
        return (getEnviadosHoy() + cantidad) <= LIMITE_DIARIO;
    }
}
