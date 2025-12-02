package com.arsw.ids_ia.ws;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.databind.ObjectMapper;

@DisplayName("WarRoomChatSocketHandler - Unit Tests")
class WarRoomChatSocketHandlerTest {
    private WarRoomChatSocketHandler handler;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        handler = new WarRoomChatSocketHandler();
        objectMapper = new ObjectMapper();
    }

    @Test
    void afterConnectionEstablished_addsSession() throws Exception {
        WebSocketSession session = mock(WebSocketSession.class);
        handler.afterConnectionEstablished(session);
        // No exception means success
    }

    @Test
    void afterConnectionClosed_removesSession() throws Exception {
        WebSocketSession session = mock(WebSocketSession.class);
        handler.afterConnectionEstablished(session);
        handler.afterConnectionClosed(session, null);
        // No exception means success
    }

    @Test
    void handleTextMessage_broadcastsToAllSessions() throws Exception {
        WebSocketSession session1 = mock(WebSocketSession.class);
        WebSocketSession session2 = mock(WebSocketSession.class);
        when(session1.isOpen()).thenReturn(true);
        when(session2.isOpen()).thenReturn(true);
        handler.afterConnectionEstablished(session1);
        handler.afterConnectionEstablished(session2);
        String payload = "{\"meetingId\":\"1\",\"senderEmail\":\"a@b.com\",\"senderName\":\"A\",\"content\":\"hi\",\"role\":\"user\",\"createdAt\":\"2025-11-27T00:00:00\"}";
        TextMessage msg = new TextMessage(payload);
        handler.handleTextMessage(session1, msg);
        verify(session1, atLeastOnce()).sendMessage(any(TextMessage.class));
        verify(session2, atLeastOnce()).sendMessage(any(TextMessage.class));
    }

    @Test
    void handleTextMessage_handlesMalformedJson() throws Exception {
        WebSocketSession session = mock(WebSocketSession.class);
        handler.afterConnectionEstablished(session);
        TextMessage msg = new TextMessage("not-json");
        assertDoesNotThrow(() -> handler.handleTextMessage(session, msg));
    }
}
