package com.management.restaurant.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuration for JPA Auditing
 * Enables automatic population of:
 * - @CreatedDate
 * - @CreatedBy
 * - @LastModifiedDate
 * - @LastModifiedBy
 */
@Slf4j
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {
    @Bean
    public AuditorAware<String> auditorProvider() {
        log.info("Initializing JPA Auditing with AuditorAwareImpl");
        return new AuditorAwareImpl();
    }
}