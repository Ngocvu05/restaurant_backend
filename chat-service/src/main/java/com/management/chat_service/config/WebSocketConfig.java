package com.management.chat_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // ✅ Enable simple broker for topics
        config.enableSimpleBroker("/topic", "/queue", "/user"); //user = for private messages

        // ✅ Set application destination prefix
        config.setApplicationDestinationPrefixes("/app");

        // ✅ Set user destination prefix
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // ✅ Register STOMP endpoint with SockJS support
        registry.addEndpoint("/ws")
                .addInterceptors(new AuthHandshakeInterceptor()) // Use custom handshake interceptor
                .setAllowedOriginPatterns(
                        "http://localhost:3000",
                        "http://127.0.0.1:3000",
                        "http://localhost:8080"
                )
                .withSockJS()
                .setHeartbeatTime(25000) // 25 seconds
                .setDisconnectDelay(5000) // 5 seconds
                .setSessionCookieNeeded(false);

        // ✅ Also register endpoint without SockJS for native WebSocket clients
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(
                        "http://localhost:3000",
                        "http://127.0.0.1:3000",
                        "http://localhost:8080"
                );
    }
}
