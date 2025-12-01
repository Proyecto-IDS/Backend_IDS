package com.arsw.ids_ia.ws;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
public class WarRoomChatSocketHandler extends TextWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(WarRoomChatSocketHandler.class);
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        sessions.add(session);
        logger.info("Chat WebSocket connected: {} (open sessions={})", session.getId(), sessions.size());
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) {
        try {
            JsonNode node = objectMapper.readTree(message.getPayload());
            String senderEmail = node.has("senderEmail") ? node.get("senderEmail").asText() : null;
            String senderName = node.has("senderName") ? node.get("senderName").asText() : null;
            String content = node.has("content") ? node.get("content").asText() : null;
            String role = node.has("role") ? node.get("role").asText() : null;
            String meetingId = node.has("meetingId") ? node.get("meetingId").asText() : null;
            String createdAt = node.has("createdAt") ? node.get("createdAt").asText() : null;

            logger.info("[Chat WS] Recibido mensaje: meetingId={}, senderEmail={}, senderName={}, role={}, content={}", meetingId, senderEmail, senderName, role, content);

            if (senderName == null && senderEmail != null) {
                int atIdx = senderEmail.indexOf('@');
                if (atIdx > 0) senderName = senderEmail.substring(0, atIdx);
            }

            ObjectNode outgoing = objectMapper.createObjectNode();
            outgoing.put("meetingId", meetingId);
            outgoing.put("senderEmail", senderEmail);
            outgoing.put("senderName", senderName);
            outgoing.put("role", role);
            outgoing.put("content", content);
            outgoing.put("createdAt", createdAt);

            String outgoingStr = objectMapper.writeValueAsString(outgoing);

            for (WebSocketSession s : sessions) {
                if (s.isOpen()) {
                    try {
                        s.sendMessage(new TextMessage(outgoingStr));
                    } catch (IOException e) {
                        logger.warn("Error sending chat message to {}: {}", s.getId(), e.getMessage());
                    }
                }
            }
            logger.info("[Chat WS] Broadcasted mensaje: meetingId={}, senderEmail={}, senderName={}, role={}, content={}", meetingId, senderEmail, senderName, role, content);
        } catch (IOException ex) {
            logger.error("[Chat WS] Error de IO procesando mensaje: {}", ex.getMessage(), ex);
        } catch (RuntimeException ex) {
            logger.error("[Chat WS] Error procesando mensaje: {}", ex.getMessage(), ex);
        }
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        sessions.remove(session);
        logger.info("Chat WebSocket closed: {} (open sessions={})", session.getId(), sessions.size());
    }
}
