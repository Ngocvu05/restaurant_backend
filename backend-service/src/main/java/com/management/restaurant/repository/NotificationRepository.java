package com.management.restaurant.repository;

import com.management.restaurant.model.Notification;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByToUserUsernameOrderByCreatedAtDesc(String username);

    Page<Notification> findAllByToUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.toUser.id = :userId")
    void markAllAsReadByUserId(@Param("userId") Long userId);

    Page<Notification> findByToUser_Id(Long toUserId, Pageable pageable);
}
