package com.saicon.callcard.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SecurityException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT Token Provider for CallCard microservice.
 *
 * Responsibilities:
 * - Generate JWT tokens from authenticated principals
 * - Validate JWT tokens
 * - Extract claims from tokens
 * - Handle token expiration and signature verification
 *
 * Configuration:
 * - jwt.secret: Secret key for signing tokens (from application.yml)
 * - jwt.expiration: Token expiration time in milliseconds
 * - Algorithm: HS512 (HMAC with SHA-512)
 *
 * Token Claims:
 * - sub (subject): Username or user ID
 * - iat (issued at): Token creation time
 * - exp (expiration): Token expiration time
 * - roles: List of user roles (ROLE_USER, ROLE_ADMIN, etc.)
 * - organization: Organization ID (for multi-tenant support)
 *
 * Example:
 * <pre>
 * // Generate token from Authentication
 * String token = jwtTokenProvider.generateToken(authentication);
 *
 * // Validate token
 * boolean isValid = jwtTokenProvider.validateToken(token);
 *
 * // Extract username
 * String username = jwtTokenProvider.getUsernameFromToken(token);
 *
 * // Extract all claims
 * Claims claims = jwtTokenProvider.getClaimsFromToken(token);
 * </pre>
 *
 * @see JwtAuthenticationFilter
 * @see SecurityConfig
 */
@Component
public class JwtTokenProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${jwt.secret:your-secret-key-change-in-production}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}") // Default: 24 hours
    private long jwtExpirationMs;

    /**
     * Generate JWT token from authentication
     *
     * @param authentication Authenticated principal with user details and roles
     * @return JWT token string
     */
    public String generateToken(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        String username = null;
        List<String> roles = null;

        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            username = userDetails.getUsername();
            roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        } else {
            username = principal.toString();
            roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        }

        return generateTokenFromUsername(username, roles);
    }

    /**
     * Generate JWT token from username and roles
     *
     * @param username Username or user ID
     * @param roles List of user roles
     * @return JWT token string
     */
    public String generateTokenFromUsername(String username, List<String> roles) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        SecretKeySpec key = new SecretKeySpec(
            jwtSecret.getBytes(),
            SignatureAlgorithm.HS512.getJcaName(),
            null
        );

        return Jwts.builder()
            .setSubject(username)
            .claim("roles", roles)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(key, SignatureAlgorithm.HS512)
            .compact();
    }

    /**
     * Get username from JWT token
     *
     * @param token JWT token string
     * @return Username (subject claim)
     */
    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    /**
     * Get roles from JWT token
     *
     * @param token JWT token string
     * @return List of roles
     */
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return (List<String>) claims.get("roles");
    }

    /**
     * Get all claims from JWT token
     *
     * @param token JWT token string
     * @return Claims object containing all token data
     */
    public Claims getClaimsFromToken(String token) {
        try {
            SecretKeySpec key = new SecretKeySpec(
                jwtSecret.getBytes(),
                SignatureAlgorithm.HS512.getJcaName(),
                null
            );

            return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        } catch (SecurityException ex) {
            LOGGER.error("Invalid JWT signature: {}", ex.getMessage());
            throw ex;
        } catch (MalformedJwtException ex) {
            LOGGER.error("Invalid JWT token: {}", ex.getMessage());
            throw ex;
        } catch (ExpiredJwtException ex) {
            LOGGER.error("Expired JWT token: {}", ex.getMessage());
            throw ex;
        } catch (UnsupportedJwtException ex) {
            LOGGER.error("Unsupported JWT token: {}", ex.getMessage());
            throw ex;
        } catch (IllegalArgumentException ex) {
            LOGGER.error("JWT claims string is empty: {}", ex.getMessage());
            throw ex;
        }
    }

    /**
     * Validate JWT token
     *
     * Checks:
     * - Signature validity
     * - Token not expired
     * - Token format correctness
     *
     * @param token JWT token string
     * @return true if token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            SecretKeySpec key = new SecretKeySpec(
                jwtSecret.getBytes(),
                SignatureAlgorithm.HS512.getJcaName(),
                null
            );

            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);

            return true;
        } catch (SecurityException ex) {
            LOGGER.error("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            LOGGER.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            LOGGER.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            LOGGER.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            LOGGER.error("JWT claims string is empty: {}", ex.getMessage());
        } catch (Exception ex) {
            LOGGER.error("Unexpected error validating JWT token: {}", ex.getMessage());
        }

        return false;
    }

    /**
     * Check if token is expired
     *
     * @param token JWT token string
     * @return true if token is expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException ex) {
            return true;
        } catch (Exception ex) {
            LOGGER.warn("Error checking token expiration: {}", ex.getMessage());
            return true; // Treat any error as expired for security
        }
    }

    /**
     * Get token expiration time
     *
     * @param token JWT token string
     * @return Expiration date
     */
    public Date getTokenExpirationTime(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getExpiration();
        } catch (Exception ex) {
            LOGGER.warn("Error getting token expiration: {}", ex.getMessage());
            return null;
        }
    }
}
