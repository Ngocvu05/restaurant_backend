package com.management.chat_service.service.base;

import java.util.List;

public interface IBaseChatService <T>{
    void handleMessage(T message);
    List<T> getMessages(String identifier);
    void processAIResponse(String identifier, String content);
}
