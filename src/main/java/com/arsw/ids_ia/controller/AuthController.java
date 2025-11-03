package com.arsw.ids_ia.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtDecoder jwtDecoder;

    public AuthController(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    /**
     * Accept a Google id_token sent from the frontend (client-side flow) and validate it.
     * Returns a small JSON with token and basic claims if valid.
     */
    @PostMapping("/google")
    public ResponseEntity<?> loginWithGoogle(@RequestBody Map<String, String> body) {
        String idToken = body.get("idToken");
        if (idToken == null || idToken.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "missing idToken"));
        }

        try {
            Jwt jwt = jwtDecoder.decode(idToken);

            Map<String, Object> resp = new HashMap<>();
            resp.put("token", idToken);
            resp.put("sub", jwt.getSubject());
            resp.put("email", jwt.getClaimAsString("email"));
            resp.put("issuer", jwt.getIssuer());
            resp.put("aud", jwt.getAudience());
            resp.put("claims", jwt.getClaims());

            return ResponseEntity.ok(resp);
        } catch (JwtException e) {
            return ResponseEntity.status(401).body(Map.of("error", "invalid_token", "message", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of("error", "unauthenticated"));
        }

        // Extract email from JWT principal (format is usually the email or subject)
        String email = authentication.getName();
        
        // Get authorities and extract role
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(auth -> auth.getAuthority())
                .orElse("ROLE_USER");

        Map<String, Object> resp = Map.of(
                "email", email,
                "role", role,
                "authorities", authentication.getAuthorities(),
                "authenticated", authentication.isAuthenticated()
        );

        return ResponseEntity.ok(resp);
    }
}
