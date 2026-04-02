package com.management.restaurant.controller;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/actuator/datasource")
public class DataSourceInfoController {
    @Autowired
    @Qualifier("restaurantDataSource")
    private HikariDataSource restaurantDS;

    @Autowired
    @Qualifier("analyticsDataSource")
    private HikariDataSource analyticsDS;

    @GetMapping("/restaurant")
    public Map<String, Object> getRestaurantDSInfo() {
        return Map.of(
                "active", restaurantDS.getHikariPoolMXBean().getActiveConnections(),
                "idle", restaurantDS.getHikariPoolMXBean().getIdleConnections(),
                "total", restaurantDS.getHikariPoolMXBean().getTotalConnections(),
                "waiting", restaurantDS.getHikariPoolMXBean().getThreadsAwaitingConnection()
        );
    }

    @GetMapping("/analytics")
    public Map<String, Object> getAnalyticsDSInfo() {
        return Map.of(
                "active", analyticsDS.getHikariPoolMXBean().getActiveConnections(),
                "idle", analyticsDS.getHikariPoolMXBean().getIdleConnections(),
                "total", analyticsDS.getHikariPoolMXBean().getTotalConnections(),
                "waiting", analyticsDS.getHikariPoolMXBean().getThreadsAwaitingConnection()
        );
    }
}