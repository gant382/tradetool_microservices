package com.saicon.callcard.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Authentication Entry Point for CallCard microservice.
 *
 * Responsibilities:
 * - Handle unauthenticated requests to protected endpoints
 * - Return JSON error response for REST endpoints
 * - Log authentication failures for security auditing
 *
 * Response Format:
 * <pre>
 * HTTP/1.1 401 Unauthorized
 * Content-Type: application/json
 *
 * {
 *   "timestamp": "2025-01-15T10:30:00Z",
 *   "status": 401,
 *   "error": "Unauthorized",
 *   "message": "Full authentication is required to access this resource",
 *   "path": "/callcard/rest/callcards"
 * }
 * </pre>
 *
 * Error Cases:
 * 1. Missing Authorization header - No JWT token provided
 * 2. Invalid Bearer token format - Token doesn't start with "Bearer "
 * 3. Expired token - Token's exp claim is in the past
 * 4. Invalid signature - Token signature doesn't match secret key
 * 5. Malformed token - Token is not valid JWT format
 *
 * Logging:
 * - Logs IP address of requester for security audit trail
 * - Logs requested resource path
 * - Logs reason for authentication failure
 *
 * @see SecurityConfig
 * @see JwtAuthenticationFilter
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Commence authentication error handling
     *
     * Called when unauthenticated user attempts to access protected resource.
     * Returns JSON error response instead of default HTTP 401 page.
     *
     * @param request HTTP request
     * @param response HTTP response
     * @param authException Authentication exception details
     * @throws IOException IO error when writing response
     * @throws ServletException Servlet error
     */
    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authException
    ) throws IOException, ServletException {

        // Log authentication failure
        String clientIp = getClientIp(request);
        String requestPath = request.getRequestURI();

        LOGGER.warn(
            "Unauthorized access attempt - IP: {}, Path: {}, Reason: {}",
            clientIp,
            requestPath,
            authException.getMessage()
        );

        // Set response content type and status
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // Build error response
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", System.currentTimeMillis());
        errorResponse.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        errorResponse.put("error", "Unauthorized");
        errorResponse.put("message", "Full authentication is required to access this resource");
        errorResponse.put("path", requestPath);

        // Write JSON response
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    /**
     * Get client IP address from request
     *
     * Checks X-Forwarded-For header first (for proxied requests),
     * then falls back to remote address.
     *
     * @param request HTTP request
     * @return Client IP address
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs, use the first one
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
