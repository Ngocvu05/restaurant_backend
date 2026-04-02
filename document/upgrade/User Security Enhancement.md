# User Security Enhancement - Complete Guide

## 📋 Overview

Comprehensive upgrade to User model and services adding enterprise-grade security features including account locking, password reset, email verification, and two-factor authentication.

## 🎯 Features Added

### 1. **Failed Login Protection**
- ✅ Track consecutive failed login attempts
- ✅ Auto-lock account after N failed attempts (default: 5)
- ✅ Configurable lock duration (default: 30 minutes)
- ✅ Auto-unlock when lock period expires
- ✅ Manual unlock by admin

### 2. **Password Management**
- ✅ Password reset via email token
- ✅ Configurable token expiry (default: 24 hours)
- ✅ Password change with old password verification
- ✅ Password rotation tracking
- ✅ Password expiry policy support
- ✅ Revoke all tokens on password change

### 3. **Email Verification**
- ✅ Email verification on registration
- ✅ Verification token with expiry (default: 48 hours)
- ✅ Resend verification email
- ✅ Track verification status
- ✅ Prevent actions until email verified (optional)

### 4. **Two-Factor Authentication (2FA)**
- ✅ TOTP-based 2FA (Google Authenticator compatible)
- ✅ Backup codes for account recovery
- ✅ Enable/disable 2FA
- ✅ Track 2FA usage

### 5. **Security Audit Trail**
- ✅ Last login timestamp
- ✅ Failed login attempt count
- ✅ Account lock/unlock history
- ✅ Password change tracking
- ✅ Email verification status

### 6. **Caching Strategy**
- ✅ L1 (Caffeine) + L2 (Redis) caching
- ✅ Cache by ID, username, email
- ✅ Smart cache eviction on updates
- ✅ Cache warming for frequent queries

## 📊 Database Schema Changes

### New Columns Added to `users` Table:

| Column | Type | Default | Description |
|--------|------|---------|-------------|
| `failed_login_attempts` | INTEGER | 0 | Count of failed logins |
| `account_locked_until` | TIMESTAMP | NULL | Lock expiry time |
| `reset_password_token` | VARCHAR(100) | NULL | Password reset token |
| `reset_token_expiry` | TIMESTAMP | NULL | Token expiry time |
| `email_verified` | BOOLEAN | FALSE | Email verification status |
| `email_verification_token` | VARCHAR(100) | NULL | Email verification token |
| `email_verification_expiry` | TIMESTAMP | NULL | Verification expiry |
| `two_factor_enabled` | BOOLEAN | FALSE | 2FA enabled status |
| `two_factor_secret` | VARCHAR(32) | NULL | TOTP secret key |
| `backup_codes` | VARCHAR(500) | NULL | 2FA backup codes |
| `last_login_at` | TIMESTAMP | NULL | Last login time |
| `password_changed_at` | TIMESTAMP | NULL | Last password change |

## 🚀 Implementation Steps

### Step 1: Run Database Migration

```sql
-- Run the migration script
psql -U your_user -d your_database -f V2__add_user_security_fields.sql
```

Or with Flyway:
```bash
# Place script in db/migration/ folder
# Flyway will auto-run on startup
```

### Step 2: Update Entity Classes

Replace existing files:
- `User.java` → `User_upgraded.java`
- `AuthServiceImpl.java` → `AuthServiceImpl_upgraded.java`
- `UserServiceImpl.java` → `UserServiceImpl_upgraded.java`

### Step 3: Add New DTOs

Add new DTO classes:
- `UserSecurityDTO`
- `ResetPasswordRequest`
- `ChangePasswordRequest`
- `ForgotPasswordRequest`
- `EmailVerificationRequest`
- `LockAccountRequest`
- `SecurityActionResponse`

### Step 4: Update Repository

Replace `UserRepository.java` with enhanced version containing new query methods.

### Step 5: Add Email Service

Add `EmailService` interface and implementation for sending emails.

Configure in `application.yml`:

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${EMAIL_USERNAME}
    password: ${EMAIL_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

### Step 6: Update Controller

Add new endpoints to `UserController` for security operations.

### Step 7: Configure Security Settings

Add to `application.yml`:

```yaml
app:
  security:
    max-login-attempts: 5
    account-lock-duration-minutes: 30
    password-reset-expiry-hours: 24
    email-verification-expiry-hours: 48
    password-max-age-days: 90  # Optional password rotation
  
  frontend:
    url: http://localhost:3000  # For email links
```

## 🔐 API Endpoints

### Authentication

```
POST   /api/auth/register              # Register with email verification
POST   /api/auth/login                 # Login with security checks
POST   /api/auth/logout                # Logout and revoke tokens
POST   /api/auth/refresh               # Refresh access token
```

### Password Management

```
POST   /api/users/forgot-password      # Request password reset
POST   /api/users/reset-password       # Reset password with token
POST   /api/users/change-password      # Change password (authenticated)
```

### Email Verification

```
POST   /api/users/verify-email         # Verify email with token
POST   /api/users/resend-verification  # Resend verification email
```

### Security Management (Admin)

```
GET    /api/users/{id}/security        # Get security info
POST   /api/users/{id}/lock            # Lock account
POST   /api/users/{id}/unlock          # Unlock account
POST   /api/users/{id}/reset-failed-attempts  # Reset failed login count
GET    /api/users/locked               # List locked accounts
GET    /api/users/unverified-emails    # List unverified emails
```

## 📖 Usage Examples

### 1. Register with Email Verification

```java
// Registration automatically sends verification email
RegisterRequest request = new RegisterRequest();
request.setUsername("john_doe");
request.setPassword("SecurePass123!");
request.setEmail("john@example.com");
request.setFullName("John Doe");

AuthResponse response = authService.register(request, null);
// User receives email with verification link
```

### 2. Login with Failed Attempt Protection

```java
// Login attempt
LoginRequest loginRequest = new LoginRequest();
loginRequest.setUsername("john_doe");
loginRequest.setPassword("wrong_password");

try {
    authService.login(loginRequest);
} catch (RuntimeException e) {
    // After 5 failed attempts:
    // "Account locked due to too many failed login attempts. 
    //  Please try again in 30 minutes."
}
```

### 3. Password Reset Flow

```java
// Step 1: Request reset
authService.requestPasswordReset("john@example.com");
// Email sent with reset link

// Step 2: Reset with token (from email)
authService.resetPassword(token, "NewSecurePass123!");
// Password updated, all tokens revoked
```

### 4. Admin Lock Account

```java
// Lock for 60 minutes
userService.lockUserAccount(userId, 60, "Suspicious activity");

// Lock indefinitely (0 = no expiry)
userService.lockUserAccount(userId, 0, "Manual review required");
```

### 5. Check Security Status

```java
UserSecurityDTO security = userService.getUserSecurityInfo(userId);

System.out.println("Failed attempts: " + security.getFailedLoginAttempts());
System.out.println("Is locked: " + security.getIsAccountLocked());
System.out.println("Locked until: " + security.getAccountLockedUntil());
System.out.println("Email verified: " + security.getEmailVerified());
System.out.println("2FA enabled: " + security.getTwoFactorEnabled());
```

## 🔒 Security Best Practices

### 1. Password Reset
- ✅ Tokens are single-use
- ✅ Tokens expire after 24 hours
- ✅ All sessions revoked on password reset
- ✅ Email confirmation required

### 2. Account Locking
- ✅ Auto-locks after 5 failed attempts
- ✅ 30-minute lock duration (configurable)
- ✅ Auto-unlocks after expiry
- ✅ Manual unlock by admin

### 3. Email Verification
- ✅ Required before certain actions
- ✅ Tokens expire after 48 hours
- ✅ Can resend if needed
- ✅ Tracked in audit trail

### 4. Token Management
- ✅ Refresh tokens rotated on use
- ✅ All tokens revoked on:
    - Password change
    - Account lock
    - Admin action
- ✅ Device and IP tracking

## 📊 Monitoring & Maintenance

### 1. Scheduled Cleanup Task

```java
@Scheduled(cron = "0 2 * * *")  // Run at 2 AM daily
public void cleanupExpiredTokens() {
    jdbcTemplate.update("SELECT cleanup_expired_tokens()");
    log.info("Cleaned up expired tokens");
}
```

### 2. Security Metrics

Monitor these metrics:
- Failed login rate
- Locked accounts count
- Unverified email count
- Password reset requests
- 2FA adoption rate

### 3. Admin Dashboard Queries

```sql
-- Locked accounts
SELECT username, account_locked_until, failed_login_attempts
FROM users 
WHERE account_locked_until > CURRENT_TIMESTAMP;

-- Unverified emails
SELECT username, email, created_at
FROM users 
WHERE email_verified = FALSE 
  AND created_at < CURRENT_TIMESTAMP - INTERVAL '7 days';

-- Users with high failed attempts
SELECT username, failed_login_attempts, last_login_at
FROM users 
WHERE failed_login_attempts >= 3
ORDER BY failed_login_attempts DESC;

-- Password reset activity
SELECT COUNT(*) as reset_requests, DATE(created_at) as date
FROM users 
WHERE reset_password_token IS NOT NULL
GROUP BY DATE(created_at)
ORDER BY date DESC;
```

## 🎯 Configuration Options

### Security Settings

```yaml
app:
  security:
    # Login Protection
    max-login-attempts: 5
    account-lock-duration-minutes: 30
    
    # Token Expiry
    password-reset-expiry-hours: 24
    email-verification-expiry-hours: 48
    refresh-token-ttl-days: 7
    
    # Password Policy
    password-min-length: 8
    password-require-uppercase: true
    password-require-lowercase: true
    password-require-digit: true
    password-require-special: true
    password-max-age-days: 90  # 0 = no expiry
    
    # 2FA
    two-factor-issuer: "YourApp"
    backup-codes-count: 10
```

### Email Templates

Customize in `EmailService`:
- Verification email
- Password reset email
- Account locked notification
- Password changed notification

## 🐛 Troubleshooting

### Issue: Emails not sending

```yaml
# Check email config
spring:
  mail:
    host: smtp.gmail.com
    username: ${EMAIL_USERNAME}  # Check env variable
    password: ${EMAIL_PASSWORD}  # Check env variable
    
# For Gmail, enable "Less secure app access" or use App Password
```

### Issue: Cache not working

```yaml
# Verify Redis connection
spring:
  data:
    redis:
      host: localhost
      port: 6379

# Check cache manager beans
@Bean(name = "redisCacheManager")
@Primary
public CacheManager redisCacheManager(...)
```

### Issue: Tokens not expiring

```sql
-- Check token cleanup function
SELECT cleanup_expired_tokens();

-- Verify cron job (if using pg_cron)
SELECT * FROM cron.job WHERE jobname = 'cleanup-expired-tokens';
```

## 📈 Performance Optimization

### 1. Database Indexes

All necessary indexes are created by migration script:
- `idx_users_reset_token`
- `idx_users_verification_token`
- `idx_users_email`
- `idx_users_locked`
- `idx_users_last_login`
- `idx_users_email_verified`

### 2. Caching Strategy

```java
// High-frequency: L1 (Caffeine)
@Cacheable(value = "users", cacheManager = "caffeineCacheManager")

// Medium-frequency: L2 (Redis)
@Cacheable(value = "users", cacheManager = "redisCacheManager")

// Cache eviction on updates
@CacheEvict(value = "users", key = "'id:' + #userId")
```

### 3. Async Operations

```java
// Email sending is async
@Async
public void sendEmail(...)

// Token cleanup is scheduled
@Scheduled(cron = "0 2 * * *")
public void cleanupExpiredTokens()
```

## ✅ Testing Checklist

- [ ] Register new user → Email verification sent
- [ ] Verify email with token → Status updated
- [ ] Login with wrong password 5 times → Account locked
- [ ] Wait 30 minutes → Account auto-unlocked
- [ ] Request password reset → Email sent with token
- [ ] Reset password → Old tokens revoked
- [ ] Change password → Old tokens revoked
- [ ] Admin lock account → User cannot login
- [ ] Admin unlock account → User can login
- [ ] Cache hit on repeated queries
- [ ] Cache eviction on user update

## 🔗 Related Documentation

- [Spring Security](https://spring.io/projects/spring-security)
- [Spring Mail](https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#mail)
- [Spring Cache](https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#cache)
- [JWT Authentication](https://jwt.io/)
- [TOTP (2FA)](https://tools.ietf.org/html/rfc6238)

## 📞 Support

For issues or questions:
- Check logs for detailed error messages
- Verify database migration ran successfully
- Ensure email configuration is correct
- Check Redis connection for caching
- Review security settings in application.yml