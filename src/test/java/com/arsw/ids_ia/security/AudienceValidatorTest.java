package com.arsw.ids_ia.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AudienceValidatorTest {

    private AudienceValidator validator;
    private static final String TEST_AUDIENCE = "test-audience";

    @BeforeEach
    void setUp() {
        validator = new AudienceValidator(TEST_AUDIENCE);
    }

    @Test
    void testValidateWithCorrectAudience() {
        // Arrange
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "RS256");
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user123");
        
        Jwt jwt = new Jwt(
            "token-value",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            headers,
            claims
        );
        
        // Mock the audience claim
        Jwt jwtWithAudience = Jwt.withTokenValue("token-value")
            .header("alg", "RS256")
            .claim("sub", "user123")
            .audience(Arrays.asList(TEST_AUDIENCE))
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .build();

        // Act
        OAuth2TokenValidatorResult result = validator.validate(jwtWithAudience);

        // Assert
        assertNotNull(result);
        assertFalse(result.hasErrors());
    }

    @Test
    void testValidateWithIncorrectAudience() {
        // Arrange
        Jwt jwtWithWrongAudience = Jwt.withTokenValue("token-value")
            .header("alg", "RS256")
            .claim("sub", "user123")
            .audience(Arrays.asList("wrong-audience"))
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .build();

        // Act
        OAuth2TokenValidatorResult result = validator.validate(jwtWithWrongAudience);

        // Assert
        assertNotNull(result);
        assertTrue(result.hasErrors());
        assertEquals(1, result.getErrors().size());
        assertEquals("invalid_token", result.getErrors().iterator().next().getErrorCode());
    }

    @Test
    void testValidateWithEmptyAudience() {
        // Arrange
        Jwt jwtWithEmptyAudience = Jwt.withTokenValue("token-value")
            .header("alg", "RS256")
            .claim("sub", "user123")
            .audience(Collections.emptyList())
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .build();

        // Act
        OAuth2TokenValidatorResult result = validator.validate(jwtWithEmptyAudience);

        // Assert
        assertNotNull(result);
        assertTrue(result.hasErrors());
    }

    @Test
    void testValidateWithMultipleAudiencesIncludingCorrect() {
        // Arrange
        Jwt jwtWithMultipleAudiences = Jwt.withTokenValue("token-value")
            .header("alg", "RS256")
            .claim("sub", "user123")
            .audience(Arrays.asList("other-audience", TEST_AUDIENCE, "another-audience"))
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .build();

        // Act
        OAuth2TokenValidatorResult result = validator.validate(jwtWithMultipleAudiences);

        // Assert
        assertNotNull(result);
        assertFalse(result.hasErrors());
    }
}
