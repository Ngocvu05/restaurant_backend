package com.management.restaurant.event.implement;

import com.management.restaurant.event.BaseEvent;
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
public class ReviewEvent extends BaseEvent {
    public enum Type {
        REVIEW_CREATED,
        REVIEW_UPDATED,
        REVIEW_DELETED,
        REVIEW_STATUS_CHANGED
    }

    private Long reviewId;
    private Long dishId;
    private String customerName;
    private String customerEmail;
    private String customerAvatar;
    private Integer rating;
    private String comment;
    private Boolean isActive;
    private Boolean isVerified;
    private LocalDateTime createdAt;
}