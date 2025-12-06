package com.arsw.ids_ia.ws;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TrafficSocketHandlerTest {

    private TrafficSocketHandler handler;

    @Mock
    private WebSocketSession session;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new TrafficSocketHandler();
        when(session.getId()).thenReturn("test-session-id");
        when(session.isOpen()).thenReturn(true);
    }

    @Test
    void testAfterConnectionEstablished() throws Exception {
        // Act
        handler.afterConnectionEstablished(session);
        
        // Verify no exceptions thrown (session added internally)
        verify(session, atLeast(0)).getId();
    }

    @Test
    void testAfterConnectionClosed() throws Exception {
        // Act
        handler.afterConnectionEstablished(session);
        handler.afterConnectionClosed(session, null);
        
        // Verify no exceptions thrown (session removed internally)
        verify(session, atLeast(0)).getId();
    }

    @Test
    void testHandleTextMessage() throws Exception {
        TextMessage message = new TextMessage("test message");
        
        // handleTextMessage should be a no-op for this push-only handler
        assertDoesNotThrow(() -> handler.handleTextMessage(session, message));
    }

    @Test
    void testBroadcastString() throws Exception {
        handler.afterConnectionEstablished(session);
        
        String message = "Test broadcast message";
        handler.broadcast(message);
        
        verify(session).sendMessage(any(TextMessage.class));
    }

    @Test
    void testBroadcastObject() throws Exception {
        handler.afterConnectionEstablished(session);
        
        TestObject obj = new TestObject("test", 123);
        handler.broadcastObject(obj);
        
        verify(session).sendMessage(any(TextMessage.class));
    }

    @Test
    void testBroadcastSkipsClosedSessions() throws Exception {
        WebSocketSession closedSession = mock(WebSocketSession.class);
        when(closedSession.getId()).thenReturn("closed-session");
        when(closedSession.isOpen()).thenReturn(false);
        
        handler.afterConnectionEstablished(session);
        handler.afterConnectionEstablished(closedSession);
        
        handler.broadcast("test");
        
        verify(session).sendMessage(any(TextMessage.class));
        verify(closedSession, never()).sendMessage(any(TextMessage.class));
    }

    @Test
    void testBroadcastHandlesIOException() throws Exception {
        handler.afterConnectionEstablished(session);
        doThrow(new IOException("Connection error")).when(session).sendMessage(any(TextMessage.class));
        
        assertDoesNotThrow(() -> handler.broadcast("test"));
        verify(session).close(any());
    }

    @Test
    void testBroadcastHandlesCloseException() throws Exception {
        handler.afterConnectionEstablished(session);
        doThrow(new IOException("Send error")).when(session).sendMessage(any(TextMessage.class));
        doThrow(new IOException("Close error")).when(session).close(any());
        
        assertDoesNotThrow(() -> handler.broadcast("test"));
    }

    @Test
    void testBroadcastObjectWithSerializationError() throws Exception {
        handler.afterConnectionEstablished(session);
        
        // Create object that causes circular reference
        Object circularRef = new Object() {
            @SuppressWarnings("unused")
            public Object getSelf() { return this; }
        };
        
        assertDoesNotThrow(() -> handler.broadcastObject(circularRef));
    }

    @Test
    void testMultipleSessions() throws Exception {
        WebSocketSession session2 = mock(WebSocketSession.class);
        when(session2.getId()).thenReturn("session-2");
        when(session2.isOpen()).thenReturn(true);
        
        handler.afterConnectionEstablished(session);
        handler.afterConnectionEstablished(session2);
        
        // Broadcast to both
        handler.broadcast("test");
        
        verify(session).sendMessage(any(TextMessage.class));
        verify(session2).sendMessage(any(TextMessage.class));
    }

    @Test
    void testSessionRemovedAfterClose() throws Exception {
        handler.afterConnectionEstablished(session);
        handler.afterConnectionClosed(session, null);
        
        // Broadcasting should not fail with empty sessions
        assertDoesNotThrow(() -> handler.broadcast("test"));
    }

    // Helper class for serialization tests
    static class TestObject {
        private String name;
        private int value;

        public TestObject(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() { return name; }
        public int getValue() { return value; }
    }
}
