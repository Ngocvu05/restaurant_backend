# SQL Migration Files Explanation

## 📋 Overview

Hai file migration SQL này bổ sung các tính năng security và OAuth2 cho hệ thống User Management:

1. **05-add-user-security-fields.sql** - User Security Features
2. **06-add-oauth2-links.sql** - OAuth2 Multi-Provider Support

---

## 📁 File 1: 05-add-user-security-fields.sql

### 🎯 Tác dụng:

Thêm **12 cột mới** vào bảng `users` để hỗ trợ các tính năng bảo mật:

### ✅ Tính năng được thêm:

#### 1. **Failed Login Protection** (Chống brute force)
```sql
failed_login_attempts INTEGER     -- Đếm số lần đăng nhập thất bại
account_locked_until TIMESTAMP    -- Khóa tài khoản đến khi nào
```

**Cách hoạt động:**
- Mỗi lần đăng nhập sai → tăng `failed_login_attempts`
- Sau 5 lần sai → tự động khóa tài khoản 30 phút
- Đăng nhập thành công → reset về 0

**Ví dụ:**
```java
// User nhập sai password lần 5
user.recordFailedLogin(5, 30); // Max 5 attempts, lock 30 minutes
// → account_locked_until = current_time + 30 minutes
```

#### 2. **Password Reset** (Quên mật khẩu)
```sql
reset_password_token VARCHAR(100)    -- Token để reset password
reset_token_expiry TIMESTAMP         -- Hết hạn sau 24 giờ
```

**Cách hoạt động:**
- User click "Forgot Password"
- Hệ thống tạo random token, gửi email
- Token có hiệu lực 24 giờ
- User click link → reset password

**Ví dụ:**
```java
user.setPasswordResetToken(UUID.randomUUID().toString(), 24); // 24 hours
emailService.sendPasswordResetEmail(user, token);
```

#### 3. **Email Verification** (Xác thực email)
```sql
email_verified BOOLEAN                    -- Đã verify chưa?
email_verification_token VARCHAR(100)     -- Token để verify
email_verification_expiry TIMESTAMP       -- Hết hạn sau 48 giờ
```

**Cách hoạt động:**
- User đăng ký → nhận email verification
- Click link trong email → verify thành công
- `email_verified` = TRUE

**Ví dụ:**
```java
user.setEmailVerificationToken(token, 48); // 48 hours
emailService.sendVerificationEmail(user, token);
```

#### 4. **Two-Factor Authentication (2FA)**
```sql
two_factor_enabled BOOLEAN        -- Có bật 2FA không?
two_factor_secret VARCHAR(32)     -- Secret key cho Google Authenticator
backup_codes VARCHAR(500)         -- Backup codes để recover
```

**Cách hoạt động:**
- User enable 2FA → nhận QR code (Google Authenticator)
- Mỗi lần login → nhập 6-digit code
- Có backup codes nếu mất điện thoại

#### 5. **Login Tracking** (Theo dõi đăng nhập)
```sql
last_login_at TIMESTAMP           -- Lần đăng nhập cuối
password_changed_at TIMESTAMP     -- Lần đổi password cuối
```

**Tác dụng:**
- Audit trail (biết user login khi nào)
- Phát hiện tài khoản bị hack
- Password rotation policy (bắt đổi pass sau 90 ngày)

### 📊 Stored Procedures Created:

```sql
-- Tự động xóa token hết hạn
CALL cleanup_expired_tokens();

-- Xem thống kê bảo mật
CALL get_user_security_stats();
```

### 🔄 Auto Cleanup Event:

```sql
-- Tự động chạy cleanup mỗi ngày lúc 2 giờ sáng
CREATE EVENT cleanup_expired_tokens_daily
ON SCHEDULE EVERY 1 DAY
STARTS (... + INTERVAL 2 HOUR)
```

---

## 📁 File 2: 06-add-oauth2-links.sql

### 🎯 Tác dụng:

Tạo bảng mới `oauth2_links` để cho phép user **link nhiều OAuth2 providers** (Google, Facebook, GitHub, Apple...) vào 1 tài khoản.

### ✅ Tính năng được thêm:

#### 1. **Multi-Provider Authentication**

User có thể đăng nhập bằng:
- ✅ Google
- ✅ Facebook  
- ✅ GitHub
- ✅ Apple
- ✅ Twitter/X
- ✅ LinkedIn
- ✅ ...và nhiều provider khác

**Ví dụ:**
```
User "john_doe" có thể link:
- Google: john@gmail.com
- Facebook: john@fb.com
- GitHub: john_github
→ Đăng nhập bằng bất kỳ provider nào đều vào cùng 1 tài khoản!
```

#### 2. **Provider Information Storage**

Bảng `oauth2_links` lưu:
```sql
provider VARCHAR(50)              -- Tên provider (google, facebook...)
provider_user_id VARCHAR(255)     -- ID của user ở provider
provider_email VARCHAR(255)       -- Email từ provider
provider_display_name VARCHAR(255) -- Tên hiển thị
provider_picture_url VARCHAR(500)  -- Avatar URL
```

#### 3. **Token Management**

```sql
access_token VARCHAR(1000)        -- OAuth2 access token
refresh_token VARCHAR(1000)       -- Refresh token
token_expires_at TIMESTAMP        -- Thời hạn token
scopes VARCHAR(500)               -- Quyền được cấp
```

**Tác dụng:**
- Lưu token để call API của provider (nếu cần)
- Tự động cleanup token hết hạn
- Security: nên encrypt token trước khi lưu

#### 4. **Usage Tracking**

```sql
linked_at TIMESTAMP               -- Khi nào link provider này
last_used_at TIMESTAMP            -- Lần cuối login bằng provider này
is_primary BOOLEAN                -- Provider chính (default login)
```

**Ví dụ:**
```
User có 3 providers:
- Google (is_primary=true, last_used: hôm nay)
- Facebook (is_primary=false, last_used: 30 ngày trước)
- GitHub (is_primary=false, last_used: chưa dùng)
```

#### 5. **Soft Delete Support**

```sql
deleted_at TIMESTAMP              -- Soft delete khi unlink
deleted_by VARCHAR(255)           -- Ai unlink
```

**Tác dụng:**
- Unlink provider → không xóa hẳn, chỉ đánh dấu deleted
- Có thể restore nếu cần
- Giữ audit trail

### 📊 Stored Procedures Created:

```sql
-- Thống kê OAuth2 usage
CALL get_oauth2_stats();
-- Output:
-- provider | user_count | total_links | avg_links_per_user
-- google   | 1500       | 1520        | 1.01
-- facebook | 800        | 810         | 1.01

-- Xem users link nhiều providers
CALL get_multi_provider_users();
-- Output: Users có 2+ providers

-- Tìm OAuth2 links không active
CALL get_inactive_oauth2_links(90); // 90 days

-- Set primary provider
CALL set_primary_oauth2_provider(user_id, 'google');

-- Cleanup expired tokens
CALL cleanup_expired_oauth2_tokens();
```

### 📈 Views Created:

```sql
-- View active OAuth2 links
SELECT * FROM v_active_oauth2_links;

-- View provider statistics
SELECT * FROM v_oauth2_provider_stats;
```

---

## 🔗 Mối quan hệ giữa 2 files:

```
users table (với security fields từ file 05)
    ↓ (1 user can have many OAuth2 links)
oauth2_links table (từ file 06)
```

**Ví dụ thực tế:**

```sql
-- User table (after file 05)
users:
  id: 1
  username: john_doe
  email: john@example.com
  email_verified: TRUE              ← From file 05
  two_factor_enabled: TRUE          ← From file 05
  last_login_at: 2026-01-05 10:30  ← From file 05

-- OAuth2 links table (from file 06)
oauth2_links:
  Row 1:
    user_id: 1
    provider: google
    provider_email: john@gmail.com
    is_primary: TRUE
    
  Row 2:
    user_id: 1
    provider: facebook
    provider_email: john@fb.com
    is_primary: FALSE
```

**User này có thể:**
- ✅ Login bằng Google (primary)
- ✅ Login bằng Facebook
- ✅ Login bằng username/password (nếu có set)
- ✅ Có 2FA protection
- ✅ Email đã verified

---

## 🚀 Cách sử dụng:

### Installation:

```bash
# 1. Copy files to sql_init folder
cp 05-add-user-security-fields.sql sql_init/
cp 06-add-oauth2-links.sql sql_init/

# 2. If fresh database (will auto-run)
docker-compose up -d mysql

# 3. If existing database (run manually)
docker exec -i mysql mysql -uroot -proot restaurant < sql_init/05-add-user-security-fields.sql
docker exec -i mysql mysql -uroot -proot restaurant < sql_init/06-add-oauth2-links.sql
```

### Verification:

```bash
# Connect to MySQL
docker exec -it mysql mysql -uroot -proot restaurant

# Check users table has new columns
DESCRIBE users;

# Check oauth2_links table exists
DESCRIBE oauth2_links;

# View statistics
CALL get_user_security_stats();
CALL get_oauth2_stats();
```

---

## 💡 Use Cases:

### Use Case 1: Password Reset Flow

```
User clicks "Forgot Password"
  ↓
Backend: user.setPasswordResetToken(token, 24)
  ↓
Email sent with link: /reset-password?token=abc123
  ↓
User clicks link (within 24 hours)
  ↓
Backend: user.isResetTokenValid(token) → TRUE
  ↓
User enters new password
  ↓
Backend: user.updatePassword(newPass)
  ↓
reset_password_token cleared
  ↓
All refresh tokens revoked
```

### Use Case 2: OAuth2 Login

```
User clicks "Login with Google"
  ↓
Google authentication
  ↓
Backend receives Google user data
  ↓
Check: oauth2_links where provider='google' AND provider_user_id='123'
  ↓
If found: Login user
If not: Create oauth2_link → Link to existing user or create new user
  ↓
Update: last_used_at = now
  ↓
Generate JWT tokens
  ↓
User logged in
```

### Use Case 3: Link Additional Provider

```
User already logged in with Google
  ↓
User clicks "Link Facebook"
  ↓
Facebook authentication
  ↓
Backend: Check email matches
  ↓
Create new oauth2_link:
  user_id: 1
  provider: facebook
  provider_user_id: fb_123
  is_primary: FALSE
  ↓
User can now login with either Google or Facebook
```

---

## 🔒 Security Benefits:

### From File 05 (User Security):
- ✅ Brute force protection (auto-lock after failed attempts)
- ✅ Secure password reset (token-based, time-limited)
- ✅ Email verification (prevent fake accounts)
- ✅ 2FA support (extra security layer)
- ✅ Login tracking (detect unauthorized access)
- ✅ Password rotation (enforce periodic changes)

### From File 06 (OAuth2):
- ✅ No password needed (use trusted providers)
- ✅ Multi-provider support (flexibility)
- ✅ Provider email auto-verified (Google/Facebook already verified)
- ✅ Token management (secure API access)
- ✅ Audit trail (track which provider used)
- ✅ Soft delete (can restore if needed)

---

## 📊 Statistics & Monitoring:

### Security Stats:
```sql
CALL get_user_security_stats();
-- Shows:
-- - Total users
-- - Verified emails
-- - Users with 2FA
-- - Locked accounts
-- - High failed attempts
-- - Never logged in
-- - Inactive 90+ days
```

### OAuth2 Stats:
```sql
CALL get_oauth2_stats();
-- Shows per provider:
-- - User count
-- - Total links
-- - Average links per user
-- - Primary count
-- - Active in last 30 days
```

---

## ✅ Summary:

| Feature | File 05 | File 06 |
|---------|---------|---------|
| **Failed Login Protection** | ✅ | - |
| **Account Locking** | ✅ | - |
| **Password Reset** | ✅ | - |
| **Email Verification** | ✅ | - |
| **Two-Factor Auth (2FA)** | ✅ | - |
| **Login Tracking** | ✅ | - |
| **OAuth2 Multi-Provider** | - | ✅ |
| **Social Login** | - | ✅ |
| **Token Management** | - | ✅ |
| **Provider Linking** | - | ✅ |

**Kết hợp cả 2 = Full-featured User Management System!** 🚀
