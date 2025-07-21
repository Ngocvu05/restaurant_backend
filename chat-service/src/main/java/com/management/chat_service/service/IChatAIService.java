package com.management.chat_service.service;

public interface IChatAIService {
    String sendToAI(String userInput);
    void ask(String prompt);
}
