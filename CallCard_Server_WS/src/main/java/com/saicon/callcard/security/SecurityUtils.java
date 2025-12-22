package com.saicon.callcard.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Security utility methods for CallCard microservice.
 *
 * Provides helper methods for:
 * - Getting current authenticated user
 * - Checking user roles
 * - Accessing security context information
 *
 * Usage:
 * <pre>
 * // Get current username
 * String username = SecurityUtils.getCurrentUsername();
 *
 * // Check if user has specific role
 * if (SecurityUtils.hasRole("ROLE_ADMIN")) {
 *     // Admin-specific logic
 * }
 *
 * // Check if user has any of the provided roles
 * if (SecurityUtils.hasAnyRole("ROLE_ADMIN", "ROLE_MANAGER")) {
 *     // Admin or Manager specific logic
 * }
 *
 * // Get full authentication object
 * Authentication auth = SecurityUtils.getAuthentication();
 * </pre>
 *
 * @see SecurityConfig
 * @see JwtAuthenticationFilter
 */
public final class SecurityUtils {

    private SecurityUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Get current authentication from security context
     *
     * @return Authentication object or null if not authenticated
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Get currently authenticated username
     *
     * @return Username of authenticated user or null if not authenticated
     */
    public static String getCurrentUsername() {
        Authentication authentication = getAuthentication();

        if (authentication == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }

        return principal.toString();
    }

    /**
     * Check if current user is authenticated
     *
     * @return true if user is authenticated, false otherwise
     */
    public static boolean isAuthenticated() {
        Authentication authentication = getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }

    /**
     * Check if current user has specific role
     *
     * @param role Role to check (e.g., "ROLE_ADMIN", "ROLE_USER")
     * @return true if user has role, false otherwise
     */
    public static boolean hasRole(String role) {
        Authentication authentication = getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals(role));
    }

    /**
     * Check if current user has any of the provided roles
     *
     * @param roles Roles to check (e.g., "ROLE_ADMIN", "ROLE_MANAGER")
     * @return true if user has any of the roles, false otherwise
     */
    public static boolean hasAnyRole(String... roles) {
        Authentication authentication = getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        for (String role : roles) {
            if (authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals(role))) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if current user has all of the provided roles
     *
     * @param roles Roles to check (e.g., "ROLE_ADMIN", "ROLE_AUDIT")
     * @return true if user has all roles, false otherwise
     */
    public static boolean hasAllRoles(String... roles) {
        Authentication authentication = getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        for (String role : roles) {
            if (authentication.getAuthorities().stream()
                .noneMatch(auth -> auth.getAuthority().equals(role))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Get count of authorities for current user
     *
     * @return Number of authorities (roles)
     */
    public static int getAuthoritiesCount() {
        Authentication authentication = getAuthentication();

        if (authentication == null) {
            return 0;
        }

        return authentication.getAuthorities().size();
    }

    /**
     * Get all roles for current user
     *
     * @return Comma-separated list of roles or empty string
     */
    public static String getAllRoles() {
        Authentication authentication = getAuthentication();

        if (authentication == null || authentication.getAuthorities().isEmpty()) {
            return "";
        }

        return authentication.getAuthorities().stream()
            .map(auth -> auth.getAuthority())
            .reduce((a, b) -> a + ", " + b)
            .orElse("");
    }
}
