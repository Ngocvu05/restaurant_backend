package com.management.chat_service.service.implement;

import com.management.chat_service.config.RabbitMQConfig;
import com.management.chat_service.dto.ChatMessageResponse;
import com.management.chat_service.model.ChatRoom;
import com.management.chat_service.repository.ChatRoomRepository;
import com.management.chat_service.service.IAIWorker;
import com.management.chat_service.service.IChatAIService;
import com.management.chat_service.status.SenderType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AIWorkerImpl implements IAIWorker {
    private final IChatAIService chatAIService;
    private final ChatRoomRepository chatRoomRepository;
    private final RabbitTemplate rabbitTemplate;

    @Async
    @Override
    public void processAIMessage(String roomId, String content) {
        try {
            ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy roomId: " + roomId));

            log.info("🤖 AIWorker - Gửi nội dung tới Groq API cho room: {}", roomId);
            String aiResponse = chatAIService.sendToAI(content);

            ChatMessageResponse response = ChatMessageResponse.builder()
                    .response(aiResponse)
                    .sessionId(chatRoom.getSessionId())
                    .userId(chatRoom.getUserId())
                    .chatRoomId(chatRoom.getId())
                    .senderType(SenderType.AI)
                    .build();

            log.info("✅ AIWorker - Gửi response AI về queue chat.response: {}", response);
            rabbitTemplate.convertAndSend(RabbitMQConfig.CHAT_EXCHANGE, RabbitMQConfig.CHAT_RESPONSE_ROUTING_KEY, response);
        } catch (Exception e) {
            log.error("❌ AIWorker - Lỗi khi xử lý AI message", e);
        }
    }
}
