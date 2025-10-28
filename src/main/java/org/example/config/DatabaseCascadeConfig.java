package org.example.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Componente que configura las restricciones CASCADE en la base de datos
 * después de que Hibernate cree las tablas.
 * 
 * Esto es necesario porque JPA/Hibernate no puede configurar CASCADE
 * a nivel de base de datos desde las anotaciones.
 */
@Component
public class DatabaseCascadeConfig {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void configureCascadeConstraints() {
        try {
            // Leer el script SQL
            ClassPathResource resource = new ClassPathResource("schema-post.sql");
            String sql = FileCopyUtils.copyToString(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)
            );

            // Ejecutar cada sentencia SQL
            String[] statements = sql.split(";");
            for (String statement : statements) {
                if (statement.trim().length() > 0 && !statement.trim().startsWith("--")) {
                    try {
                        jdbcTemplate.execute(statement.trim());
                    } catch (Exception e) {
                        // Ignorar errores si la FK no existe
                        System.out.println("Info: " + e.getMessage());
                    }
                }
            }
            
            System.out.println("✅ Restricciones CASCADE configuradas correctamente");
            
        } catch (Exception e) {
            System.err.println("⚠️ Error al configurar restricciones CASCADE: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
