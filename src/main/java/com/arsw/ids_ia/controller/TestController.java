package com.arsw.ids_ia.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.arsw.ids_ia.repository.AlertRepository;
import com.arsw.ids_ia.repository.UserRepository;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private AlertRepository alertRepository;
    
    @Autowired
    private UserRepository userRepository;

    @DeleteMapping("/clean-db")
    public ResponseEntity<String> cleanDatabase() {
        try {
            // Limpiar alertas primero (tienen FK hacia users)
            alertRepository.deleteAll();
            
            // Luego limpiar usuarios
            userRepository.deleteAll();
            
            return ResponseEntity.ok("Database cleaned successfully - alerts and users deleted");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error cleaning database: " + e.getMessage());
        }
    }

    @GetMapping("/public")
    public ResponseEntity<String> publicEndpoint() {
        return ResponseEntity.ok("public ok");
    }

    @GetMapping("/protected")
    public ResponseEntity<Object> protectedEndpoint(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken) {
            Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
            // Return a small subset of claims to keep output compact
            Map<String, Object> response = Map.of(
                "sub", jwt.getSubject(),
                "issuer", jwt.getIssuer(),
                "aud", jwt.getAudience(),
                "claims", jwt.getClaims()
            );
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(403).body("Forbidden: no JWT authentication present");
    }
}
