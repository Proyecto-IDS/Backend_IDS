package com.arsw.ids_ia.controller;

import com.arsw.ids_ia.dto.request.CreateMeetingRequest;
import com.arsw.ids_ia.dto.request.JoinMeetingRequest;
import com.arsw.ids_ia.model.Meeting;
import com.arsw.ids_ia.model.User;
import com.arsw.ids_ia.service.MeetingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeetingControllerTest {

    @Mock
    private MeetingService meetingService;

    @Mock
    private Jwt jwt;

    @InjectMocks
    private MeetingController meetingController;

    private User testUser;
    private Meeting testMeeting;
    private CreateMeetingRequest createRequest;
    private JoinMeetingRequest joinRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("admin@test.com");
        testUser.setName("Admin User");

        testMeeting = new Meeting();
        testMeeting.setId(1L);
        testMeeting.setCode("MEET123");
        testMeeting.setTitle("Test Meeting");
        testMeeting.setDescription("Test Description");
        testMeeting.setCreator(testUser);
        testMeeting.setParticipants(new HashSet<>(Set.of(testUser)));
        testMeeting.setCurrentParticipantCount(1);
        testMeeting.setStatus("ACTIVE");
        testMeeting.setStartTime(LocalDateTime.now());

        createRequest = new CreateMeetingRequest();
        createRequest.setTitle("New Meeting");
        createRequest.setDescription("New Description");
        createRequest.setIncidentId("INC-001");

        joinRequest = new JoinMeetingRequest();
        joinRequest.setCode("MEET123");
    }

    @Test
    void testCreateMeeting_Success() {
        when(jwt.getClaim("email")).thenReturn("admin@test.com");
        when(meetingService.createMeeting(any(CreateMeetingRequest.class), eq("admin@test.com")))
            .thenReturn(testMeeting);

        var response = meetingController.createMeeting(createRequest, jwt);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("MEET123", response.getBody().code());
        verify(meetingService).createMeeting(createRequest, "admin@test.com");
    }

    @Test
    void testCreateMeeting_WithNullIncidentId() {
        createRequest.setIncidentId(null);
        when(jwt.getClaim("email")).thenReturn("admin@test.com");
        when(meetingService.createMeeting(any(CreateMeetingRequest.class), eq("admin@test.com")))
            .thenReturn(testMeeting);

        var response = meetingController.createMeeting(createRequest, jwt);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(meetingService).createMeeting(createRequest, "admin@test.com");
    }

    @Test
    void testJoinMeeting_Success() {
        when(jwt.getClaim("email")).thenReturn("user@test.com");
        when(meetingService.joinMeeting(any(JoinMeetingRequest.class), eq("user@test.com")))
            .thenReturn(testMeeting);

        var response = meetingController.joinMeeting(joinRequest, jwt);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("MEET123", response.getBody().code());
        verify(meetingService).joinMeeting(joinRequest, "user@test.com");
    }

    @Test
    void testGetMeeting_Success() {
        when(meetingService.getMeetingById(1L)).thenReturn(testMeeting);

        var response = meetingController.getMeeting(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().id());
        assertEquals("MEET123", response.getBody().code());
        verify(meetingService).getMeetingById(1L);
    }

    @Test
    void testLeaveMeeting_Success() {
        when(jwt.getClaim("email")).thenReturn("user@test.com");
        User leavingUser = new User();
        leavingUser.setEmail("user@test.com");
        testMeeting.setParticipants(new HashSet<>());
        testMeeting.setCurrentParticipantCount(0);
        when(meetingService.leaveMeeting(1L, "user@test.com")).thenReturn(testMeeting);

        var response = meetingController.leaveMeeting(1L, jwt);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().currentParticipantCount());
        verify(meetingService).leaveMeeting(1L, "user@test.com");
    }

    @Test
    void testMarkIncidentAsResolved_Success() {
        when(jwt.getClaim("email")).thenReturn("admin@test.com");
        testMeeting.setStatus("RESOLVED");
        testMeeting.setEndTime(LocalDateTime.now());
        when(meetingService.markIncidentAsResolved(1L, "admin@test.com")).thenReturn(testMeeting);

        var response = meetingController.markIncidentAsResolved(1L, jwt);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("RESOLVED", response.getBody().status());
        verify(meetingService).markIncidentAsResolved(1L, "admin@test.com");
    }

    @Test
    void testCreateMeetingResponse_WithNullTimestamps() {
        testMeeting.setStartTime(null);
        testMeeting.setEndTime(null);
        when(jwt.getClaim("email")).thenReturn("admin@test.com");
        when(meetingService.createMeeting(any(CreateMeetingRequest.class), eq("admin@test.com")))
            .thenReturn(testMeeting);

        var response = meetingController.createMeeting(createRequest, jwt);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().startTime());
        assertNull(response.getBody().endTime());
    }

    @Test
    void testCreateMeetingResponse_WithChecklistAndContext() {
        testMeeting.setChecklistJson("[{\"id\":1,\"label\":\"Test\",\"done\":false}]");
        testMeeting.setIncidentContextJson("{\"severity\":\"HIGH\"}");
        when(jwt.getClaim("email")).thenReturn("admin@test.com");
        when(meetingService.createMeeting(any(CreateMeetingRequest.class), eq("admin@test.com")))
            .thenReturn(testMeeting);

        var response = meetingController.createMeeting(createRequest, jwt);

        assertNotNull(response);
        assertNotNull(response.getBody().checklistJson());
        assertNotNull(response.getBody().incidentContextJson());
    }

    @Test
    void testCreateMeetingResponse_WithMultipleParticipants() {
        User user2 = new User();
        user2.setEmail("user2@test.com");
        User user3 = new User();
        user3.setEmail("user3@test.com");
        
        testMeeting.setParticipants(new HashSet<>(Set.of(testUser, user2, user3)));
        testMeeting.setCurrentParticipantCount(3);
        
        when(jwt.getClaim("email")).thenReturn("admin@test.com");
        when(meetingService.createMeeting(any(CreateMeetingRequest.class), eq("admin@test.com")))
            .thenReturn(testMeeting);

        var response = meetingController.createMeeting(createRequest, jwt);

        assertNotNull(response);
        assertEquals(3, response.getBody().currentParticipantCount());
        assertEquals(3, response.getBody().participantEmails().size());
        assertTrue(response.getBody().participantEmails().contains("admin@test.com"));
        assertTrue(response.getBody().participantEmails().contains("user2@test.com"));
    }

    @Test
    void testGetMeeting_WithDurationSeconds() {
        testMeeting.setDurationSeconds(3600L);
        when(meetingService.getMeetingById(1L)).thenReturn(testMeeting);

        var response = meetingController.getMeeting(1L);

        assertNotNull(response);
        assertEquals(3600L, response.getBody().durationSeconds());
    }
}
