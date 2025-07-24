package com.management.chat_service.repository;

import com.management.chat_service.dto.ChatRoomDTO;
import com.management.chat_service.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByRoomId(String roomId);

    List<ChatRoom> findByUserId(Long userId);

    List<ChatRoom> findAllByUserId(Long userId);

    Optional<ChatRoom> findBySessionId(String sessionId);

    Optional<ChatRoom> findByUserIdAndSessionId(Long userId, String sessionId);

    List<ChatRoom> findBySessionIdAndUserIdIsNull(String sessionId);
}
