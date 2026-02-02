package com.management.restaurant.service;

import com.management.restaurant.model.PreOrder;
import com.management.restaurant.model.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface EmailService {
    void sendBookingConfirmation(String to, String fullName, String tableName,
                                 LocalDateTime bookingTime, int guests, List<PreOrder> preOrders, BigDecimal totalAmount);

    /**
     * Send simple email
     */
    void sendEmail(String to, String subject, String body);

    /**
     * Send HTML email
     */
    void sendHtmlEmail(String to, String subject, String htmlBody);

    /**
     * Send email with attachment
     */
    void sendEmailWithAttachment(String to, String subject, String body, String attachmentPath);

    /**
     * Send email verification (async if possible)
     */
    void sendVerificationEmail(User user, String token);

    /**
     * Send password reset email
     */
    void sendPasswordResetEmail(User user, String token);

}
