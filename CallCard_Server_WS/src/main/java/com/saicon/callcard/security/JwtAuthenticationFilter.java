package com.saicon.callcard.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT Authentication Filter for CallCard microservice.
 *
 * Responsibilities:
 * - Extract JWT token from HTTP Authorization header
 * - Validate JWT token signature and expiration
 * - Create Authentication object from token claims
 * - Set authentication in Spring Security context
 *
 * Request Flow:
 * 1. Extract Bearer token from "Authorization: Bearer &lt;token&gt;" header
 * 2. Validate token using JwtTokenProvider
 * 3. Extract username and roles from token claims
 * 4. Create UsernamePasswordAuthenticationToken
 * 5. Set token in SecurityContext for downstream processing
 *
 * Token Header Format:
 * <pre>
 * Authorization: Bearer eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...
 * </pre>
 *
 * Error Handling:
 * - Missing token: Logs warning, continues (endpoint determines access)
 * - Invalid token: Logs warning, continues (endpoint determines access)
 * - Expired token: Logs warning, continues (endpoint determines access)
 * - Generic error: Logs error, continues (fail-open principle)
 *
 * Note: This filter uses "fail-open" principle. Invalid tokens are handled
 * by downstream @PreAuthorize annotations rather than aborting the request here.
 *
 * @see JwtTokenProvider
 * @see SecurityConfig
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider tokenProvider;

    /**
     * Constructor
     *
     * @param tokenProvider JWT token provider for validation
     */
    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    /**
     * Filter execution - runs once per request
     *
     * @param request HTTP request
     * @param response HTTP response
     * @param filterChain Filter chain
     * @throws ServletException Servlet exception
     * @throws IOException IO exception
     */
    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // Extract JWT token from Authorization header
            String jwt = extractJwtFromRequest(request);

            // Validate token and set authentication
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                setAuthenticationFromToken(jwt);
            } else if (StringUtils.hasText(jwt)) {
                LOGGER.warn("Invalid JWT token in request from: {}", getClientIp(request));
            }

        } catch (Exception ex) {
            LOGGER.error("Could not set user authentication in security context: {}", ex.getMessage());
            // Continue processing - let @PreAuthorize handle access control
        }

        // Continue filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header
     *
     * Expected format: "Authorization: Bearer &lt;token&gt;"
     *
     * @param request HTTP request
     * @return JWT token or null if not found
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(authHeader) && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }

        return null;
    }

    /**
     * Set authentication in security context from JWT token
     *
     * @param token JWT token string
     */
    private void setAuthenticationFromToken(String token) {
        try {
            String username = tokenProvider.getUsernameFromToken(token);
            List<String> rolesList = tokenProvider.getRolesFromToken(token);

            // Convert roles to GrantedAuthority objects
            List<SimpleGrantedAuthority> authorities = rolesList.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

            // Create authentication token
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    username,
                    null, // No credentials needed - token is already validated
                    authorities
                );

            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            LOGGER.debug("Set user authentication for username: {} with roles: {}", username, rolesList);

        } catch (Exception ex) {
            LOGGER.error("Failed to set user authentication: {}", ex.getMessage());
            // Clear authentication on error
            SecurityContextHolder.clearContext();
        }
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
        if (StringUtils.hasText(xForwardedFor)) {
            // X-Forwarded-For can contain multiple IPs, use the first one
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
