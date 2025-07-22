package com.management.chat_service.model.base;

import com.management.chat_service.status.SenderType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class BaseMessage implements IMessage{
    private String content;
    private SenderType senderType;
    private LocalDateTime createdAt;

    //Protected methods for subclasses
    protected void validateContent() {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Message content cannot be empty");
        }
    }

    protected void setDefaultTimestamp() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    @Override
    public boolean isValid() {
        return content != null && !content.trim().isEmpty() && senderType != null;
    }
}
