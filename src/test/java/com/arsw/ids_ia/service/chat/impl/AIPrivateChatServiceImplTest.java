package com.arsw.ids_ia.service.chat.impl;

import com.arsw.ids_ia.model.chat.AIPrivateMessage;
import com.arsw.ids_ia.repository.AIPrivateMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AIPrivateChatServiceImpl
 * Tests AI private chat message operations (save, retrieve by meeting/user)
 */
@ExtendWith(MockitoExtension.class)
class AIPrivateChatServiceImplTest {

    @Mock
    private AIPrivateMessageRepository messageRepository;

    @InjectMocks
    private AIPrivateChatServiceImpl service;

    private AIPrivateMessage testMessage;
    
    @BeforeEach
    void setUp() {
        testMessage = new AIPrivateMessage();
        testMessage.setId(1L);
        testMessage.setRole("assistant");
        testMessage.setContent("Test AI response");
        testMessage.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void getMessagesByMeetingAndUser_returnsMessages() {
        // Arrange
        Long meetingId = 100L;
        Long userId = 50L;
        List<AIPrivateMessage> messages = Arrays.asList(testMessage);
        when(messageRepository.findByMeetingIdAndUserIdOrderByCreatedAtAsc(meetingId, userId))
            .thenReturn(messages);

        // Act
        List<AIPrivateMessage> result = service.getMessagesByMeetingAndUser(meetingId, userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testMessage.getId(), result.get(0).getId());
        verify(messageRepository).findByMeetingIdAndUserIdOrderByCreatedAtAsc(meetingId, userId);
    }

    @Test
    void getMessagesByMeetingAndUser_withNoMessages_returnsEmptyList() {
        // Arrange
        Long meetingId = 200L;
        Long userId = 60L;
        when(messageRepository.findByMeetingIdAndUserIdOrderByCreatedAtAsc(meetingId, userId))
            .thenReturn(Arrays.asList());

        // Act
        List<AIPrivateMessage> result = service.getMessagesByMeetingAndUser(meetingId, userId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAdminMessagesByMeeting_returnsEmptyList() {
        // Act - método temporalmente retorna lista vacía según el código
        List<AIPrivateMessage> result = service.getAdminMessagesByMeeting(100L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllMessagesByMeeting_returnsAllMessages() {
        // Arrange
        Long meetingId = 300L;
        AIPrivateMessage msg2 = new AIPrivateMessage();
        msg2.setId(2L);
        msg2.setRole("user");
        msg2.setContent("User question");
        
        List<AIPrivateMessage> messages = Arrays.asList(testMessage, msg2);
        when(messageRepository.findByMeetingIdOrderByCreatedAtAsc(meetingId))
            .thenReturn(messages);

        // Act
        List<AIPrivateMessage> result = service.getAllMessagesByMeeting(meetingId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(messageRepository).findByMeetingIdOrderByCreatedAtAsc(meetingId);
    }

    @Test
    void getAllMessagesByMeeting_withNoMessages_returnsEmptyList() {
        // Arrange
        Long meetingId = 400L;
        when(messageRepository.findByMeetingIdOrderByCreatedAtAsc(meetingId))
            .thenReturn(Arrays.asList());

        // Act
        List<AIPrivateMessage> result = service.getAllMessagesByMeeting(meetingId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void saveMessage_savesAndReturnsMessage() {
        // Arrange
        AIPrivateMessage newMessage = new AIPrivateMessage();
        newMessage.setRole("user");
        newMessage.setContent("New user message");
        
        when(messageRepository.save(any(AIPrivateMessage.class)))
            .thenReturn(testMessage);

        // Act
        AIPrivateMessage result = service.saveMessage(newMessage);

        // Assert
        assertNotNull(result);
        assertEquals(testMessage.getId(), result.getId());
        verify(messageRepository).save(newMessage);
    }

    @Test
    void saveMessage_withNullContent_savesSuccessfully() {
        // Arrange
        AIPrivateMessage messageWithNullContent = new AIPrivateMessage();
        messageWithNullContent.setRole("assistant");
        messageWithNullContent.setContent(null);
        
        when(messageRepository.save(any(AIPrivateMessage.class)))
            .thenReturn(messageWithNullContent);

        // Act
        AIPrivateMessage result = service.saveMessage(messageWithNullContent);

        // Assert
        assertNotNull(result);
        assertNull(result.getContent());
        verify(messageRepository).save(messageWithNullContent);
    }

    @Test
    void getMessagesByMeetingAndUser_withMultipleMessages_maintainsOrder() {
        // Arrange
        Long meetingId = 500L;
        Long userId = 70L;
        
        AIPrivateMessage msg1 = new AIPrivateMessage();
        msg1.setId(1L);
        msg1.setCreatedAt(LocalDateTime.now().minusMinutes(10));
        
        AIPrivateMessage msg2 = new AIPrivateMessage();
        msg2.setId(2L);
        msg2.setCreatedAt(LocalDateTime.now().minusMinutes(5));
        
        AIPrivateMessage msg3 = new AIPrivateMessage();
        msg3.setId(3L);
        msg3.setCreatedAt(LocalDateTime.now());
        
        List<AIPrivateMessage> messages = Arrays.asList(msg1, msg2, msg3);
        when(messageRepository.findByMeetingIdAndUserIdOrderByCreatedAtAsc(meetingId, userId))
            .thenReturn(messages);

        // Act
        List<AIPrivateMessage> result = service.getMessagesByMeetingAndUser(meetingId, userId);

        // Assert
        assertEquals(3, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
        assertEquals(3L, result.get(2).getId());
    }
}
