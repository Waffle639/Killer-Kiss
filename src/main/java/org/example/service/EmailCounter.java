package org.example.service;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Componente para rastrear el número de emails enviados por día.
 * SendGrid Free permite 100 emails/día.
 */
@Component
public class EmailCounter {

    private LocalDate fechaActual = LocalDate.now();
    private AtomicInteger contadorHoy = new AtomicInteger(0);
    private static final int LIMITE_DIARIO = 100;

    /**
     * Incrementa el contador de emails enviados hoy.
     */
    public synchronized void incrementar() {
        verificarYResetearSiNuevoDia();
        contadorHoy.incrementAndGet();
        System.out.println("Email enviado. Total hoy: " + getEnviadosHoy() + "/" + LIMITE_DIARIO);
    }

    /**
     * Obtiene el número de emails enviados hoy.
     */
    public synchronized int getEnviadosHoy() {
        verificarYResetearSiNuevoDia();
        return contadorHoy.get();
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
        verificarYResetearSiNuevoDia();
        return contadorHoy.get() >= LIMITE_DIARIO;
    }

    /**
     * Verifica si es un nuevo día y resetea el contador si es necesario.
     */
    private void verificarYResetearSiNuevoDia() {
        LocalDate hoy = LocalDate.now();
        if (!hoy.equals(fechaActual)) {
            System.out.println("Nuevo día detectado. Reseteando contador de emails.");
            fechaActual = hoy;
            contadorHoy.set(0);
        }
    }
}
