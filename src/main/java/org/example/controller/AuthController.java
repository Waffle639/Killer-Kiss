package org.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Generar hash BCrypt para cualquier contraseña
     */
    @GetMapping("/generate-hash")
    public String generateHash(@RequestParam String password) {
        return passwordEncoder.encode(password);
    }

    /**
     * Login: verifica usuario y contraseña
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        try {
            String sql = "SELECT password FROM usuarios WHERE username = ?";
            String hashedPassword = jdbcTemplate.queryForObject(sql, String.class, username);

            System.out.println("=== LOGIN DEBUG ===");
            System.out.println("Hash DB: " + hashedPassword);
            System.out.println("Password: " + password);
            System.out.println("Hash length: " + (hashedPassword != null ? hashedPassword.length() : "null"));
            
            boolean matches = passwordEncoder.matches(password, hashedPassword);
            System.out.println("BCrypt matches: " + matches);

            if (hashedPassword != null && matches) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Login exitoso"));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Contraseña incorrecta"));
            }
        } catch (Exception e) {
            System.out.println("Error en login: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("success", false, "message", "Usuario no encontrado"));
        }
    }

    /**
     * Crear nuevo usuario (encripta la contraseña)
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        try {
            String hashedPassword = passwordEncoder.encode(password);
            String sql = "INSERT INTO usuarios (username, password) VALUES (?, ?)";
            jdbcTemplate.update(sql, username, hashedPassword);
            
            return ResponseEntity.ok(Map.of("success", true, "message", "Usuario creado"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "message", "Error: usuario ya existe"));
        }
    }
}
