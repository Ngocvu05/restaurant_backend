package com.management.restaurant.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.Properties;

/**
 * Configuration for Distributed Transactions across multiple databases
 * Using HikariCP for high-performance connection pooling
 */
@Slf4j
@Configuration
@EnableTransactionManagement
public class DistributedTransactionConfig {
    // ========================================
    // PRIMARY DATABASE: Restaurant DB
    // ========================================
    @Value("${spring.datasource.jdbc-url:jdbc:mysql://localhost:3306/restaurant?useSSL=false&serverTimezone=UTC}")
    private String primaryJdbcUrl;

    @Value("${spring.datasource.username:restaurant_user}")
    private String primaryUsername;

    @Value("${spring.datasource.password:restaurant_pass}")
    private String primaryPassword;

    @Value("${spring.datasource.driver-class-name:com.mysql.cj.jdbc.Driver}")
    private String primaryDriverClassName;

    @Primary
    @Bean(name = "restaurantDataSource")
    public DataSource restaurantDataSource() {
        log.info("Initializing Restaurant DataSource with URL: {}", primaryJdbcUrl);

        HikariDataSource dataSource = new HikariDataSource();

        // JDBC Connection
        dataSource.setJdbcUrl(primaryJdbcUrl);
        dataSource.setUsername(primaryUsername);
        dataSource.setPassword(primaryPassword);
        dataSource.setDriverClassName(primaryDriverClassName);

        // HikariCP Pool Settings
        dataSource.setMaximumPoolSize(10);
        dataSource.setMinimumIdle(5);
        dataSource.setConnectionTimeout(30000);
        dataSource.setIdleTimeout(600000);
        dataSource.setMaxLifetime(1800000);
        dataSource.setPoolName("RestaurantHikariPool");

        // Connection Test
        dataSource.setConnectionTestQuery("SELECT 1");
        dataSource.setValidationTimeout(5000);

        // Auto-commit settings
        dataSource.setAutoCommit(true);

        log.info("Restaurant DataSource initialized successfully");
        return dataSource;
    }

    @Primary
    @Bean(name = "restaurantEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean restaurantEntityManagerFactory(
            @Qualifier("restaurantDataSource") DataSource dataSource) {

        log.info("Configuring Restaurant EntityManagerFactory");

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.management.restaurant.model",
                "com.management.restaurant.event.model");
        em.setPersistenceUnitName("restaurant");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(true);
        vendorAdapter.setShowSql(true);
        vendorAdapter.setDatabasePlatform("org.hibernate.dialect.MySQLDialect");
        em.setJpaVendorAdapter(vendorAdapter);

        em.setJpaProperties(hibernateProperties(true));

        log.info("Restaurant EntityManagerFactory configured successfully");
        return em;
    }

    @Primary
    @Bean(name = "transactionManager") // Primary transaction manager
    public PlatformTransactionManager restaurantTransactionManager(
            @Qualifier("restaurantEntityManagerFactory") LocalContainerEntityManagerFactoryBean factory) {

        log.info("Configuring Restaurant TransactionManager");

        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(factory.getObject());
        transactionManager.setNestedTransactionAllowed(true);

        log.info("Restaurant TransactionManager configured successfully");
        return transactionManager;
    }

    // ========================================
    // SECONDARY DATABASE: Analytics DB
    // ========================================

    @Value("${spring.datasource.analytics.jdbc-url:jdbc:mysql://localhost:3306/analytics?useSSL=false&serverTimezone=UTC}")
    private String analyticsJdbcUrl;

    @Value("${spring.datasource.analytics.username:restaurant_user}")
    private String analyticsUsername;

    @Value("${spring.datasource.analytics.password:restaurant_pass}")
    private String analyticsPassword;

    @Value("${spring.datasource.analytics.driver-class-name:com.mysql.cj.jdbc.Driver}")
    private String analyticsDriverClassName;

    @Bean(name = "analyticsDataSource")
    public DataSource analyticsDataSource() {
        log.info("Initializing Analytics DataSource with URL: {}", analyticsJdbcUrl);

        HikariDataSource dataSource = new HikariDataSource();

        // JDBC Connection
        dataSource.setJdbcUrl(analyticsJdbcUrl);
        dataSource.setUsername(analyticsUsername);
        dataSource.setPassword(analyticsPassword);
        dataSource.setDriverClassName(analyticsDriverClassName);

        // HikariCP Pool Settings
        dataSource.setMaximumPoolSize(5);
        dataSource.setMinimumIdle(2);
        dataSource.setConnectionTimeout(30000);
        dataSource.setIdleTimeout(600000);
        dataSource.setMaxLifetime(1800000);
        dataSource.setPoolName("AnalyticsHikariPool");

        // Connection Test
        dataSource.setConnectionTestQuery("SELECT 1");
        dataSource.setValidationTimeout(5000);

        // Auto-commit settings
        dataSource.setAutoCommit(true);

        log.info("Analytics DataSource initialized successfully");
        return dataSource;
    }

    @Bean(name = "analyticsEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean analyticsEntityManagerFactory(
            @Qualifier("analyticsDataSource") DataSource dataSource) {

        log.info("Configuring Analytics EntityManagerFactory");

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.management.restaurant.analytics.model");
        em.setPersistenceUnitName("analytics");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(true);
        vendorAdapter.setShowSql(false);
        vendorAdapter.setDatabasePlatform("org.hibernate.dialect.MySQLDialect");
        em.setJpaVendorAdapter(vendorAdapter);

        em.setJpaProperties(hibernateProperties(false));

        log.info("Analytics EntityManagerFactory configured successfully");
        return em;
    }

    @Bean(name = "analyticsTransactionManager")
    public PlatformTransactionManager analyticsTransactionManager(
            @Qualifier("analyticsEntityManagerFactory") LocalContainerEntityManagerFactoryBean factory) {

        log.info("Configuring Analytics TransactionManager");

        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(factory.getObject());
        transactionManager.setNestedTransactionAllowed(true);

        log.info("Analytics TransactionManager configured successfully");
        return transactionManager;
    }

    // ========================================
    // ALIAS BEANS (for backward compatibility)
    // ========================================

    @Bean(name = "entityManagerFactory")
    public EntityManagerFactory entityManagerFactory(
            @Qualifier("restaurantEntityManagerFactory") LocalContainerEntityManagerFactoryBean factory) {
        return factory.getObject();
    }

    // ========================================
    // SHARED HIBERNATE PROPERTIES
    // ========================================

    private Properties hibernateProperties(boolean isPrimary) {
        Properties properties = new Properties();

        // Dialect
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");

        // Schema generation
        properties.setProperty("hibernate.hbm2ddl.auto", "update");

        // SQL logging (only for primary in debug)
        if (isPrimary) {
            properties.setProperty("hibernate.show_sql", "true");
            properties.setProperty("hibernate.format_sql", "true");
            properties.setProperty("hibernate.use_sql_comments", "true");
        } else {
            properties.setProperty("hibernate.show_sql", "false");
        }

        // Connection handling
        properties.setProperty("hibernate.connection.provider_disables_autocommit", "false");

        // Batch processing
        properties.setProperty("hibernate.jdbc.batch_size", "20");
        properties.setProperty("hibernate.jdbc.batch_versioned_data", "true");
        properties.setProperty("hibernate.order_inserts", "true");
        properties.setProperty("hibernate.order_updates", "true");

        // Query optimization
        properties.setProperty("hibernate.query.fail_on_pagination_over_collection_fetch", "true");
        properties.setProperty("hibernate.query.plan_cache_max_size", "2048");
        properties.setProperty("hibernate.query.plan_parameter_metadata_max_size", "128");

        // Default schema
        if (isPrimary) {
            properties.setProperty("hibernate.default_schema", "restaurant");
        } else {
            properties.setProperty("hibernate.default_schema", "analytics");
        }

        log.info("Hibernate properties configured for {} database",
                isPrimary ? "Restaurant (Primary)" : "Analytics (Secondary)");

        return properties;
    }
}

/**
 * Repository Configuration for Primary Database (Restaurant)
 */
@Configuration
@EnableJpaRepositories(
        basePackages = "com.management.restaurant.repository",
        entityManagerFactoryRef = "restaurantEntityManagerFactory",
        transactionManagerRef = "transactionManager"
)
class RestaurantRepositoryConfig {
}

/**
 * Repository Configuration for Secondary Database (Analytics)
 */
@Configuration
@EnableJpaRepositories(
        basePackages = "com.management.restaurant.analytics.repository",
        entityManagerFactoryRef = "analyticsEntityManagerFactory",
        transactionManagerRef = "analyticsTransactionManager"
)
class AnalyticsRepositoryConfig {
}