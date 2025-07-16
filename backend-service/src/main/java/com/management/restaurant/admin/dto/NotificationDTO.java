package com.management.restaurant.admin.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class NotificationDTO {
    private Long id;
    private String title;
    private String content;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
