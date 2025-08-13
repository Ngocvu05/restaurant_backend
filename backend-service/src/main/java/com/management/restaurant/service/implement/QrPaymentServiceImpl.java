package com.management.restaurant.service.implement;

import com.management.restaurant.dto.BookingDTO;
import com.management.restaurant.dto.PaymentDTO;
import com.management.restaurant.service.QrPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class QrPaymentServiceImpl implements QrPaymentService {
    @Override
    public String generateQrPaymentUrl(BookingDTO booking, PaymentDTO payment) {
        // The actual QR code format may depend on the specific bank; here is an example with VNPAY:
        String accountNumber = "123456789";
        String bankCode = "970422";
        String content = "Thanh toan don dat ban #" + booking.getId();

        // Example using VNPAY static QR (or switch to dynamic if an intermediate server is available).
        return "https://img.vietqr.io/image/" + bankCode + "-" + accountNumber + "-print.png?amount="
                + payment.getAmount() + "&addInfo=" + URLEncoder.encode(content, StandardCharsets.UTF_8);
    }
}