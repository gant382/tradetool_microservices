package com.saicon.callcard.config;

import com.saicon.games.callcard.resources.CallCardResources;
import com.saicon.games.callcard.resources.CallCardStatisticsResources;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.springframework.context.annotation.Configuration;

import javax.ws.rs.ApplicationPath;

/**
 * Jersey (JAX-RS) configuration for REST endpoints.
 *
 * Configures:
 * - REST resources (CallCardResources)
 * - JSON serialization via Jackson
 * - Request/response filters
 *
 * REST endpoints available at: /rest/*
 */
@Configuration
@ApplicationPath("/rest")
public class JerseyConfiguration extends ResourceConfig {

    public JerseyConfiguration() {
        // Register REST resource classes
        register(CallCardResources.class);
        register(CallCardStatisticsResources.class);

        // Register Jackson for JSON serialization/deserialization
        register(JacksonFeature.class);

        // Register filters and interceptors
        // register(SessionAuthenticationFilter.class); // Add when implementing authentication

        // Enable WADL (Web Application Description Language)
        property("jersey.config.server.wadl.disableWadl", "false");

        // Enable logging for debugging
        property("jersey.config.server.tracing.type", "ALL");
        property("jersey.config.server.tracing.threshold", "VERBOSE");
    }
}
