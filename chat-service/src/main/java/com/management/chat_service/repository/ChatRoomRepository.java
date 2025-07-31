package com.management.chat_service.repository;

import com.management.chat_service.dto.ChatRoomDTO;
import com.management.chat_service.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.type = 'PRIVATE' AND cr.id IN " +
            "(SELECT p.chatRoom.id FROM ChatParticipant p WHERE p.userId IN :userIds GROUP BY p.chatRoom.id HAVING COUNT(p.chatRoom.id) = 2) " +
            "AND (SELECT COUNT(p2.id) FROM ChatParticipant p2 WHERE p2.chatRoom.id = cr.id) = 2")
    Optional<ChatRoom> findPrivateRoomByParticipants(@Param("userIds") List<Long> userIds);
}
