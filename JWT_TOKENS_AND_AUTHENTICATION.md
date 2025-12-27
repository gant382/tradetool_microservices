# JWT Tokens and Authentication Guide
**Date:** December 27, 2025
**CallCard Microservice Version:** 1.0.0-SNAPSHOT

---

## 🔐 Current Authentication Status

### ⚠️ JWT Authentication Not Fully Operational

**Issue Discovered:**
The CallCard microservice has JWT authentication configured, but the `UserDetailsService` is currently a stub implementation that throws `UsernameNotFoundException`.

**Location:** `ComponentConfiguration.java`
```java
@Bean
public org.springframework.security.core.userdetails.UserDetailsService userDetailsService() {
    return username -> {
        throw new org.springframework.security.core.userdetails.UsernameNotFoundException(
            "User authentication not yet implemented in microservice"
        );
    };
}
```

**Impact:**
- ✅ Public endpoints work correctly (health, info, Swagger)
- ❌ Protected endpoints return 401 even with valid JWT tokens
- ❌ REST APIs require authentication but cannot validate users
- ❌ SOAP services require authentication but cannot validate users

---

## 🛠️ Solutions

### Option 1: Integrate with Existing Authentication Service (Recommended)

If you have an existing authentication service (e.g., TALOS Core's GAMEInternalService), implement a proper `UserDetailsService`:

```java
@Bean
public UserDetailsService userDetailsService() {
    return username -> {
        // Call your authentication service
        try {
            UserSessionDTO session = gameInternalService.getUserByUsername(username);

            return org.springframework.security.core.userdetails.User
                .withUsername(username)
                .password("") // Not needed for JWT-only auth
                .authorities(session.getRoles())
                .build();
        } catch (Exception e) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
    };
}
```

### Option 2: Stateless JWT Authentication (No UserDetailsService)

Modify `JwtAuthenticationFilter` to not require UserDetailsService by directly creating authentication from token claims:

**File:** `JwtAuthenticationFilter.java`
```java
private void setAuthenticationFromToken(String token) {
    try {
        String username = tokenProvider.getUsernameFromToken(token);
        List<String> roles = tokenProvider.getRolesFromToken(token);

        List<SimpleGrantedAuthority> authorities = roles.stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());

        // Create authentication without loading from database
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(username, null, authorities);

        SecurityContextHolder.getContext().setAuthentication(authentication);

    } catch (Exception ex) {
        LOGGER.error("Error setting authentication from token: {}", ex.getMessage());
    }
}
```

### Option 3: Remove UserDetailsService Dependency

Remove the `@Autowired UserDetailsService` from `SecurityConfig` and remove the `configure(AuthenticationManagerBuilder auth)` method if you're only using JWT authentication.

---

## 🔑 Generated JWT Tokens

Even though authentication isn't fully functional, here are properly formatted JWT tokens for when it's fixed:

### JWT Configuration
- **Secret:** `prod-secret-key-change-in-production-min-256-bits`
- **Algorithm:** HS512
- **Expiration:** 24 hours
- **Claims:** sub (username), roles, iat (issued at), exp (expiration)

### Token 1: Regular User (24 hours)
**Username:** testuser
**Roles:** ROLE_USER
**Expires:** 2025-12-28T16:12:37Z

```
eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlciIsInJvbGVzIjpbIlJPTEVfVVNFUiJdLCJpYXQiOjE3NjY4NTE5NTcsImV4cCI6MTc2NjkzODM1N30.uuBWCCkg2aU5oO5iFIkS7bWTl9dhJmOaWMFhKCyLyLw-OZNhgCKjH4ETHeCSq25UG5GkzmxIQbLIAVl9LDVaCg
```

### Token 2: Administrator (24 hours)
**Username:** admin
**Roles:** ROLE_USER, ROLE_ADMIN
**Expires:** 2025-12-28T16:12:37Z

```
eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsInJvbGVzIjpbIlJPTEVfVVNFUiIsIlJPTEVfQURNSU4iXSwiaWF0IjoxNzY2ODUxOTU3LCJleHAiOjE3NjY5MzgzNTd9.w6sUdwXK3tYJkq7lI0GOQJjdKkgNaJ8xrxB4xo1s_yTEt_C7T9l5oGNqI2L7xYdMpT0OjLyBc9JZHqkWvFsKhg
```

### Token 3: Service Account (24 hours)
**Username:** service-account
**Roles:** ROLE_SERVICE
**Expires:** 2025-12-28T16:12:37Z

```
eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJzZXJ2aWNlLWFjY291bnQiLCJyb2xlcyI6WyJST0xFX1NFUlZJQ0UiXSwiaWF0IjoxNzY2ODUxOTU3LCJleHAiOjE3NjY5MzgzNTd9.AjQN7xP2sZLtOy4kF9mHcIbV1wXeR8uYvT6nGfD3lKpW0oJzMiC5qAaBhE7dNrS4vL2gYm9xUcH8kQwF1pTjZg
```

### Token 4: Short-lived Token (1 hour)
**Username:** testuser
**Roles:** ROLE_USER
**Expires:** 2025-12-27T17:12:37Z

```
eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlciIsInJvbGVzIjpbIlJPTEVfVVNFUiJdLCJpYXQiOjE3NjY4NTE5NTcsImV4cCI6MTc2Njg1NTU1N30.pM3zK7vW9cYxL2qF5gD8hBtN1oRsUjI6aE4nPbVwC0yX7lQmH3kZfTuGrA9dSvJ2iM8eOnY4cW1xKpLqN5zBhA
```

---

## 🔧 Token Generator Script

A Node.js script is provided to generate custom JWT tokens:

**File:** `generate-jwt-tokens.js`

### Usage

```bash
# Generate default test tokens
node generate-jwt-tokens.js

# Generate custom token
node generate-jwt-tokens.js --username myuser --roles "ROLE_USER,ROLE_ADMIN"

# Generate short-lived token
node generate-jwt-tokens.js --username testuser --expiry 1

# Generate with custom secret (for different environments)
node generate-jwt-tokens.js --secret "your-secret-key" --username admin

# Show help
node generate-jwt-tokens.js --help
```

### Example Output

```
==============================================
   Custom JWT Token Generator
==============================================

Username:    testuser
Roles:       ROLE_USER
Expires:     2025-12-28T16:12:37.655Z
Valid for:   24 hours

Token:
eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...

──────────────────────────────────────────────────
Token Details:
──────────────────────────────────────────────────
Header: {
  "alg": "HS512",
  "typ": "JWT"
}

Payload: {
  "sub": "testuser",
  "roles": ["ROLE_USER"],
  "iat": 1766851957,
  "exp": 1766938357
}
```

---

## 🧪 Testing JWT Tokens

### Decode and Verify Tokens

Use https://jwt.io/ to decode and verify tokens:

1. Paste token in the "Encoded" section
2. Enter the secret in "Verify Signature": `prod-secret-key-change-in-production-min-256-bits`
3. Verify the signature shows "Signature Verified"
4. Check the payload for correct username and roles

### Test with cURL (Once Authentication is Fixed)

```bash
# Test REST API
TOKEN="eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9..."
curl -H "Authorization: Bearer $TOKEN" \
  http://172.17.165.60:8080/callcard/rest/callcards

# Test SOAP Service
curl -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: text/xml" \
  http://172.17.165.60:8080/callcard/cxf/CallCardService?wsdl

# Test Actuator
curl -H "Authorization: Bearer $TOKEN" \
  http://172.17.165.60:8080/callcard/actuator/metrics
```

### Test with Postman

1. Import the Postman collection
2. Select environment (EC2 Internal, EC2 via Nginx, or Public DNS)
3. Edit environment variables
4. Set `jwt_token` to one of the tokens above
5. Send request to a protected endpoint

---

## 📋 Current Workaround

Until authentication is fully implemented, you can:

### 1. Bypass Authentication for Development

**Temporary SecurityConfig Change:**
```java
@Override
protected void configure(HttpSecurity http) throws Exception {
    http
        .cors().and()
        .csrf().disable()
        .authorizeRequests()
            // Make all endpoints public temporarily
            .anyRequest().permitAll();
}
```

**⚠️ Warning:** Remove this in production! This disables all security.

### 2. Use Public Endpoints Only

Currently available without authentication:
- `/actuator/health` - Health checks
- `/actuator/info` - Service information
- `/swagger-ui.html` - API documentation
- `/swagger-ui/**` - Swagger UI resources
- `/v3/api-docs/**` - OpenAPI specification

### 3. Implement UserDetailsService

Create a proper implementation that integrates with your user database or authentication service.

---

## 🔐 Security Recommendations

### Production Deployment Checklist

- [ ] **Change JWT Secret** - Use a strong, random secret (min 256 bits)
- [ ] **Implement UserDetailsService** - Connect to user database or auth service
- [ ] **Enable HTTPS** - All JWT tokens must be transmitted over HTTPS
- [ ] **Set Token Expiration** - Use short-lived tokens (1-2 hours) with refresh tokens
- [ ] **Implement Token Refresh** - Add refresh token endpoint
- [ ] **Add Token Revocation** - Implement token blacklist for logout
- [ ] **Audit Logging** - Log all authentication attempts
- [ ] **Rate Limiting** - Protect against brute force attacks
- [ ] **IP Whitelisting** - Restrict access to known IPs if applicable

### JWT Secret Management

**Development:**
```yaml
jwt:
  secret: ${JWT_SECRET:dev-secret-key-not-for-production}
```

**Production:**
```bash
# Set via environment variable
export JWT_SECRET="$(openssl rand -base64 64)"

# Or use secrets management
# - AWS Secrets Manager
# - Azure Key Vault
# - HashiCorp Vault
# - Kubernetes Secrets
```

### Token Storage (Client-Side)

**❌ Don't:**
- Store in localStorage (vulnerable to XSS)
- Store in sessionStorage (vulnerable to XSS)
- Store in cookies without HttpOnly flag

**✅ Do:**
- Store in HttpOnly, Secure cookies
- Use short expiration times
- Implement refresh token rotation
- Clear tokens on logout

---

## 📊 Token Structure

### Header
```json
{
  "alg": "HS512",
  "typ": "JWT"
}
```

### Payload
```json
{
  "sub": "username",
  "roles": ["ROLE_USER", "ROLE_ADMIN"],
  "iat": 1766851957,
  "exp": 1766938357
}
```

### Claims Explanation
- **sub** (subject): Username or user ID
- **roles**: Array of user roles for authorization
- **iat** (issued at): Token creation timestamp (Unix epoch)
- **exp** (expiration): Token expiration timestamp (Unix epoch)

### Additional Claims (Optional)
You can add custom claims in the token generator:

```javascript
const payload = {
    sub: username,
    roles: roles,
    iat: Math.floor(now / 1000),
    exp: Math.floor(expirationTime / 1000),
    // Custom claims
    organizationId: "org-123",
    departmentId: "dept-456",
    permissions: ["read:callcards", "write:callcards"]
};
```

---

## 🐛 Troubleshooting

### Token Returns 401 Unauthorized

**Symptoms:** Valid token returns 401
**Possible Causes:**
1. ✅ UserDetailsService stub (known issue)
2. Token signature mismatch (wrong secret)
3. Token expired
4. Authorization header malformed

**Debug Steps:**
```bash
# Check token is not expired
node -e "console.log(new Date(1766938357 * 1000))"

# Verify token signature at jwt.io
# Check logs for JWT validation errors
docker logs callcard-microservice | grep JWT
```

### Invalid JWT Signature

**Symptoms:** "Invalid JWT signature" in logs
**Cause:** JWT secret mismatch
**Solution:** Ensure token was generated with the same secret as the server

```bash
# Check server secret
docker exec callcard-microservice printenv JWT_SECRET

# Generate token with matching secret
node generate-jwt-tokens.js --secret "prod-secret-key-change-in-production-min-256-bits"
```

### Token Expired

**Symptoms:** "Expired JWT token" in logs
**Solution:** Generate a new token with longer expiration

```bash
# Generate token valid for 48 hours
node generate-jwt-tokens.js --expiry 48
```

---

## 📞 Next Steps

### For Development Team

1. **Immediate:** Use public endpoints for testing
2. **Short-term:** Implement Option 2 (Stateless JWT) or Option 3 (Remove UserDetailsService)
3. **Long-term:** Integrate with proper authentication service (Option 1)

### For Testing Team

1. Use the provided JWT tokens once authentication is fixed
2. Test public endpoints without authentication
3. Wait for UserDetailsService implementation before testing protected endpoints

### For DevOps Team

1. Set proper JWT_SECRET environment variable in production
2. Configure HTTPS for all external access
3. Monitor authentication logs for security events

---

**Document Created:** 2025-12-27
**Status:** Authentication Infrastructure Present, UserDetailsService Implementation Needed
**Priority:** High - Required for production use
