package com.management.restaurant.service;

import com.management.restaurant.dto.BookingDTO;
import com.management.restaurant.dto.PaymentDTO;

public interface QrPaymentService {
    String generateQrPaymentUrl(BookingDTO booking, PaymentDTO payment);
}
