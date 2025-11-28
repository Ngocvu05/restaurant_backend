package com.management.restaurant;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Restaurant Management System - Main Application
 * <p>
 * Features:
 * - Multiple Database Support (Restaurant DB + Analytics DB)
 * - Service Discovery with Eureka
 * - Message Queue with RabbitMQ
 * - Event Sourcing with MongoDB
 * <p>
 * Note:
 * - JPA Repository configuration is handled in DistributedTransactionConfig
 * - Entity scanning is handled by EntityManagerFactory beans
 * - DO NOT add @EnableJpaRepositories or @EntityScan here to avoid conflicts
 */
@SpringBootApplication(
        scanBasePackages = "com.management.restaurant",
        exclude = {
                org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
                org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class
        }
)
@EnableDiscoveryClient
@EnableRabbit
@EnableScheduling
public class RestaurantApplication {
    public static void main(String[] args) {
        SpringApplication.run(RestaurantApplication.class, args);
    }
}