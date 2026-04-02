# Login Tracking Enhancement Guide

## 📋 Vấn đề bạn đang gặp

Từ ảnh database, tôi thấy các cột này vẫn **NULL** cho tất cả users:

```
❌ force_password_change  → NULL
❌ last_login_device      → NULL  
❌ last_login_ip          → NULL
❌ last_login_user_agent  → NULL
❌ login_count            → NULL
```

## 🔍 Nguyên nhân

Các cột này **ĐÃ TỒN TẠI** trong database (migration 05 đã thêm) nhưng **CHƯA ĐƯỢC SỬ DỤNG** trong code vì:

1. ❌ User.java entity **thiếu** các fields này
2. ❌ AuthService **không update** các fields khi login
3. ❌ Không có code để track device, IP, user agent

## ✅ Giải pháp (3 bước)

### Bước 1: Thêm cột mới vào database

Chạy migration mới để thêm các tracking fields:

```bash
# Copy file migration
cp 07-add-login-tracking-fields.sql sql_init/

# Chạy migration
docker exec -i mysql mysql -uroot -proot restaurant < sql_init/07-add-login-tracking-fields.sql
```

### Bước 2: Update User.java entity

Replace file `User.java` với version mới có đầy đủ tracking fields:

**File mới:** `User_COMPLETE.java`

**Các fields mới được thêm:**
```java
// Force password change flag
@Column(name = "force_password_change")
private Boolean forcePasswordChange = false;

// Last login IP address
@Column(name = "last_login_ip", length = 45)
private String lastLoginIp;

// Device name/type from last login
@Column(name = "last_login_device", length = 255)
private String lastLoginDevice;

// Full user agent string
@Column(name = "last_login_user_agent", length = 500)
private String lastLoginUserAgent;

// Total login count
@Column(name = "login_count")
private Integer loginCount = 0;
```

**Các methods mới:**
```java
// Enhanced successful login recording
public void recordSuccessfulLogin(String ipAddress, String device, String userAgent) {
    this.failedLoginAttempts = 0;
    this.accountLockedUntil = null;
    this.lastLoginAt = LocalDateTime.now();
    this.lastLoginIp = ipAddress;           // ✅ NEW
    this.lastLoginDevice = device;          // ✅ NEW
    this.lastLoginUserAgent = userAgent;    // ✅ NEW
    this.loginCount = (this.loginCount == null ? 0 : this.loginCount) + 1; // ✅ NEW
}

// Force password change
public void requirePasswordChange() {
    this.forcePasswordChange = true;
}

public boolean needsPasswordChange() {
    return Boolean.TRUE.equals(this.forcePasswordChange);
}
```

### Bước 3: Update AuthServiceImpl

Update method `login()` để track device info:

```java
@Override
public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
    User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new RuntimeException("Invalid credentials"));

    // ... security checks ...

    // ✅ NEW: Extract tracking information
    String ipAddress = requestHelper.getCurrentIpAddress();
    String device = extractDeviceInfo(httpRequest);
    String userAgent = requestHelper.getUserAgent();

    // ✅ NEW: Record login with tracking
    user.recordSuccessfulLogin(ipAddress, device, userAgent);
    userRepository.save(user);

    // ✅ NEW: Enhanced logging
    log.info("✅ User logged in: {} | IP: {} | Device: {} | Login #{}",
            user.getUsername(), ipAddress, device, user.getLoginCount());

    // ✅ NEW: Check password change requirement
    boolean requiresPasswordChange = user.needsPasswordChange() || 
                                    user.isPasswordExpired(90);

    return AuthResponse.builder()
            // ... existing fields ...
            .requiresPasswordChange(requiresPasswordChange)  // ✅ NEW
            .lastLoginAt(user.getLastLoginAt())              // ✅ NEW
            .build();
}
```

## 📊 Kết quả sau khi apply

### Trước (hiện tại):
```sql
SELECT username, last_login_at, last_login_ip, last_login_device, login_count
FROM users;

-- Output:
-- admin  | 2026-02-12 03:51:43 | NULL | NULL | NULL
-- user1  | NULL                | NULL | NULL | NULL
```

### Sau khi update:
```sql
SELECT username, last_login_at, last_login_ip, last_login_device, login_count
FROM users;

-- Output:
-- admin  | 2026-02-12 03:51:43 | 192.168.1.100 | Desktop Chrome | 156
-- user1  | 2026-02-11 15:30:00 | 10.0.0.25     | Mobile iPhone  | 23
-- user2  | 2026-02-10 09:15:22 | 172.16.0.50   | Desktop Firefox| 8
```

## 🧪 Testing

### Test 1: Login và check tracking

```bash
# 1. Login qua API
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'

# 2. Check database
mysql -h localhost -P 3306 -uroot -proot restaurant -e "
  SELECT username, last_login_at, last_login_ip, last_login_device, login_count
  FROM users 
  WHERE username = 'admin';
"

# Expected output:
# ✅ last_login_ip: 127.0.0.1 hoặc Docker container IP
# ✅ last_login_device: Desktop Chrome/Firefox/etc
# ✅ login_count: Increased by 1
```

### Test 2: Check statistics

```sql
-- Get login tracking statistics
CALL get_login_tracking_stats();

-- Expected output:
-- total_users: 10
-- unique_ips: 5
-- unique_devices: 3
-- total_logins: 234
-- avg_logins_per_user: 23.40
-- active_24h: 5
-- active_7d: 8
-- never_logged_in: 2
```

### Test 3: View user activity

```sql
-- View login activity for all users
SELECT * FROM v_user_login_activity;

-- Output shows:
-- username | last_login_at | last_login_ip | device | login_count | activity_status
-- admin    | 2026-02-12... | 192.168.1.100 | Chrome | 156         | Active (24h)
-- user1    | 2026-02-10... | 10.0.0.25     | iPhone | 23          | Active (7d)
```

### Test 4: Find suspicious activity

```sql
-- Find users with high failed attempts or suspicious logins
CALL find_suspicious_logins();
```

## 📁 Files cần update

### 1. Database Migration
```
sql_init/07-add-login-tracking-fields.sql
```

### 2. Java Entity
```
backend-service/src/main/java/com/management/restaurant/model/User.java
→ Replace with User_COMPLETE.java
```

### 3. Service Layer
```
backend-service/src/main/java/com/management/restaurant/service/implement/AuthServiceImpl.java
→ Update login() method với tracking code
```

### 4. DTOs (optional - already have in AuthResponse)
```java
// AuthResponse.java already has:
private Boolean requiresPasswordChange;
private LocalDateTime lastLoginAt;
// No changes needed!
```

## 🔧 Helper Class: RequestHelper

Nếu chưa có class này, tạo mới:

```java
package com.management.restaurant.analytics.help;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@RequiredArgsConstructor
public class RequestHelper {
    
    /**
     * Get current IP address from request
     */
    public String getCurrentIpAddress() {
        ServletRequestAttributes attributes = 
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        
        if (attributes == null) {
            return "Unknown";
        }
        
        HttpServletRequest request = attributes.getRequest();
        
        // Check for proxy headers first
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // If multiple IPs, get first one
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip != null ? ip : "Unknown";
    }
    
    /**
     * Get User Agent string
     */
    public String getUserAgent() {
        ServletRequestAttributes attributes = 
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        
        if (attributes == null) {
            return "Unknown";
        }
        
        HttpServletRequest request = attributes.getRequest();
        String userAgent = request.getHeader("User-Agent");
        
        return userAgent != null ? userAgent : "Unknown";
    }
    
    /**
     * Get simplified device info from User Agent
     */
    public String getDeviceInfoString() {
        String userAgent = getUserAgent();
        
        if (userAgent.contains("Mobile") || userAgent.contains("Android")) {
            if (userAgent.contains("Android")) return "Mobile Android";
            if (userAgent.contains("iPhone")) return "Mobile iPhone";
            return "Mobile Device";
        }
        
        if (userAgent.contains("Chrome")) return "Desktop Chrome";
        if (userAgent.contains("Firefox")) return "Desktop Firefox";
        if (userAgent.contains("Safari")) return "Desktop Safari";
        if (userAgent.contains("Edge")) return "Desktop Edge";
        
        return "Unknown Browser";
    }
    
    /**
     * Extract device name from request
     */
    public String extractDeviceName(HttpServletRequest request) {
        if (request == null) {
            return "Unknown Device";
        }
        
        String userAgent = request.getHeader("User-Agent");
        return getDeviceInfoFromUserAgent(userAgent);
    }
    
    private String getDeviceInfoFromUserAgent(String userAgent) {
        if (userAgent == null) {
            return "Unknown";
        }
        
        // Mobile devices
        if (userAgent.contains("iPhone")) return "iPhone";
        if (userAgent.contains("iPad")) return "iPad";
        if (userAgent.contains("Android")) return "Android";
        
        // Desktop browsers
        if (userAgent.contains("Windows")) return "Windows PC";
        if (userAgent.contains("Mac")) return "Mac";
        if (userAgent.contains("Linux")) return "Linux";
        
        return "Unknown Device";
    }
}
```

## 💡 Best Practices

### 1. Privacy & GDPR Compliance

```java
// Mask IP addresses in logs
private String maskIp(String ip) {
    if (ip == null || !ip.contains(".")) {
        return ip;
    }
    
    String[] parts = ip.split("\\.");
    if (parts.length == 4) {
        return parts[0] + "." + parts[1] + ".***." + "***";
    }
    
    return ip;
}

// Use in logs
log.info("Login from IP: {}", maskIp(ipAddress));
```

### 2. Geolocation (Optional)

Để có country/city info, có thể dùng:
- MaxMind GeoIP2
- IP2Location
- ipapi.co API

```java
// Example with ipapi.co
public String getLocationFromIp(String ip) {
    try {
        String url = "https://ipapi.co/" + ip + "/json/";
        // Call API and parse response
        return "Vietnam, Ho Chi Minh City";
    } catch (Exception e) {
        return "Unknown Location";
    }
}
```

### 3. Device Fingerprinting (Advanced)

Để tracking device chính xác hơn:
- Generate device fingerprint từ browser
- Store fingerprint ID in database
- Detect new devices và alert user

## ✅ Checklist

- [ ] Run migration: 07-add-login-tracking-fields.sql
- [ ] Update User.java với tracking fields
- [ ] Update AuthServiceImpl login() method
- [ ] Add/verify RequestHelper class exists
- [ ] Test login và verify database updates
- [ ] Check logs show IP and device info
- [ ] Run statistics queries
- [ ] Verify AuthResponse includes new fields
- [ ] Test on different devices/browsers

## 📊 Monitoring Queries

```sql
-- Most active users (by login count)
SELECT username, login_count, last_login_at
FROM users
ORDER BY login_count DESC
LIMIT 10;

-- Users by device type
SELECT last_login_device, COUNT(*) as count
FROM users
WHERE last_login_device IS NOT NULL
GROUP BY last_login_device
ORDER BY count DESC;

-- Login activity in last 24 hours
SELECT username, last_login_at, last_login_ip, last_login_device
FROM users
WHERE last_login_at > DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 24 HOUR)
ORDER BY last_login_at DESC;

-- Users needing password change
SELECT username, email, force_password_change, password_changed_at
FROM users
WHERE force_password_change = TRUE
   OR password_changed_at < DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 90 DAY);
```

---

**All files ready in outputs!** 🚀
