package com.arsw.ids_ia.controller.chat;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import com.arsw.ids_ia.dto.chat.AIPrivateChatRequest;
import com.arsw.ids_ia.dto.chat.AIPrivateChatResponse;
import com.arsw.ids_ia.model.Meeting;
import com.arsw.ids_ia.model.User;
import com.arsw.ids_ia.model.chat.AIPrivateMessage;
import com.arsw.ids_ia.repository.MeetingRepository;
import com.arsw.ids_ia.repository.UserRepository;
import com.arsw.ids_ia.service.ai.AIResponseService;
import com.arsw.ids_ia.service.ai.AIResponseService.IncidentContext;
import com.arsw.ids_ia.service.chat.AIPrivateChatService;
import com.arsw.ids_ia.ws.WarRoomChatSocketHandler;

@ExtendWith(MockitoExtension.class)
class AIPrivateChatControllerTest {

    @Mock
    private AIPrivateChatService chatService;
    
    @Mock
    private MeetingRepository meetingRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private AIResponseService aiResponseService;
    
    @Mock
    private WarRoomChatSocketHandler chatSocketHandler;
    
    @InjectMocks
    private AIPrivateChatController controller;
    
    @Mock
    private Jwt jwt;
    
    private User testUser;
    private Meeting testMeeting;
    private AIPrivateMessage testMessage;
    
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");
        
        testMeeting = new Meeting();
        testMeeting.setId(100L);
        testMeeting.setCode("MEET123");
        testMeeting.setTitle("Test Meeting");
        testMeeting.setIncidentContextJson("{\"attackType\":\"DOS\",\"attackProbability\":0.85,\"severity\":\"high\"}");
        
        testMessage = AIPrivateMessage.builder()
            .id(1L)
            .meeting(testMeeting)
            .user(testUser)
            .content("Test message")
            .role("user")
            .createdAt(LocalDateTime.now())
            .build();
    }
    
    @Test
    void testGetPrivateMessages_Success() {
        // Arrange
        when(jwt.getClaim("email")).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(chatService.getAllMessagesByMeeting(100L)).thenReturn(Arrays.asList(testMessage));
        
        // Act
        ResponseEntity<List<AIPrivateChatResponse>> response = controller.getPrivateMessages(100L, jwt);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        AIPrivateChatResponse message = response.getBody().get(0);
        assertEquals(1L, message.getId());
        assertEquals(100L, message.getMeetingId());
        assertEquals("Test message", message.getContent());
        assertEquals("user", message.getRole());
        
        verify(userRepository).findByEmail("test@example.com");
        verify(chatService).getAllMessagesByMeeting(100L);
    }
    
    @Test
    void testGetPrivateMessages_UserNotFound() {
        // Arrange
        when(jwt.getClaim("email")).thenReturn("unknown@example.com");
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
        
        // Act
        ResponseEntity<List<AIPrivateChatResponse>> response = controller.getPrivateMessages(100L, jwt);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userRepository).findByEmail("unknown@example.com");
        verify(chatService, never()).getAllMessagesByMeeting(anyLong());
    }
    
    @Test
    void testSendPrivateMessage_Success() throws Exception {
        // Arrange
        AIPrivateChatRequest request = new AIPrivateChatRequest();
        request.setContent("User question");
        
        when(jwt.getClaim("email")).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(meetingRepository.findById(100L)).thenReturn(Optional.of(testMeeting));
        when(chatService.saveMessage(any(AIPrivateMessage.class))).thenReturn(testMessage);
        when(aiResponseService.generateResponse(anyString(), any(IncidentContext.class)))
            .thenReturn("AI response");
        doNothing().when(chatSocketHandler).broadcastMessage(anyString(), anyString(), anyString(), 
            anyString(), anyString(), anyString());
        
        // Act
        ResponseEntity<AIPrivateChatResponse> response = controller.sendPrivateMessage(100L, request, jwt);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Test message", response.getBody().getContent());
        
        verify(chatService, atLeast(1)).saveMessage(any(AIPrivateMessage.class));
        verify(aiResponseService).generateResponse(eq("User question"), any(IncidentContext.class));
        verify(chatSocketHandler, times(2)).broadcastMessage(anyString(), anyString(), anyString(), 
            anyString(), anyString(), anyString());
    }
    
    @Test
    void testSendPrivateMessage_EmptyContent() {
        // Arrange
        AIPrivateChatRequest request = new AIPrivateChatRequest();
        request.setContent("");
        
        // Act
        ResponseEntity<AIPrivateChatResponse> response = controller.sendPrivateMessage(100L, request, jwt);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(chatService, never()).saveMessage(any());
    }
    
    @Test
    void testSendPrivateMessage_NullContent() {
        // Arrange
        AIPrivateChatRequest request = new AIPrivateChatRequest();
        request.setContent(null);
        
        // Act
        ResponseEntity<AIPrivateChatResponse> response = controller.sendPrivateMessage(100L, request, jwt);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(chatService, never()).saveMessage(any());
    }
    
    @Test
    void testSendPrivateMessage_UserNotFound() {
        // Arrange
        AIPrivateChatRequest request = new AIPrivateChatRequest();
        request.setContent("Test message");
        
        when(jwt.getClaim("email")).thenReturn("unknown@example.com");
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
        
        // Act
        ResponseEntity<AIPrivateChatResponse> response = controller.sendPrivateMessage(100L, request, jwt);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(chatService, never()).saveMessage(any());
    }
    
    @Test
    void testSendPrivateMessage_MeetingNotFound() {
        // Arrange
        AIPrivateChatRequest request = new AIPrivateChatRequest();
        request.setContent("Test message");
        
        when(jwt.getClaim("email")).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(meetingRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act
        ResponseEntity<AIPrivateChatResponse> response = controller.sendPrivateMessage(999L, request, jwt);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(chatService, never()).saveMessage(any());
    }
    
    @Test
    void testSendPrivateMessage_WebSocketBroadcastFails() throws Exception {
        // Arrange
        AIPrivateChatRequest request = new AIPrivateChatRequest();
        request.setContent("User question");
        
        when(jwt.getClaim("email")).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(meetingRepository.findById(100L)).thenReturn(Optional.of(testMeeting));
        when(chatService.saveMessage(any(AIPrivateMessage.class))).thenReturn(testMessage);
        when(aiResponseService.generateResponse(anyString(), any(IncidentContext.class)))
            .thenReturn("AI response");
        doThrow(new RuntimeException("WebSocket error"))
            .when(chatSocketHandler).broadcastMessage(anyString(), anyString(), anyString(), 
                anyString(), anyString(), anyString());
        
        // Act
        ResponseEntity<AIPrivateChatResponse> response = controller.sendPrivateMessage(100L, request, jwt);
        
        // Assert - Should still return OK even if WebSocket fails
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(chatService, atLeast(1)).saveMessage(any(AIPrivateMessage.class));
    }
    
    @Test
    void testSendPrivateMessage_AIGenerationFails() throws Exception {
        // Arrange
        AIPrivateChatRequest request = new AIPrivateChatRequest();
        request.setContent("User question");
        
        when(jwt.getClaim("email")).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(meetingRepository.findById(100L)).thenReturn(Optional.of(testMeeting));
        when(chatService.saveMessage(any(AIPrivateMessage.class))).thenReturn(testMessage);
        when(aiResponseService.generateResponse(anyString(), any(IncidentContext.class)))
            .thenThrow(new RuntimeException("AI service error"));
        
        // Act
        ResponseEntity<AIPrivateChatResponse> response = controller.sendPrivateMessage(100L, request, jwt);
        
        // Assert - Should still return OK even if AI fails
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(chatService).saveMessage(any(AIPrivateMessage.class));
        verify(aiResponseService).generateResponse(anyString(), any(IncidentContext.class));
    }
    
    @Test
    void testParseIncidentContext_ValidJson() throws Exception {
        // Arrange
        AIPrivateChatRequest request = new AIPrivateChatRequest();
        request.setContent("Test");
        
        testMeeting.setIncidentContextJson("{\"attackType\":\"SQL_INJECTION\",\"attackProbability\":0.95,\"severity\":\"critical\"}");
        
        when(jwt.getClaim("email")).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(meetingRepository.findById(100L)).thenReturn(Optional.of(testMeeting));
        when(chatService.saveMessage(any(AIPrivateMessage.class))).thenReturn(testMessage);
        when(aiResponseService.generateResponse(anyString(), any(IncidentContext.class)))
            .thenReturn("AI response");
        
        // Act
        ResponseEntity<AIPrivateChatResponse> response = controller.sendPrivateMessage(100L, request, jwt);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(aiResponseService).generateResponse(anyString(), any(IncidentContext.class));
    }
    
    @Test
    void testParseIncidentContext_NullJson() throws Exception {
        // Arrange
        AIPrivateChatRequest request = new AIPrivateChatRequest();
        request.setContent("Test");
        
        testMeeting.setIncidentContextJson(null);
        
        when(jwt.getClaim("email")).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(meetingRepository.findById(100L)).thenReturn(Optional.of(testMeeting));
        when(chatService.saveMessage(any(AIPrivateMessage.class))).thenReturn(testMessage);
        when(aiResponseService.generateResponse(anyString(), any(IncidentContext.class)))
            .thenReturn("AI response");
        
        // Act
        ResponseEntity<AIPrivateChatResponse> response = controller.sendPrivateMessage(100L, request, jwt);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(aiResponseService).generateResponse(anyString(), any(IncidentContext.class));
    }
    
    @Test
    void testParseIncidentContext_EmptyJson() throws Exception {
        // Arrange
        AIPrivateChatRequest request = new AIPrivateChatRequest();
        request.setContent("Test");
        
        testMeeting.setIncidentContextJson("");
        
        when(jwt.getClaim("email")).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(meetingRepository.findById(100L)).thenReturn(Optional.of(testMeeting));
        when(chatService.saveMessage(any(AIPrivateMessage.class))).thenReturn(testMessage);
        when(aiResponseService.generateResponse(anyString(), any(IncidentContext.class)))
            .thenReturn("AI response");
        
        // Act
        ResponseEntity<AIPrivateChatResponse> response = controller.sendPrivateMessage(100L, request, jwt);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(aiResponseService).generateResponse(anyString(), any(IncidentContext.class));
    }
    
    @Test
    void testParseIncidentContext_InvalidJson() throws Exception {
        // Arrange
        AIPrivateChatRequest request = new AIPrivateChatRequest();
        request.setContent("Test");
        
        testMeeting.setIncidentContextJson("{invalid json}");
        
        when(jwt.getClaim("email")).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(meetingRepository.findById(100L)).thenReturn(Optional.of(testMeeting));
        when(chatService.saveMessage(any(AIPrivateMessage.class))).thenReturn(testMessage);
        when(aiResponseService.generateResponse(anyString(), any(IncidentContext.class)))
            .thenReturn("AI response");
        
        // Act
        ResponseEntity<AIPrivateChatResponse> response = controller.sendPrivateMessage(100L, request, jwt);
        
        // Assert - Should use default context
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(aiResponseService).generateResponse(anyString(), any(IncidentContext.class));
    }
}
