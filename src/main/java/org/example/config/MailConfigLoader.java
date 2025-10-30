package org.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Configuración para cargar propiedades del archivo mail.config
 * Este archivo solo se carga si existe (para desarrollo local).
 * En producción (Render), se usan variables de entorno directamente.
 */
@Configuration
@PropertySource(value = "classpath:mail.config", ignoreResourceNotFound = true)
public class MailConfigLoader {
    // Esta clase carga el archivo mail.config si existe
    // Si no existe (como en producción), lo ignora sin error
}
