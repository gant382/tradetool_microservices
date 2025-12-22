package com.saicon.callcard.security;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * AOP aspect for authorization and audit logging.
 *
 * Responsibilities:
 * - Log method-level authorization events
 * - Track user access patterns
 * - Provide security audit trail
 * - Monitor role-based access
 *
 * Monitored Methods:
 * - All methods in com.saicon.callcard.service package
 * - All methods in com.saicon.games.callcard.service package
 * - All methods marked with @Auditable annotation
 *
 * Logged Information:
 * - Method name and class
 * - Current authenticated user
 * - User roles
 * - Method parameters (non-sensitive)
 * - Timestamp
 *
 * Example Log Output:
 * <pre>
 * INFO: [AUTHORIZATION] User: admin, Roles: [ROLE_ADMIN, ROLE_USER], Method: CallCardServiceImpl.updateCallCard, Params: [cardId=123]
 * </pre>
 *
 * Security Considerations:
 * - Never log sensitive data (passwords, tokens, PII)
 * - Include enough information for audit compliance
 * - Use appropriate log levels (DEBUG for normal, WARN/ERROR for suspicious)
 * - Ensure logs are not accessible to unauthorized users
 *
 * @see SecurityConfig
 * @see SecurityUtils
 */
@Aspect
@Component
public class AuthorizationAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationAspect.class);
    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("AUDIT");

    /**
     * Log method access for service layer methods
     *
     * Pointcut: All methods in com.saicon.callcard.service and com.saicon.games.callcard.service packages
     *
     * @param joinPoint AOP join point
     */
    @Before("execution(* com.saicon.callcard.service..*(..)) || execution(* com.saicon.games.callcard.service..*(..))")
    public void logServiceMethodAccess(JoinPoint joinPoint) {
        try {
            String methodName = joinPoint.getSignature().getName();
            String className = joinPoint.getTarget().getClass().getSimpleName();

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                String roles = SecurityUtils.getAllRoles();

                LOGGER.debug(
                    "[SERVICE_ACCESS] User: {}, Roles: {}, Method: {}.{}, Args count: {}",
                    username,
                    roles,
                    className,
                    methodName,
                    joinPoint.getArgs().length
                );

                // Audit log for sensitive operations
                auditServiceAccess(username, className, methodName);
            } else {
                LOGGER.debug(
                    "[SERVICE_ACCESS] Anonymous, Method: {}.{}",
                    className,
                    methodName
                );
            }
        } catch (Exception ex) {
            LOGGER.error("Error logging service method access: {}", ex.getMessage());
        }
    }

    /**
     * Log protected REST endpoint access
     *
     * Pointcut: All methods in com.saicon.callcard.controller and com.saicon.games.callcard.ws packages
     *
     * @param joinPoint AOP join point
     */
    @Before("execution(* com.saicon.callcard.controller..*(..)) || execution(* com.saicon.games.callcard.ws..*(..))")
    public void logControllerMethodAccess(JoinPoint joinPoint) {
        try {
            String methodName = joinPoint.getSignature().getName();
            String className = joinPoint.getTarget().getClass().getSimpleName();

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                int roleCount = SecurityUtils.getAuthoritiesCount();

                LOGGER.debug(
                    "[ENDPOINT_ACCESS] User: {}, Roles: {}, Endpoint: {}.{}, Args: {}",
                    username,
                    roleCount,
                    className,
                    methodName,
                    joinPoint.getArgs().length
                );

                // Audit log for all endpoint access
                auditEndpointAccess(username, className, methodName);
            }
        } catch (Exception ex) {
            LOGGER.error("Error logging controller method access: {}", ex.getMessage());
        }
    }

    /**
     * Log access to administrative/sensitive operations
     *
     * @param username Authenticated username
     * @param className Target class name
     * @param methodName Target method name
     */
    private void auditServiceAccess(String username, String className, String methodName) {
        // Log sensitive operations: create, update, delete, export
        if (methodName.matches("(create|update|delete|remove|export|import|process).*")) {
            AUDIT_LOGGER.info(
                "SENSITIVE_OPERATION - User: {}, Class: {}, Method: {}, Timestamp: {}",
                username,
                className,
                methodName,
                System.currentTimeMillis()
            );
        }
    }

    /**
     * Log endpoint access for audit trail
     *
     * @param username Authenticated username
     * @param className Target class name
     * @param methodName Target method name
     */
    private void auditEndpointAccess(String username, String className, String methodName) {
        AUDIT_LOGGER.debug(
            "ENDPOINT_ACCESSED - User: {}, Endpoint: {}.{}, Timestamp: {}",
            username,
            className,
            methodName,
            System.currentTimeMillis()
        );
    }

    /**
     * Log authorization failures
     *
     * Called when @PreAuthorize check fails
     *
     * @param username Attempted username
     * @param method Method name
     * @param requiredRoles Required roles
     */
    public static void logAuthorizationFailure(String username, String method, String requiredRoles) {
        AUDIT_LOGGER.warn(
            "AUTHORIZATION_FAILED - User: {}, Method: {}, Required: {}, Timestamp: {}",
            username,
            method,
            requiredRoles,
            System.currentTimeMillis()
        );
    }

    /**
     * Log suspicious authentication attempts
     *
     * Examples:
     * - Multiple failed login attempts
     * - Access from unusual locations
     * - Privilege escalation attempts
     *
     * @param username Attempted username
     * @param reason Reason for suspicion
     * @param clientIp Client IP address
     */
    public static void logSuspiciousActivity(String username, String reason, String clientIp) {
        AUDIT_LOGGER.error(
            "SUSPICIOUS_ACTIVITY - User: {}, Reason: {}, IP: {}, Timestamp: {}",
            username,
            reason,
            clientIp,
            System.currentTimeMillis()
        );
    }

    /**
     * Log successful authentication
     *
     * @param username Authenticated username
     * @param roles User roles
     * @param clientIp Client IP address
     */
    public static void logSuccessfulAuthentication(String username, String roles, String clientIp) {
        AUDIT_LOGGER.info(
            "AUTHENTICATION_SUCCESS - User: {}, Roles: {}, IP: {}, Timestamp: {}",
            username,
            roles,
            clientIp,
            System.currentTimeMillis()
        );
    }

    /**
     * Log failed authentication attempts
     *
     * @param username Attempted username
     * @param reason Failure reason
     * @param clientIp Client IP address
     */
    public static void logFailedAuthentication(String username, String reason, String clientIp) {
        AUDIT_LOGGER.warn(
            "AUTHENTICATION_FAILED - User: {}, Reason: {}, IP: {}, Timestamp: {}",
            username,
            reason,
            clientIp,
            System.currentTimeMillis()
        );
    }
}
