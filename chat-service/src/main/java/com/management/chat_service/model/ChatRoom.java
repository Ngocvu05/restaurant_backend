package com.management.chat_service.model;

import com.management.chat_service.status.ChatRoomStatus;
import com.management.chat_service.status.ChatRoomType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chat_rooms",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "session_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String roomId;

    @Column(nullable = false)
    private String name;
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatRoomType type;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private ChatRoomStatus status = ChatRoomStatus.ACTIVE;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("createdAt ASC")
    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "admin_id")
    private Long adminId;

    @Column(name = "resolved")
    @Builder.Default
    private Boolean resolved = false;

    @OneToMany(
            mappedBy = "chatRoom",                // 'chatRoom' is a field name entity ChatParticipant
            cascade = CascadeType.ALL,            // Participant records related to the ChatRoom will be handled automatically on save or delete.
            fetch = FetchType.LAZY,               // Fetch this list only when necessary for better performance.
            orphanRemoval = true                  // Automatically delete orphan participants (no longer belonging to any room).
    )
    @Builder.Default
    private List<ChatParticipant> participants = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    private void prePersist() {
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