package com.management.restaurant.service.implement;

import com.management.restaurant.admin.service.NotificationService;
import com.management.restaurant.common.BookingStatus;
import com.management.restaurant.common.PaymentStatus;
import com.management.restaurant.dto.BookingDTO;
import com.management.restaurant.dto.BookingDetailResponseDTO;
import com.management.restaurant.dto.PaymentDTO;
import com.management.restaurant.dto.PreOrderDTO;
import com.management.restaurant.exception.NotFoundException;
import com.management.restaurant.mapper.BookingMapper;
import com.management.restaurant.mapper.PaymentMapper;
import com.management.restaurant.model.*;
import com.management.restaurant.repository.*;
import com.management.restaurant.service.BookingService;
import com.management.restaurant.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final TableRepository tableRepository;
    private final BookingMapper bookingMapper;
    private final DishRepository dishRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final PreorderRepository preorderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final QrPaymentServiceImpl qrPaymentService;

    @Override
    public BookingDTO createBooking(BookingDTO dto) {
        // 1. Find user and tables
        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        TableEntity table = tableRepository.findById(dto.getTableId())
                .orElseThrow(() -> new RuntimeException("Table not found"));

        // 2. Convert dto to entity
        Booking booking = bookingMapper.toEntity(dto);
        booking.setStatus(BookingStatus.PENDING);
        booking.setUser(user);
        booking.setTable(table);

        List<PreOrder> preOrders = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (dto.getPreOrderDishes() != null && !dto.getPreOrderDishes().isEmpty()) {
            // 3. fetch all dish
            List<Long> dishIds = dto.getPreOrderDishes().stream()
                    .map(PreOrderDTO::getDishId)
                    .distinct()
                    .toList();

            Map<Long, Dish> dishMap = dishRepository.findAllById(dishIds).stream()
                    .collect(Collectors.toMap(Dish::getId, d -> d));

            for (PreOrderDTO preDto : dto.getPreOrderDishes()) {
                Dish dish = dishMap.get(preDto.getDishId());
                if (dish == null) continue;

                // Create PreOrder
                PreOrder preOrder = new PreOrder();
                preOrder.setDish(dish);
                preOrder.setBooking(booking);
                preOrder.setQuantity(preDto.getQuantity());
                preOrder.setNote(preDto.getNote());
                preOrders.add(preOrder);
                // Sum total amount
                totalAmount = totalAmount.add(
                        dish.getPrice().multiply(BigDecimal.valueOf(preDto.getQuantity()))
                );

                // Increase orderCount
                dish.setOrderCount(dish.getOrderCount() + preDto.getQuantity());
            }
            booking.setPreOrders(preOrders);
        }
        booking.setTotalAmount(totalAmount);
        // 4. Storage booking + update dish
        Booking savedBooking = bookingRepository.save(booking);
        dishRepository.saveAll(preOrders.stream()
                .map(PreOrder::getDish)
                .distinct()
                .toList());

        // 5. Send email to confirm
        if (user.getEmail() != null) {
            emailService.sendBookingConfirmation(
                    user.getEmail(),
                    user.getFullName(),
                    table.getTableName(),
                    booking.getBookingTime(),
                    booking.getNumberOfGuests(),
                    preOrders,
                    booking.getTotalAmount()
            );
        }
        //Send notication to admin
        notificationService.notifyAllAdmins("Đặt bàn mới", "Người dùng " + user.getUsername() + " đã đặt bàn.");

        return bookingMapper.toDTO(savedBooking);
    }


    @Override
    public BookingDTO getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        return bookingMapper.toDTO(booking);
    }

    public BookingDetailResponseDTO getBookingDetail(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy booking"));

        Payment payment = paymentRepository.findFirstByBookingId(bookingId)
                .orElse(null);

        BookingDTO bookingDTO = bookingMapper.toDTO(booking);
        PaymentDTO paymentDTO = payment != null ? paymentMapper.toDTO(payment) : null;

        String qrUrl = (payment != null)
                ? qrPaymentService.generateQrPaymentUrl(bookingDTO, paymentDTO)
                : null;

        return new BookingDetailResponseDTO(bookingDTO, paymentDTO, qrUrl);
    }

    @Override
    public List<BookingDTO> getAllBookings() {
        return bookingRepository.findAll()
                .stream()
                .map(bookingMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BookingDTO updateBooking(Long id, BookingDTO dto) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        booking.setBookingTime(dto.getBookingTime());
        booking.setNumberOfGuests(dto.getNumberOfGuests());
        booking.setNote(dto.getNote());
        booking.setStatus(BookingStatus.valueOf(dto.getStatus()));
        User user = dto.getUserId() != null
                ? userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"))
                : userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        booking.setUser(user);
        booking.setTable(tableRepository.findById(dto.getTableId())
                .orElseThrow(() -> new RuntimeException("Table not found")));
        if (dto.getPreOrderDishes() != null && !dto.getPreOrderDishes().isEmpty()) {
            List<PreOrder> dishes = preorderRepository.findAllById(dto.getPreOrderDishes().stream().map(PreOrderDTO::getDishId).collect(Collectors.toList()));
            booking.setPreOrders(dishes);
        }

        return bookingMapper.toDTO(bookingRepository.save(booking));
    }

    @Override
    public void deleteBooking(Long id) {
        bookingRepository.deleteById(id);
    }

    @Override
    public List<BookingDTO> getBookingHistory(Long userId) {
        List<Booking> bookings = bookingRepository.findByUserIdOrderByBookingTimeDesc(userId);
        return bookings.stream().map(bookingMapper::toDTO).collect(Collectors.toList());
    }
}