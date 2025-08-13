package com.management.search_service.events.implement;

import com.management.search_service.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserEvent extends BaseEvent {
    public enum Type {
        USER_CREATED,
        USER_UPDATED,
        USER_DELETED,
        USER_STATUS_CHANGED
    }

    private Long userId;
    private String username;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String address;
    private String roleName;
    private String status;
    private String avatarUrl;
    private LocalDateTime createdAt;
}