package com.arsw.ids_ia.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
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

import com.arsw.ids_ia.model.User;
import com.arsw.ids_ia.repository.UserRepository;
import com.arsw.ids_ia.utils.enums.Role;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String KEY_ERROR = "error";
    private static final String KEY_EMAIL = "email";

    private final JwtDecoder jwtDecoder;
    private final UserRepository userRepository;
    
    @Value("${app.initial-admins:}")
    private String initialAdmins;

    /**
     * Accept a Google id_token sent from the frontend (client-side flow) and validate it.
     * Returns a small JSON with token and basic claims if valid.
     */
    @PostMapping("/google")
    public ResponseEntity<Map<String, Object>> loginWithGoogle(@RequestBody Map<String, String> body) {
        String idToken = body.get("idToken");
        if (idToken == null || idToken.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(KEY_ERROR, "missing idToken"));
        }

        try {
            Jwt jwt = jwtDecoder.decode(idToken);

            Map<String, Object> resp = new HashMap<>();
            resp.put("token", idToken);
            resp.put("sub", jwt.getSubject());
            resp.put(KEY_EMAIL, jwt.getClaimAsString(KEY_EMAIL));
            resp.put("issuer", jwt.getIssuer());
            resp.put("aud", jwt.getAudience());
            resp.put("claims", jwt.getClaims());

            return ResponseEntity.ok(resp);
        } catch (JwtException e) {
            return ResponseEntity.status(401).body(Map.of(KEY_ERROR, "invalid_token", "message", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of(KEY_ERROR, "unauthenticated"));
        }

        // Extract email and name from JWT
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String email = jwt.getClaimAsString(KEY_EMAIL);
        String name = jwt.getClaimAsString("name");
        
        // Create or get user from database
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            // Determine role: ADMIN if email is in initialAdmins list, otherwise USER
            List<String> adminEmails = List.of(initialAdmins.split(","));
            Role role = adminEmails.stream()
                    .map(String::trim)
                    .anyMatch(adminEmail -> adminEmail.equalsIgnoreCase(email))
                    ? Role.ADMIN
                    : Role.USER;
            
            // Create new user
            User newUser = User.builder()
                    .email(email)
                    .name(name)
                    .role(role)
                    .build();
            
            return userRepository.save(newUser);
        });
        
        // Get authorities and extract role
        String roleStr = user.getRole() != null ? "ROLE_" + user.getRole().name() : "ROLE_USER";

        Map<String, Object> resp = Map.of(
                KEY_EMAIL, user.getEmail(),
                "name", user.getName() != null ? user.getName() : email,
                "role", roleStr,
                "authorities", authentication.getAuthorities(),
                "authenticated", authentication.isAuthenticated()
        );

        return ResponseEntity.ok(resp);
    }
}
