package com.arsw.ids_ia.controller.chat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.arsw.ids_ia.dto.chat.AIPrivateChatRequest;
import com.arsw.ids_ia.dto.chat.AIPrivateChatResponse;
import com.arsw.ids_ia.model.Meeting;
import com.arsw.ids_ia.model.User;
import com.arsw.ids_ia.model.ai.AttackType;
import com.arsw.ids_ia.model.chat.AIPrivateMessage;
import com.arsw.ids_ia.repository.MeetingRepository;
import com.arsw.ids_ia.repository.UserRepository;
import com.arsw.ids_ia.service.ai.AIResponseService;
import com.arsw.ids_ia.service.ai.AIResponseService.IncidentContext;
import com.arsw.ids_ia.service.chat.AIPrivateChatService;
import com.arsw.ids_ia.ws.WarRoomChatSocketHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/ai-chat")
public class AIPrivateChatController {
    private static final Logger logger = LoggerFactory.getLogger(AIPrivateChatController.class);
    
    @Autowired
    private AIPrivateChatService chatService;
    @Autowired
    private MeetingRepository meetingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AIResponseService aiResponseService;
    @Autowired
    private WarRoomChatSocketHandler chatSocketHandler;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/meeting/{meetingId}")
    public ResponseEntity<List<AIPrivateChatResponse>> getPrivateMessages(
            @PathVariable Long meetingId,
            @AuthenticationPrincipal Jwt jwt) {
        
        String userEmail = jwt.getClaim("email");
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }
        
        // Get ALL messages for the meeting (shared AI chat for all participants)
        List<AIPrivateMessage> messages = chatService.getAllMessagesByMeeting(meetingId);
        
        // Note: Welcome message should be created only when the meeting is created, not here
        // This prevents duplicate welcome messages on every page refresh
        
        List<AIPrivateChatResponse> response = messages.stream().map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/meeting/{meetingId}")
    public ResponseEntity<AIPrivateChatResponse> sendPrivateMessage(
            @PathVariable Long meetingId,
            @RequestBody AIPrivateChatRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        String userEmail = jwt.getClaim("email");
        User user = userRepository.findByEmail(userEmail).orElse(null);
        Meeting meeting = meetingRepository.findById(meetingId).orElse(null);
        
        if (user == null || meeting == null) {
            return ResponseEntity.badRequest().build();
        }
        
        // TODO: Enable admin check later when system is stable
        // boolean isAdmin = user.getAuthorities().stream()
        //     .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()));
        // if (!isAdmin) {
        //     return ResponseEntity.status(403).build();
        // }
        
        // Save user message
        AIPrivateMessage userMessage = AIPrivateMessage.builder()
            .meeting(meeting)
            .user(user)
            .content(request.getContent())
            .role("user")
            .createdAt(LocalDateTime.now())
            .build();
        AIPrivateMessage savedUserMessage = chatService.saveMessage(userMessage);
        
        // Broadcast user message to all participants via WebSocket
        try {
            chatSocketHandler.broadcastMessage(
                String.valueOf(meetingId),
                user.getEmail(),
                user.getName() != null ? user.getName() : user.getEmail().split("@")[0],
                "ai_user", // Special role for AI chat user messages
                request.getContent(),
                savedUserMessage.getCreatedAt().toString()
            );
        } catch (Exception e) {
            logger.warn("Failed to broadcast AI user message via WebSocket: {}", e.getMessage());
        }
        
        // Generate AI response
        try {
            IncidentContext context = parseIncidentContext(meeting);
            String aiResponse = aiResponseService.generateResponse(request.getContent(), context);
            
            // Save AI response
            AIPrivateMessage aiMessage = AIPrivateMessage.builder()
                .meeting(meeting)
                .user(user)
                .content(aiResponse)
                .role("assistant")
                .createdAt(LocalDateTime.now())
                .build();
            AIPrivateMessage savedAIMessage = chatService.saveMessage(aiMessage);
            
            // Broadcast AI response to all participants via WebSocket
            try {
                chatSocketHandler.broadcastMessage(
                    String.valueOf(meetingId),
                    "ai@system.local", // Special email for AI
                    "IA Asistente",
                    "ai_assistant", // Special role for AI responses
                    aiResponse,
                    savedAIMessage.getCreatedAt().toString()
                );
            } catch (Exception e) {
                logger.warn("Failed to broadcast AI response via WebSocket: {}", e.getMessage());
            }
            
            logger.info("AI private response generated for user {} in meeting {}", userEmail, meetingId);
            
        } catch (Exception e) {
            logger.error("Failed to generate AI private response: {}", e.getMessage(), e);
        }
        
        return ResponseEntity.ok(toResponse(savedUserMessage));
    }
    
    /**
     * Parse incident context from meeting JSON
     */
    private IncidentContext parseIncidentContext(Meeting meeting) {
        try {
            if (meeting.getIncidentContextJson() != null && !meeting.getIncidentContextJson().isEmpty()) {
                JsonNode contextNode = objectMapper.readTree(meeting.getIncidentContextJson());
                
                String attackTypeStr = contextNode.has("attackType") ? contextNode.get("attackType").asText() : "UNKNOWN";
                double probability = contextNode.has("attackProbability") ? contextNode.get("attackProbability").asDouble() : 0.5;
                String severity = contextNode.has("severity") ? contextNode.get("severity").asText() : "medium";
                
                AttackType attackType = AttackType.fromPrediction(attackTypeStr);
                return new IncidentContext(attackType, probability, severity);
            }
        } catch (Exception e) {
            logger.warn("Failed to parse incident context for private chat, using defaults: {}", e.getMessage());
        }
        
        return new IncidentContext(AttackType.UNKNOWN, 0.5, "medium");
    }

    private AIPrivateChatResponse toResponse(AIPrivateMessage message) {
        AIPrivateChatResponse resp = new AIPrivateChatResponse();
        resp.setId(message.getId());
        resp.setMeetingId(message.getMeeting().getId());
        resp.setUserId(message.getUser().getId());
        resp.setUserEmail(message.getUser().getEmail());
        resp.setContent(message.getContent());
        resp.setRole(message.getRole());
        resp.setCreatedAt(message.getCreatedAt());
        return resp;
    }
}