package com.management.restaurant.analytics.repository;

import com.management.restaurant.analytics.model.SalesReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface SalesReportRepository extends JpaRepository<SalesReport, Long> {
    List<SalesReport> findByReportDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT SUM(s.amount) FROM SalesReport s WHERE s.reportDate = :date")
    BigDecimal getTotalRevenueByDate(LocalDate date);

    List<SalesReport> findByStatus(String status);
}