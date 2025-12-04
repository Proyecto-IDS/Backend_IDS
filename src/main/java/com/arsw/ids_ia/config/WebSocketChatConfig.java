package com.arsw.ids_ia.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.arsw.ids_ia.ws.WarRoomChatSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketChatConfig implements WebSocketConfigurer {
    @Autowired
    private WarRoomChatSocketHandler chatSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        String frontendUrl = System.getenv("FRONTEND_URL");
        
        if (frontendUrl != null && !frontendUrl.isEmpty()) {
            // Production: Use the specified frontend URL
            registry.addHandler(chatSocketHandler, "/ws/warroom/chat")
                   .setAllowedOrigins(frontendUrl);
        } else {
            // Development: Allow all origins for easier testing
            registry.addHandler(chatSocketHandler, "/ws/warroom/chat")
                   .setAllowedOriginPatterns("*");
        }
    }
}
