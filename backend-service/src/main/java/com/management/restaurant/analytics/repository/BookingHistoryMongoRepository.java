package com.management.restaurant.analytics.repository;

import com.management.restaurant.analytics.model.BookingHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingHistoryMongoRepository extends MongoRepository<BookingHistory,String> {
    List<BookingHistory> findByBookingId(Long bookingId);

    List<BookingHistory> findByUserId(Long userId);

    Page<BookingHistory> findByBookingIdOrderByTimestampDesc(
            Long bookingId, Pageable pageable
    );

    @Query("{ 'bookingId': ?0, 'timestamp': { $gte: ?1, $lte: ?2 } }")
    List<BookingHistory> findBookingHistoryInRange(
            Long bookingId, LocalDateTime start, LocalDateTime end
    );
}