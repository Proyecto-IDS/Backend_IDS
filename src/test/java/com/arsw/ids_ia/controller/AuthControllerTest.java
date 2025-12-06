package com.arsw.ids_ia.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.test.util.ReflectionTestUtils;

import com.arsw.ids_ia.model.User;
import com.arsw.ids_ia.repository.UserRepository;
import com.arsw.ids_ia.utils.enums.Role;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController - Pruebas OAuth2 y roles")
class AuthControllerTest {

    @Mock
    private JwtDecoder jwtDecoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthController authController;

    private Jwt mockJwt;

    @BeforeEach
    void setUp() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "google-12345");
        claims.put("email", "user@example.com");
        claims.put("name", "Test User");
        claims.put("aud", List.of("test-client-id"));
        claims.put("iss", "https://accounts.google.com");

        mockJwt = new Jwt(
            "mock-token",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            Map.of("alg", "RS256"),
            claims
        );
    }

    @Test
    @DisplayName("Debe validar JWT correctamente en loginWithGoogle")
    void shouldValidateJwt_WhenLoginWithGoogle() {
        Map<String, String> request = Map.of("idToken", "valid-token");
        when(jwtDecoder.decode("valid-token")).thenReturn(mockJwt);

        ResponseEntity<Map<String, Object>> response = authController.loginWithGoogle(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("valid-token", body.get("token"));
        assertEquals("google-12345", body.get("sub"));
        assertEquals("user@example.com", body.get("email"));
        verify(jwtDecoder).decode("valid-token");
    }

    @Test
    @DisplayName("Debe retornar 401 cuando JWT es inválido")
    void shouldReturn401_WhenJwtInvalid() {
        Map<String, String> request = Map.of("idToken", "invalid-token");
        when(jwtDecoder.decode("invalid-token")).thenThrow(new JwtException("Invalid signature"));

        ResponseEntity<Map<String, Object>> response = authController.loginWithGoogle(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("invalid_token", body.get("error"));
    }

    @Test
    @DisplayName("Debe crear nuevo usuario con rol ADMIN para email en initialAdmins")
    void shouldCreateAdminUser_WhenEmailInInitialAdmins() {
        ReflectionTestUtils.setField(authController, "initialAdmins", "admin@example.com,user@example.com");
        
        when(authentication.getPrincipal()).thenReturn(mockJwt);
        when(authentication.isAuthenticated()).thenReturn(true);
        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")))
            .when(authentication).getAuthorities();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        ResponseEntity<Map<String, Object>> response = authController.me(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("user@example.com", body.get("email"));
        assertEquals("ROLE_ADMIN", body.get("role"));
        
        verify(userRepository).save(argThat(user -> 
            user.getRole() == Role.ADMIN && 
            user.getEmail().equals("user@example.com")
        ));
    }

    @Test
    @DisplayName("Debe crear nuevo usuario con rol USER para email no en initialAdmins")
    void shouldCreateRegularUser_WhenEmailNotInInitialAdmins() {
        ReflectionTestUtils.setField(authController, "initialAdmins", "admin@example.com");
        
        when(authentication.getPrincipal()).thenReturn(mockJwt);
        when(authentication.isAuthenticated()).thenReturn(true);
        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
            .when(authentication).getAuthorities();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L);
            return user;
        });

        ResponseEntity<Map<String, Object>> response = authController.me(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("ROLE_USER", body.get("role"));
        
        verify(userRepository).save(argThat(user -> 
            user.getRole() == Role.USER
        ));
    }

    @Test
    @DisplayName("Debe retornar 401 cuando no está autenticado")
    void shouldReturn401_WhenNotAuthenticated() {
        ResponseEntity<Map<String, Object>> response = authController.me(null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("unauthenticated", body.get("error"));
    }
}
