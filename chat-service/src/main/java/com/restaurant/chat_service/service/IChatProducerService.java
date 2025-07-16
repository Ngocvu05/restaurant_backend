package com.restaurant.chat_service.service;

public interface IChatProducerService {
    void sendToAI(String roomId, String content);
}
