package com.management.chat_service.service.implement;

import com.management.chat_service.dto.ChatMessageRequest;
import com.management.chat_service.dto.ChatRoomDTO;
import com.management.chat_service.dto.UserDTO;
import com.management.chat_service.mapper.IChatMessageMapper;
import com.management.chat_service.mapper.IChatRoomMapper;
import com.management.chat_service.model.ChatMessage;
import com.management.chat_service.model.ChatRoom;
import com.management.chat_service.repository.ChatMessageRepository;
import com.management.chat_service.repository.ChatRoomRepository;
import com.management.chat_service.service.IChatRoomService;
import com.management.chat_service.status.ChatRoomStatus;
import com.management.chat_service.status.ChatRoomType;
import com.management.chat_service.status.SenderType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomServiceImpl implements IChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final IChatRoomMapper chatRoomMapper;
    private final IChatMessageMapper chatMessageMapper;
    private final ChatMessageRepository chatMessageRepository;
    private final RestTemplate restTemplate;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Override
    @Transactional
    public ChatRoom getOrCreateRoom(ChatMessageRequest request) {
        log.info(">>> ChatRoomService - getOrCreateRoom - Request: {}", request);
        String sessionId = request.getSessionId();
        Long userId = request.getUserId();
        SenderType senderType = request.getSenderType();

        try{
            if ("ADMIN".equals(senderType.name()) || "USER".equals(senderType.name())) {
                Optional<ChatRoom> existingRoom = chatRoomRepository.findByRoomId(request.getChatRoomId());
                if (existingRoom.isPresent()){
                    ChatRoom room = existingRoom.get();
                    log.info("🛑 ChatRoomService - ADMIN message, Existing room: {}", room.getSessionId());
                    if( room.getAdminId() == null && "ADMIN".equals(senderType.name())) {
                        room.setAdminId(userId);
                        room.setUpdatedAt(LocalDateTime.now());
                        log.info("🛑 ChatRoomService - ADMIN message, Storage on DB: {}", request);
                        return chatRoomRepository.save(room);
                    }
                    return room;
                }
            }

            return chatRoomRepository.findByUserIdAndSessionId(userId, sessionId)
                    .orElseGet(() -> {
                        ChatRoom newRoom = createRoomFromRequest(request);
                        return chatRoomRepository.save(newRoom);
                    });
        } catch (Exception e) {
            log.warn("Room already exists, fetching existing room: {}", sessionId);
            return chatRoomRepository.findByUserIdAndSessionId(userId, sessionId)
                    .orElseThrow(() -> new RuntimeException("Cannot find or create chat room"));
        }

    }

    private ChatRoom createRoomFromRequest (ChatMessageRequest request) {
        log.info(">>> ChatRoomService - Room doesn't exist, create new room - Request: {}", request);
        return ChatRoom.builder()
                .roomId(request.getChatRoomId())
                .name("Chat with AI" + LocalDateTime.now())
                .type(ChatRoomType.AI_SUPPORT)
                .description("AI assistant chat " + LocalDateTime.now())
                .status(ChatRoomStatus.ACTIVE)
                .sessionId(request.getSessionId())
                .userId(request.getUserId())
                .build();
    }

    @Override
    @Transactional
    public void convertSessionToUser(String sessionId, Long userId) {
        List<ChatRoom> rooms = chatRoomRepository.findBySessionIdAndUserIdIsNull(sessionId);
        for (ChatRoom room : rooms) {
            room.setUserId(userId);
            chatRoomRepository.save(room);
        }
        log.info("✅ convertSessionToUser - Assigned {} room(s) from session {} to userId {}", rooms.size(), sessionId, userId);
    }

    @Override
    public List<ChatRoom> getRooms(Long userId) {
        return chatRoomRepository.findByUserId(userId);
    }

    @Override
    public List<ChatRoomDTO> getAllRooms(Long userId) {
        if (userId == null) {
            return List.of();
        }

        List<ChatRoom> rooms = chatRoomRepository.findAllByUserId(userId);
        if (rooms == null || rooms.isEmpty()) {
            return List.of();
        }

        return rooms.stream()
                .map(room -> {
                    ChatMessage lastMessage = chatMessageRepository
                            .findTopByChatRoomIdOrderByCreatedAtDesc(room.getId())
                            .orElse(null);
                    if (lastMessage != null) {
                        room.setMessages(List.of(lastMessage));
                    } else {
                        room.setMessages(List.of());
                    }
                    return chatRoomMapper.toDTO(room);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChatRoomDTO> getAllRoomsForAdmin() {
        List<ChatRoom> rooms = chatRoomRepository.findAll();
        if (rooms.isEmpty()) {
            return Collections.emptyList();
        }

        // Collect a set of unique user IDs
        Set<Long> userIds = rooms.stream()
                .map(ChatRoom::getUserId)
                .collect(Collectors.toSet());
        log.info(">>> getAllRoomsForAdmin - List user IDs : {}", userIds);
        Map<Long, String> userIdToUsernameMap = Collections.emptyMap();

        HttpHeaders headers = new HttpHeaders();
        String jwt = httpServletRequest.getHeader("Authorization");
        if (jwt != null) {
            headers.set("Authorization", jwt);
        }
        log.info(">>> getAllRoomsForAdmin - JWT Token: {}", jwt);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        if (!userIds.isEmpty()) {
            try {
                // Replace with the actual URL or a configuration variable
                // e.g., using Service Discovery: "http://user-service/api/v1/users/batch?ids={ids}"
                String userServiceUrl = "http://user-service:8081/api/v1/users/batch?ids="
                        + userIds.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(","));

                ResponseEntity<List<UserDTO>> response = restTemplate.exchange(
                        userServiceUrl,
                        HttpMethod.GET,
                        entity,
                        new ParameterizedTypeReference<List<UserDTO>>() {}
                );

                List<UserDTO> users = response.getBody();
                if (users != null && !users.isEmpty()) {
                    userIdToUsernameMap = users.stream()
                            .collect(Collectors.toMap(UserDTO::getId, UserDTO::getUsername));
                }
            } catch (Exception e) {
                log.error(">>> Error calling User-Service: {}", e.getMessage());
                // Can proceed without usernames or throw an exception, depending on requirements
            }
        }

        // Final variable to be used inside a lambda expression
        final Map<Long, String> finalUserIdToUsernameMap = userIdToUsernameMap;

        log.info(">>> getAllRoomsForAdmin - Fetching all chat rooms for admin {}", finalUserIdToUsernameMap);
        return rooms.stream()
                .map(room -> {
                    // 1. Convert the ChatRoom entity to a basic DTO
                    ChatRoomDTO dto = chatRoomMapper.toDTO(room);
                    log.info(">>> getAllRoomsForAdmin - Converting ChatRoom to DTO: {}", dto);

                    // 2. Attach the username to the DTO
                    String username = finalUserIdToUsernameMap.getOrDefault(room.getUserId(), "Anonymous User");
                    dto.setUserName(username);

                    // 3. Fetch and attach the last message to the DTO
                    // This approach is more efficient than modifying the `room` entity
                    chatMessageRepository.findTopByChatRoomIdOrderByCreatedAtDesc(room.getId())
                            .ifPresent(lastMessage -> {
                                // Assuming ChatRoomDTO has a lastMessage field of type ChatMessageDTO
                                // and you have a chatMessageMapper
                                dto.setLastMessage(chatMessageMapper.toDTO(lastMessage));
                            });
                    log.info(">>> getAllRoomsForAdmin - Final ChatRoomDTO: {}", dto);
                    return dto;
                })
                .filter(obj -> true)
                .collect(Collectors.toList());
    }
}