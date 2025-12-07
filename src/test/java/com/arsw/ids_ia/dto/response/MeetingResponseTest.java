package com.arsw.ids_ia.dto.response;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MeetingResponseTest {

    @Test
    void testRecordCreation() {
        Set<String> participants = new HashSet<>();
        participants.add("user1@test.com");
        participants.add("user2@test.com");
        
        MeetingResponse response = new MeetingResponse(
            1L,
            "MEET123",
            "Security Meeting",
            "Discuss incident",
            "2025-12-05T10:00:00",
            "2025-12-05T11:00:00",
            "admin@test.com",
            participants,
            2,
            3600L,
            "COMPLETED",
            "{\"items\": []}",
            "{\"incident\": \"INC-001\"}"
        );
        
        assertEquals(1L, response.id());
        assertEquals("MEET123", response.code());
        assertEquals("Security Meeting", response.title());
        assertEquals("Discuss incident", response.description());
        assertEquals("2025-12-05T10:00:00", response.startTime());
        assertEquals("2025-12-05T11:00:00", response.endTime());
        assertEquals("admin@test.com", response.creatorEmail());
        assertEquals(2, response.participantEmails().size());
        assertEquals(2, response.currentParticipantCount());
        assertEquals(3600L, response.durationSeconds());
        assertEquals("COMPLETED", response.status());
        assertNotNull(response.checklistJson());
        assertNotNull(response.incidentContextJson());
    }

    @Test
    void testRecordWithNullValues() {
        MeetingResponse response = new MeetingResponse(
            null, null, null, null, null, null, null, null, null, null, null, null, null
        );
        
        assertNull(response.id());
        assertNull(response.code());
        assertNull(response.title());
        assertNull(response.description());
        assertNull(response.startTime());
        assertNull(response.endTime());
        assertNull(response.creatorEmail());
        assertNull(response.participantEmails());
        assertNull(response.currentParticipantCount());
        assertNull(response.durationSeconds());
        assertNull(response.status());
        assertNull(response.checklistJson());
        assertNull(response.incidentContextJson());
    }

    @Test
    void testRecordWithEmptyParticipants() {
        MeetingResponse response = new MeetingResponse(
            2L,
            "MEET456",
            "Test Meeting",
            "Test",
            "2025-12-05T14:00:00",
            null,
            "admin@test.com",
            Collections.emptySet(),
            0,
            0L,
            "ACTIVE",
            null,
            null
        );
        
        assertEquals(2L, response.id());
        assertTrue(response.participantEmails().isEmpty());
        assertEquals(0, response.currentParticipantCount());
        assertEquals("ACTIVE", response.status());
        assertNull(response.endTime());
    }

    @Test
    void testRecordEquality() {
        Set<String> participants = Set.of("user@test.com");
        
        MeetingResponse response1 = new MeetingResponse(
            1L, "CODE", "Title", "Desc", "Start", "End", 
            "creator@test.com", participants, 1, 100L, "ACTIVE", null, null
        );
        
        MeetingResponse response2 = new MeetingResponse(
            1L, "CODE", "Title", "Desc", "Start", "End", 
            "creator@test.com", participants, 1, 100L, "ACTIVE", null, null
        );
        
        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }
}
