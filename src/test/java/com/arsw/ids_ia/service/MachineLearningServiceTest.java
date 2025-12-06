package com.arsw.ids_ia.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.arsw.ids_ia.utils.enums.AlertSeverity;

@DisplayName("MachineLearningService - Pruebas de lógica ML")
class MachineLearningServiceTest {

    private final MachineLearningService mlService = new MachineLearningService();

    @Test
    @DisplayName("Debe determinar severidad NORMAL para probabilidad < 0.3")
    void shouldDetermineSeverity_Normal() {
        AlertSeverity severity1 = mlService.determineSeverity(0.0);
        AlertSeverity severity2 = mlService.determineSeverity(0.15);
        AlertSeverity severity3 = mlService.determineSeverity(0.29);

        assertEquals(AlertSeverity.NORMAL, severity1);
        assertEquals(AlertSeverity.NORMAL, severity2);
        assertEquals(AlertSeverity.NORMAL, severity3);
    }

    @Test
    @DisplayName("Debe determinar severidad FALSO_POSITIVO para probabilidad 0.3-0.7")
    void shouldDetermineSeverity_FalsoPositivo() {
        AlertSeverity severity1 = mlService.determineSeverity(0.3);
        AlertSeverity severity2 = mlService.determineSeverity(0.5);
        AlertSeverity severity3 = mlService.determineSeverity(0.69);

        assertEquals(AlertSeverity.FALSO_POSITIVO, severity1);
        assertEquals(AlertSeverity.FALSO_POSITIVO, severity2);
        assertEquals(AlertSeverity.FALSO_POSITIVO, severity3);
    }

    @Test
    @DisplayName("Debe determinar severidad CONOCIDO para probabilidad 0.7-0.9")
    void shouldDetermineSeverity_Conocido() {
        AlertSeverity severity1 = mlService.determineSeverity(0.7);
        AlertSeverity severity2 = mlService.determineSeverity(0.8);
        AlertSeverity severity3 = mlService.determineSeverity(0.89);

        assertEquals(AlertSeverity.CONOCIDO, severity1);
        assertEquals(AlertSeverity.CONOCIDO, severity2);
        assertEquals(AlertSeverity.CONOCIDO, severity3);
    }

    @Test
    @DisplayName("Debe determinar severidad CRITICO para probabilidad >= 0.9")
    void shouldDetermineSeverity_Critico() {
        AlertSeverity severity1 = mlService.determineSeverity(0.9);
        AlertSeverity severity2 = mlService.determineSeverity(0.95);
        AlertSeverity severity3 = mlService.determineSeverity(1.0);

        assertEquals(AlertSeverity.CRITICO, severity1);
        assertEquals(AlertSeverity.CRITICO, severity2);
        assertEquals(AlertSeverity.CRITICO, severity3);
    }

    @Test
    @DisplayName("Debe retornar false cuando probabilidad < 0.3 (no crear alerta)")
    void shouldNotCreateAlert_WhenProbabilityLow() {
        assertFalse(mlService.shouldCreateAlert(0.0));
        assertFalse(mlService.shouldCreateAlert(0.1));
        assertFalse(mlService.shouldCreateAlert(0.29));
    }

    @Test
    @DisplayName("Debe retornar true cuando probabilidad >= 0.3 (crear alerta)")
    void shouldCreateAlert_WhenProbabilityThresholdMet() {
        assertTrue(mlService.shouldCreateAlert(0.3));
        assertTrue(mlService.shouldCreateAlert(0.5));
        assertTrue(mlService.shouldCreateAlert(0.8));
        assertTrue(mlService.shouldCreateAlert(1.0));
    }
}
