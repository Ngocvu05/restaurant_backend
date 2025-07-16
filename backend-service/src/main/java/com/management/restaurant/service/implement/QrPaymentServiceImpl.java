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
        // ⚠️ Mẫu QR thật có thể phụ thuộc vào ngân hàng cụ thể, đây là ví dụ với VNPAY:
        String accountNumber = "123456789"; // hoặc Mã Merchant của bạn
        String bankCode = "970422"; // Mã ngân hàng (Agribank, Vietcombank,...)
        String content = "Thanh toan don dat ban #" + booking.getId();

        // Ví dụ dùng VNPAY QR static (hoặc chuyển sang dynamic nếu có server trung gian)
        return "https://img.vietqr.io/image/" + bankCode + "-" + accountNumber + "-print.png?amount="
                + payment.getAmount() + "&addInfo=" + URLEncoder.encode(content, StandardCharsets.UTF_8);
    }
}
