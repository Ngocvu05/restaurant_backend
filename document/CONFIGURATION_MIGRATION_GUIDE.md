# Configuration Migration Guide

## 📋 Files Created

```
src/main/resources/
├── application.properties (Main config - UPDATED)
├── application-dev.properties (Development profile)
├── application-prod.properties (Production profile)
├── application-test.properties (Testing profile)
└── .env.example (Environment variables template)
```

---

## 🔄 What Changed?

### ✅ Added Transaction Management Configurations

1. **Isolation Levels**
   ```properties
   spring.jpa.properties.hibernate.connection.isolation=2
   # 2 = READ_COMMITTED (default, good balance)
   # 4 = REPEATABLE_READ (production recommendation)
   ```

2. **Batch Processing**
   ```properties
   spring.jpa.properties.hibernate.jdbc.batch_size=20
   spring.jpa.properties.hibernate.order_inserts=true
   spring.jpa.properties.hibernate.order_updates=true
   ```

3. **Connection Pool Settings**
   ```properties
   spring.datasource.hikari.maximum-pool-size=10
   spring.datasource.hikari.minimum-idle=5
   spring.datasource.hikari.connection-timeout=30000
   ```

4. **Transaction Timeout**
   ```properties
   spring.transaction.default-timeout=30
   ```

5. **Retry Configuration**
   ```properties
   spring.retry.enabled=true
   ```

6. **Analytics Database Support** (for distributed transactions)
   ```properties
   # Commented by default, uncomment when needed
   #spring.datasource.analytics.jdbc-url=...
   ```

---

## 🚀 Quick Start

### Step 1: Backup Your Current File
```bash
cp src/main/resources/application.properties src/main/resources/application.properties.backup
```

### Step 2: Replace with New Config
1. Copy content from `application.properties` artifact
2. Paste into your `src/main/resources/application.properties`

### Step 3: Create Profile-Specific Files
```bash
# Create dev profile
touch src/main/resources/application-dev.properties

# Create prod profile
touch src/main/resources/application-prod.properties

# Create test profile
touch src/main/resources/application-test.properties
```

Copy content from the corresponding artifacts into each file.

### Step 4: Setup Environment Variables
```bash
# Copy .env.example to .env
cp .env.example .env

# Edit .env with your actual values
nano .env
# or
vim .env
```

### Step 5: Update Environment Variables
Edit `.env` file with your actual credentials:

```bash
# Database
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/restaurant
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=your_actual_password

# JWT
JWT_SECRET=generate-a-strong-256-bit-secret-key-here
JWT_EXPIRATION=86400000

# Email
SPRING_MAIL_USERNAME=your-email@gmail.com
SPRING_MAIL_PASSWORD=your-app-specific-password

# Cloudinary
CLOUDINARY_CLOUD_NAME=your-cloud-name
CLOUDINARY_API_KEY=your-api-key
CLOUDINARY_API_SECRET=your-api-secret
```

### Step 6: Load Environment Variables
Add to your IDE or use:

```bash
# For Linux/Mac
export $(cat .env | xargs)

# For Windows (PowerShell)
Get-Content .env | ForEach-Object {
    $name, $value = $_.split('=')
    Set-Content env:\$name $value
}
```

---

## 🎯 Profile Selection

### Development (Default)
```bash
# Run with dev profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Or set in application.properties
spring.profiles.active=dev
```

### Production
```bash
# Run with prod profile
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# Or
java -jar your-app.jar --spring.profiles.active=prod
```

### Testing
```bash
# Tests automatically use test profile
mvn test
```

---

## 📊 Configuration Comparison

| Setting | Development | Production | Test |
|---------|------------|------------|------|
| **Isolation Level** | READ_COMMITTED (2) | REPEATABLE_READ (4) | READ_COMMITTED (2) |
| **Show SQL** | true | false | true |
| **DDL Auto** | update | validate | create-drop |
| **Pool Size** | 5 | 20 | 5 |
| **Logging** | DEBUG | WARN/INFO | DEBUG |
| **Transaction Timeout** | 60s | 30s | 10s |

---

## 🔧 Analytics Database Setup (Optional)

If you want to use distributed transactions:

### Step 1: Create Analytics Database
```sql
CREATE DATABASE analytics;
USE analytics;

CREATE TABLE sales_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    report_date DATE NOT NULL,
    status VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_booking_id (booking_id),
    INDEX idx_report_date (report_date)
);

CREATE TABLE daily_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stat_date DATE UNIQUE NOT NULL,
    total_bookings INT DEFAULT 0,
    total_revenue DECIMAL(12, 2) DEFAULT 0,
    total_customers INT DEFAULT 0,
    popular_dish_id BIGINT,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Step 2: Uncomment Analytics Config
In `application.properties`:
```properties
# Uncomment these lines:
spring.datasource.analytics.jdbc-url=jdbc:mysql://localhost:3306/analytics?useSSL=false&serverTimezone=UTC
spring.datasource.analytics.username=root
spring.datasource.analytics.password=your_password
spring.datasource.analytics.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.analytics.hikari.maximum-pool-size=5
spring.datasource.analytics.hikari.minimum-idle=2
spring.datasource.analytics.hikari.connection-timeout=30000
```

### Step 3: Add Analytics Config Class
```java
@Configuration
public class AnalyticsDataSourceConfig {
    // See DistributedTransactionConfig.java for full implementation
}
```

---

## 🐛 Troubleshooting

### Issue 1: Environment Variables Not Loading
```bash
# Check if variables are set
echo $SPRING_DATASOURCE_URL

# If empty, reload .env
export $(cat .env | xargs)
```

### Issue 2: Connection Pool Exhausted
```properties
# Increase pool size in application-dev.properties
spring.datasource.hikari.maximum-pool-size=20
```

### Issue 3: Transaction Timeout
```properties
# Increase timeout for long operations
spring.transaction.default-timeout=60
```

### Issue 4: Deadlock Detection Not Working
```properties
# Enable deadlock logging
logging.level.org.hibernate.engine.jdbc.spi.SqlExceptionHelper=DEBUG
```

---

## 📝 Key Changes Summary

### 1. **Multiple Profiles Support**
- ✅ Development: Fast, verbose logging
- ✅ Production: Secure, optimized
- ✅ Test: In-memory H2 database

### 2. **Transaction Management**
- ✅ Configurable isolation levels
- ✅ Connection pooling optimized
- ✅ Batch processing enabled
- ✅ Retry mechanism ready

### 3. **Performance Tuning**
- ✅ Batch size configured
- ✅ Query cache enabled
- ✅ Statement ordering enabled

### 4. **Monitoring & Debugging**
- ✅ Transaction logging
- ✅ SQL logging
- ✅ Deadlock detection
- ✅ Metrics enabled

---

## ✅ Verification Steps

### 1. Check Configuration Loading
```bash
# Start application
mvn spring-boot:run

# Check logs for:
# "The following profiles are active: dev"
```

### 2. Test Database Connection
```bash
# Run simple test
curl http://localhost:8081/actuator/health

# Expected response:
# {"status":"UP"}
```

### 3. Verify Transaction Settings
Check application logs for:
```
Hibernate: 
    select
        ...
    from
        bookings 
# If you see formatted SQL, configuration is working
```

### 4. Test Isolation Level
```java
@Test
void testIsolationLevel() {
    // This should use READ_COMMITTED in dev
    // and REPEATABLE_READ in prod
}
```

---

## 🎓 Best Practices

### 1. **Never Commit `.env` File**
```bash
# Add to .gitignore
echo ".env" >> .gitignore
```

### 2. **Use Separate Databases per Environment**
- `restaurant_dev` for development
- `restaurant` for production
- `testdb` (H2) for testing

### 3. **Monitor Connection Pool**
```properties
# Enable HikariCP metrics
management.metrics.enable.hikaricp=true
```

### 4. **Regular Backup**
```bash
# Backup before changes
cp application.properties application.properties.$(date +%Y%m%d)
```

---

## 🚀 Next Steps

1. ✅ Replace your application.properties
2. ✅ Create profile-specific files
3. ✅ Setup .env file
4. ✅ Test with development profile
5. ✅ Run integration tests
6. ✅ Configure production environment
7. ✅ Setup monitoring (Prometheus/Grafana)

---

## 📞 Support

If you encounter issues:
1. Check logs in `logs/user-service.log`
2. Verify environment variables are loaded
3. Test with H2 database first (test profile)
4. Review transaction logs

---

## 📚 Additional Resources

- [Spring Boot Properties Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html)
- [HikariCP Configuration](https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby)
- [Hibernate Properties](https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html#configurations)

---

**Last Updated:** October 31, 2025