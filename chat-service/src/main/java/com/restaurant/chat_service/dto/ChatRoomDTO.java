package com.restaurant.chat_service.dto;

import com.restaurant.chat_service.status.ChatRoomStatus;
import com.restaurant.chat_service.status.ChatRoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomDTO {
    private Long id;
    private String roomId;
    private String roomName;
    private Long userId;
    private String description;
    private ChatRoomType roomType;
    private ChatRoomStatus isActive;
    private Long unreadCount;
    private ChatMessageDTO lastMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
