package com.management.chat_service.service;

public interface IChatAIService {
    String sendToAI(String userInput);
    String ask(String prompt);
}
