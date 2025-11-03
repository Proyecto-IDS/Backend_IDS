package com.arsw.ids_ia.security;

// JwtAuthenticationEntryPoint removed; resource server will handle authentication errors
import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtUserAuthoritiesConverter jwtUserAuthoritiesConverter;

    public SecurityConfig(JwtUserAuthoritiesConverter jwtUserAuthoritiesConverter) {
        this.jwtUserAuthoritiesConverter = jwtUserAuthoritiesConverter;
    }

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
                // Allow traffic stream websocket handshake without requiring JWT (development convenience)
                .requestMatchers("/traffic/stream").permitAll()
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
        // Configure a JwtAuthenticationConverter to extract roles by mapping the JWT email to local user roles.
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
     * This uses JwtUserAuthoritiesConverter to map user email to roles from the database.
     */
    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwtUserAuthoritiesConverter);
        return converter;
    }
}
