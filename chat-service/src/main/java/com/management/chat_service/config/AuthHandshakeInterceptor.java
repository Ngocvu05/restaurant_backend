package com.management.chat_service.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
public class AuthHandshakeInterceptor implements HandshakeInterceptor {
    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse  response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {

        if (request instanceof ServletServerHttpRequest servletRequestWrapper) {
            HttpServletRequest servletRequest = servletRequestWrapper.getServletRequest();

            String sessionId = servletRequest.getParameter("sessionId");
            String userId = servletRequest.getHeader("X-User-Id");
            String role = servletRequest.getHeader("X-User-Role");

            if (sessionId != null) {
                attributes.put("sessionId", sessionId);
            }

            if (userId != null) {
                attributes.put("userId", userId);
            }

            if (role != null) {
                attributes.put("role", role);
            }

            log.info("🌐 WebSocket - AuthHandshakeInterceptor - sessionId={}, userId={}, role={}", sessionId, userId, role);
        }

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        // do nothing after handshake
    }
}
