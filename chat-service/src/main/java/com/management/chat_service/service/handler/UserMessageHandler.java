package com.management.chat_service.service.handler;

import com.management.chat_service.model.ChatMessage;
import com.management.chat_service.model.ChatRoom;
import com.management.chat_service.repository.ChatMessageRepository;
import com.management.chat_service.status.MessageStatus;
import com.management.chat_service.status.MessageType;
import com.management.chat_service.status.SenderType;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserMessageHandler implements IChatMessageHandler {
    private final ChatMessageRepository chatMessageRepository;

    public UserMessageHandler(ChatMessageRepository repo) {
        this.chatMessageRepository = repo;
    }

    @Override
    public boolean supports(SenderType senderType) {
        return senderType == SenderType.USER;
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
        // Save message to the database
        return chatMessageRepository.save(message);
    }
}
