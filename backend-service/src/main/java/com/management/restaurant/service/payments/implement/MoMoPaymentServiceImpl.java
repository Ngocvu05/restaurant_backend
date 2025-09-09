package com.management.restaurant.service.payments.implement;

import com.management.restaurant.common.PaymentMethod;
import com.management.restaurant.common.PaymentStatus;
import com.management.restaurant.dto.PaymentDTO;
import com.management.restaurant.exception.MoMoPaymentException;
import com.management.restaurant.model.Booking;
import com.management.restaurant.model.Payment;
import com.management.restaurant.repository.BookingRepository;
import com.management.restaurant.repository.PaymentRepository;
import com.management.restaurant.service.payments.MoMoPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MoMoPaymentServiceImpl implements MoMoPaymentService {
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final RestTemplate restTemplate;

    //@Value("${momo.partner.code}")
    private String partnerCode;

    //@Value("${momo.access.key}")
    private String accessKey;

    //@Value("${momo.secret.key}")
    private String secretKey;

    //@Value("${momo.endpoint}")
    private String endpoint;

    //@Value("${momo.redirect.url}")
    private String redirectUrl;

    //@Value("${momo.ipn.url}")
    private String ipnUrl;

    @Override
    public Map<String, Object> createPaymentRequest(Long bookingId, BigDecimal amount, String orderInfo) {
        try{
            // Find booking
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

            // Create preOrder
            String orderId = "BOOKING_" + bookingId + "_" + System.currentTimeMillis();
            String requestId = UUID.randomUUID().toString();

            // Create payment with status PENDING
            Payment payment = Payment.builder()
                    .booking(booking)
                    .amount(amount)
                    .paymentMethod(PaymentMethod.MOMO)
                    .status(PaymentStatus.PENDING)
                    .paymentTime(LocalDateTime.now())
                    .build();

            paymentRepository.save(payment);

            // Create raw signature
            String rawSignature = "accessKey=" + accessKey +
                    "&amount=" + amount.longValue() +
                    "&extraData=" +
                    "&ipnUrl=" + ipnUrl +
                    "&orderId=" + orderId +
                    "&orderInfo=" + orderInfo +
                    "&partnerCode=" + partnerCode +
                    "&redirectUrl=" + redirectUrl +
                    "&requestId=" + requestId +
                    "&requestType=captureWallet";

            // Create signature
            String signature = generateSignature(rawSignature, secretKey);

            // Create request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("partnerCode", partnerCode);
            requestBody.put("accessKey", accessKey);
            requestBody.put("requestId", requestId);
            requestBody.put("amount", amount.longValue());
            requestBody.put("orderId", orderId);
            requestBody.put("orderInfo", orderInfo);
            requestBody.put("redirectUrl", redirectUrl);
            requestBody.put("ipnUrl", ipnUrl);
            requestBody.put("extraData", "");
            requestBody.put("requestType", "captureWallet");
            requestBody.put("signature", signature);
            requestBody.put("lang", "vi");

            // Call MoMo API
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    endpoint, HttpMethod.POST, entity, Map.class);

            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null && "0".equals(responseBody.get("resultCode").toString())) {
                // Successfully
                Map<String, Object> result = new HashMap<>();
                result.put("payUrl", responseBody.get("payUrl"));
                result.put("orderId", orderId);
                result.put("paymentId", payment.getId());
                result.put("requestId", requestId);

                log.info("MoMo payment request created successfully for booking: {}", bookingId);
                return result;
            } else {
                // Failed, update status
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);

                String errorMsg = responseBody != null ? responseBody.get("message").toString() : "Unknown error";
                throw new MoMoPaymentException("MoMo API error: " + errorMsg);
            }

        } catch (Exception e) {
            log.error("Error creating MoMo payment request for booking: {}", bookingId, e);
            throw new MoMoPaymentException("Failed to create MoMo payment request: " + e.getMessage());
        }
    }

    @Override
    public PaymentDTO handlePaymentCallback(Map<String, Object> callbackData) {
        try {
            String orderId = (String) callbackData.get("orderId");
            String resultCode = callbackData.get("resultCode").toString();
            String message = (String) callbackData.get("message");
            String signature = (String) callbackData.get("signature");

            // Verify signature
            if (!verifyCallback(callbackData, signature)) {
                throw new MoMoPaymentException("Invalid signature from MoMo callback");
            }

            Long bookingId = parseBookingIdFromOrderId(orderId);

            Payment payment = paymentRepository.findByBookingId(bookingId)
                    .stream()
                    .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                    .findFirst()
                    .orElseThrow(() -> new MoMoPaymentException("Payment not found for orderId: " + orderId));

            // Update payment status
            if ("0".equals(resultCode)) {
                payment.setStatus(PaymentStatus.SUCCESS);
                log.info("Payment completed successfully for orderId: {}", orderId);
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                log.warn("Payment failed for orderId: {}, message: {}", orderId, message);
            }

            payment.setPaymentTime(LocalDateTime.now());
            Payment savedPayment = paymentRepository.save(payment);

            // Convert to DTO
            return PaymentDTO.builder()
                    .id(savedPayment.getId())
                    .bookingId(savedPayment.getBooking().getId())
                    .amount(savedPayment.getAmount())
                    .paymentMethod(savedPayment.getPaymentMethod())
                    .paymentTime(savedPayment.getPaymentTime())
                    .status(savedPayment.getStatus())
                    .build();

        } catch (Exception e) {
            log.error("Error handling MoMo payment callback", e);
            throw new MoMoPaymentException("Failed to handle payment callback: " + e.getMessage());
        }
    }

    @Override
    public PaymentDTO getPaymentStatus(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new MoMoPaymentException("Payment not found with id: " + paymentId));

        return PaymentDTO.builder()
                .id(payment.getId())
                .bookingId(payment.getBooking().getId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .paymentTime(payment.getPaymentTime())
                .status(payment.getStatus())
                .build();
    }

    private String generateSignature(String rawSignature, String secretKey) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(rawSignature.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new MoMoPaymentException("Error generating signature", e);
        }
    }

    private boolean verifyCallback(Map<String, Object> callbackData, String signature) {
        // Create signature from callback data and compare
        String rawSignature = "accessKey=" + accessKey +
                "&amount=" + callbackData.get("amount") +
                "&extraData=" + callbackData.get("extraData") +
                "&message=" + callbackData.get("message") +
                "&orderId=" + callbackData.get("orderId") +
                "&orderInfo=" + callbackData.get("orderInfo") +
                "&orderType=" + callbackData.get("orderType") +
                "&partnerCode=" + partnerCode +
                "&payType=" + callbackData.get("payType") +
                "&requestId=" + callbackData.get("requestId") +
                "&responseTime=" + callbackData.get("responseTime") +
                "&resultCode=" + callbackData.get("resultCode") +
                "&transId=" + callbackData.get("transId");

        String expectedSignature = generateSignature(rawSignature, secretKey);
        return expectedSignature.equals(signature);
    }

    private Long parseBookingIdFromOrderId(String orderId) {
        // Assuming orderId format is "BOOKING_{bookingId}_{timestamp}"
        String[] parts = orderId.split("_");
        if (parts.length >= 2) {
            return Long.parseLong(parts[1]);
        }
        throw new MoMoPaymentException("Invalid orderId format: " + orderId);
    }
}
