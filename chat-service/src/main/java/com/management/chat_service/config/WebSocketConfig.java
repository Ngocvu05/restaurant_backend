package com.management.chat_service.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Enhanced WebSocket Configuration with RabbitMQ STOMP Relay
 * <p>
 * Features:    </br>
 * - STOMP Relay with RabbitMQ (production-ready) </br>
 * - Heartbeat for connection stability   </br>
 * - Virtual host configuration   </br>
 * - Configuration validation </br>
 * - SockJS fallback support  </br>
 * - Custom auth interceptor
 * <p>
 * FIXED ISSUES:    </br>
 * - Removed conflicting SimpleBroker </br>
 * - Using STOMP Relay only   </br>
 * - Added heartbeat settings </br>
 * - Added virtual host   </br>
 * - Using @Value for allowed origins
 */
@Slf4j
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Value("${websocket.relay.host}")
    private String relayHost;

    @Value("${websocket.relay.port}")
    private int relayPort;

    @Value("${websocket.client.login}")
    private String clientLogin;

    @Value("${websocket.client.passcode}")
    private String clientPasscode;

    @Value("${websocket.allowed-origins}")
    private String allowedOriginsStr;

    private String[] allowedOrigins;

    @PostConstruct
    public void init() {
        // Parse allowed origins
        allowedOrigins = allowedOriginsStr.split(",");

        // Log configuration
        log.info("============================================");
        log.info("📡 WebSocket Configuration");
        log.info("============================================");
        log.info("Broker Type: STOMP Relay (RabbitMQ)");
        log.info("RabbitMQ Host: {}", relayHost);
        log.info("RabbitMQ Port: {}", relayPort);
        log.info("RabbitMQ User: {}", clientLogin);
        log.info("Allowed Origins: {}", allowedOriginsStr);

        // Validate configuration
        validateConfiguration();

        log.info("============================================");
    }

    /**
     * Configure message broker </br>
     * Using STOMP Relay with RabbitMQ (NOT SimpleBroker)
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        log.info("🔧 Configuring STOMP Broker Relay...");

        // Use STOMP Relay ONLY (removed SimpleBroker to avoid conflict)
        config.enableStompBrokerRelay("/topic", "/queue", "/user")
                .setRelayHost(relayHost)
                .setRelayPort(relayPort)
                .setClientLogin(clientLogin)
                .setClientPasscode(clientPasscode)
                .setSystemLogin(clientLogin)
                .setSystemPasscode(clientPasscode)
                // Virtual host configuration
                .setVirtualHost("/")
                // Heartbeat settings for connection stability
                .setSystemHeartbeatSendInterval(20000)      // Send heartbeat every 20s
                .setSystemHeartbeatReceiveInterval(20000);  // Expect heartbeat every 20s

        // Application destination prefix (for @MessageMapping endpoints)
        config.setApplicationDestinationPrefixes("/app");

        // User destination prefix (for private/direct messages)
        config.setUserDestinationPrefix("/user");

        log.info(" STOMP Broker Relay configured successfully");
    }

    /**
     * Register STOMP endpoints </br>
     * - /ws with SockJS fallback   </br>
     * - /ws without SockJS for native clients
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        log.info("🔧 Registering STOMP endpoints...");

        // STOMP endpoint WITH SockJS fallback (for browsers that don't support WebSocket)
        registry.addEndpoint("/ws")
                .addInterceptors(new AuthHandshakeInterceptor())
                .setAllowedOriginPatterns(allowedOrigins)
                .withSockJS()
                .setHeartbeatTime(25000)          // SockJS heartbeat every 25s
                .setDisconnectDelay(5000)         // Wait 5s before disconnect
                .setSessionCookieNeeded(false);   // Don't require session cookie

        // STOMP endpoint WITHOUT SockJS (for native WebSocket clients)
        registry.addEndpoint("/ws")
                .addInterceptors(new AuthHandshakeInterceptor())
                .setAllowedOriginPatterns(allowedOrigins);

        log.info(" STOMP endpoints registered: /ws");
    }

    /**
     * Validate WebSocket configuration
     */
    private void validateConfiguration() {
        // Check if credentials are set
        if (clientLogin == null || clientLogin.isEmpty()) {
            log.error(" WEBSOCKET_CLIENT_LOGIN is not configured!");
            throw new IllegalStateException("WebSocket client login must be configured");
        }

        if (clientPasscode == null || clientPasscode.isEmpty()) {
            log.error(" WEBSOCKET_CLIENT_PASSCODE is not configured!");
            throw new IllegalStateException("WebSocket client passcode must be configured");
        }

        // Warn if using default 'guest' credentials
        if ("guest".equals(clientLogin)) {
            log.warn("⚠️ WARNING: Using default 'guest' credentials for WebSocket!");
            log.warn("⚠️ For production, please use proper credentials (chat_user/chat_pass)");
        }

        // Check if relay host is configured
        if (relayHost == null || relayHost.isEmpty()) {
            log.error(" WEBSOCKET_RELAY_HOST is not configured!");
            throw new IllegalStateException("WebSocket relay host must be configured");
        }

        // Validate port
        if (relayPort <= 0 || relayPort > 65535) {
            log.error(" Invalid WEBSOCKET_RELAY_PORT: {}", relayPort);
            throw new IllegalStateException("WebSocket relay port must be between 1 and 65535");
        }

        log.info("✅ Configuration validation passed");
    }
}