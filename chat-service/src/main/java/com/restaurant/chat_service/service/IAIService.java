package com.restaurant.chat_service.service;

import reactor.core.publisher.Mono;

import java.util.Map;

public interface IAIService {
    void generateResponse(String roomId, String userMessage);
    Mono<String> callHuggingFaceAPI(String prompt);
    Map<String, Object> buildRequestBody(String prompt);
    String parseHuggingFaceResponse(String response);
    String buildContextualPrompt(Long chatRoomId, String userMessage);
    String cleanAIResponse(String response);
    void testModel(String testPrompt);
}
