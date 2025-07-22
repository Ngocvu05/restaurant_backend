package com.management.chat_service.model.base;

import com.management.chat_service.status.SenderType;

import java.time.LocalDateTime;

public interface IMessage {
    String getContent();
    void setContent(String content);
    SenderType getSenderType();
    void setSenderType(SenderType senderType);
    LocalDateTime getCreatedAt();
    void setCreatedAt(LocalDateTime createdAt);
    boolean isValid();
}
