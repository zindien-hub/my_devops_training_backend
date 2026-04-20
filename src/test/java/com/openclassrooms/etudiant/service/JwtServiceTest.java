package com.openclassrooms.etudiant.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        // Injecte les champs de configuration car aucun contexte Spring n'est demarre ici.
        ReflectionTestUtils.setField(jwtService, "secretKey", "test-secret-key-at-least-32-characters-long");
        ReflectionTestUtils.setField(jwtService, "expirationTime", 3600000L);

        // Execute explicitement la garde type @PostConstruct pour echouer rapidement si la config est invalide.
        jwtService.validateJwtConfig();

        userDetails = User.builder()
                .username("agent1")
                .password("encoded-password")
                .build();
    }

    @Test
    void shouldGenerateToken() {
        // When
        String token = jwtService.generateToken(userDetails);

        // Then
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void shouldExtractUsernameFromToken() {
        // Given
        String token = jwtService.generateToken(userDetails);

        // When
        String username = jwtService.extractUsername(token);

        // Then
        assertEquals("agent1", username);
    }

    @Test
    void shouldValidateToken() {
        // Given
        String token = jwtService.generateToken(userDetails);

        // When
        boolean valid = jwtService.isTokenValid(token, userDetails);

        // Then
        assertTrue(valid);
    }
}