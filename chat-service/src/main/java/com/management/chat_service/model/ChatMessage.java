package com.management.chat_service.model;

import com.management.chat_service.status.MessageStatus;
import com.management.chat_service.status.MessageType;
import com.management.chat_service.status.SenderType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "chat_messages",
        indexes = {
                @Index(name = "idx_chat_room_id", columnList = "chat_room_id"),
                @Index(name = "idx_message_id", columnList = "message_id"),
                @Index(name = "idx_sender_id", columnList = "sender_id"),
                @Index(name = "idx_created_at", columnList = "created_at"),
                @Index(name = "idx_messages_room_created", columnList = "chat_room_id, created_at")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String messageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @Column(nullable = false)
    private Long senderId;

    @Column(nullable = false)
    private String senderName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MessageType type = MessageType.TEXT;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "parent_message_id")
    private String parentMessageId;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageStatus status = MessageStatus.SENT;

    @Builder.Default
    private Boolean isAiGenerated = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SenderType senderType = SenderType.USER;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @PrePersist
    private void prePersist() {
        if (messageId == null) {
            messageId = UUID.randomUUID().toString();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }
    @PreUpdate
    private void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}