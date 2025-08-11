package com.management.restaurant.service.payments.implement;

import com.management.restaurant.common.PaymentMethod;
import com.management.restaurant.common.PaymentStatus;
import com.management.restaurant.config.VNPayConfig;
import com.management.restaurant.dto.PaymentDTO;
import com.management.restaurant.exception.VNPayPaymentException;
import com.management.restaurant.model.Booking;
import com.management.restaurant.model.Payment;
import com.management.restaurant.repository.BookingRepository;
import com.management.restaurant.repository.PaymentRepository;
import com.management.restaurant.service.payments.VNPayPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class VNPayPaymentServiceImpl implements VNPayPaymentService {
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final VNPayConfig vnPayConfig;

    // VNPay constants
    private static final String VNPAY_VERSION = "2.1.0";
    private static final String VNPAY_COMMAND = "pay";
    private static final String VNPAY_ORDER_TYPE = "other";
    private static final String VNPAY_CURRENCY_CODE = "VND";
    private static final String VNPAY_LOCALE = "vn";
    private static final String VNPAY_QUERY_COMMAND = "querydr";
    private static final int PAYMENT_TIMEOUT_MINUTES = 15;

    @Override
    public Map<String, Object> createPaymentRequest(Long bookingId, BigDecimal amount, String orderInfo) {
        try {
            // Find booking
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

            Optional<Payment> existingPayment = paymentRepository.findByBooking_Id(bookingId);

            Payment payment;
            if (existingPayment.isPresent()) {
                payment = existingPayment.get();
                payment.setBooking(booking);
                payment.setAmount(amount);
                payment.setPaymentMethod(PaymentMethod.VNPAY);
                payment.setStatus(PaymentStatus.PENDING);
            }else{
                payment = Payment.builder()
                        .booking(booking)
                        .amount(amount)
                        .paymentMethod(PaymentMethod.VNPAY)
                        .status(PaymentStatus.PENDING)
                        .paymentTime(LocalDateTime.now())
                        .build();
            }

            paymentRepository.save(payment);

            // Create transaction reference
            String txnRef = "BOOKING_" + bookingId + "_" + System.currentTimeMillis();

            // Create reference no
            Map<String, String> vnpParams = new HashMap<>();
            vnpParams.put("vnp_Version", VNPAY_VERSION);
            vnpParams.put("vnp_Command", VNPAY_COMMAND);
            vnpParams.put("vnp_TmnCode", vnPayConfig.getTmnCode());
            vnpParams.put("vnp_Amount", String.valueOf(amount.multiply(new BigDecimal("100")).longValue())); // VNPay yêu cầu amount * 100
            vnpParams.put("vnp_CurrCode", VNPAY_CURRENCY_CODE);
            vnpParams.put("vnp_TxnRef", txnRef);
            vnpParams.put("vnp_OrderInfo", orderInfo);
            vnpParams.put("vnp_OrderType", VNPAY_ORDER_TYPE);
            vnpParams.put("vnp_Locale", VNPAY_LOCALE);
            vnpParams.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
            vnpParams.put("vnp_IpAddr", getClientIpAddress());

            // Create time
            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String vnpCreateDate = formatter.format(cld.getTime());
            vnpParams.put("vnp_CreateDate", vnpCreateDate);

            // Thêm 15 phút cho expire date
            cld.add(Calendar.MINUTE, PAYMENT_TIMEOUT_MINUTES);
            String vnpExpireDate = formatter.format(cld.getTime());
            vnpParams.put("vnp_ExpireDate", vnpExpireDate);

            // Arrange variables and time
            List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
            Collections.sort(fieldNames);

            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();

            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = vnpParams.get(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    // Build hash data
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                    // Build query
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }

            String queryUrl = query.toString();
            String vnpSecureHash = hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
            queryUrl += "&vnp_SecureHash=" + vnpSecureHash;

            String paymentUrl = vnPayConfig.getApiUrl() + "?" + queryUrl;

            // Return result
            Map<String, Object> result = new HashMap<>();
            result.put("payUrl", paymentUrl);
            result.put("txnRef", txnRef);
            result.put("paymentId", payment.getId());
            result.put("amount", amount);
            result.put("orderInfo", orderInfo);

            log.info("VNPay payment request created successfully for booking: {}", bookingId);
            return result;

        } catch (Exception e) {
            log.error("Error creating VNPay payment request for booking: {}", bookingId, e);
            throw new VNPayPaymentException("Failed to create VNPay payment request: " + e.getMessage());
        }
    }

    @Override
    public PaymentDTO handlePaymentReturn(Map<String, String> params) {
        try {
            String vnpSecureHash = params.get("vnp_SecureHash");
            String vnpTxnRef = params.get("vnp_TxnRef");
            String vnpResponseCode = params.get("vnp_ResponseCode");
            String vnpTransactionStatus = params.get("vnp_TransactionStatus");
            String vnpAmount = params.get("vnp_Amount");

            // Xóa secure hash khỏi params để verify
            params.remove("vnp_SecureHash");
            params.remove("vnp_SecureHashType");

            // Verify signature
            if (!verifyPaymentSignature(params, vnpSecureHash)) {
                throw new VNPayPaymentException("Invalid signature from VNPay");
            }

            // Parse booking ID từ txnRef
            Long bookingId = parseBookingIdFromTxnRef(vnpTxnRef);

            // Find payment
            Payment payment = paymentRepository.findByBookingId(bookingId)
                    .stream()
                    .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Payment not found for txnRef: " + vnpTxnRef));

            // Update payment status
            if ("00".equals(vnpResponseCode) && "00".equals(vnpTransactionStatus)) {
                payment.setStatus(PaymentStatus.SUCCESS);
                log.info("VNPay payment completed successfully for txnRef: {}", vnpTxnRef);
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                log.warn("VNPay payment failed for txnRef: {}, responseCode: {}, transactionStatus: {}",
                        vnpTxnRef, vnpResponseCode, vnpTransactionStatus);
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
            log.error("Error handling VNPay payment return", e);
            throw new VNPayPaymentException("Failed to handle payment return: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> queryTransaction(String txnRef) {
        try {
            // Create request query transaction
            Map<String, String> vnpParams = new HashMap<>();
            vnpParams.put("vnp_Version", VNPAY_VERSION);
            vnpParams.put("vnp_Command", VNPAY_QUERY_COMMAND);
            vnpParams.put("vnp_TmnCode", vnPayConfig.getTmnCode());
            vnpParams.put("vnp_TxnRef", txnRef);

            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String vnpCreateDate = formatter.format(cld.getTime());
            vnpParams.put("vnp_CreateDate", vnpCreateDate);
            vnpParams.put("vnp_IpAddr", getClientIpAddress());

            // Create transaction date từ txnRef (get timestamp)
            String timestamp = txnRef.substring(txnRef.lastIndexOf("_") + 1);
            Date transactionDate = new Date(Long.parseLong(timestamp));
            String vnpTransactionDate = formatter.format(transactionDate);
            vnpParams.put("vnp_TransactionDate", vnpTransactionDate);

            // Create secure hash
            String hashData = buildHashData(vnpParams);
            String vnpSecureHash = hmacSHA512(vnPayConfig.getHashSecret(), hashData);
            vnpParams.put("vnp_SecureHash", vnpSecureHash);

            // Call API query (need implement HTTP client to call VNPay query API)
            Map<String, Object> result = new HashMap<>();
            result.put("txnRef", txnRef);
            result.put("status", "queried");
            result.put("message", "Transaction queried successfully");

            return result;

        } catch (Exception e) {
            log.error("Error querying VNPay transaction: {}", txnRef, e);
            throw new VNPayPaymentException("Failed to query transaction: " + e.getMessage());
        }
    }

    @Override
    public PaymentDTO getPaymentStatus(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + paymentId));

        return PaymentDTO.builder()
                .id(payment.getId())
                .bookingId(payment.getBooking().getId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .paymentTime(payment.getPaymentTime())
                .status(payment.getStatus())
                .build();
    }

    private boolean verifyPaymentSignature(Map<String, String> params, String vnpSecureHash) {
        String hashData = buildHashData(params);
        String calculatedHash = hmacSHA512(vnPayConfig.getHashSecret(), hashData);
        return calculatedHash.equals(vnpSecureHash);
    }

    private String buildHashData(Map<String, String> params) {
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();

        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    hashData.append('&');
                }
            }
        }

        return hashData.toString();
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder result = new StringBuilder();
            for (byte b : hash) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        } catch (Exception e) {
            throw new VNPayPaymentException("Error generating HMAC-SHA512", e);
        }
    }

    private Long parseBookingIdFromTxnRef(String txnRef) {
        // Assuming txnRef format is "BOOKING_{bookingId}_{timestamp}"
        String[] parts = txnRef.split("_");
        if (parts.length >= 2) {
            return Long.parseLong(parts[1]);
        }
        throw new VNPayPaymentException("Invalid txnRef format: " + txnRef);
    }

    private String getClientIpAddress() {
        return "127.0.0.1";
    }
}