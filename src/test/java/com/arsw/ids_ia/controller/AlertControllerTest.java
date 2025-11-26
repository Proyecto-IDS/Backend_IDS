package com.arsw.ids_ia.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
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
import org.springframework.http.ResponseEntity;

import com.arsw.ids_ia.model.Alert;
import com.arsw.ids_ia.model.Meeting;
import com.arsw.ids_ia.service.AlertService;
import com.arsw.ids_ia.service.MeetingService;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertController - Pruebas de lógica crítica")
class AlertControllerTest {

    @Mock
    private AlertService alertService;

    @Mock
    private MeetingService meetingService;

    @InjectMocks
    private AlertController alertController;

    private Alert testAlert;
    private Meeting testMeeting;

    @BeforeEach
    void setUp() {
        testAlert = new Alert.Builder()
            .packetId("PKT-001")
            .incidentId("INC-001")
            .severity("HIGH")
            .warRoomId(1L)
            .timestamp(Instant.now())
            .build();
        testAlert.setId(1L);

        testMeeting = new Meeting();
        testMeeting.setId(1L);
        testMeeting.setTitle("War Room INC-001");
        testMeeting.setStatus("ACTIVE");
    }

    @Test
    @DisplayName("Debe mapear warRoom en respuesta de alerta individual")
    void shouldMapWarRoomInAlertResponse() {
        when(alertService.getById(1L)).thenReturn(Optional.of(testAlert));
        when(meetingService.getMeetingById(1L)).thenReturn(testMeeting);
        testMeeting.setCode("ABC123");

        ResponseEntity<Map<String, Object>> response = alertController.get(1L);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        
        assertEquals(1L, body.get("id"));
        assertEquals("INC-001", body.get("incidentId"));
        assertEquals(1L, body.get("warRoomId"));
        assertEquals("ABC123", body.get("warRoomCode"));
        
        verify(meetingService).getMeetingById(1L);
    }

    @Test
    @DisplayName("Debe contar alertas por severidad correctamente")
    void shouldCountBySeverity() {
        Alert highAlert = new Alert.Builder()
            .packetId("PKT-HIGH")
            .incidentId("INC-HIGH")
            .severity("high")
            .build();
        
        Alert criticalAlert = new Alert.Builder()
            .packetId("PKT-CRIT")
            .incidentId("INC-CRIT")
            .severity("critical")
            .build();

        when(alertService.recent(Integer.MAX_VALUE))
            .thenReturn(List.of(highAlert, criticalAlert));

        Map<String, Long> counts = alertController.countBySeverity();

        assertEquals(2L, counts.get("total"));
        assertEquals(1L, counts.get("critical"));
        assertEquals(1L, counts.get("high"));
        verify(alertService).recent(Integer.MAX_VALUE);
    }

    @Test
    @DisplayName("Debe contar alertas de hoy correctamente")
    void shouldCountTodayAlerts() {
        Alert alert1 = new Alert.Builder()
            .packetId("PKT-TODAY-1")
            .incidentId("INC-TODAY")
            .severity("high")
            .timestamp(Instant.now())
            .build();
        
        Alert alert2 = new Alert.Builder()
            .packetId("PKT-TODAY-2")
            .incidentId("INC-TODAY")
            .severity("critical")
            .timestamp(Instant.now())
            .build();

        when(alertService.today()).thenReturn(List.of(alert1, alert2));

        Map<String, Long> counts = alertController.todayCount();

        assertEquals(2L, counts.get("total"));
        assertEquals(1L, counts.get("critical"));
        assertEquals(1L, counts.get("high"));
        verify(alertService).today();
    }

    @Test
    @DisplayName("Debe retornar incidentes resueltos correctamente")
    void shouldGetResolvedIncidents() {
        when(alertService.getResolvedIncidents()).thenReturn(List.of(testAlert));

        List<Alert> result = alertController.getResolvedIncidents();

        assertEquals(1, result.size());
        assertEquals("INC-001", result.get(0).getIncidentId());
        verify(alertService).getResolvedIncidents();
    }
}
