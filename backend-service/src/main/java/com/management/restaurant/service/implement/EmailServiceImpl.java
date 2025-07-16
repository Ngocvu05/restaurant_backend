package com.management.restaurant.service.implement;

import com.management.restaurant.common.InvoicePdfGenerator;
import com.management.restaurant.model.Dish;
import com.management.restaurant.model.PreOrder;
import com.management.restaurant.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;


import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;

    @Autowired
    private InvoicePdfGenerator invoicePdfGenerator;
    @Autowired
    private SpringTemplateEngine templateEngine;

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
            helper.setFrom(new InternetAddress("klassy@yourdomain.com", "Klassy Cafe")); // ✅ Tên hiển thị
            helper.setSubject("Xác nhận đặt bàn tại Klassy Cafe"); // ✅ Subject cố định hoặc tùy biến
            helper.addAttachment("hoa-don.pdf", new ByteArrayResource(pdfBytes));
            helper.setText(htmlContent, true); // ✅ true: gửi HTML

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Lỗi gửi email: " + e.getMessage(), e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
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
