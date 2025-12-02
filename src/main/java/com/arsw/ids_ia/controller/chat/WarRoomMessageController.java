package com.arsw.ids_ia.controller.chat;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.arsw.ids_ia.dto.chat.WarRoomMessageRequest;
import com.arsw.ids_ia.dto.chat.WarRoomMessageResponse;
import com.arsw.ids_ia.model.Meeting;
import com.arsw.ids_ia.model.User;
import com.arsw.ids_ia.model.ai.AttackType;
import com.arsw.ids_ia.model.chat.WarRoomMessage;
import com.arsw.ids_ia.repository.MeetingRepository;
import com.arsw.ids_ia.repository.UserRepository;
import com.arsw.ids_ia.service.ai.AIResponseService;
import com.arsw.ids_ia.service.ai.AIResponseService.IncidentContext;
import com.arsw.ids_ia.service.chat.WarRoomMessageService;
import com.arsw.ids_ia.ws.WarRoomChatSocketHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/warroom/messages")
public class WarRoomMessageController {
    private static final Logger logger = LoggerFactory.getLogger(WarRoomMessageController.class);
    
    @Autowired
    private WarRoomMessageService messageService;
    @Autowired
    private MeetingRepository meetingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private WarRoomChatSocketHandler chatSocketHandler;
    @Autowired
    private AIResponseService aiResponseService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping
    public ResponseEntity<List<WarRoomMessageResponse>> getMessages(Long meetingId) {
        List<WarRoomMessage> messages = messageService.getMessagesByMeetingId(meetingId);
        List<WarRoomMessageResponse> response = messages.stream().map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<WarRoomMessageResponse> sendMessage(@RequestBody WarRoomMessageRequest request, @AuthenticationPrincipal Jwt jwt) {
        Long meetingIdValue = request.getMeetingId();
        if (meetingIdValue == null || request.getContent() == null) {
            return ResponseEntity.badRequest().build();
        }
        
        User sender = userRepository.findByEmail(jwt.getClaim("email")).orElse(null);
        Meeting meeting = meetingRepository.findById(meetingIdValue).orElse(null);
        if (sender == null || meeting == null) {
            return ResponseEntity.badRequest().build();
        }
        
        // Save user message
        WarRoomMessage message = WarRoomMessage.builder()
            .meeting(meeting)
            .sender(sender)
            .content(request.getContent())
            .role(request.getRole())
            .createdAt(java.time.LocalDateTime.now())
            .build();
        WarRoomMessage saved = messageService.saveMessage(message);
        
        // Broadcast user message to all connected WebSocket clients
        chatSocketHandler.broadcastMessage(
            String.valueOf(saved.getMeeting().getId()),
            saved.getSender().getEmail(),
            saved.getSender().getName(),
            saved.getRole(),
            saved.getContent(),
            saved.getCreatedAt().toString()
        );
        
        // Generate AI response if this is a user message (not assistant)
        if ("user".equals(request.getRole()) || request.getRole() == null) {
            try {
                generateAndSendAIResponse(meeting, sender, request.getContent());
            } catch (Exception e) {
                logger.error("Failed to generate AI response: {}", e.getMessage(), e);
            }
        }
        
        return ResponseEntity.ok(toResponse(saved));
    }
    
    /**
     * Genera y envía una respuesta de IA basada en el contexto del incidente
     */
    private void generateAndSendAIResponse(Meeting meeting, User originalSender, String userMessage) {
        try {
            // Parse incident context from meeting
            IncidentContext context = parseIncidentContext(meeting);
            
            // Generate AI response
            String aiResponse = aiResponseService.generateResponse(userMessage, context);
            
            // Create AI system user (email: "ai-assistant@system")
            User aiUser = userRepository.findByEmail("ai-assistant@system")
                .orElseGet(() -> {
                    User newAiUser = new User();
                    newAiUser.setEmail("ai-assistant@system");
                    newAiUser.setName("Asistente IA");
                    return userRepository.save(newAiUser);
                });
            
            // Save AI message
            WarRoomMessage aiMessage = WarRoomMessage.builder()
                .meeting(meeting)
                .sender(aiUser)
                .content(aiResponse)
                .role("assistant")
                .createdAt(java.time.LocalDateTime.now())
                .build();
            WarRoomMessage savedAiMessage = messageService.saveMessage(aiMessage);
            
            // Broadcast AI response via WebSocket
            chatSocketHandler.broadcastMessage(
                String.valueOf(meeting.getId()),
                savedAiMessage.getSender().getEmail(),
                savedAiMessage.getSender().getName(),
                "assistant",
                savedAiMessage.getContent(),
                savedAiMessage.getCreatedAt().toString()
            );
            
            logger.info("AI response generated and sent for meeting {}", meeting.getId());
            
        } catch (Exception e) {
            logger.error("Error generating AI response: {}", e.getMessage(), e);
        }
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
                IncidentContext context = new IncidentContext(attackType, probability, severity);
                
                // Parse checklist if exists
                if (meeting.getChecklistJson() != null && !meeting.getChecklistJson().isEmpty()) {
                    // TODO: Parse checklist from JSON
                }
                
                return context;
            }
        } catch (Exception e) {
            logger.warn("Failed to parse incident context, using defaults: {}", e.getMessage());
        }
        
        // Default context if parsing fails
        return new IncidentContext(AttackType.UNKNOWN, 0.5, "medium");
    }

    private WarRoomMessageResponse toResponse(WarRoomMessage message) {
        WarRoomMessageResponse resp = new WarRoomMessageResponse();
        resp.setId(message.getId());
        resp.setMeetingId(message.getMeeting().getId());
        resp.setSenderEmail(message.getSender().getEmail());
        resp.setSenderName(message.getSender().getName());
        resp.setContent(message.getContent());
        resp.setRole(message.getRole());
        resp.setCreatedAt(message.getCreatedAt());
        return resp;
    }
}
