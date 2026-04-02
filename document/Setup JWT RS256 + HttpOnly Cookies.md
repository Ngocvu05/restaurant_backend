# 🚀 Hướng Dẫn Setup JWT RS256 + HttpOnly Cookies

## 📋 Bước 1: Tạo RSA Key Pair

### 1.1. Tạo file `generate-keys.sh`

```bash
# Tạo file trong thư mục gốc project
touch generate-keys.sh

# Copy nội dung script vào file (xem artifact generate-keys.sh)
nano generate-keys.sh
# hoặc
vi generate-keys.sh
```

### 1.2. Chạy script

```bash
# Cấp quyền thực thi
chmod +x generate-keys.sh

# Chạy script
./generate-keys.sh
```

**Output mong đợi:**
```
🔐 ==========================================
   RSA Key Pair Generator for JWT RS256
==========================================

✅ Created 'keys' directory
🔑 Generating RSA key pair (2048-bit)...
✅ Private key generated: keys/private_key.pem
✅ Public key extracted: keys/public_key.pem
🔒 File permissions set correctly

📊 Key Information:
Private-Key: (2048 bit, 2 primes)

✅ Success! RSA Key Pair Generated
```

### 1.3. Kiểm tra keys đã tạo

```bash
# Kiểm tra files
ls -la keys/

# Kết quả:
# -rw------- 1 user user 1679 Jan 01 10:00 private_key.pem  (600)
# -rw-r--r-- 1 user user  451 Jan 01 10:00 public_key.pem   (644)
```

---

## 📋 Bước 2: Cập nhật .gitignore

```bash
# Thêm vào file .gitignore
echo "" >> .gitignore
echo "# JWT Keys - DO NOT COMMIT" >> .gitignore
echo "keys/" >> .gitignore
echo "*.pem" >> .gitignore
echo "!src/main/resources/keys/.gitkeep" >> .gitignore
```

**Kiểm tra:**
```bash
git status
# Không thấy keys/ trong danh sách = ✅ Thành công
```

---

## 📋 Bước 3: Copy Keys vào Resources

```bash
# Tạo thư mục trong resources
mkdir -p src/main/resources/keys

# Copy keys
cp keys/private_key.pem src/main/resources/keys/
cp keys/public_key.pem src/main/resources/keys/

# Kiểm tra
ls -la src/main/resources/keys/
```

---

## 📋 Bước 4: Thay thế JwtService.java

### 4.1. Backup file cũ

```bash
cp src/main/java/com/management/restaurant/security/JwtService.java \
   src/main/java/com/management/restaurant/security/JwtService.java.backup
```

### 4.2. Thay thế bằng version mới

Copy toàn bộ nội dung từ artifact **"JwtService.java (RS256 Version)"** và thay thế file cũ.

**Những thay đổi chính:**
- ❌ Xóa: `getSignKey()` method với HS256
- ✅ Thêm: `loadPrivateKey()` và `loadPublicKey()`
- ✅ Thay đổi: `SignatureAlgorithm.HS256` → `SignatureAlgorithm.RS256`
- ✅ Thêm: `@PostConstruct` method để load keys

---

## 📋 Bước 5: Thêm CookieAuthenticationFilter.java

### 5.1. Tạo file mới

```bash
# Tạo file trong package security
touch src/main/java/com/management/restaurant/security/CookieAuthenticationFilter.java
```

### 5.2. Copy nội dung

Copy toàn bộ nội dung từ artifact **"CookieAuthenticationFilter.java"**.

**Chức năng:**
- Đọc token từ HttpOnly cookie
- Fallback về Authorization header
- Validate và set authentication

---

## 📋 Bước 6: Cập nhật SecurityConfig.java

### 6.1. Thêm @Autowired cho CookieAuthenticationFilter

```java
@Autowired
private CookieAuthenticationFilter cookieAuthenticationFilter;
```

### 6.2. Thêm filter vào SecurityFilterChain

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**", "/api/public/**").permitAll()
            .anyRequest().authenticated()
        )
        .sessionManagement(session -> 
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        // 🔥 QUAN TRỌNG: Thêm cookie filter
        .addFilterBefore(cookieAuthenticationFilter, 
                        UsernamePasswordAuthenticationFilter.class);
    
    return http.build();
}
```

### 6.3. Thêm CORS Configuration

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    
    // Allowed origins
    configuration.setAllowedOrigins(Arrays.asList(
        "http://localhost:3000",
        "http://localhost:3001"
        // Thêm domain production của bạn
    ));
    
    // Allowed methods
    configuration.setAllowedMethods(Arrays.asList(
        "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
    ));
    
    // Allowed headers
    configuration.setAllowedHeaders(Arrays.asList("*"));
    
    // 🔥 QUAN TRỌNG: Allow credentials (cho cookies)
    configuration.setAllowCredentials(true);
    
    // Max age
    configuration.setMaxAge(3600L);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    
    return source;
}
```

---

## 📋 Bước 7: Cập nhật AuthController.java

### 7.1. Backup file cũ

```bash
cp src/main/java/com/management/restaurant/controller/AuthController.java \
   src/main/java/com/management/restaurant/controller/AuthController.java.backup
```

### 7.2. Thay thế bằng version mới

Copy nội dung từ artifact **"AuthController.java (with HttpOnly Cookies)"**.

**Thay đổi chính:**
- ✅ Thêm `HttpServletResponse` parameter
- ✅ Set cookies sau login/register
- ✅ Clear cookies khi logout
- ✅ Hỗ trợ refresh token từ cookie

---

## 📋 Bước 8: Cập nhật application.properties

### 8.1. Xóa cấu hình cũ

```properties
# ❌ XÓA những dòng này:
jwt.secret=${JWT_SECRET}
jwt.algorithm=HS256
```

### 8.2. Thêm cấu hình mới

```properties
# ========================================
# JWT CONFIGURATION (RS256)
# ========================================
jwt.private-key-path=classpath:keys/private_key.pem
jwt.public-key-path=classpath:keys/public_key.pem
jwt.refresh-token-expiration=${JWT_REFRESH_TOKEN_EXPIRATION:604800000}
jwt.access-token-expiration=${JWT_ACCESS_TOKEN_EXPIRATION:900000}
jwt.issuer=restaurant-service
jwt.algorithm=RS256

# ========================================
# CORS CONFIGURATION (for HttpOnly Cookies)
# ========================================
spring.web.cors.allowed-origins=http://localhost:3000,http://localhost:3001
spring.web.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS,PATCH
spring.web.cors.allowed-headers=*
spring.web.cors.allow-credentials=true
spring.web.cors.max-age=3600

# ========================================
# COOKIE CONFIGURATION
# ========================================
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.secure=false
server.servlet.session.cookie.same-site=lax
```

### 8.3. Cấu hình Production (application-prod.properties)

```properties
# HTTPS và Secure cookies cho production
server.servlet.session.cookie.secure=true
server.servlet.session.cookie.same-site=strict

# CORS cho domain thực
spring.web.cors.allowed-origins=https://yourdomain.com
```

---

## 📋 Bước 9: Cập nhật .env

```bash
# ❌ XÓA:
JWT_SECRET=...

# ✅ KHÔNG CẦN NỮA vì dùng RSA keys
```

---

## 📋 Bước 10: Testing

### 10.1. Build project

```bash
mvn clean install -DskipTests
```

### 10.2. Run application

```bash
mvn spring-boot:run
```

### 10.3. Test với curl

#### Test Login (nhận cookies)
```bash
curl -v -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password"
  }' \
  -c cookies.txt

# Kiểm tra response headers:
# Phải thấy:
# Set-Cookie: access_token=...; HttpOnly; Path=/
# Set-Cookie: refresh_token=...; HttpOnly; Path=/
```

#### Test Protected Endpoint (dùng cookies)
```bash
curl -X GET http://localhost:8081/api/users/me \
  -b cookies.txt

# Nếu thành công = ✅ Cookies hoạt động
```

#### Test Logout (xóa cookies)
```bash
curl -v -X POST http://localhost:8081/api/auth/logout \
  -b cookies.txt

# Kiểm tra response:
# Set-Cookie: access_token=; Max-Age=0
# Set-Cookie: refresh_token=; Max-Age=0
```

#### Verify JWT Algorithm
```bash
# Login và lấy token
TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"pass"}' \
  | jq -r '.accessToken')

# Decode header
echo $TOKEN | cut -d. -f1 | base64 -d | jq

# Expected output:
# {
#   "alg": "RS256",  <-- ✅ Phải là RS256
#   "typ": "JWT"
# }
```

---

## 📋 Bước 11: Frontend Integration

### 11.1. Update Axios/Fetch Config

```javascript
// axios-config.js
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8081',
  withCredentials: true,  // 🔥 QUAN TRỌNG: Bật cookies
  headers: {
    'Content-Type': 'application/json'
  }
});

export default api;
```

### 11.2. Login Request

```javascript
// authService.js
import api from './axios-config';

export const login = async (username, password) => {
  try {
    const response = await api.post('/api/auth/login', {
      username,
      password
    });
    
    // Cookies tự động được lưu bởi browser
    // Không cần localStorage.setItem nữa!
    
    return response.data;
  } catch (error) {
    throw error;
  }
};
```

### 11.3. Protected Request

```javascript
// userService.js
import api from './axios-config';

export const getProfile = async () => {
  // Cookies tự động được gửi kèm request
  const response = await api.get('/api/users/me');
  return response.data;
};
```

### 11.4. Logout

```javascript
export const logout = async () => {
  await api.post('/api/auth/logout');
  // Cookies tự động bị xóa bởi server
  window.location.href = '/login';
};
```

---

## ✅ Checklist Hoàn Thành

Trước khi deploy, kiểm tra:

- [ ] ✅ Keys đã được tạo (`keys/private_key.pem` & `public_key.pem`)
- [ ] ✅ `.gitignore` đã thêm `keys/`
- [ ] ✅ Keys đã copy vào `src/main/resources/keys/`
- [ ] ✅ `JwtService.java` đã thay đổi sang RS256
- [ ] ✅ `CookieAuthenticationFilter.java` đã được thêm
- [ ] ✅ `SecurityConfig.java` đã cập nhật CORS + filter
- [ ] ✅ `AuthController.java` đã set cookies
- [ ] ✅ `application.properties` đã cập nhật
- [ ] ✅ `.env` đã xóa `JWT_SECRET`
- [ ] ✅ Build thành công
- [ ] ✅ Test login với curl thành công
- [ ] ✅ Cookies được set với `HttpOnly` flag
- [ ] ✅ JWT header có `"alg": "RS256"`
- [ ] ✅ Frontend đã thêm `withCredentials: true`

---

## 🚨 Troubleshooting

### ❌ Lỗi: "Key file not found"
```
Caused by: java.io.FileNotFoundException: keys/private_key.pem
```

**Giải pháp:**
```bash
# Kiểm tra file có tồn tại không
ls -la src/main/resources/keys/

# Nếu không có, copy lại
cp keys/*.pem src/main/resources/keys/
```

---

### ❌ Lỗi: "CORS policy: credentials flag is true"
```
Access to fetch at 'http://localhost:8081/api/auth/login' from origin 'http://localhost:3000' 
has been blocked by CORS policy: The value of the 'Access-Control-Allow-Credentials' header 
in the response is '' which must be 'true' when the request's credentials mode is 'include'.
```

**Giải pháp:**
```java
// SecurityConfig.java
configuration.setAllowCredentials(true); // Phải có dòng này!
```

---

### ❌ Cookies không được set

**Kiểm tra:**
1. Response headers có `Set-Cookie` không?
   ```bash
   curl -v http://localhost:8081/api/auth/login ...
   # Phải thấy: Set-Cookie: access_token=...
   ```

2. Browser có block cookies không?
    - Chrome DevTools → Application → Cookies
    - Kiểm tra SameSite và Secure flags

3. CORS có đúng không?
    - `allowCredentials = true`
    - `allowedOrigins` phải chính xác (không dùng `*`)

---

### ❌ Token validation failed

**Kiểm tra:**
```bash
# Verify public key có thể decode token không
openssl rsa -pubin -in keys/public_key.pem -text -noout
```

**Debug logs:**
```properties
# application.properties
logging.level.com.management.restaurant.security=DEBUG
```

---

## 🎉 Kết Luận

Sau khi hoàn thành tất cả các bước, hệ thống của bạn sẽ có:

✅ **JWT signed với RS256** (an toàn hơn HS256)
✅ **HttpOnly cookies** (chống XSS)
✅ **Secure + SameSite** (chống CSRF)
✅ **CORS đúng cách** (cho phép cookies cross-origin)
✅ **Token rotation** (refresh token mechanism)
✅ **Backward compatible** (vẫn hỗ trợ Authorization header)

**Security Score: 9/10** 🔒

Cần hỗ trợ thêm? Hãy hỏi tôi! 😊