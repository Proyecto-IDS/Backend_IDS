package com.arsw.ids_ia.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Alert - Pruebas de modelo")
class AlertTest {

    @Test
    @DisplayName("Constructor vacío debe crear instancia")
    void testDefaultConstructor() {
        Alert alert = new Alert();
        assertNotNull(alert);
        assertNull(alert.getId());
        assertNull(alert.getPacketId());
    }

    @Test
    @DisplayName("Constructor con parámetros debe establecer campos básicos")
    void testParameterizedConstructor() {
        Instant now = Instant.now();
        
        Alert alert = new Alert("PKT-001", "INC-001", "HIGH", 0.95, "v1.0", now);
        
        assertNotNull(alert);
        assertEquals("PKT-001", alert.getPacketId());
        assertEquals("INC-001", alert.getIncidentId());
        assertEquals("HIGH", alert.getSeverity());
        assertEquals(0.95, alert.getScore());
        assertEquals("v1.0", alert.getModelVersion());
        assertEquals(now, alert.getTimestamp());
    }

    @Test
    @DisplayName("Builder debe crear alerta con todos los campos")
    void testBuilder() {
        Instant timestamp = Instant.parse("2025-01-01T00:00:00Z");
        
        Alert alert = new Alert.Builder()
            .packetId("PKT-123")
            .incidentId("INC-456")
            .severity("CRITICAL")
            .score(0.98)
            .modelVersion("v2.0")
            .timestamp(timestamp)
            .warRoomId(10L)
            .attackProbability(0.95)
            .prediction("dos")
            .category("network")
            .standardProtocol("tcp")
            .probabilities("{\"dos\":0.95}")
            .originalFeatures("{\"duration\":100}")
            .state("OPEN")
            .build();
        
        assertNotNull(alert);
        assertEquals("PKT-123", alert.getPacketId());
        assertEquals("INC-456", alert.getIncidentId());
        assertEquals("CRITICAL", alert.getSeverity());
        assertEquals(0.98, alert.getScore());
        assertEquals("v2.0", alert.getModelVersion());
        assertEquals(timestamp, alert.getTimestamp());
        assertEquals(10L, alert.getWarRoomId());
        assertEquals(0.95, alert.getAttackProbability());
        assertEquals("dos", alert.getPrediction());
        assertEquals("network", alert.getCategory());
        assertEquals("tcp", alert.getStandardProtocol());
        assertEquals("{\"dos\":0.95}", alert.getProbabilities());
        assertEquals("{\"duration\":100}", alert.getOriginalFeatures());
        assertEquals("OPEN", alert.getState());
    }

    @Test
    @DisplayName("Builder con valores mínimos debe usar defaults")
    void testBuilderMinimal() {
        Alert alert = new Alert.Builder()
            .packetId("PKT-MIN")
            .incidentId("INC-MIN")
            .build();
        
        assertNotNull(alert);
        assertEquals("PKT-MIN", alert.getPacketId());
        assertEquals("INC-MIN", alert.getIncidentId());
        assertEquals("v1.0", alert.getModelVersion()); // default
        assertNotNull(alert.getTimestamp()); // default Instant.now()
        assertNull(alert.getSeverity());
        assertNull(alert.getWarRoomId());
    }

    @Test
    @DisplayName("Setters deben modificar todos los campos")
    void testSetters() {
        Alert alert = new Alert();
        Instant timestamp = Instant.parse("2025-06-15T10:30:00Z");
        
        alert.setId(999L);
        alert.setPacketId("PKT-SET");
        alert.setIncidentId("INC-SET");
        alert.setSeverity("MEDIUM");
        alert.setScore(0.75);
        alert.setModelVersion("v3.0");
        alert.setTimestamp(timestamp);
        alert.setWarRoomId(20L);
        alert.setAttackProbability(0.80);
        alert.setPrediction("probe");
        alert.setCategory("scan");
        alert.setStandardProtocol("udp");
        alert.setProbabilities("{\"probe\":0.80}");
        alert.setOriginalFeatures("{\"srcBytes\":500}");
        alert.setState("RESOLVED");
        
        assertEquals(999L, alert.getId());
        assertEquals("PKT-SET", alert.getPacketId());
        assertEquals("INC-SET", alert.getIncidentId());
        assertEquals("MEDIUM", alert.getSeverity());
        assertEquals(0.75, alert.getScore());
        assertEquals("v3.0", alert.getModelVersion());
        assertEquals(timestamp, alert.getTimestamp());
        assertEquals(20L, alert.getWarRoomId());
        assertEquals(0.80, alert.getAttackProbability());
        assertEquals("probe", alert.getPrediction());
        assertEquals("scan", alert.getCategory());
        assertEquals("udp", alert.getStandardProtocol());
        assertEquals("{\"probe\":0.80}", alert.getProbabilities());
        assertEquals("{\"srcBytes\":500}", alert.getOriginalFeatures());
        assertEquals("RESOLVED", alert.getState());
    }

    @Test
    @DisplayName("Builder debe soportar encadenamiento de métodos")
    void testBuilderMethodChaining() {
        Alert.Builder builder = new Alert.Builder();
        
        Alert.Builder result = builder
            .packetId("PKT-CHAIN")
            .incidentId("INC-CHAIN")
            .severity("LOW")
            .score(0.45);
        
        assertSame(builder, result);
        
        Alert alert = result.build();
        assertEquals("PKT-CHAIN", alert.getPacketId());
        assertEquals("INC-CHAIN", alert.getIncidentId());
        assertEquals("LOW", alert.getSeverity());
        assertEquals(0.45, alert.getScore());
    }

    @Test
    @DisplayName("Getters deben retornar valores establecidos")
    void testGetters() {
        Instant timestamp = Instant.parse("2025-12-31T23:59:59Z");
        
        Alert alert = new Alert.Builder()
            .packetId("PKT-GET")
            .incidentId("INC-GET")
            .severity("HIGH")
            .score(0.92)
            .modelVersion("v4.0")
            .timestamp(timestamp)
            .warRoomId(30L)
            .originalFeatures("{\"flag\":\"S0\"}")
            .build();
        
        assertEquals("PKT-GET", alert.getPacketId());
        assertEquals("INC-GET", alert.getIncidentId());
        assertEquals("HIGH", alert.getSeverity());
        assertEquals(0.92, alert.getScore());
        assertEquals("v4.0", alert.getModelVersion());
        assertEquals(timestamp, alert.getTimestamp());
        assertEquals(30L, alert.getWarRoomId());
        assertEquals("{\"flag\":\"S0\"}", alert.getOriginalFeatures());
    }

    @Test
    @DisplayName("Builder debe aceptar nulls en campos opcionales")
    void testBuilderWithNulls() {
        Alert alert = new Alert.Builder()
            .packetId("PKT-NULL")
            .incidentId("INC-NULL")
            .severity(null)
            .score(null)
            .warRoomId(null)
            .attackProbability(null)
            .prediction(null)
            .build();
        
        assertNotNull(alert);
        assertEquals("PKT-NULL", alert.getPacketId());
        assertEquals("INC-NULL", alert.getIncidentId());
        assertNull(alert.getSeverity());
        assertNull(alert.getScore());
        assertNull(alert.getWarRoomId());
        assertNull(alert.getAttackProbability());
        assertNull(alert.getPrediction());
    }
}
