package com.arsw.ids_ia.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import com.arsw.ids_ia.model.Alert;
import com.arsw.ids_ia.repository.AlertRepository;
import com.arsw.ids_ia.ws.TrafficSocketHandler;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertService - Pruebas de lógica crítica")
class AlertServiceTest {

    @Mock
    private AlertRepository repository;

    @Mock
    private TrafficSocketHandler socketHandler;

    @InjectMocks
    private AlertService alertService;

    private Alert testAlert;

    @BeforeEach
    void setUp() {
        testAlert = new Alert.Builder()
            .packetId("PKT-001")
            .incidentId("INC-001")
            .severity("HIGH")
            .score(0.85)
            .attackProbability(0.85)
            .prediction("dos")
            .category("dos")
            .build();
    }

    @Test
    @DisplayName("Debe crear alerta sin duplicados y hacer broadcast exitoso")
    void shouldCreateAlert_WhenNoDuplicates() {
        when(repository.findDuplicate(anyString(), anyString(), anyString())).thenReturn(Optional.empty());
        when(repository.save(any(Alert.class))).thenReturn(testAlert);

        Alert result = alertService.create(testAlert);

        assertNotNull(result);
        assertEquals("PKT-001", result.getPacketId());
        verify(repository).findDuplicate("PKT-001", "INC-001", "HIGH");
        verify(repository).save(testAlert);
        verify(socketHandler).broadcastObject(any());
    }

    @Test
    @DisplayName("Debe retornar alerta existente cuando hay duplicado")
    void shouldReturnExisting_WhenDuplicateDetected() {
        Alert existingAlert = new Alert.Builder()
            .packetId("PKT-001")
            .incidentId("INC-001")
            .severity("HIGH")
            .build();
        existingAlert.setId(100L);

        when(repository.findDuplicate("PKT-001", "INC-001", "HIGH"))
            .thenReturn(Optional.of(existingAlert));

        Alert result = alertService.create(testAlert);

        assertEquals(100L, result.getId());
        verify(repository, never()).save(any());
        verify(socketHandler, never()).broadcastObject(any());
    }

    @Test
    @DisplayName("Debe crear alerta aunque falle el WebSocket broadcast")
    void shouldCreateAlert_WhenWebSocketFails() {
        when(repository.findDuplicate(anyString(), anyString(), anyString())).thenReturn(Optional.empty());
        when(repository.save(any(Alert.class))).thenReturn(testAlert);
        doThrow(new RuntimeException("WebSocket error")).when(socketHandler).broadcastObject(any());

        Alert result = alertService.create(testAlert);

        assertNotNull(result);
        verify(repository).save(testAlert);
    }

    @Test
    @DisplayName("Debe filtrar alertas de hoy correctamente")
    void shouldFilterTodayAlerts() {
        Instant today = Instant.now();
        Instant yesterday = today.minus(1, ChronoUnit.DAYS);
        
        Alert todayAlert = new Alert.Builder()
            .packetId("PKT-TODAY")
            .incidentId("INC-TODAY")
            .severity("HIGH")
            .timestamp(today)
            .build();
        
        Alert yesterdayAlert = new Alert.Builder()
            .packetId("PKT-YESTERDAY")
            .incidentId("INC-YESTERDAY")
            .severity("LOW")
            .timestamp(yesterday)
            .build();

        when(repository.findAllByOrderByTimestampDesc(any(PageRequest.class)))
            .thenReturn(List.of(todayAlert, yesterdayAlert));

        List<Alert> result = alertService.today();

        assertEquals(1, result.size());
        assertEquals("PKT-TODAY", result.get(0).getPacketId());
    }

    @Test
    @DisplayName("Debe respetar límite en recent y usar valor por defecto si es inválido")
    void shouldRespectLimit_InRecent() {
        when(repository.findActiveAlertsOrderByTimestampDesc(any(PageRequest.class)))
            .thenReturn(List.of(testAlert));

        List<Alert> resultWithZero = alertService.recent(0);
        List<Alert> resultWithNegative = alertService.recent(-5);

        verify(repository, times(2)).findActiveAlertsOrderByTimestampDesc(PageRequest.of(0, 10));
        assertEquals(1, resultWithZero.size());
        assertEquals(1, resultWithNegative.size());
    }

    @Test
    @DisplayName("Debe obtener incidentes resueltos correctamente")
    void shouldGetResolvedIncidents() {
        Alert resolvedAlert = new Alert.Builder()
            .packetId("PKT-RESOLVED")
            .incidentId("INC-RESOLVED")
            .severity("CRITICAL")
            .warRoomId(1L)
            .build();

        when(repository.findResolvedIncidents()).thenReturn(List.of(resolvedAlert));

        List<Alert> result = alertService.getResolvedIncidents();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getWarRoomId());
        verify(repository).findResolvedIncidents();
    }
}
