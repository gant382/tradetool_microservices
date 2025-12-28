package com.saicon.callcard.config;

import com.saicon.callcard.security.JwtAuthenticationEntryPoint;
import com.saicon.callcard.security.JwtAuthenticationFilter;
import com.saicon.callcard.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

/**
 * Comprehensive security configuration for CallCard microservice.
 *
 * Features:
 * - JWT authentication via Authorization Bearer tokens
 * - CORS configuration for cross-origin requests
 * - CSRF disabled for API/SOAP endpoints
 * - Session-less stateless authentication
 * - Method-level security with @PreAuthorize/@PostAuthorize
 * - Role-based access control (RBAC)
 * - SOAP endpoint security via SessionAuthenticationInterceptor
 * - REST endpoint security via JwtAuthenticationFilter
 * - Actuator endpoint protection
 *
 * Request Flow:
 * 1. Client provides JWT token in Authorization: Bearer &lt;token&gt; header
 * 2. JwtAuthenticationFilter extracts and validates token
 * 3. SessionAuthenticationInterceptor validates SOAP header tokens
 * 4. Method-level @PreAuthorize checks role/permission
 * 5. Request processed with authenticated principal
 *
 * Security Endpoints:
 * - /actuator/health - Public (health checks)
 * - /actuator/info - Public (application info)
 * - /cxf/* - Protected (SOAP services)
 * - /rest/* - Protected (REST endpoints)
 * - /swagger-ui.html - Public (API documentation)
 * - /v3/api-docs - Public (OpenAPI specification)
 *
 * Token Structure:
 * - Issued by: IGameInternalService
 * - Format: JWT (JSON Web Token)
 * - Claims: userId, username, roles, organization
 * - Expiry: Configurable via application.yml
 *
 * Roles:
 * - ROLE_SYSTEM_ADMIN - Full system access
 * - ROLE_ORG_ADMIN - Organization administrator
 * - ROLE_MANAGER - Operational manager
 * - ROLE_USER - Basic user
 *
 * Example Usage:
 * <pre>
 * curl -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..." \
 *      http://localhost:8080/callcard/cxf/CallCardService
 * </pre>
 *
 * @see JwtAuthenticationFilter
 * @see JwtTokenProvider
 * @see SessionAuthenticationInterceptor
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
    securedEnabled = true,
    jsr250Enabled = true,
    prePostEnabled = true
)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * JWT Authentication Filter Bean
     * Extracts and validates JWT tokens from Authorization header
     *
     * Note: This implementation uses stateless JWT authentication.
     * Authentication is derived directly from token claims without
     * requiring UserDetailsService or database lookups.
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider);
    }

    /**
     * Password Encoder Bean
     * BCrypt with strength 12 for secure password hashing
     *
     * Note: Not required for JWT-only authentication but provided
     * for future use if username/password login is implemented.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Authentication Manager Bean
     * Required for authentication of credentials
     *
     * Note: Not used in current JWT-only implementation but provided
     * for future use if username/password login is implemented.
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    // Note: configure(AuthenticationManagerBuilder) method removed
    // JWT authentication is stateless and doesn't require UserDetailsService

    /**
     * CORS Configuration Source
     * Allows cross-origin requests from trusted domains
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        // TODO: Configure allowed origins based on environment
        corsConfiguration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*",      // Development
            "http://127.0.0.1:*",      // Local testing
            "https://*.saicon.com"     // Production (requires wildcards in Spring 6+)
        ));

        corsConfiguration.setAllowedMethods(Arrays.asList(
            HttpMethod.GET.name(),
            HttpMethod.POST.name(),
            HttpMethod.PUT.name(),
            HttpMethod.DELETE.name(),
            HttpMethod.OPTIONS.name(),
            HttpMethod.PATCH.name()
        ));

        corsConfiguration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "Accept",
            "X-Requested-With",
            "Cache-Control",
            "X-API-Version"
        ));

        corsConfiguration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Total-Count",
            "X-Page-Number",
            "X-Page-Size"
        ));

        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setMaxAge(3600L); // Pre-flight cache for 1 hour

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

    /**
     * Main security filter chain configuration
     *
     * Configures:
     * - CSRF protection (disabled for stateless API)
     * - CORS handling
     * - HTTP Basic auth (fallback for SOAP)
     * - JWT token validation
     * - Session management (stateless)
     * - Authorization rules
     * - Exception handling
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            // CORS Configuration
            .cors()
                .and()

            // CSRF Protection
            .csrf()
                .disable() // Disabled for stateless REST/SOAP APIs with token-based auth
                           // Re-enable if using form-based authentication

            // Exception Handling
            .exceptionHandling()
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .and()

            // Session Management
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // No HTTP sessions
                .and()

            // HTTP Basic Authentication
            .httpBasic()
                .and()

            // Authorization Rules
            .authorizeRequests()
                // Public endpoints - health checks and API documentation
                .antMatchers("/actuator/health/**").permitAll()
                .antMatchers("/actuator/info").permitAll()
                .antMatchers("/swagger-ui.html").permitAll()
                .antMatchers("/swagger-ui/**").permitAll()
                .antMatchers("/v3/api-docs/**").permitAll()
                .antMatchers("/v3/api-docs.yaml").permitAll()
                .antMatchers("/webjars/**").permitAll()

                // Health probe endpoints (Kubernetes/Docker)
                .antMatchers("/actuator/health/live").permitAll()
                .antMatchers("/actuator/health/ready").permitAll()

                // Protected endpoints - require authentication
                .antMatchers("/cxf/**").authenticated()
                .antMatchers("/rest/**").authenticated()
                .antMatchers("/actuator/**").authenticated() // Metrics, logging, etc.

                // Default: require authentication for any other requests
                .anyRequest().authenticated()
                .and()

            // Add JWT authentication filter
            .addFilterBefore(
                jwtAuthenticationFilter(),
                UsernamePasswordAuthenticationFilter.class
            );
    }
}
