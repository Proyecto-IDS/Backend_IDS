package com.arsw.ids_ia.dto.request;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CreateMeetingRequest - Pruebas DTO")
class CreateMeetingRequestTest {

    @Test
    @DisplayName("Constructor vacío debe crear instancia")
    void testDefaultConstructor() {
        CreateMeetingRequest request = new CreateMeetingRequest();
        assertNotNull(request);
        assertNull(request.getTitle());
        assertNull(request.getDescription());
        assertNull(request.getIncidentId());
    }

    @Test
    @DisplayName("Constructor con parámetros debe establecer todos los campos")
    void testParameterizedConstructor() {
        CreateMeetingRequest request = new CreateMeetingRequest(
            "Emergency Meeting",
            "Critical incident",
            "INC-001"
        );
        
        assertNotNull(request);
        assertEquals("Emergency Meeting", request.getTitle());
        assertEquals("Critical incident", request.getDescription());
        assertEquals("INC-001", request.getIncidentId());
    }

    @Test
    @DisplayName("Builder debe crear instancia con todos los campos")
    void testBuilder() {
        CreateMeetingRequest request = CreateMeetingRequest.builder()
            .title("Test Meeting")
            .description("Test description")
            .incidentId("INC-123")
            .build();
        
        assertNotNull(request);
        assertEquals("Test Meeting", request.getTitle());
        assertEquals("Test description", request.getDescription());
        assertEquals("INC-123", request.getIncidentId());
    }

    @Test
    @DisplayName("Builder sin incidentId debe permitir null")
    void testBuilderWithoutIncidentId() {
        CreateMeetingRequest request = CreateMeetingRequest.builder()
            .title("Meeting without incident")
            .description("Just a regular meeting")
            .build();
        
        assertNotNull(request);
        assertEquals("Meeting without incident", request.getTitle());
        assertEquals("Just a regular meeting", request.getDescription());
        assertNull(request.getIncidentId());
    }

    @Test
    @DisplayName("Setters deben establecer campos correctamente")
    void testSetters() {
        CreateMeetingRequest request = new CreateMeetingRequest();
        
        request.setTitle("New Title");
        request.setDescription("New Description");
        request.setIncidentId("INC-999");
        
        assertEquals("New Title", request.getTitle());
        assertEquals("New Description", request.getDescription());
        assertEquals("INC-999", request.getIncidentId());
    }

    @Test
    @DisplayName("equals debe comparar correctamente por todos los campos")
    void testEquals() {
        CreateMeetingRequest request1 = new CreateMeetingRequest("Title", "Desc", "INC-1");
        CreateMeetingRequest request2 = new CreateMeetingRequest("Title", "Desc", "INC-1");
        CreateMeetingRequest request3 = new CreateMeetingRequest("Different", "Desc", "INC-1");
        
        assertEquals(request1, request2);
        assertNotEquals(request1, request3);
    }

    @Test
    @DisplayName("hashCode debe ser consistente con equals")
    void testHashCode() {
        CreateMeetingRequest request1 = new CreateMeetingRequest("Title", "Desc", "INC-1");
        CreateMeetingRequest request2 = new CreateMeetingRequest("Title", "Desc", "INC-1");
        
        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    @DisplayName("toString debe incluir todos los campos")
    void testToString() {
        CreateMeetingRequest request = new CreateMeetingRequest("Meeting", "Description", "INC-100");
        
        String toString = request.toString();
        
        assertTrue(toString.contains("Meeting"));
        assertTrue(toString.contains("Description"));
        assertTrue(toString.contains("INC-100"));
        assertTrue(toString.contains("title"));
        assertTrue(toString.contains("description"));
        assertTrue(toString.contains("incidentId"));
    }
}
