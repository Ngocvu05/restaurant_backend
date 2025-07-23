package com.management.chat_service.service.handler;

import com.management.chat_service.model.ChatMessage;
import com.management.chat_service.model.ChatRoom;
import com.management.chat_service.status.MessageStatus;
import com.management.chat_service.status.MessageType;
import com.management.chat_service.status.SenderType;

import java.time.LocalDateTime;

public class GuestMessageHandler implements IChatMessageHandler{
    @Override
    public boolean supports(SenderType senderType) {
        return senderType == SenderType.GUEST;
    }

    @Override
    public ChatMessage handleMessage(ChatRoom chatRoom, String senderName, Long senderId, String content) {
        // process message (storage db, send to redis,...)
        ChatMessage message = ChatMessage.builder()
                .chatRoom(chatRoom)
                .senderId(senderId)
                .senderName(senderName)
                .content(content)
                .senderType(SenderType.USER)
                .type(MessageType.TEXT)
                .status(MessageStatus.SENT)
                .isAiGenerated(false)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        return  message;
    }
}
