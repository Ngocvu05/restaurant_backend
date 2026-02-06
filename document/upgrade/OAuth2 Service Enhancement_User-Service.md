# OAuth2 Service Enhancement - Complete Guide

## 📋 Overview

Comprehensive upgrade to OAuth2 authentication system with support for multiple providers, account linking, security features, and audit trail.

## 🎯 New Features

### 1. **Enhanced Security**
- ✅ Failed login protection for OAuth2
- ✅ Account locking integration
- ✅ Email auto-verification via OAuth2
- ✅ Rate limiting per provider
- ✅ Device and IP tracking
- ✅ Audit trail for all OAuth2 actions

### 2. **Multiple Provider Support**
- ✅ Link multiple OAuth2 providers to one account
- ✅ Google, Facebook, GitHub, Apple, etc.
- ✅ Set primary provider
- ✅ Unlink providers safely
- ✅ Prevent orphan accounts

### 3. **Token Management**
- ✅ Refresh token rotation
- ✅ Token expiry tracking
- ✅ Automatic cleanup of expired tokens
- ✅ Secure token storage

### 4. **Account Integration**
- ✅ Link OAuth2 to existing accounts
- ✅ Auto-create accounts via OAuth2
- ✅ Merge accounts by email
- ✅ Chat session conversion

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────┐
│           OAuth2 Authentication Flow             │
└─────────────────────────────────────────────────┘
                      ↓
┌─────────────────────────────────────────────────┐
│  1. User clicks "Login with Google"             │
│  2. Redirect to Google OAuth2                   │
│  3. User grants permissions                     │
│  4. Google returns access token                 │
│  5. Our backend validates token                 │
│  6. Check/create user account                   │
│  7. Apply security checks                       │
│  8. Generate our JWT tokens                     │
│  9. Return to frontend                          │
└─────────────────────────────────────────────────┘
```

## 📊 Database Schema

### oauth2_links Table

| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT | Primary key |
| user_id | BIGINT | User reference |
| provider | VARCHAR(50) | Provider name (google, facebook, etc.) |
| provider_user_id | VARCHAR(255) | Provider's user ID |
| provider_email | VARCHAR(255) | Email from provider |
| provider_display_name | VARCHAR(255) | Display name |
| provider_picture_url | VARCHAR(500) | Profile picture URL |
| access_token | VARCHAR(1000) | OAuth2 access token (encrypted) |
| refresh_token | VARCHAR(1000) | OAuth2 refresh token (encrypted) |
| token_expires_at | TIMESTAMP | Token expiry |
| scopes | VARCHAR(500) | Granted scopes |
| linked_at | TIMESTAMP | When linked |
| last_used_at | TIMESTAMP | Last usage |
| is_primary | BOOLEAN | Primary provider flag |
| metadata | TEXT | Additional data (JSON) |

## 🚀 Implementation

### Step 1: Run Migration

```sql
-- Create oauth2_links table
psql -U your_user -d your_database -f V3__add_oauth2_links.sql
```

### Step 2: Update OAuth2ServiceImpl

Replace existing `OAuth2ServiceImpl.java` with optimized version.

**Key Improvements:**
```java
// Before
public AuthResponse authenticateOAuth2User(OAuth2LoginRequest request) {
    // Simple authentication
}

// After
public AuthResponse authenticateOAuth2User(OAuth2LoginRequest request, HttpServletRequest httpRequest) {
    // ✅ Request validation
    // ✅ Rate limiting
    // ✅ Security checks (locked, inactive, email verified)
    // ✅ Login tracking
    // ✅ Token rotation
    // ✅ Device tracking
    // ✅ Audit trail
}
```

### Step 3: Add OAuth2Link Entity & Services

Add new files:
- `OAuth2Link.java` - Entity for provider links
- `OAuth2LinkRepository.java` - Repository with queries
- `OAuth2LinkService.java` - Business logic
- `OAuth2LinkServiceImpl.java` - Implementation

### Step 4: Add Controller Endpoints

Add `OAuth2Controller.java` with endpoints:
- POST `/api/oauth2/login` - OAuth2 authentication
- POST `/api/oauth2/link` - Link provider to account
- DELETE `/api/oauth2/unlink/{provider}` - Unlink provider
- GET `/api/oauth2/linked` - Get linked providers
- PUT `/api/oauth2/set-primary/{provider}` - Set primary

### Step 5: Update Configuration

```yaml
app:
  oauth2:
    # Enable/disable providers
    enabled-providers:
      - google
      - facebook
      - github
      - apple
    
    # Auto-create accounts
    auto-create-accounts: true
    
    # Auto-verify emails from OAuth2
    auto-verify-email: true
    
    # Token storage
    store-access-tokens: false  # Security: don't store access tokens
    store-refresh-tokens: true  # Store for token refresh
    
  security:
    # Rate limiting per provider
    oauth2-rate-limit: 10  # requests per minute
```

## 📖 Usage Examples

### 1. OAuth2 Login

```java
// Frontend sends OAuth2 request
OAuth2LoginRequest request = new OAuth2LoginRequest();
request.setProvider("google");
request.setAccessToken("ya29.a0AfH6SMB...");
request.setEmail("user@gmail.com");
request.setSessionId("guest-session-123"); // Optional: for chat conversion

AuthResponse response = oauth2Service.authenticateOAuth2User(request, httpRequest);

// Response includes:
// - JWT access token
// - JWT refresh token
// - User info
// - Email verified = true (auto-verified via Google)
// - Linked providers list
```

### 2. Link OAuth2 to Existing Account

```java
// User is logged in, wants to link Google
LinkOAuth2Request request = new LinkOAuth2Request();
request.setProvider("google");
request.setAccessToken("ya29.a0AfH6SMB...");

oauth2Service.linkOAuth2Account(userId, oauth2Request);

// User can now login with either:
// 1. Username/password
// 2. Google OAuth2
```

### 3. Get Linked Providers

```java
List<OAuth2LinkDTO> linked = oauth2LinkService.getLinkedProviders(userId);

// Example output:
// [
//   {
//     "provider": "google",
//     "providerEmail": "user@gmail.com",
//     "linkedAt": "2026-01-05T10:30:00",
//     "isPrimary": true
//   },
//   {
//     "provider": "facebook",
//     "providerEmail": "user@fb.com",
//     "linkedAt": "2026-01-06T14:20:00",
//     "isPrimary": false
//   }
// ]
```

### 4. Unlink Provider

```java
// Can only unlink if:
// 1. User has password set, OR
// 2. User has other OAuth2 providers linked

oauth2LinkService.unlinkAccount(userId, "facebook");

// If this is the only auth method and user has no password:
// RuntimeException: "Cannot unlink the only authentication method"
```

### 5. Security Checks

```java
// OAuth2 login automatically:

// 1. Checks if account is locked
if (user.isAccountLocked()) {
    throw new RuntimeException("Account locked");
}

// 2. Checks if account is active
if (!user.isActive()) {
    throw new RuntimeException("Account inactive");
}

// 3. Auto-verifies email (since OAuth2 provider verified it)
if (!user.hasVerifiedEmail()) {
    user.setEmailVerified(true);
}

// 4. Updates last login timestamp
user.recordSuccessfulLogin();

// 5. Tracks device and IP
tokenEntity.setIpAddress(requestHelper.getCurrentIpAddress());
tokenEntity.setDeviceId(requestHelper.getDeviceInfoString());
```

## 🔐 Security Features

### 1. Rate Limiting

```java
// Prevent OAuth2 abuse
rateLimitService.checkRateLimit("google_user@gmail.com");

// Configuration
oauth2-rate-limit: 10  # Max 10 OAuth2 login attempts per minute
```

### 2. Account Locking

```java
// OAuth2 respects account locks
if (user.isAccountLocked()) {
    auditService.logOAuth2Failure("google", email, "Account locked");
    throw new RuntimeException("Account locked");
}
```

### 3. Audit Trail

```java
// All OAuth2 actions are logged
auditService.logOAuth2Success("google", email);
auditService.logOAuth2Failure("google", email, reason);
auditService.logOAuth2Link(userId, "google", email);
auditService.logOAuth2Unlink(userId, "google");
```

### 4. Token Security

```java
// Refresh token rotation
String newRefreshToken = authService.rotateRefreshToken(oldToken, user, httpRequest);

// Old token is soft-deleted, new token issued
// Prevents token replay attacks
```

## 📊 Monitoring & Analytics

### 1. OAuth2 Statistics

```sql
-- Get provider usage stats
SELECT * FROM get_oauth2_stats();

-- Output:
-- provider | user_count | active_links | avg_links_per_user
-- google   | 1500       | 1520         | 1.01
-- facebook | 800        | 810          | 1.01
-- github   | 200        | 205          | 1.03
```

### 2. Find Multi-Provider Users

```sql
-- Users with multiple OAuth2 providers
SELECT 
    u.username,
    COUNT(o.id) as provider_count,
    STRING_AGG(o.provider, ', ') as providers
FROM users u
JOIN oauth2_links o ON u.id = o.user_id
WHERE o.deleted_at IS NULL
GROUP BY u.id, u.username
HAVING COUNT(o.id) > 1;
```

### 3. Inactive OAuth2 Links

```sql
-- Find OAuth2 links not used in 90 days
SELECT 
    u.username,
    o.provider,
    o.last_used_at,
    AGE(CURRENT_TIMESTAMP, o.last_used_at) as inactive_duration
FROM oauth2_links o
JOIN users u ON o.user_id = u.id
WHERE o.last_used_at < CURRENT_TIMESTAMP - INTERVAL '90 days'
  AND o.deleted_at IS NULL;
```

## 🔧 Configuration

### Frontend Integration

```typescript
// React example
const handleGoogleLogin = async (googleResponse) => {
  const response = await fetch('/api/oauth2/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      provider: 'google',
      accessToken: googleResponse.accessToken,
      email: googleResponse.profileObj.email,
      sessionId: localStorage.getItem('guestSessionId') // Optional
    })
  });
  
  const data = await response.json();
  
  // Store tokens
  localStorage.setItem('accessToken', data.accessToken);
  localStorage.setItem('refreshToken', data.refreshToken);
  
  // User is logged in
  console.log('Logged in as:', data.username);
  console.log('Email verified:', data.emailVerified); // true
  console.log('Linked providers:', data.linkedProviders);
};
```

### Provider Configuration

```yaml
# application.yml

spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope:
              - email
              - profile
          
          facebook:
            client-id: ${FACEBOOK_APP_ID}
            client-secret: ${FACEBOOK_APP_SECRET}
            scope:
              - email
              - public_profile
          
          github:
            client-id: ${GITHUB_CLIENT_ID}
            client-secret: ${GITHUB_CLIENT_SECRET}
            scope:
              - read:user
              - user:email
```

## 🐛 Troubleshooting

### Issue: OAuth2 login fails

```
Check:
1. OAuth2 provider credentials correct?
2. Redirect URI configured in provider console?
3. Rate limit exceeded?
4. Account locked or inactive?
5. Check logs for detailed error
```

### Issue: Cannot unlink provider

```
Error: "Cannot unlink the only authentication method"

Solution:
- User must set a password first, OR
- User must link another OAuth2 provider

Code:
POST /api/users/change-password
{
  "oldPassword": null,
  "newPassword": "SecurePass123!"
}
```

### Issue: Email mismatch when linking

```
Error: "Email mismatch - Cannot link OAuth2 account"

Cause: Trying to link Google account (email: john@gmail.com)
       to user account (email: john@work.com)

Solution: Can only link OAuth2 accounts with same email
```

## ✅ Testing Checklist

- [ ] OAuth2 login with Google
- [ ] OAuth2 login with Facebook
- [ ] Auto-create account on first OAuth2 login
- [ ] Auto-verify email via OAuth2
- [ ] Link Google to existing account
- [ ] Link multiple providers to same account
- [ ] Set primary provider
- [ ] Unlink provider (with password)
- [ ] Prevent unlink (only auth method)
- [ ] OAuth2 login respects account lock
- [ ] Rate limiting works
- [ ] Token rotation on refresh
- [ ] Chat session conversion
- [ ] Audit trail logging
- [ ] Statistics queries work

## 📈 Performance Metrics

### Expected Improvements

| Metric | Before | After |
|--------|--------|-------|
| OAuth2 login time | ~500ms | ~300ms (optimized) |
| Security checks | None | 4 comprehensive checks |
| Audit trail | No | Yes (all actions) |
| Token security | Basic | Rotation + tracking |
| Multi-provider | No | Yes (unlimited) |

## 🔗 Related Documentation

- [OAuth 2.0 Specification](https://oauth.net/2/)
- [Google OAuth2](https://developers.google.com/identity/protocols/oauth2)
- [Facebook Login](https://developers.facebook.com/docs/facebook-login)
- [GitHub OAuth](https://docs.github.com/en/developers/apps/building-oauth-apps)
- [Apple Sign In](https://developer.apple.com/sign-in-with-apple/)

## 📞 Support

For issues:
1. Check provider configuration
2. Verify credentials in environment variables
3. Check rate limits
4. Review audit logs
5. Test with Postman/curl first