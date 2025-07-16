package com.management.restaurant.repository;

import com.management.restaurant.model.PreOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PreorderRepository extends JpaRepository<PreOrder, Long> {
    List<PreOrder> findByBookingId(Long bookingId);
}
