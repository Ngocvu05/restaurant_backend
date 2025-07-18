package com.management.chat_service.repository;

import com.management.chat_service.model.ChatMessage;
import com.management.chat_service.model.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatRoomOrderByCreatedAtAsc(ChatRoom chatRoom);

    Page<ChatMessage> findByChatRoom_RoomId(String chatRoomRoomId, Pageable pageable);
}
