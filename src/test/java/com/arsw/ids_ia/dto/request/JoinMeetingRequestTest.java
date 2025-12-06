package com.arsw.ids_ia.dto.request;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("JoinMeetingRequest - Pruebas DTO")
class JoinMeetingRequestTest {

    @Test
    @DisplayName("Constructor vacío debe crear instancia")
    void testDefaultConstructor() {
        JoinMeetingRequest request = new JoinMeetingRequest();
        assertNotNull(request);
        assertNull(request.getCode());
    }

    @Test
    @DisplayName("Constructor con parámetros debe establecer code")
    void testParameterizedConstructor() {
        JoinMeetingRequest request = new JoinMeetingRequest("ABC123");
        
        assertNotNull(request);
        assertEquals("ABC123", request.getCode());
    }

    @Test
    @DisplayName("Builder debe crear instancia correctamente")
    void testBuilder() {
        JoinMeetingRequest request = JoinMeetingRequest.builder()
            .code("TEST-CODE")
            .build();
        
        assertNotNull(request);
        assertEquals("TEST-CODE", request.getCode());
    }

    @Test
    @DisplayName("Setter debe establecer code correctamente")
    void testSetCode() {
        JoinMeetingRequest request = new JoinMeetingRequest();
        request.setCode("NEW-CODE");
        
        assertEquals("NEW-CODE", request.getCode());
    }

    @Test
    @DisplayName("Getter debe retornar code establecido")
    void testGetCode() {
        JoinMeetingRequest request = new JoinMeetingRequest("GET-TEST");
        
        String code = request.getCode();
        
        assertEquals("GET-TEST", code);
    }

    @Test
    @DisplayName("equals debe comparar correctamente por code")
    void testEquals() {
        JoinMeetingRequest request1 = new JoinMeetingRequest("CODE1");
        JoinMeetingRequest request2 = new JoinMeetingRequest("CODE1");
        JoinMeetingRequest request3 = new JoinMeetingRequest("CODE2");
        
        assertEquals(request1, request2);
        assertNotEquals(request1, request3);
    }

    @Test
    @DisplayName("hashCode debe ser consistente con equals")
    void testHashCode() {
        JoinMeetingRequest request1 = new JoinMeetingRequest("CODE1");
        JoinMeetingRequest request2 = new JoinMeetingRequest("CODE1");
        
        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    @DisplayName("toString debe incluir code")
    void testToString() {
        JoinMeetingRequest request = new JoinMeetingRequest("TEST-123");
        
        String toString = request.toString();
        
        assertTrue(toString.contains("TEST-123"));
        assertTrue(toString.contains("code"));
    }
}
