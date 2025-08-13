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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class AIWorkerImpl implements IAIWorker {
    private final IChatAIService chatAIService;
    private final ChatRoomRepository chatRoomRepository;
    private final RabbitTemplate rabbitTemplate;
    private final Set<String> processingSessionIds = ConcurrentHashMap.newKeySet();
    @Autowired
    @Qualifier("aiWorkerExecutor")
    private TaskExecutor aiWorkerExecutor;

    @Async("aiWorkerExecutor")
    @Override
    public void processAIMessage(String roomId, String content) {
        if (canProcessSession(roomId)) return;

        try {
            ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId)
                    .orElseThrow(() -> new RuntimeException("Not found room ID: " + roomId));

            log.info("🤖 AIWorker - Send the message content to the Groq API for this room.: {}", roomId);
            String aiResponse = chatAIService.sendToAI(content);

            sendAIResponse(ChatMessageResponse.builder()
                    .response(aiResponse)
                    .sessionId(chatRoom.getSessionId())
                    .userId(chatRoom.getUserId())
                    .chatRoomId(chatRoom.getId())
                    .senderType(SenderType.AI)
                    .build());
        } catch (Exception e) {
            handleError("AIWorker - An error occurred while processing the AI message.", e);
        } finally {
            completeProcessing(roomId, "room");
        }
    }

    @Async("aiWorkerExecutor")
    @Override
    public void processGuestMessage(String sessionId, String content) {
        if (canProcessSession(sessionId)) return;

        try {
            log.info("🤖 [AIWorker] Process message from GUEST - sessionId: {} - content: {}", sessionId, content);
            String aiResponse = chatAIService.ask(content);

            sendAIResponse(ChatMessageResponse.builder()
                    .sessionId(sessionId)
                    .response(aiResponse)
                    .senderType(SenderType.AI)
                    .build());
        } catch (Exception e) {
            handleError("[AIWorker] An error occurred while processing the Guest message.", e);
        } finally {
            completeProcessing(sessionId, "guest");
        }
    }

    private boolean canProcessSession(String sessionId) {
        if (!processingSessionIds.add(sessionId)) {
            log.warn("⚠️ AIWorker - This session has already been processed.: {}", sessionId);
            return true;
        }
        return false;
    }

    private void sendAIResponse(ChatMessageResponse response) {
        log.info("✅ AIWorker - Send response AI to queue chat.response: {}", response);
        rabbitTemplate.convertAndSend(RabbitMQConfig.CHAT_EXCHANGE, RabbitMQConfig.RESPONSE_ROUTING_KEY, response);
    }

    private void handleError(String message, Exception e) {
        log.error("❌ {}", message, e);
    }

    private void completeProcessing(String id, String type) {
        processingSessionIds.remove(id);
        log.info("🤖 AIWorker - Message processing completed for {}: {}", type, id);
    }
}