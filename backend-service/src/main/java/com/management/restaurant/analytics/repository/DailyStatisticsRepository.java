package com.management.restaurant.analytics.repository;

import com.management.restaurant.analytics.model.DailyStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailyStatisticsRepository extends JpaRepository<DailyStatistics, Long> {
    DailyStatistics findByStatDate(LocalDate date);

    List<DailyStatistics> findByStatDateBetweenOrderByStatDateDesc(
            LocalDate startDate, LocalDate endDate);
}
