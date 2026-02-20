package org.example.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Expone la configuración pública del frontend (EmailJS).
 * Los valores se leen desde application.properties o variables de entorno del servidor.
 * Así las IDs no se hardcodean en el JS del repositorio.
 */
@RestController
@RequestMapping("/api/config")
public class ConfigController {

    @Value("${emailjs.service-id}")
    private String emailjsServiceId;

    @Value("${emailjs.template-id}")
    private String emailjsTemplateId;

    @Value("${emailjs.public-key}")
    private String emailjsPublicKey;

    /**
     * GET /api/config
     * Devuelve la configuración pública necesaria para el frontend.
     */
    @GetMapping
    public ResponseEntity<Map<String, String>> getConfig() {
        return ResponseEntity.ok(Map.of(
            "emailjsServiceId",  emailjsServiceId,
            "emailjsTemplateId", emailjsTemplateId,
            "emailjsPublicKey",  emailjsPublicKey
        ));
    }
}
