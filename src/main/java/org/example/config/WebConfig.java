package org.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración de CORS para permitir peticiones del frontend.
 * CORS (Cross-Origin Resource Sharing) permite que el navegador haga peticiones
 * desde el frontend (localhost:8080) a la API REST (localhost:8080/api).
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")  // Aplica a todas las rutas que empiecen con /api
                .allowedOrigins("*")     // Permite peticiones desde cualquier origen
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // Métodos HTTP permitidos
                .allowedHeaders("*")     // Permite todos los headers
                .allowCredentials(false) // No necesita credenciales
                .maxAge(3600);          // Cache de configuración CORS por 1 hora
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Redirige la raíz "/" a index.html
        registry.addViewController("/").setViewName("forward:/index.html");
    }
}
