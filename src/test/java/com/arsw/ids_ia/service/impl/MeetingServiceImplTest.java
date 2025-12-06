package com.arsw.ids_ia.service.impl;

import com.arsw.ids_ia.dto.request.CreateMeetingRequest;
import com.arsw.ids_ia.dto.request.JoinMeetingRequest;
import com.arsw.ids_ia.exception.UnauthorizedException;
import com.arsw.ids_ia.model.Meeting;
import com.arsw.ids_ia.model.User;
import com.arsw.ids_ia.repository.AlertRepository;
import com.arsw.ids_ia.repository.MeetingRepository;
import com.arsw.ids_ia.repository.UserRepository;
import com.arsw.ids_ia.service.ai.AIResponseService;
import com.arsw.ids_ia.service.ai.ChecklistGenerator;
import com.arsw.ids_ia.service.chat.AIPrivateChatService;
import com.arsw.ids_ia.utils.enums.Role;
import com.arsw.ids_ia.ws.TrafficSocketHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeetingServiceImplTest {

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private TrafficSocketHandler socketHandler;

    @Mock
    private ChecklistGenerator checklistGenerator;

    @Mock
    private AIResponseService aiResponseService;

    @Mock
    private AIPrivateChatService aiChatService;

    @InjectMocks
    private MeetingServiceImpl meetingService;

    private User adminUser;
    private User regularUser;
    private CreateMeetingRequest createRequest;

    @BeforeEach
    void setUp() {
        adminUser = User.builder()
                .id(1L)
                .email("admin@test.com")
                .name("Admin User")
                .role(Role.ADMIN)
                .build();

        regularUser = User.builder()
                .id(2L)
                .email("user@test.com")
                .name("Regular User")
                .role(Role.USER)
                .build();

        createRequest = new CreateMeetingRequest();
        createRequest.setTitle("Test Meeting");
        createRequest.setDescription("Test Description");
    }

    @Test
    void testCreateMeetingWithAdminUser() {
        // Arrange
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));
        
        Meeting savedMeeting = Meeting.builder()
                .id(1L)
                .code("TEST123")
                .title("Test Meeting")
                .description("Test Description")
                .creator(adminUser)
                .participants(new HashSet<>())
                .status("ACTIVE")
                .currentParticipantCount(1)
                .startTime(LocalDateTime.now())
                .build();
        
        when(meetingRepository.save(any(Meeting.class))).thenReturn(savedMeeting);
        when(meetingRepository.saveAndFlush(any(Meeting.class))).thenReturn(savedMeeting);

        // Act
        Meeting result = meetingService.createMeeting(createRequest, "admin@test.com");

        // Assert
        assertNotNull(result);
        assertEquals("Test Meeting", result.getTitle());
        assertEquals("admin@test.com", result.getCreator().getEmail());
        verify(meetingRepository, atLeastOnce()).save(any(Meeting.class));
    }

    @Test
    void testCreateMeetingWithNonAdminUserThrowsException() {
        // Arrange
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(regularUser));

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> {
            meetingService.createMeeting(createRequest, "user@test.com");
        });
        
        verify(meetingRepository, never()).save(any(Meeting.class));
    }

    @Test
    void testCreateMeetingWithNonExistentUserThrowsException() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            meetingService.createMeeting(createRequest, "nonexistent@test.com");
        });
        
        verify(meetingRepository, never()).save(any(Meeting.class));
    }

    @Test
    void testCreateMeetingWithIncidentId() {
        // Arrange
        createRequest.setIncidentId("INC-001");
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));
        
        Meeting savedMeeting = Meeting.builder()
                .id(1L)
                .code("TEST123")
                .title("Test Meeting")
                .description("Test Description")
                .creator(adminUser)
                .participants(new HashSet<>())
                .status("ACTIVE")
                .currentParticipantCount(1)
                .startTime(LocalDateTime.now())
                .build();
        
        when(meetingRepository.save(any(Meeting.class))).thenReturn(savedMeeting);
        when(meetingRepository.saveAndFlush(any(Meeting.class))).thenReturn(savedMeeting);
        when(alertRepository.findByIncidentId(anyString())).thenReturn(java.util.Collections.emptyList());

        // Act
        Meeting result = meetingService.createMeeting(createRequest, "admin@test.com");

        // Assert
        assertNotNull(result);
        verify(alertRepository).findByIncidentId("INC-001");
    }

    @Test
    void testJoinMeetingWithValidCode() {
        // Arrange
        JoinMeetingRequest joinRequest = new JoinMeetingRequest();
        joinRequest.setCode("TEST123");
        
        Meeting meeting = Meeting.builder()
                .id(1L)
                .code("TEST123")
                .title("Test Meeting")
                .creator(adminUser)
                .participants(new HashSet<>())
                .status("ACTIVE")
                .currentParticipantCount(0)
                .build();
        
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(regularUser));
        when(meetingRepository.findByCode("TEST123")).thenReturn(Optional.of(meeting));
        when(meetingRepository.save(any(Meeting.class))).thenReturn(meeting);

        // Act
        Meeting result = meetingService.joinMeeting(joinRequest, "user@test.com");

        // Assert
        assertNotNull(result);
        assertEquals("TEST123", result.getCode());
        verify(meetingRepository).save(any(Meeting.class));
    }

    @Test
    void testJoinMeetingWithInvalidCodeThrowsException() {
        // Arrange
        JoinMeetingRequest joinRequest = new JoinMeetingRequest();
        joinRequest.setCode("INVALID");
        
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(regularUser));
        when(meetingRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            meetingService.joinMeeting(joinRequest, "user@test.com");
        });
    }

    @Test
    void testGetMeetingById() {
        // Arrange
        Meeting meeting = Meeting.builder()
                .id(1L)
                .code("TEST123")
                .title("Test Meeting")
                .creator(adminUser)
                .build();
        
        when(meetingRepository.findById(1L)).thenReturn(Optional.of(meeting));

        // Act
        Meeting result = meetingService.getMeetingById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testGetMeetingByIdNotFound() {
        // Arrange
        when(meetingRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            meetingService.getMeetingById(999L);
        });
    }

    @Test
    void testGetMeetingByCode() {
        // Arrange
        Meeting meeting = Meeting.builder()
                .id(1L)
                .code("ABC123")
                .title("Test Meeting")
                .creator(adminUser)
                .build();
        
        when(meetingRepository.findByCode("ABC123")).thenReturn(Optional.of(meeting));

        // Act
        Meeting result = meetingService.getMeetingByCode("ABC123");

        // Assert
        assertNotNull(result);
        assertEquals("ABC123", result.getCode());
    }

    @Test
    void testGetMeetingByCodeNotFound() {
        // Arrange
        when(meetingRepository.findByCode("NOTFOUND")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            meetingService.getMeetingByCode("NOTFOUND");
        });
    }

    @Test
    void testLeaveMeetingSuccess() {
        // Arrange
        Meeting meeting = Meeting.builder()
                .id(1L)
                .code("TEST123")
                .title("Test Meeting")
                .creator(adminUser)
                .participants(new HashSet<>())
                .currentParticipantCount(1)
                .status("ACTIVE")
                .build();
        
        meeting.getParticipants().add(regularUser);
        
        when(meetingRepository.findById(1L)).thenReturn(Optional.of(meeting));
        when(meetingRepository.save(any(Meeting.class))).thenReturn(meeting);

        // Act
        Meeting result = meetingService.leaveMeeting(1L, "user@test.com");

        // Assert
        assertNotNull(result);
        verify(meetingRepository).save(any(Meeting.class));
    }

    @Test
    void testLeaveMeetingNotFound() {
        // Arrange
        when(meetingRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            meetingService.leaveMeeting(999L, "user@test.com");
        });
    }

    @Test
    void testLeaveMeetingUserNotInParticipants() {
        // Arrange - meeting without the user
        Meeting meeting = Meeting.builder()
                .id(1L)
                .code("TEST123")
                .participants(new HashSet<>())
                .build();
        
        when(meetingRepository.findById(1L)).thenReturn(Optional.of(meeting));

        // Act - método retorna meeting sin modificar, NO lanza excepción
        Meeting result = meetingService.leaveMeeting(1L, "unknown@test.com");

        // Assert
        assertNotNull(result);
        verify(meetingRepository, never()).save(any(Meeting.class));
    }

    @Test
    void testBroadcastDurationUpdate() {
        // Arrange
        Meeting meeting = Meeting.builder()
                .id(1L)
                .code("TEST123")
                .startTime(LocalDateTime.now().minusMinutes(30))
                .status("ACTIVE")
                .build();
        
        when(meetingRepository.findById(1L)).thenReturn(Optional.of(meeting));

        // Act & Assert - should not throw
        assertDoesNotThrow(() -> meetingService.broadcastDurationUpdate(1L));
    }

    @Test
    void testBroadcastDurationUpdateMeetingNotFound() {
        // Arrange
        when(meetingRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert - should handle gracefully
        assertDoesNotThrow(() -> meetingService.broadcastDurationUpdate(999L));
    }

    @Test
    void testMarkIncidentAsResolved() {
        // Arrange
        Meeting meeting = Meeting.builder()
                .id(1L)
                .code("TEST123")
                .creator(adminUser)
                .startTime(LocalDateTime.now().minusHours(2))
                .status("ACTIVE")
                .build();
        
        when(meetingRepository.findById(1L)).thenReturn(Optional.of(meeting));
        when(meetingRepository.save(any(Meeting.class))).thenReturn(meeting);

        // Act
        meetingService.markIncidentAsResolved(1L, "admin@test.com");

        // Assert
        verify(meetingRepository).save(any(Meeting.class));
    }

    @Test
    void testMarkIncidentAsResolvedNotCreator() {
        // Arrange
        Meeting meeting = Meeting.builder()
                .id(1L)
                .code("TEST123")
                .creator(adminUser)
                .status("ACTIVE")
                .build();
        
        when(meetingRepository.findById(1L)).thenReturn(Optional.of(meeting));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            meetingService.markIncidentAsResolved(1L, "user@test.com");
        });
    }

    @Test
    void testMarkIncidentAsResolvedAlreadyResolved() {
        // Arrange
        Meeting meeting = Meeting.builder()
                .id(1L)
                .code("TEST123")
                .creator(adminUser)
                .status("RESOLVED")
                .endTime(LocalDateTime.now())
                .build();
        
        when(meetingRepository.findById(1L)).thenReturn(Optional.of(meeting));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            meetingService.markIncidentAsResolved(1L, "admin@test.com");
        });
    }

    @Test
    void testJoinMeetingUserAlreadyParticipant() {
        // Arrange
        Meeting meeting = Meeting.builder()
                .id(1L)
                .code("TEST123")
                .creator(adminUser)
                .participants(new HashSet<>())
                .status("ACTIVE")
                .currentParticipantCount(1)
                .build();
        
        meeting.getParticipants().add(regularUser); // User already in participants
        
        when(meetingRepository.findByCode("TEST123")).thenReturn(Optional.of(meeting));
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(regularUser));

        JoinMeetingRequest request = new JoinMeetingRequest();
        request.setCode("TEST123");

        // Act
        Meeting result = meetingService.joinMeeting(request, "user@test.com");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getParticipants().size()); // Should still be 1, not duplicated
    }
}

