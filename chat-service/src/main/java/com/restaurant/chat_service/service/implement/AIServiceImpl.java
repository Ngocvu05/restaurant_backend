package com.restaurant.chat_service.service.implement;

import com.restaurant.chat_service.service.IAIService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class AIServiceImpl implements IAIService {

    @Override
    public void generateResponse(String roomId, String userMessage) {

    }

    @Override
    public Mono<String> callHuggingFaceAPI(String prompt) {
        return null;
    }

    @Override
    public Map<String, Object> buildRequestBody(String prompt) {
        return Map.of();
    }

    @Override
    public String parseHuggingFaceResponse(String response) {
        return "";
    }

    @Override
    public String buildContextualPrompt(Long chatRoomId, String userMessage) {
        return "";
    }

    @Override
    public String cleanAIResponse(String response) {
        return "";
    }

    @Override
    public void testModel(String testPrompt) {

    }
}
