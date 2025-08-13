package com.management.chat_service.service.handler;

import com.management.chat_service.dto.ChatMessageDTO;
import com.management.chat_service.mapper.IChatMessageMapper;
import com.management.chat_service.model.ChatMessage;
import com.management.chat_service.model.ChatRoom;
import com.management.chat_service.repository.ChatMessageRepository;
import com.management.chat_service.status.MessageStatus;
import com.management.chat_service.status.MessageType;
import com.management.chat_service.status.SenderType;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminMessageHandler implements IChatMessageHandler{
    private final ChatMessageRepository chatMessageRepository;
    private final IChatMessageMapper chatMessageMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public boolean supports(SenderType senderType) {
        return senderType == SenderType.ADMIN;
    }

    @Override
    public ChatMessage handleMessage(ChatRoom chatRoom, String senderName, Long senderId, String content) {
        ChatMessage message = ChatMessage.builder()
                .chatRoom(chatRoom)
                .senderId(senderId)
                .senderName(senderName)
                .content(content)
                .senderType(SenderType.ADMIN)
                .type(MessageType.TEXT)
                .status(MessageStatus.SENT)
                .isRead(false)
                .isAiGenerated(false)
                .createdAt(LocalDateTime.now())
                .build();

        ChatMessage saved = chatMessageRepository.save(message);

        // Send a message to Websocket for user
        ChatMessageDTO dto = chatMessageMapper.toDTO(saved);
        messagingTemplate.convertAndSend("/topic/room/" + chatRoom.getRoomId(), dto);

        return saved;
    }
}