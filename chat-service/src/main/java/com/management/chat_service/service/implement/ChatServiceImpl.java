package com.management.chat_service.service.implement;

import com.management.chat_service.dto.ChatMessageDTO;
import com.management.chat_service.dto.ChatMessageResponse;
import com.management.chat_service.mapper.IChatMessageMapper;
import com.management.chat_service.model.ChatMessage;
import com.management.chat_service.model.ChatRoom;
import com.management.chat_service.repository.ChatRoomRepository;
import com.management.chat_service.service.IChatService;
import com.management.chat_service.service.IChatWebSocketService;
import com.management.chat_service.service.handler.IChatMessageHandler;
import com.management.chat_service.status.SenderType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChatServiceImpl implements IChatService {
    private final ChatRoomRepository chatRoomRepository;
    private final List<IChatMessageHandler> handlers;
    private final IChatMessageMapper chatMessageMapper;
    private final IChatWebSocketService chatWebSocketService;

    @Override
    public ChatMessageDTO processMessage(String roomId, Long senderId, String senderName, String content, SenderType senderType) {
        ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));

        for (IChatMessageHandler handler : handlers) {
            if (handler.supports(senderType)) {
                ChatMessage saved = handler.handleMessage(chatRoom, senderName, senderId, content);
                ChatMessageDTO dto = chatMessageMapper.toDTO(saved);

                ChatMessageResponse response = ChatMessageResponse.builder()
                        .sessionId(roomId)
                        .response(content)
                        .userId(senderId)
                        .senderType(senderType)
                        .build();
                chatWebSocketService.sendMessageToRoom(roomId, response);
                return dto;
            }
        }
        throw new UnsupportedOperationException("No handler for senderType: " + senderType);
    }
}
