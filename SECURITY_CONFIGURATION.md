# CallCard Microservice - Comprehensive Security Configuration

## Overview

The CallCard microservice implements enterprise-grade security with JWT authentication, CORS configuration, CSRF protection, and role-based access control (RBAC).

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│ Client Request                                          │
│ Authorization: Bearer eyJhbGciOiJIUzI1NiIs...          │
└────────────────────┬────────────────────────────────────┘
                     │
        ┌────────────▼────────────┐
        │ JwtAuthenticationFilter │
        │ (Extract & Validate)    │
        └────────────┬────────────┘
                     │
        ┌────────────▼──────────────────────┐
        │ SecurityContext                   │
        │ Set Authentication Principal      │
        └────────────┬──────────────────────┘
                     │
        ┌────────────▼─────────────────────┐
        │ @PreAuthorize Evaluation         │
        │ Role-based Access Control        │
        └────────────┬─────────────────────┘
                     │
        ┌────────────▼──────────────────────┐
        │ ServiceImpl (Protected Method)     │
        │ User context available via        │
        │ SecurityUtils.getCurrentUsername()│
        └──────────────────────────────────┘
```

## Components

### 1. SecurityConfig
**Location:** `CallCard_Server_WS/src/main/java/com/saicon/callcard/config/SecurityConfig.java`

Main security configuration class that:
- Configures HTTP security filter chain
- Enables JWT authentication
- Configures CORS
- Sets up authorization rules
- Enables method-level security

**Key Features:**
- CSRF disabled for stateless REST/SOAP APIs
- Session management: STATELESS (no HTTP sessions)
- HTTP Basic auth fallback
- Role-based endpoint protection

### 2. JwtTokenProvider
**Location:** `CallCard_Server_WS/src/main/java/com/saicon/callcard/security/JwtTokenProvider.java`

Handles JWT token generation and validation:
- Generates tokens from authenticated principals
- Validates token signature and expiration
- Extracts claims (username, roles, etc.)
- HS512 signing algorithm with 256-bit+ secret key

**Token Claims:**
```json
{
  "sub": "username",
  "roles": ["ROLE_USER", "ROLE_MANAGER"],
  "iat": 1705338600,
  "exp": 1705425000
}
```

### 3. JwtAuthenticationFilter
**Location:** `CallCard_Server_WS/src/main/java/com/saicon/callcard/security/JwtAuthenticationFilter.java`

Per-request filter that:
- Extracts JWT from `Authorization: Bearer <token>` header
- Validates token signature and expiration
- Creates Spring Security Authentication object
- Sets authentication in SecurityContext

**Error Handling:**
- Missing token: Logs warning, continues
- Invalid token: Logs warning, continues
- Expired token: Logs warning, continues
- Generic error: Logs error, continues

### 4. JwtAuthenticationEntryPoint
**Location:** `CallCard_Server_WS/src/main/java/com/saicon/callcard/security/JwtAuthenticationEntryPoint.java`

Handles unauthenticated access attempts:
- Returns JSON 401 error response
- Logs security events with client IP
- Prevents information disclosure

### 5. SessionAuthenticationInterceptor
**Location:** `CallCard_Server_WS/src/main/java/com/saicon/callcard/security/SessionAuthenticationInterceptor.java`

CXF SOAP interceptor for session validation:
- Extracts session token from SOAP headers
- Validates with IGameInternalService
- Stores authenticated user info in message context

**SOAP Header Format:**
```xml
<SessionToken xmlns="http://ws.saicon.com/session">
  <token>session-token-value</token>
  <userId>user-id</userId>
</SessionToken>
```

### 6. SecurityUtils
**Location:** `CallCard_Server_WS/src/main/java/com/saicon/callcard/security/SecurityUtils.java`

Helper utility class for:
- Getting current authenticated user
- Checking user roles
- Accessing security context information

## Configuration

### JWT Settings (application.yml)

```yaml
jwt:
  # Secret key for signing tokens (256+ bits recommended)
  secret: ${JWT_SECRET:your-super-secret-key-change-in-production-min-256-bits}

  # Token expiration in milliseconds (24 hours = 86400000)
  expiration: ${JWT_EXPIRATION:86400000}
```

### CORS Configuration (application.yml)

```yaml
security:
  cors:
    allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:4200,http://localhost:3000}
    allowed-methods: GET,POST,PUT,DELETE,OPTIONS,PATCH
    allowed-headers: Authorization,Content-Type,Accept,X-Requested-With
    exposed-headers: Authorization,Content-Type,X-Total-Count
    max-age: 3600
    allow-credentials: true
```

### Spring Security Properties

```yaml
spring:
  security:
    user:
      name: admin
      password: admin123  # Development only - use proper authentication in production
```

## Authentication Flow

### REST Endpoints

```
1. Client sends: GET /callcard/rest/callcards
   Header: Authorization: Bearer eyJhbGc...

2. JwtAuthenticationFilter intercepts request
   - Extracts token from Authorization header
   - Validates signature and expiration
   - Creates Authentication object

3. SecurityContext receives Authentication
   - Principal: username
   - Authorities: [ROLE_USER, ROLE_MANAGER]

4. @PreAuthorize evaluates method-level security
   - Checks if user has required roles
   - Allows/denies access

5. ServiceImpl executes with authenticated user
   - Can access current user via SecurityUtils
```

### SOAP Endpoints

```
1. Client sends SOAP request with header:
   <SessionToken>
     <token>...</token>
     <userId>...</userId>
   </SessionToken>

2. SessionAuthenticationInterceptor intercepts
   - Extracts session token from SOAP header
   - Validates with IGameInternalService
   - Stores user info in message context

3. ServiceImpl executes with authenticated user
```

## Endpoint Protection

### Public Endpoints (No Authentication Required)

```
GET  /actuator/health/**          Health check endpoints
GET  /actuator/info               Application info
GET  /swagger-ui.html             Swagger UI
GET  /v3/api-docs/**              OpenAPI specification
GET  /webjars/**                  Web resources
```

### Protected Endpoints (Authentication Required)

```
GET  /cxf/**                       SOAP services
GET  /rest/**                      REST endpoints
GET  /actuator/**                  Metrics, logging, etc.
```

## Usage Examples

### 1. Getting Current User in Service

```java
import com.saicon.callcard.security.SecurityUtils;

@Service
public class CallCardServiceImpl implements ICallCardService {

    public CallCardDTO getCallCard(Long cardId) {
        String username = SecurityUtils.getCurrentUsername();

        if (!SecurityUtils.hasRole("ROLE_USER")) {
            throw new AccessDeniedException("Insufficient permissions");
        }

        // Proceed with business logic
    }
}
```

### 2. Method-Level Security

```java
import org.springframework.security.access.prepost.PreAuthorize;

@Service
public class CallCardServiceImpl {

    @PreAuthorize("hasRole('ROLE_USER')")
    public List<CallCardDTO> listCallCards() {
        // Only users with ROLE_USER can access
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public void deleteCallCard(Long cardId) {
        // Only admins or managers can delete
    }

    @PreAuthorize("@callCardService.isOwner(#cardId)")
    public CallCardDTO updateCallCard(Long cardId, CallCardDTO dto) {
        // Custom authorization logic via bean method
    }
}
```

### 3. Client Request with JWT

```bash
# Generate token (typically from auth service)
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# REST endpoint
curl -H "Authorization: Bearer $TOKEN" \
     http://localhost:8080/callcard/rest/callcards

# SOAP endpoint
curl -H "Authorization: Bearer $TOKEN" \
     -H "Content-Type: text/xml" \
     -d @request.xml \
     http://localhost:8080/callcard/cxf/CallCardService
```

### 4. Accessing User from Request

```java
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/rest/callcards")
public class CallCardController {

    @GetMapping
    public List<CallCardDTO> getCallCards() {
        // Get current authentication
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        // List user's call cards
    }
}
```

## Security Best Practices

### 1. Secret Key Management

```yaml
# Development (application-dev.yml)
jwt:
  secret: development-secret-key

# Production (environment variable)
export JWT_SECRET=your-very-long-production-secret-256bits-minimum
```

**Recommendations:**
- Minimum 256 bits (32 bytes) for HS512
- Generate with: `openssl rand -hex 32`
- Rotate regularly (every 90 days recommended)
- Use Spring Cloud Config or Vault for production

### 2. CORS Configuration

**Development:**
```yaml
security:
  cors:
    allowed-origins: http://localhost:4200,http://localhost:3000
```

**Production:**
```yaml
security:
  cors:
    allowed-origins: https://app.saicon.com,https://api.saicon.com
```

**Security Concerns:**
- Never use wildcard `*` in production (except for public APIs)
- Explicitly list trusted origins
- Set `allow-credentials: false` unless needed

### 3. Token Expiration

```yaml
jwt:
  expiration: 3600000  # 1 hour for sensitive operations
  # or
  expiration: 86400000 # 24 hours for less sensitive operations
```

**Considerations:**
- Shorter expiration = better security, more user inconvenience
- Longer expiration = better UX, reduced security
- Consider refresh tokens for very sensitive operations

### 4. HTTPS in Production

**Always use HTTPS for:**
- Sending JWT tokens
- Transmitting sensitive data
- Production deployments

```yaml
server:
  ssl:
    key-store: /path/to/keystore.p12
    key-store-password: ${KEYSTORE_PASSWORD}
    key-store-type: PKCS12
```

### 5. Password Policy

**Never hardcode passwords in code:**
```java
// WRONG
private String password = "admin123";

// RIGHT
@Value("${app.admin.password}")
private String password;
```

### 6. SQL Injection Prevention

All endpoints use:
- Parameterized queries (no string concatenation)
- JPA repositories with type-safe queries
- Input validation via @Valid annotations

### 7. XSS Prevention

- Spring Security adds anti-XSS headers by default
- Input validation and sanitization in services
- Response headers configured in SecurityConfig

### 8. CSRF Protection

**Status:** Disabled for stateless REST APIs
- CSRF protection added to SOAP endpoints via SessionAuthenticationInterceptor
- Re-enable CSRF if form-based authentication is added

## Deployment Checklist

- [ ] Change `jwt.secret` in production (use environment variable)
- [ ] Set appropriate `jwt.expiration` (shorter for sensitive data)
- [ ] Configure `CORS_ALLOWED_ORIGINS` for production domains
- [ ] Enable HTTPS with valid SSL certificate
- [ ] Set `spring.security.user.password` or use SSO/LDAP
- [ ] Enable security logging and monitoring
- [ ] Test endpoint authorization with various user roles
- [ ] Set up audit logging for sensitive operations
- [ ] Configure rate limiting to prevent brute force attacks
- [ ] Set up intrusion detection/WAF rules
- [ ] Regularly rotate security credentials
- [ ] Monitor and log authentication failures

## Troubleshooting

### 401 Unauthorized Errors

**Problem:** Requests return 401 even with valid token

**Solutions:**
1. Verify token format: `Authorization: Bearer <token>`
2. Check token expiration: `jwtTokenProvider.isTokenExpired(token)`
3. Verify secret key matches token issuer
4. Check token signature: Enable debug logging in JwtTokenProvider

### CORS Errors

**Problem:** Browser blocks requests with CORS error

**Solutions:**
1. Verify origin is in `CORS_ALLOWED_ORIGINS`
2. Check preflight OPTIONS request succeeds
3. Verify CORS headers in response
4. Check browser console for specific error message

### Token Validation Fails

**Problem:** Valid-looking token is rejected

**Solutions:**
1. Verify token not expired: check `exp` claim
2. Verify secret key matches issuer
3. Verify token format is valid JWT
4. Check token wasn't tampered with

### Method-Level Security Not Working

**Problem:** @PreAuthorize annotation ignored

**Solutions:**
1. Ensure `@EnableGlobalMethodSecurity` is in SecurityConfig
2. Verify method is called through Spring proxy (not direct instantiation)
3. Check role names match exactly (case-sensitive)
4. Enable method security in logs:
   ```yaml
   logging:
     level:
       org.springframework.security.access.intercept: DEBUG
   ```

## Monitoring and Logging

### Key Log Messages

```
# Successful authentication
INFO: Set user authentication for username: admin with roles: [ROLE_ADMIN]

# Failed authentication
WARN: Invalid JWT signature: ...
WARN: Unauthorized access attempt - IP: 192.168.1.1, Path: /callcard/rest/callcards

# Expired token
ERROR: Expired JWT token: ...
```

### Metrics to Monitor

```
# Spring Security metrics
spring_security_authentication_success_total
spring_security_authentication_failures_total

# Custom metrics
callcard.security.token_validation_duration
callcard.security.unauthorized_access_attempts
```

### Audit Logging

Enable audit logging for compliance:
```yaml
logging:
  level:
    org.springframework.security.access.intercept: DEBUG
    com.saicon.callcard.security: DEBUG
```

## Integration with IGameInternalService

The microservice validates sessions against the TALOS Core platform:

```yaml
callcard:
  talos-core:
    session-validation-url: http://localhost:8080/Game_Server_WS/cxf/GAMEInternalService
    connection-timeout: 5000
    read-timeout: 10000
```

**Session Validation Flow:**
1. Client provides session token in SOAP header
2. SessionAuthenticationInterceptor calls IGameInternalService
3. IGameInternalService validates token and returns user info
4. SessionAuthenticationInterceptor stores user info in message context
5. ServiceImpl executes with authenticated user

## References

- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [JWT (RFC 7519)](https://tools.ietf.org/html/rfc7519)
- [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)
- [Spring Boot Security Properties](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html#application-properties.security)

## Support

For security issues or questions:
- Technical: tech@saicon.com
- Security: security@saicon.com
- Architecture: architecture@saicon.com
