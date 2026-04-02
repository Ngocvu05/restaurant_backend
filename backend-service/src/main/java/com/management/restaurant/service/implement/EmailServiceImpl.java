package com.management.restaurant.service.implement;

import com.management.restaurant.contains.InvoicePdfGenerator;
import com.management.restaurant.model.Dish;
import com.management.restaurant.model.PreOrder;
import com.management.restaurant.model.User;
import com.management.restaurant.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    @Value("${app.frontend.url}")
    private String frontendUrl;

    private static final int PASSWORD_RESET_TOKEN_EXPIRY_HOURS = 24;
    private static final int EMAIL_VERIFICATION_EXPIRY_HOURS = 48;

    private final JavaMailSender mailSender;

    @Autowired
    private InvoicePdfGenerator invoicePdfGenerator;
    @Autowired
    private SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;


    @Override
    public void sendBookingConfirmation(String to, String fullName, String tableName,
                                        LocalDateTime bookingTime, int guests, List<PreOrder> preOrders, BigDecimal totalAmount) {
        Context context = new Context();
        context.setVariable("fullName", fullName);
        context.setVariable("tableName", tableName);
        context.setVariable("guests", guests);
        context.setVariable("bookingTime", bookingTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        context.setVariable("preOrderTableHtml", buildPreOrderTableHTML(preOrders)); // HTML-safe content

        // Render HTML from Thymeleaf template
        String htmlContent = templateEngine.process("booking-confirmation", context); // Don't include .html extension
        byte[] pdfBytes = invoicePdfGenerator.generateInvoice(
                fullName, tableName, String.valueOf(bookingTime), guests, preOrders, totalAmount
        );
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setFrom(new InternetAddress(fromEmail, "Klassy Cafe"));
            helper.setSubject("Xác nhận đặt bàn tại Klassy Cafe"); //  Subject
            helper.addAttachment("hoa-don.pdf", new ByteArrayResource(pdfBytes));
            helper.setText(htmlContent, true); // true: send HTML

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Error send email: " + e.getMessage(), e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Send simple email
     *
     * @param to
     * @param subject
     * @param body
     */
    @Override
    @Async
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("✅ Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("❌ Failed to send email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Send HTML email
     *
     * @param to
     * @param subject
     * @param htmlBody
     */
    @Override
    @Async
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("✅ HTML email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("❌ Failed to send HTML email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send HTML email", e);
        }
    }

    /**
     * Send email with attachment
     *
     * @param to
     * @param subject
     * @param body
     * @param attachmentPath
     */
    @Override
    @Async
    public void sendEmailWithAttachment(String to, String subject, String body, String attachmentPath) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);

            File file = new File(attachmentPath);
            helper.addAttachment(file.getName(), file);

            mailSender.send(message);
            log.info("✅ Email with attachment sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("❌ Failed to send email with attachment to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email with attachment", e);
        }
    }

    /**
     * Send email verification (async if possible)
     *
     * @param user
     * @param token
     */
    @Override
    public void sendVerificationEmail(User user, String token) {
        String verificationUrl = frontendUrl + "/verify-email?token=" + token;
        String subject = "Verify Your Email Address";
        String body = String.format(
                "Hello %s,\n\n" +
                        "Please click the link below to verify your email address:\n\n" +
                        "%s\n\n" +
                        "This link will expire in %d hours.\n\n" +
                        "If you didn't create an account, please ignore this email.",
                user.getFullName() != null ? user.getFullName() : user.getUsername(),
                verificationUrl,
                EMAIL_VERIFICATION_EXPIRY_HOURS
        );

        try {
            sendEmail(user.getEmail(), subject, body);
            log.info("✅ Verification email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("❌ Failed to send verification email: {}", e.getMessage());
        }
    }

    /**
     * Send password reset email
     *
     * @param user
     * @param token
     */
    @Override
    public void sendPasswordResetEmail(User user, String token) {
        String resetUrl = frontendUrl + "/reset-password?token=" + token;
        String subject = "Password Reset Request";
        String body = String.format(
                "Hello %s,\n\n" +
                        "You requested to reset your password. Click the link below:\n\n" +
                        "%s\n\n" +
                        "This link will expire in %d hours.\n\n" +
                        "If you didn't request this, please ignore this email and your password will remain unchanged.",
                user.getFullName() != null ? user.getFullName() : user.getUsername(),
                resetUrl,
                PASSWORD_RESET_TOKEN_EXPIRY_HOURS
        );

        try {
            sendEmail(user.getEmail(), subject, body);
            log.info("✅ Password reset email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("❌ Failed to send password reset email: {}", e.getMessage());
        }
    }

    private String buildPreOrderTableHTML(List<PreOrder> preOrders) {
        if (preOrders == null || preOrders.isEmpty()) {
            return "<p><em>Không có món ăn đặt trước.</em></p>";
        }

        StringBuilder sb = new StringBuilder();
        BigDecimal total = BigDecimal.ZERO;

        sb.append("<h3 style='margin-top: 20px;'>🍽 Món ăn đặt trước:</h3>");
        sb.append("<table style='width: 100%; border-collapse: collapse; font-size: 14px;'>")
                .append("<thead>")
                .append("<tr style='background-color: #f2f2f2;'>")
                .append("<th style='padding: 8px; border: 1px solid #ddd;'>Ảnh</th>")
                .append("<th style='padding: 8px; border: 1px solid #ddd;'>Tên món</th>")
                .append("<th style='padding: 8px; border: 1px solid #ddd;'>Số lượng</th>")
                .append("<th style='padding: 8px; border: 1px solid #ddd;'>Giá</th>")
                .append("<th style='padding: 8px; border: 1px solid #ddd;'>Ghi chú</th>")
                .append("</tr>")
                .append("</thead><tbody>");

        for (PreOrder p : preOrders) {
            Dish dish = p.getDish();
            if (dish == null) continue;

            BigDecimal itemTotal = dish.getPrice().multiply(BigDecimal.valueOf(p.getQuantity()));
            total = total.add(itemTotal);

            sb.append("<tr>")
                    .append("<td style='padding: 8px; border: 1px solid #ddd; text-align: center;'>")
                    .append("<img src='").append(dish.getImages()).append("' alt='dish' width='60' style='border-radius: 6px;'/>")
                    .append("</td>")
                    .append("<td style='padding: 8px; border: 1px solid #ddd;'>").append(dish.getName()).append("</td>")
                    .append("<td style='padding: 8px; border: 1px solid #ddd; text-align: center;'>").append(p.getQuantity()).append("</td>")
                    .append("<td style='padding: 8px; border: 1px solid #ddd;'>")
                    .append(String.format("%,.0f₫", dish.getPrice())).append("</td>")
                    .append("<td style='padding: 8px; border: 1px solid #ddd;'>")
                    .append(p.getNote() != null && !p.getNote().isEmpty() ? p.getNote() : "-").append("</td>")
                    .append("</tr>");
        }

        sb.append("</tbody></table>");
        sb.append("<p style='margin-top: 10px; font-weight: bold;'>Tổng cộng: ")
                .append(String.format("%,.0f₫", total.doubleValue()))
                .append("</p>");

        return sb.toString();
    }
}