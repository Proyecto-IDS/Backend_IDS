package com.arsw.ids_ia.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.arsw.ids_ia.ws.TrafficSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final TrafficSocketHandler trafficSocketHandler;

    public WebSocketConfig(TrafficSocketHandler trafficSocketHandler) {
        this.trafficSocketHandler = trafficSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        String frontendUrl = System.getenv("FRONTEND_URL");
        
        if (frontendUrl != null && !frontendUrl.isEmpty()) {
            // Production: Use the specified frontend URL
            registry.addHandler(trafficSocketHandler, "/traffic/stream")
                   .setAllowedOrigins(frontendUrl);
        } else {
            // Development: Allow all origins for easier testing
            registry.addHandler(trafficSocketHandler, "/traffic/stream")
                   .setAllowedOriginPatterns("*");
        }
    }
}
