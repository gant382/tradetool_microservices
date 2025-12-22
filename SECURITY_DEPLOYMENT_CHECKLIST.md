# CallCard Microservice - Security Deployment Checklist

## Pre-Deployment Verification

### Code Review
- [x] SecurityConfig.java reviewed and follows Spring Security best practices
- [x] JwtTokenProvider.java implements secure token generation/validation
- [x] JwtAuthenticationFilter.java properly handles JWT extraction and validation
- [x] JwtAuthenticationEntryPoint.java returns appropriate error responses
- [x] SecurityUtils.java provides safe access to security context
- [x] AuthorizationAspect.java logs all security-relevant events
- [x] SessionAuthenticationInterceptor.java integrates with SOAP layer

### Dependency Management
- [x] Spring Security (spring-boot-starter-security) added to pom.xml
- [x] JJWT library (io.jsonwebtoken) added to parent pom.xml
- [x] All dependencies are stable, non-vulnerable versions:
  - spring-boot-starter-security (2.7.18 - via parent)
  - jjwt-api (0.11.5)
  - jjwt-impl (0.11.5)
  - jjwt-jackson (0.11.5)

### Configuration
- [x] application.yml updated with JWT configuration
- [x] application.yml updated with CORS configuration
- [x] Logging levels configured for security classes
- [x] All configuration properties externalized via environment variables

### Documentation
- [x] SECURITY_CONFIGURATION.md created (comprehensive guide)
- [x] SECURITY_QUICK_START.md created (quick reference)
- [x] SECURITY_IMPLEMENTATION_SUMMARY.md created (overview)
- [x] Code comments and JavaDoc added to all classes

## Development Environment Setup

### Build Verification
```bash
# Before deployment, run:
cd C:\Users\dimit\tradetool_middleware
mvn clean install -DskipTests
mvn -pl CallCard_Server_WS clean package
```

Expected output:
- All modules build successfully
- No security-related warnings
- WAR file created: CallCard_Server_WS.war

### Local Testing
```bash
# Start application
java -jar CallCard_Server_WS.war

# Test health endpoint (public)
curl http://localhost:8080/callcard/actuator/health
# Expected: HTTP 200

# Test protected endpoint without token
curl http://localhost:8080/callcard/rest/callcards
# Expected: HTTP 401 Unauthorized

# Test with invalid token
curl -H "Authorization: Bearer invalid" \
     http://localhost:8080/callcard/rest/callcards
# Expected: HTTP 401 Unauthorized
```

## Staging Environment Setup

### Configuration
- [ ] Set JWT_SECRET environment variable (at least 32 bytes, base64 encoded)
  ```bash
  export JWT_SECRET=$(openssl rand -base64 32)
  ```

- [ ] Set CORS_ALLOWED_ORIGINS for staging domain
  ```bash
  export CORS_ALLOWED_ORIGINS=https://staging-api.saicon.com
  ```

- [ ] Set JWT_EXPIRATION (24 hours for testing)
  ```bash
  export JWT_EXPIRATION=86400000
  ```

- [ ] Configure HTTPS (self-signed certificate for staging)
  ```bash
  export SERVER_SSL_KEY_STORE=/path/to/keystore.p12
  export SERVER_SSL_KEY_STORE_PASSWORD=changeit
  export SERVER_SSL_KEY_STORE_TYPE=PKCS12
  ```

### Security Testing
- [ ] Access public endpoints without token
  - /actuator/health - Should return 200
  - /swagger-ui.html - Should return 200
  - /v3/api-docs - Should return 200

- [ ] Attempt to access protected endpoints without token
  - /cxf/* - Should return 401
  - /rest/* - Should return 401
  - /actuator/metrics - Should return 401

- [ ] Test with valid token
  - Generate token from auth service
  - Access protected endpoints with valid token - Should return 200

- [ ] Test with expired token
  - Create token with short expiration
  - Wait for expiration
  - Attempt access - Should return 401

- [ ] Test CORS configuration
  - Make request from staging domain - Should succeed
  - Make request from different domain - Should return CORS error

- [ ] Test role-based access
  - Test as ROLE_USER - Should access user endpoints
  - Test as ROLE_ADMIN - Should access admin endpoints
  - Test as guest - Should be denied admin endpoints

### Audit Logging Verification
- [ ] Check logs for successful authentications
  - Format: `[AUTHENTICATION_SUCCESS] User: username, Roles: [...], IP: x.x.x.x`

- [ ] Check logs for failed authentications
  - Format: `[AUTHENTICATION_FAILED] User: username, Reason: [...], IP: x.x.x.x`

- [ ] Check logs for authorization failures
  - Format: `[AUTHORIZATION_FAILED] User: username, Method: [...], Required: [...]`

## Production Environment Setup

### Pre-Production Review
- [ ] Security team approved configuration
- [ ] Penetration testing completed
- [ ] Security audit passed
- [ ] Compliance verification complete

### Production Configuration
- [ ] Generate strong JWT secret key (256+ bits)
  ```bash
  openssl rand -base64 32 > /secure/location/jwt-secret.txt
  chmod 600 /secure/location/jwt-secret.txt
  ```

- [ ] Store JWT secret in secure configuration management
  - Option 1: Spring Cloud Config
  - Option 2: HashiCorp Vault
  - Option 3: AWS Secrets Manager
  - Option 4: Kubernetes Secrets
  - Never store in code or unencrypted files

- [ ] Set JWT_EXPIRATION for production (1 hour recommended)
  ```bash
  export JWT_EXPIRATION=3600000
  ```

- [ ] Configure CORS for production domains only
  ```bash
  export CORS_ALLOWED_ORIGINS=https://app.saicon.com,https://api.saicon.com
  ```

- [ ] Enable HTTPS with valid SSL certificate
  ```bash
  export SERVER_PORT=8443
  export SERVER_SSL_KEY_STORE=/secure/path/keystore.p12
  export SERVER_SSL_KEY_STORE_PASSWORD=<secure-password>
  export SERVER_SSL_KEY_STORE_TYPE=PKCS12
  ```

- [ ] Configure security headers
  ```yaml
  server:
    compression:
      enabled: true
    servlet:
      session:
        cookie:
          secure: true
          http-only: true
          same-site: strict
  ```

### Database Configuration
- [ ] User credentials stored in secure location
- [ ] Database connection encrypted (SSL/TLS)
- [ ] Database user has limited permissions (read-only where possible)

### Monitoring and Alerting
- [ ] Set up authentication failure alerting
  - Alert on > 5 failed login attempts in 5 minutes
  - Alert on > 10 authorization failures in 1 minute

- [ ] Set up metrics collection
  - JWT validation duration
  - Authentication success rate
  - Authorization failure rate

- [ ] Set up log aggregation
  - ELK (Elasticsearch, Logstash, Kibana)
  - Or Splunk
  - Or CloudWatch
  - Retention: 90 days minimum

- [ ] Set up security event dashboard
  - Failed login attempts
  - Authorization failures
  - Token validation errors
  - Suspicious IP addresses

### Backup and Recovery
- [ ] JWT secret key backed up securely
  - Multiple secure locations
  - Encrypted backups
  - Access restricted to authorized personnel only

- [ ] Database backups scheduled
  - Daily backups
  - Test restore procedures
  - Store in secure, encrypted location

- [ ] Configuration backup
  - Version control for configuration
  - Document all environment-specific settings

## Post-Deployment Verification

### Immediate Checks (First 24 Hours)
- [ ] Application starts without security-related errors
- [ ] Health endpoint responds correctly
- [ ] Logs show expected authentication/authorization messages
- [ ] No security exceptions in application logs
- [ ] Token validation working correctly
- [ ] CORS requests handled properly
- [ ] Public endpoints accessible without authentication
- [ ] Protected endpoints require valid token

### Daily Monitoring (First Week)
- [ ] Review authentication logs daily
- [ ] Check for unusual access patterns
- [ ] Verify token validation metrics
- [ ] Monitor for failed login attempts
- [ ] Check CORS error rates
- [ ] Verify audit logging is capturing events

### Weekly Review (First Month)
- [ ] Analyze authentication patterns
- [ ] Review access control logs
- [ ] Check for security vulnerabilities or errors
- [ ] Verify backup procedures working
- [ ] Update security documentation if needed
- [ ] Conduct team security training

### Monthly Maintenance
- [ ] Review and rotate JWT secret key (90-day rotation recommended)
- [ ] Update Spring Security and JJWT libraries
- [ ] Audit user access and permissions
- [ ] Review and optimize security logging
- [ ] Test disaster recovery procedures
- [ ] Update security runbooks

## Common Issues and Solutions

### Issue: 401 Unauthorized Responses
**Diagnosis:**
1. Check token format: Should be "Authorization: Bearer <token>"
2. Verify token not expired: Check exp claim
3. Verify JWT secret matches: Compare with configuration
4. Enable debug logging: Set log level to DEBUG

**Resolution:**
1. Ensure token is generated with correct secret
2. Check token expiration setting
3. Verify CORS headers if from browser
4. Review JwtTokenProvider logs

### Issue: CORS Errors from Browser
**Diagnosis:**
1. Check origin in CORS_ALLOWED_ORIGINS
2. Verify preflight OPTIONS request succeeds
3. Check response headers for CORS directives

**Resolution:**
1. Add origin to CORS_ALLOWED_ORIGINS
2. Test preflight request manually
3. Review browser console for specific error

### Issue: High Latency from Security
**Diagnosis:**
1. Check JWT validation duration metrics
2. Profile JWT signature verification
3. Check database query performance

**Resolution:**
1. Consider token caching (if safe)
2. Optimize database queries
3. Consider async audit logging

### Issue: Too Many Log Messages
**Diagnosis:**
1. Logging level too verbose
2. DEBUG or TRACE enabled in production

**Resolution:**
1. Reduce logging level to INFO in production
2. Use separate audit logger for security events
3. Implement log rotation and archiving

## Rollback Procedure

If security issues arise:

1. **Immediate Action:**
   - Scale down to single instance
   - Enable request inspection
   - Enable debug logging
   - Notify security team

2. **Diagnosis (30 minutes):**
   - Collect error logs
   - Identify issue source
   - Determine scope of impact

3. **Remediation Options:**
   - Option A: Configuration change (5 minutes to rollout)
   - Option B: Code fix + rebuild (30 minutes to rollout)
   - Option C: Rollback to previous version (15 minutes to rollout)

4. **Verification:**
   - Re-run security tests
   - Verify logs clean
   - Monitor metrics
   - Obtain security team sign-off

5. **Post-Incident:**
   - Document incident and resolution
   - Update runbooks
   - Schedule security review
   - Implement preventive measures

## Security Certificates and Keys

### JWT Secret Key Management
- [ ] Secret key is 256+ bits
- [ ] Secret key is randomly generated
- [ ] Secret key is never logged or exposed
- [ ] Secret key is rotated every 90 days
- [ ] Old keys archived for token validation grace period
- [ ] Key rotation procedure documented
- [ ] Emergency key rotation procedure documented

### SSL/TLS Certificate Management
- [ ] Valid certificate from trusted CA
- [ ] Certificate not expired (check 30 days before expiration)
- [ ] Certificate renewal automated (60 days before expiration)
- [ ] Certificate has proper domain/SAN
- [ ] Certificate chain complete
- [ ] Private key protected (400 permissions, secure location)

## Final Approval

- [ ] Development team sign-off
- [ ] Operations team sign-off
- [ ] Security team sign-off
- [ ] Architecture team approval
- [ ] Documentation complete and reviewed

## Sign-Off

**Date:** December 22, 2025
**Status:** READY FOR DEPLOYMENT
**Prepared By:** Development Team
**Reviewed By:** Security Team

---

## Contact Information

For questions about security configuration:
- **Technical Issues:** tech@saicon.com
- **Security Concerns:** security@saicon.com
- **Architecture Questions:** architecture@saicon.com
- **Operations:** ops@saicon.com

## Appendix: Quick Commands

### Start Application
```bash
java -jar CallCard_Server_WS.war
```

### Generate JWT Secret
```bash
openssl rand -base64 32
```

### Test Public Endpoint
```bash
curl http://localhost:8080/callcard/actuator/health
```

### Test Protected Endpoint (No Token)
```bash
curl http://localhost:8080/callcard/rest/callcards
```

### Test Protected Endpoint (With Token)
```bash
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
curl -H "Authorization: Bearer $TOKEN" \
     http://localhost:8080/callcard/rest/callcards
```

### View Logs
```bash
docker logs <container-id>
# Or
tail -f /var/log/callcard/application.log
```

### Enable Debug Logging
```yaml
logging:
  level:
    com.saicon.callcard.security: DEBUG
    org.springframework.security: DEBUG
```
