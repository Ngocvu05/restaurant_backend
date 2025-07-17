package com.management.restaurant.service;

public interface ChatEventProducer {
    void sendSessionConversion(String sessionId, Long userId);
}
