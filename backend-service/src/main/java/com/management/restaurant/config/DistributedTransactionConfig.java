package com.management.restaurant.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for Distributed Transactions across multiple databases
 * Scenario: Restaurant system với 2 databases:
 * 1. restaurant_db: Chứa bookings, dishes, tables
 * 2. analytics_db: Chứa reports, statistics
 */

@Configuration
@EnableTransactionManagement
public class DistributedTransactionConfig {
    /**
     * PRIMARY DATABASE: restaurant_db
     * Main operational database
     */
    @Primary
    @Bean(name = "restaurantDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.restaurant")
    public DataSource restaurantDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = "restaurantEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean restaurantEntityManagerFactory(
            @Qualifier("restaurantDataSource") DataSource dataSource) {

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.management.restaurant.model");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        properties.put("hibernate.show_sql", true);
        properties.put("hibernate.format_sql", true);
        em.setJpaPropertyMap(properties);

        return em;
    }

    @Primary
    @Bean(name = "restaurantTransactionManager")
    public PlatformTransactionManager restaurantTransactionManager(
            @Qualifier("restaurantEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    /**
     * SECONDARY DATABASE: analytics_db
     * Reporting and analytics database
     */
    @Bean(name = "analyticsDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.analytics")
    public DataSource analyticsDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "analyticsEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean analyticsEntityManagerFactory(
            @Qualifier("analyticsDataSource") DataSource dataSource) {

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.management.restaurant.analytics.model");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        properties.put("hibernate.show_sql", true);
        em.setJpaPropertyMap(properties);

        return em;
    }

    @Bean(name = "analyticsTransactionManager")
    public PlatformTransactionManager analyticsTransactionManager(
            @Qualifier("analyticsEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}

/**
 * Repository Configuration for Multiple Databases
 */
@Configuration
@EnableJpaRepositories(
        basePackages = "com.management.restaurant.repository",
        entityManagerFactoryRef = "restaurantEntityManagerFactory",
        transactionManagerRef = "restaurantTransactionManager"
)
class RestaurantRepositoryConfig {}

@Configuration
@EnableJpaRepositories(
        basePackages = "com.management.restaurant.analytics.repository",
        entityManagerFactoryRef = "analyticsEntityManagerFactory",
        transactionManagerRef = "analyticsTransactionManager"
)
class AnalyticsRepositoryConfig {}