package com.arsw.ids_ia.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Meeting - Pruebas de modelo")
class MeetingTest {

    @Test
    @DisplayName("Constructor vacío debe crear instancia")
    void testDefaultConstructor() {
        Meeting meeting = new Meeting();
        assertNotNull(meeting);
        assertNull(meeting.getId());
        assertNull(meeting.getCode());
    }

    @Test
    @DisplayName("Constructor con todos los parámetros debe establecer campos")
    void testAllArgsConstructor() {
        User creator = new User();
        creator.setId(1L);
        Set<User> participants = new HashSet<>();
        User p1 = new User();
        p1.setId(10L);
        User p2 = new User();
        p2.setId(11L);
        participants.add(p1);
        participants.add(p2);
        LocalDateTime now = LocalDateTime.now();
        
        Meeting meeting = new Meeting(
            10L, 
            "ABC123", 
            "Emergency", 
            "Critical incident", 
            now, 
            now.plusHours(1), 
            3600L, 
            creator, 
            participants, 
            5, 
            "ACTIVE",
            "{\"items\":[]}", 
            "{\"type\":\"dos\"}", 
            null
        );
        
        assertEquals(10L, meeting.getId());
        assertEquals("ABC123", meeting.getCode());
        assertEquals("Emergency", meeting.getTitle());
        assertEquals("Critical incident", meeting.getDescription());
        assertEquals(now, meeting.getStartTime());
        assertEquals(now.plusHours(1), meeting.getEndTime());
        assertEquals(3600L, meeting.getDurationSeconds());
        assertEquals(creator, meeting.getCreator());
        assertEquals(participants, meeting.getParticipants());
        // getCurrentParticipantCount() sobrescribe campo y usa participants.size()
        assertEquals(2, meeting.getCurrentParticipantCount());
        assertEquals("ACTIVE", meeting.getStatus());
        assertEquals("{\"items\":[]}", meeting.getChecklistJson());
        assertEquals("{\"type\":\"dos\"}", meeting.getIncidentContextJson());
        assertNull(meeting.getCancelledBy());
    }

    @Test
    @DisplayName("Builder debe crear meeting con todos los campos")
    void testBuilder() {
        User creator = new User();
        creator.setId(2L);
        Set<User> participants = new HashSet<>();
        User user1 = new User();
        user1.setId(3L);
        participants.add(user1);
        LocalDateTime start = LocalDateTime.parse("2025-01-01T10:00:00");
        
        Meeting meeting = Meeting.builder()
            .id(100L)
            .code("TEST-CODE")
            .title("Test Meeting")
            .description("Test description")
            .startTime(start)
            .endTime(start.plusHours(2))
            .durationSeconds(7200L)
            .creator(creator)
            .participants(participants)
            .currentParticipantCount(1)
            .status("ENDED")
            .checklistJson("[{\"id\":1,\"text\":\"Check logs\"}]")
            .incidentContextJson("{\"attackType\":\"probe\",\"severity\":\"HIGH\"}")
            .cancelledBy(null)
            .build();
        
        assertEquals(100L, meeting.getId());
        assertEquals("TEST-CODE", meeting.getCode());
        assertEquals("Test Meeting", meeting.getTitle());
        assertEquals("Test description", meeting.getDescription());
        assertEquals(start, meeting.getStartTime());
        assertEquals(start.plusHours(2), meeting.getEndTime());
        assertEquals(7200L, meeting.getDurationSeconds());
        assertEquals(creator, meeting.getCreator());
        assertEquals(participants, meeting.getParticipants());
        assertEquals(1, meeting.getCurrentParticipantCount());
        assertEquals("ENDED", meeting.getStatus());
        assertEquals("[{\"id\":1,\"text\":\"Check logs\"}]", meeting.getChecklistJson());
        assertEquals("{\"attackType\":\"probe\",\"severity\":\"HIGH\"}", meeting.getIncidentContextJson());
        assertNull(meeting.getCancelledBy());
    }

    @Test
    @DisplayName("Builder con valores mínimos debe permitir nulls")
    void testBuilderMinimal() {
        Meeting meeting = Meeting.builder()
            .code("MIN-CODE")
            .title("Minimal")
            .build();
        
        assertNotNull(meeting);
        assertEquals("MIN-CODE", meeting.getCode());
        assertEquals("Minimal", meeting.getTitle());
        assertNull(meeting.getId());
        assertNull(meeting.getDescription());
        assertNull(meeting.getStartTime());
        assertNull(meeting.getCreator());
    }

    @Test
    @DisplayName("getCurrentParticipantCount debe retornar size de participants")
    void testGetCurrentParticipantCountFromSet() {
        Set<User> participants = new HashSet<>();
        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(2L);
        User user3 = new User();
        user3.setId(3L);
        participants.add(user1);
        participants.add(user2);
        participants.add(user3);
        
        Meeting meeting = Meeting.builder()
            .participants(participants)
            .build();
        
        assertEquals(3, meeting.getCurrentParticipantCount());
    }

    @Test
    @DisplayName("getCurrentParticipantCount debe retornar 0 si participants es null")
    void testGetCurrentParticipantCountNull() {
        Meeting meeting = Meeting.builder()
            .participants(null)
            .build();
        
        assertEquals(0, meeting.getCurrentParticipantCount());
    }

    @Test
    @DisplayName("getCurrentParticipantCount debe retornar 0 si participants está vacío")
    void testGetCurrentParticipantCountEmpty() {
        Meeting meeting = Meeting.builder()
            .participants(new HashSet<>())
            .build();
        
        assertEquals(0, meeting.getCurrentParticipantCount());
    }

    @Test
    @DisplayName("Setters deben modificar todos los campos")
    void testSetters() {
        Meeting meeting = new Meeting();
        User creator = new User();
        creator.setId(5L);
        User cancelledBy = new User();
        cancelledBy.setId(6L);
        Set<User> participants = new HashSet<>();
        LocalDateTime start = LocalDateTime.parse("2025-06-15T14:30:00");
        
        meeting.setId(200L);
        meeting.setCode("SET-CODE");
        meeting.setTitle("Updated Title");
        meeting.setDescription("Updated description");
        meeting.setStartTime(start);
        meeting.setEndTime(start.plusMinutes(30));
        meeting.setDurationSeconds(1800L);
        meeting.setCreator(creator);
        meeting.setParticipants(participants);
        meeting.setCurrentParticipantCount(0);
        meeting.setStatus("CANCELLED");
        meeting.setChecklistJson("{\"items\":[]}");
        meeting.setIncidentContextJson("{\"context\":\"none\"}");
        meeting.setCancelledBy(cancelledBy);
        
        assertEquals(200L, meeting.getId());
        assertEquals("SET-CODE", meeting.getCode());
        assertEquals("Updated Title", meeting.getTitle());
        assertEquals("Updated description", meeting.getDescription());
        assertEquals(start, meeting.getStartTime());
        assertEquals(start.plusMinutes(30), meeting.getEndTime());
        assertEquals(1800L, meeting.getDurationSeconds());
        assertEquals(creator, meeting.getCreator());
        assertEquals(participants, meeting.getParticipants());
        assertEquals(0, meeting.getCurrentParticipantCount());
        assertEquals("CANCELLED", meeting.getStatus());
        assertEquals("{\"items\":[]}", meeting.getChecklistJson());
        assertEquals("{\"context\":\"none\"}", meeting.getIncidentContextJson());
        assertEquals(cancelledBy, meeting.getCancelledBy());
    }

    @Test
    @DisplayName("equals debe comparar correctamente meetings")
    void testEquals() {
        Meeting meeting1 = Meeting.builder()
            .id(1L)
            .code("CODE1")
            .title("Title1")
            .build();
        
        Meeting meeting2 = Meeting.builder()
            .id(1L)
            .code("CODE1")
            .title("Title1")
            .build();
        
        Meeting meeting3 = Meeting.builder()
            .id(2L)
            .code("CODE2")
            .title("Title2")
            .build();
        
        assertEquals(meeting1, meeting2);
        assertNotEquals(meeting1, meeting3);
    }

    @Test
    @DisplayName("hashCode debe ser consistente con equals")
    void testHashCode() {
        Meeting meeting1 = Meeting.builder()
            .id(1L)
            .code("CODE1")
            .build();
        
        Meeting meeting2 = Meeting.builder()
            .id(1L)
            .code("CODE1")
            .build();
        
        assertEquals(meeting1.hashCode(), meeting2.hashCode());
    }

    @Test
    @DisplayName("toString debe incluir campos principales")
    void testToString() {
        Meeting meeting = Meeting.builder()
            .id(999L)
            .code("TO-STRING")
            .title("Test toString")
            .status("ACTIVE")
            .build();
        
        String toString = meeting.toString();
        
        assertTrue(toString.contains("999"));
        assertTrue(toString.contains("TO-STRING"));
        assertTrue(toString.contains("Test toString"));
        assertTrue(toString.contains("ACTIVE"));
    }
}
