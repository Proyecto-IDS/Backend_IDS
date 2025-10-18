package com.arsw.ids_ia.security;

// JwtAuthenticationEntryPoint removed; resource server will handle authentication errors
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .exceptionHandling(exception -> exception.authenticationEntryPoint((request, response, authException) -> {
                response.setStatus(401);
            }))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/public/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/test/public").permitAll()
                // Swagger endpoints (if you plan to add Swagger)
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // WebSocket endpoints
                .requestMatchers("/ws/**").permitAll()
                // All other endpoints require authentication
                .anyRequest().authenticated()
            );

        // Use Spring's OAuth2 Resource Server support to validate JWTs issued by an external IDP.
        // Configure a JwtAuthenticationConverter to extract roles from the token (e.g. Keycloak's realm_access.roles)
        http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Permitir específicamente el frontend en puertos de desarrollo (5173 y preview 4173)
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:5173",
            "http://127.0.0.1:5173",
            "https://localhost:5173",
            // Vite preview default
            "http://localhost:4173",
            "http://127.0.0.1:4173"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        // Permitir el header Authorization para JWT
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Convert JWT claims from the IDP into Spring GrantedAuthority collection.
     * This example expects a claim structure like Keycloak's: { "realm_access": { "roles": ["user","admin"] } }
     */
    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new Converter<Jwt, java.util.Collection<GrantedAuthority>>() {
            @Override
            @SuppressWarnings("unchecked")
            public java.util.Collection<GrantedAuthority> convert(Jwt jwt) {
                Object realmAccess = jwt.getClaim("realm_access");
                if (realmAccess instanceof java.util.Map) {
                    java.util.Map<String, Object> realm = (java.util.Map<String, Object>) realmAccess;
                    Object rolesObj = realm.get("roles");
                    if (rolesObj instanceof java.util.Collection) {
                        java.util.Collection<String> roles = (java.util.Collection<String>) rolesObj;
                        java.util.List<GrantedAuthority> authorities = new java.util.ArrayList<>();
                        for (String role : roles) {
                            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
                        }
                        return authorities;
                    }
                }

                // Fallback: try 'roles' claim at top-level (some IDPs use this)
                Object topRoles = jwt.getClaim("roles");
                if (topRoles instanceof java.util.Collection) {
                    java.util.Collection<String> roles = (java.util.Collection<String>) topRoles;
                    java.util.List<GrantedAuthority> authorities = new java.util.ArrayList<>();
                    for (String role : roles) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
                    }
                    return authorities;
                }

                return java.util.Collections.emptyList();
            }
        });

        return converter;
    }
}