package com.management.restaurant.event;

public interface ChatEventProducer {
    void sendSessionConversion(String sessionId, Long userId);
}