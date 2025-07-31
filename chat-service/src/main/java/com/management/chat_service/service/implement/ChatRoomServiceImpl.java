package com.management.chat_service.service.implement;

import com.management.chat_service.dto.ChatMessageRequest;
import com.management.chat_service.dto.ChatRoomDTO;
import com.management.chat_service.dto.UserDTO;
import com.management.chat_service.mapper.IChatMessageMapper;
import com.management.chat_service.mapper.IChatRoomMapper;
import com.management.chat_service.model.ChatMessage;
import com.management.chat_service.model.ChatParticipant;
import com.management.chat_service.model.ChatRoom;
import com.management.chat_service.repository.ChatMessageRepository;
import com.management.chat_service.repository.ChatRoomRepository;
import com.management.chat_service.service.IChatRoomService;
import com.management.chat_service.status.ChatRoomStatus;
import com.management.chat_service.status.ChatRoomType;
import com.management.chat_service.status.ParticipantRole;
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
        if (rooms.isEmpty()) return Collections.emptyList();

        Set<Long> userIds = rooms.stream()
                .map(ChatRoom::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, UserDTO> userInfoMap = fetchUsersInfo(userIds);

        return rooms.stream().map(room -> {
            ChatRoomDTO dto = chatRoomMapper.toDTO(room);

            UserDTO user = userInfoMap.get(room.getUserId());
            if (user != null) {
                dto.setUserName(user.getUsername());
                dto.setEmail(user.getEmail());
                dto.setAvatarUrl(user.getAvatarUrl());
            } else {
                dto.setUserName("Anonymous");
            }

            chatMessageRepository.findTopByChatRoomIdOrderByCreatedAtDesc(room.getId())
                    .ifPresent(m -> dto.setLastMessage(chatMessageMapper.toDTO(m)));

            return dto;
        }).collect(Collectors.toList());
    }


    @Override
    @Transactional
    public ChatRoom getOrCreatePrivateRoom(Long userId1, Long userId2) {
        if (userId1.equals(userId2)) {
            throw new IllegalArgumentException("Cannot create a chat room with yourself.");
        }

        List<Long> userIds = Arrays.asList(userId1, userId2);
        return chatRoomRepository.findPrivateRoomByParticipants(userIds)
                .orElseGet(() -> {
                    Map<Long, UserDTO> userMap = fetchUsersInfo(new HashSet<>(userIds));

                    String name1 = Optional.ofNullable(userMap.get(userId1)).map(UserDTO::getUsername).orElse("User " + userId1);
                    String name2 = Optional.ofNullable(userMap.get(userId2)).map(UserDTO::getUsername).orElse("User " + userId2);

                    String roomName = String.format("Private Chat: %s & %s", name1, name2);

                    ChatRoom newRoom = ChatRoom.builder()
                            .roomId(UUID.randomUUID().toString())
                            .name(roomName)
                            .type(ChatRoomType.PRIVATE)
                            .status(ChatRoomStatus.ACTIVE)
                            .build();

                    ChatParticipant p1 = ChatParticipant.builder()
                            .chatRoom(newRoom)
                            .userId(userId1)
                            .userName(name1)
                            .role(ParticipantRole.MEMBER)
                            .build();

                    ChatParticipant p2 = ChatParticipant.builder()
                            .chatRoom(newRoom)
                            .userId(userId2)
                            .userName(name2)
                            .role(ParticipantRole.MEMBER)
                            .build();

                    newRoom.setParticipants(Arrays.asList(p1, p2));
                    return chatRoomRepository.save(newRoom);
                });
    }

    private Map<Long, UserDTO> fetchUsersInfo(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return Collections.emptyMap();

        HttpHeaders headers = new HttpHeaders();
        String jwt = httpServletRequest.getHeader("Authorization");
        if (jwt != null) headers.set("Authorization", jwt);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        String url = "http://user-service:8081/api/v1/users/batch?ids=" +
                userIds.stream().map(String::valueOf).collect(Collectors.joining(","));

        try {
            ResponseEntity<List<UserDTO>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<List<UserDTO>>() {}
            );

            List<UserDTO> users = response.getBody();
            if (users != null && !users.isEmpty()) {
                return users.stream().collect(Collectors.toMap(UserDTO::getId, u -> u));
            }
        } catch (Exception e) {
            log.error("❌ Failed to call user-service: {}", e.getMessage());
        }
        return Collections.emptyMap();
    }
}