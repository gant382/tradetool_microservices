# CallCard Microservice - Security Quick Start Guide

## Overview

This guide provides quick reference for security configuration and common security tasks in the CallCard microservice.

## Files Created

### Configuration Files
- **SecurityConfig.java** - Main Spring Security configuration
- **application.yml** - JWT and CORS configuration

### Security Implementation Files
- **JwtTokenProvider.java** - JWT token generation and validation
- **JwtAuthenticationFilter.java** - JWT extraction and validation filter
- **JwtAuthenticationEntryPoint.java** - Unauthenticated request handler
- **SessionAuthenticationInterceptor.java** - SOAP session validation (existing)
- **SecurityUtils.java** - Helper utilities for security operations
- **AuthorizationAspect.java** - AOP aspect for audit logging

## Key Configuration Parameters

### JWT Configuration

Edit `application.yml`:
```yaml
jwt:
  secret: ${JWT_SECRET:your-super-secret-key-change-in-production-min-256-bits}
  expiration: ${JWT_EXPIRATION:86400000}  # 24 hours
```

**Production Deployment:**
```bash
export JWT_SECRET=your-256-bit-secret-key-generated-with-openssl
export JWT_EXPIRATION=3600000  # 1 hour for sensitive operations
```

Generate secure secret:
```bash
openssl rand -base64 32
```

### CORS Configuration

Edit `application.yml`:
```yaml
security:
  cors:
    allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:4200,http://localhost:3000}
    max-age: 3600
    allow-credentials: true
```

**Production Example:**
```bash
export CORS_ALLOWED_ORIGINS=https://app.saicon.com,https://api.saicon.com
```

## Common Tasks

### 1. Add Spring Security Dependency

Already added to `CallCard_Server_WS/pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

### 2. Protect an Endpoint

**REST Controller:**
```java
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/rest/callcards")
public class CallCardController {

    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public List<CallCardDTO> listCallCards() {
        // Protected endpoint
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public CallCardDTO createCallCard(@RequestBody CallCardDTO dto) {
        // Only managers can create
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public void deleteCallCard(@PathVariable Long id) {
        // Only admins or managers can delete
    }
}
```

**SOAP Service:**
```java
import javax.jws.WebService;

@WebService
public class CallCardServiceImpl implements ICallCardService {

    @Override
    @PreAuthorize("hasRole('ROLE_USER')")
    public CallCardDTO getCallCard(Long cardId) {
        // SOAP endpoint protection works via SessionAuthenticationInterceptor
        // and SecurityConfig SOAP endpoint configuration
    }
}
```

### 3. Access Current User in Service

```java
import com.saicon.callcard.security.SecurityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
public class CallCardServiceImpl {

    public CallCardDTO getMyCallCard(Long cardId) {
        // Option 1: Using SecurityUtils
        String username = SecurityUtils.getCurrentUsername();

        // Option 2: Direct access
        String username = SecurityContextHolder
            .getContext()
            .getAuthentication()
            .getName();

        // Use username for business logic
        return callCardRepository.findByIdAndUsername(cardId, username);
    }
}
```

### 4. Check User Roles

```java
import com.saicon.callcard.security.SecurityUtils;

@Service
public class CallCardServiceImpl {

    public void updateCallCard(Long cardId, CallCardDTO dto) {
        // Check single role
        if (!SecurityUtils.hasRole("ROLE_MANAGER")) {
            throw new AccessDeniedException("Only managers can update call cards");
        }

        // Check any role
        if (SecurityUtils.hasAnyRole("ROLE_ADMIN", "ROLE_MANAGER")) {
            // Admin or manager logic
        }

        // Check all roles
        if (SecurityUtils.hasAllRoles("ROLE_ADMIN", "ROLE_AUDIT")) {
            // Both roles required
        }

        // Get all roles
        String roles = SecurityUtils.getAllRoles();
        logger.info("User roles: {}", roles);
    }
}
```

### 5. Custom Authorization Logic

```java
@Service
public class CallCardAuthorizationService {

    // Use in @PreAuthorize annotation
    public boolean isOwner(Long cardId) {
        String currentUser = SecurityUtils.getCurrentUsername();
        CallCard card = callCardRepository.findById(cardId)
            .orElseThrow(() -> new NotFoundException("Card not found"));

        return card.getOwnerId().equals(currentUser);
    }

    public boolean canManage(Long organizationId) {
        return SecurityUtils.hasRole("ROLE_ADMIN") ||
               userService.isOrganizationManager(
                   SecurityUtils.getCurrentUsername(),
                   organizationId
               );
    }
}

// Usage in controller
@RestController
public class CallCardController {

    @Autowired
    private CallCardAuthorizationService authService;

    @PostMapping("/{id}")
    @PreAuthorize("@callCardAuthorizationService.isOwner(#id)")
    public CallCardDTO update(@PathVariable Long id, @RequestBody CallCardDTO dto) {
        // Only card owner can update
    }
}
```

### 6. Handle Authentication Errors

```java
@RestControllerAdvice
public class SecurityExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(new ErrorResponse("Access Denied", ex.getMessage()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
        AuthenticationException ex
    ) {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(new ErrorResponse("Authentication Failed", ex.getMessage()));
    }
}
```

### 7. Testing with JWT

**Get Token from Auth Service:**
```bash
# First, authenticate
curl -X POST http://localhost:8080/callcard/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Response:
# {"token":"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."}
```

**Use Token in Requests:**
```bash
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# REST endpoint
curl -H "Authorization: Bearer $TOKEN" \
     http://localhost:8080/callcard/rest/callcards

# SOAP endpoint
curl -H "Authorization: Bearer $TOKEN" \
     -H "Content-Type: text/xml" \
     -d @soap-request.xml \
     http://localhost:8080/callcard/cxf/CallCardService
```

## Role Definitions

Common roles used in CallCard microservice:

| Role | Description | Permissions |
|------|-------------|-------------|
| ROLE_SYSTEM_ADMIN | System administrator | Full access to all endpoints |
| ROLE_ORG_ADMIN | Organization administrator | Full access to organization resources |
| ROLE_MANAGER | Manager | Can create, update, delete call cards |
| ROLE_USER | Regular user | Can view and create personal call cards |
| ROLE_GUEST | Guest user | Read-only access to public endpoints |

## Security Properties Reference

### Spring Security Properties

```yaml
spring:
  security:
    user:
      name: admin           # Default username (dev only)
      password: admin123    # Default password (dev only)
      roles: ADMIN,USER     # Default roles
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://keycloak.example.com/auth/realms/saicon
```

### Actuator Security

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics  # Only expose these endpoints
        exclude: shutdown              # Never expose shutdown endpoint
  endpoint:
    health:
      show-details: always
    shutdown:
      enabled: false  # Disable shutdown endpoint
  metrics:
    tags:
      application: callcard-microservice
```

## Deployment Checklist

- [ ] Generate strong JWT secret key (256+ bits)
- [ ] Update `JWT_SECRET` environment variable
- [ ] Configure `CORS_ALLOWED_ORIGINS` for production domains
- [ ] Enable HTTPS with valid SSL certificate
- [ ] Configure database for user credentials (LDAP/AD integration recommended)
- [ ] Set up audit logging for compliance
- [ ] Configure rate limiting to prevent brute force
- [ ] Enable security headers (HSTS, CSP, X-Frame-Options, etc.)
- [ ] Set up WAF or intrusion detection
- [ ] Test endpoint authorization with test users
- [ ] Review and update security policies
- [ ] Set up monitoring and alerting for security events

## Troubleshooting

### Issue: 401 Unauthorized on All Requests

**Cause:** Token validation failing

**Solution:**
1. Verify token format: `Authorization: Bearer <token>`
2. Check JWT secret key matches token issuer
3. Enable debug logging:
   ```yaml
   logging:
     level:
       com.saicon.callcard.security: DEBUG
   ```
4. Verify token not expired

### Issue: 403 Forbidden Despite Valid Token

**Cause:** Role-based authorization failing

**Solution:**
1. Verify user has required role
2. Check `@PreAuthorize` annotation spelling and role names
3. Enable method security debugging:
   ```yaml
   logging:
     level:
       org.springframework.security.access: DEBUG
   ```
4. Ensure authentication is set in SecurityContext

### Issue: CORS Error on Browser Requests

**Cause:** CORS origin not allowed

**Solution:**
1. Add origin to `CORS_ALLOWED_ORIGINS`
2. Verify preflight OPTIONS request succeeds
3. Check response headers include:
   - `Access-Control-Allow-Origin`
   - `Access-Control-Allow-Methods`
   - `Access-Control-Allow-Headers`

### Issue: Session Validation Fails for SOAP

**Cause:** SessionAuthenticationInterceptor unable to reach IGameInternalService

**Solution:**
1. Verify TALOS Core service is running
2. Check `callcard.talos-core.session-validation-url` in config
3. Verify network connectivity to TALOS Core
4. Check firewall rules allow connection

## Next Steps

1. **Integration with Auth Service:**
   - Create authentication endpoint to issue JWT tokens
   - Integrate with existing IGameInternalService
   - Set up user provisioning

2. **Audit Logging:**
   - Enable AUDIT logger in logging config
   - Set up log aggregation (ELK, Splunk, etc.)
   - Configure retention policies for compliance

3. **Monitoring:**
   - Set up Prometheus metrics collection
   - Create dashboards for security events
   - Configure alerts for suspicious activity

4. **Advanced Features:**
   - Implement refresh tokens for long-lived sessions
   - Add multi-factor authentication (MFA)
   - Implement API rate limiting
   - Add request signing for inter-service communication

## Support

For security-related questions:
- Email: security@saicon.com
- Documentation: See SECURITY_CONFIGURATION.md
- Architecture: architecture@saicon.com
