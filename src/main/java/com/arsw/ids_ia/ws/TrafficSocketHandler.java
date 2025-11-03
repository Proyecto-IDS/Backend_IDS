package com.arsw.ids_ia.ws;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Component
public class TrafficSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(TrafficSocketHandler.class);

    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    // Configure ObjectMapper to handle Java 8+ date/time types (Instant)
    private final ObjectMapper mapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        logger.info("WebSocket connected: {} (open sessions={})", session.getId(), sessions.size());
        // push-only handler: do not read or send historic alerts here
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // push-only; ignore client messages
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        logger.info("WebSocket closed: {} (open sessions={})", session.getId(), sessions.size());
    }

    public void broadcastObject(Object obj) {
        try {
            String payload = mapper.writeValueAsString(obj);
            broadcast(payload);
        } catch (Exception e) {
            logger.warn("Error serializing WS payload: {}", e.getMessage());
        }
    }

    public void broadcast(String payload) {
        TextMessage message = new TextMessage(payload);
        sessions.removeIf(session -> !session.isOpen());
        for (WebSocketSession session : sessions) {
            try {
                session.sendMessage(message);
            } catch (IOException e) {
                logger.warn("Error sending WS message to {}: {}", session.getId(), e.getMessage());
                try {
                    session.close(CloseStatus.SERVER_ERROR);
                } catch (IOException ignored) {
                }
            }
        }
    }
}
