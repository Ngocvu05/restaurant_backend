package com.management.chat_service.service;

public interface IAIWorker {
    void processAIMessage(String roomId, String content);
}
