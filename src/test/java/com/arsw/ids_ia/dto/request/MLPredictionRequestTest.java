package com.arsw.ids_ia.dto.request;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MLPredictionRequest - Pruebas DTO")
class MLPredictionRequestTest {

    @Test
    @DisplayName("Constructor vacío debe crear instancia")
    void testDefaultConstructor() {
        MLPredictionRequest request = new MLPredictionRequest();
        assertNotNull(request);
        assertNull(request.getFeatures());
    }

    @Test
    @DisplayName("Constructor con parámetros debe establecer features")
    void testParameterizedConstructor() {
        NetworkTrafficFeatures features = new NetworkTrafficFeatures();
        features.setDuration(100);
        features.setProtocolType("tcp");
        
        MLPredictionRequest request = new MLPredictionRequest(features);
        
        assertNotNull(request);
        assertNotNull(request.getFeatures());
        assertEquals(features, request.getFeatures());
        assertEquals(100, request.getFeatures().getDuration());
        assertEquals("tcp", request.getFeatures().getProtocolType());
    }

    @Test
    @DisplayName("Setter debe establecer features correctamente")
    void testSetFeatures() {
        MLPredictionRequest request = new MLPredictionRequest();
        NetworkTrafficFeatures features = new NetworkTrafficFeatures();
        features.setSrcBytes(1000);
        
        request.setFeatures(features);
        
        assertEquals(features, request.getFeatures());
        assertEquals(1000, request.getFeatures().getSrcBytes());
    }

    @Test
    @DisplayName("Getter debe retornar features establecido")
    void testGetFeatures() {
        NetworkTrafficFeatures features = new NetworkTrafficFeatures();
        MLPredictionRequest request = new MLPredictionRequest(features);
        
        NetworkTrafficFeatures retrieved = request.getFeatures();
        
        assertSame(features, retrieved);
    }

    @Test
    @DisplayName("Setter con null debe establecer null")
    void testSetFeaturesNull() {
        NetworkTrafficFeatures features = new NetworkTrafficFeatures();
        MLPredictionRequest request = new MLPredictionRequest(features);
        
        request.setFeatures(null);
        
        assertNull(request.getFeatures());
    }
}
