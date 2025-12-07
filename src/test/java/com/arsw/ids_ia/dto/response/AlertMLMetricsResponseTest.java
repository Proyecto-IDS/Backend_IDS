package com.arsw.ids_ia.dto.response;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AlertMLMetricsResponseTest {

    @Test
    void testEmptyConstructor() {
        AlertMLMetricsResponse response = new AlertMLMetricsResponse();
        
        assertNotNull(response);
        assertNull(response.getAlertId());
        assertNull(response.getPrediction());
        assertNull(response.getAttackProbability());
    }

    @Test
    void testFullConstructor() {
        Map<String, Double> probabilities = new HashMap<>();
        probabilities.put("dos", 0.85);
        probabilities.put("normal", 0.15);
        
        AlertMLMetricsResponse response = new AlertMLMetricsResponse(
            1L,
            "dos",
            0.85,
            "CONFIRMED",
            "DDoS",
            "TCP",
            probabilities
        );
        
        assertEquals(1L, response.getAlertId());
        assertEquals("dos", response.getPrediction());
        assertEquals(0.85, response.getAttackProbability());
        assertEquals("CONFIRMED", response.getState());
        assertEquals("DDoS", response.getCategory());
        assertEquals("TCP", response.getStandardProtocol());
        assertEquals(2, response.getProbabilities().size());
    }

    @Test
    void testSettersAndGetters() {
        AlertMLMetricsResponse response = new AlertMLMetricsResponse();
        
        response.setAlertId(123L);
        response.setPrediction("sql_injection");
        response.setAttackProbability(0.92);
        response.setState("KNOWN");
        response.setCategory("SQL Injection");
        response.setStandardProtocol("HTTP");
        
        Map<String, Double> probs = new HashMap<>();
        probs.put("sql", 0.92);
        response.setProbabilities(probs);
        
        assertEquals(123L, response.getAlertId());
        assertEquals("sql_injection", response.getPrediction());
        assertEquals(0.92, response.getAttackProbability());
        assertEquals("KNOWN", response.getState());
        assertEquals("SQL Injection", response.getCategory());
        assertEquals("HTTP", response.getStandardProtocol());
        assertNotNull(response.getProbabilities());
        assertEquals(1, response.getProbabilities().size());
    }

    @Test
    void testWithNullValues() {
        AlertMLMetricsResponse response = new AlertMLMetricsResponse(
            null, null, null, null, null, null, null
        );
        
        assertNull(response.getAlertId());
        assertNull(response.getPrediction());
        assertNull(response.getAttackProbability());
        assertNull(response.getState());
        assertNull(response.getCategory());
        assertNull(response.getStandardProtocol());
        assertNull(response.getProbabilities());
    }
}
