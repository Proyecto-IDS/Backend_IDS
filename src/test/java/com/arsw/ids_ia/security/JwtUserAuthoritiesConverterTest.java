package com.arsw.ids_ia.security;

import com.arsw.ids_ia.model.User;
import com.arsw.ids_ia.repository.UserRepository;
import com.arsw.ids_ia.utils.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtUserAuthoritiesConverterTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JwtUserAuthoritiesConverter converter;

    private Jwt jwt;

    @BeforeEach
    void setUp() {
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "RS256");
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user123");
        
        jwt = new Jwt(
            "token-value",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            headers,
            claims
        );
    }

    @Test
    void testConvertWithExistingAdminUser() {
        // Arrange
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", "admin@test.com");
        claims.put("sub", "user123");
        
        Jwt jwtWithEmail = Jwt.withTokenValue("token")
            .header("alg", "RS256")
            .claims(c -> c.putAll(claims))
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .build();

        User adminUser = User.builder()
                .email("admin@test.com")
                .role(Role.ADMIN)
                .build();

        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));

        // Act
        Collection<GrantedAuthority> authorities = converter.convert(jwtWithEmail);

        // Assert
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        verify(userRepository).findByEmail("admin@test.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void testConvertWithExistingRegularUser() {
        // Arrange
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", "user@test.com");
        claims.put("sub", "user123");
        
        Jwt jwtWithEmail = Jwt.withTokenValue("token")
            .header("alg", "RS256")
            .claims(c -> c.putAll(claims))
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .build();

        User regularUser = User.builder()
                .email("user@test.com")
                .role(Role.USER)
                .build();

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(regularUser));

        // Act
        Collection<GrantedAuthority> authorities = converter.convert(jwtWithEmail);

        // Assert
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void testConvertWithNewUser() {
        // Arrange
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", "newuser@test.com");
        claims.put("sub", "user123");
        
        Jwt jwtWithEmail = Jwt.withTokenValue("token")
            .header("alg", "RS256")
            .claims(c -> c.putAll(claims))
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .build();

        when(userRepository.findByEmail("newuser@test.com")).thenReturn(Optional.empty());
        
        User newUser = User.builder()
                .email("newuser@test.com")
                .name("newuser@test.com")
                .role(Role.USER)
                .build();
        
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // Act
        Collection<GrantedAuthority> authorities = converter.convert(jwtWithEmail);

        // Assert
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testConvertWithNoEmailClaim() {
        // Arrange
        Jwt jwtWithoutEmail = Jwt.withTokenValue("token")
            .header("alg", "RS256")
            .claim("sub", "user123")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .build();

        // Act
        Collection<GrantedAuthority> authorities = converter.convert(jwtWithoutEmail);

        // Assert
        assertNotNull(authorities);
        assertTrue(authorities.isEmpty());
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void testConvertWithUserCreationFailure() {
        // Arrange
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", "failuser@test.com");
        
        Jwt jwtWithEmail = Jwt.withTokenValue("token")
            .header("alg", "RS256")
            .claims(c -> c.putAll(claims))
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .build();

        when(userRepository.findByEmail("failuser@test.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Database error"));

        // Act
        Collection<GrantedAuthority> authorities = converter.convert(jwtWithEmail);

        // Assert
        assertNotNull(authorities);
        assertTrue(authorities.isEmpty());
    }

    @Test
    void testConvertWithUserWithoutRole() {
        // Arrange
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", "norole@test.com");
        
        Jwt jwtWithEmail = Jwt.withTokenValue("token")
            .header("alg", "RS256")
            .claims(c -> c.putAll(claims))
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .build();

        User userWithoutRole = User.builder()
                .email("norole@test.com")
                .role(null)
                .build();

        when(userRepository.findByEmail("norole@test.com")).thenReturn(Optional.of(userWithoutRole));

        // Act
        Collection<GrantedAuthority> authorities = converter.convert(jwtWithEmail);

        // Assert
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void testConvertEmailIsCaseInsensitive() {
        // Arrange
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", "UPPERCASE@TEST.COM");
        
        Jwt jwtWithEmail = Jwt.withTokenValue("token")
            .header("alg", "RS256")
            .claims(c -> c.putAll(claims))
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .build();

        User user = User.builder()
                .email("uppercase@test.com")
                .role(Role.USER)
                .build();

        when(userRepository.findByEmail("uppercase@test.com")).thenReturn(Optional.of(user));

        // Act
        Collection<GrantedAuthority> authorities = converter.convert(jwtWithEmail);

        // Assert
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        verify(userRepository).findByEmail("uppercase@test.com");
    }
}
