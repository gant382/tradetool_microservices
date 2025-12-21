package com.saicon.callcard.config;

import com.saicon.games.callcard.ws.ICallCardService;
import com.saicon.games.callcard.ws.ICallCardStatisticsService;
import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.ext.logging.LoggingFeature;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.ws.Endpoint;

/**
 * Apache CXF configuration for SOAP web services.
 *
 * Configures:
 * - CXF Servlet at /cxf/*
 * - SOAP endpoints for CallCard services
 * - FastInfoset and GZIP compression (via @FastInfoset, @GZIP annotations)
 * - Request/response logging
 */
@Configuration
public class CxfConfiguration {

    @Autowired
    private ICallCardService callCardService;

    @Autowired
    private ICallCardStatisticsService callCardStatisticsService;

    /**
     * Register CXF Servlet
     */
    @Bean
    public ServletRegistrationBean<CXFServlet> cxfServlet() {
        ServletRegistrationBean<CXFServlet> servletRegistrationBean =
            new ServletRegistrationBean<>(new CXFServlet(), "/cxf/*");
        servletRegistrationBean.setLoadOnStartup(1);
        return servletRegistrationBean;
    }

    /**
     * CXF Bus configuration
     */
    @Bean(name = Bus.DEFAULT_BUS_ID)
    public SpringBus springBus() {
        SpringBus bus = new SpringBus();
        bus.getFeatures().add(loggingFeature());
        return bus;
    }

    /**
     * Logging feature for SOAP requests/responses
     */
    @Bean
    public LoggingFeature loggingFeature() {
        LoggingFeature loggingFeature = new LoggingFeature();
        loggingFeature.setPrettyLogging(true);
        loggingFeature.setVerbose(true);
        return loggingFeature;
    }

    /**
     * CallCard SOAP Service Endpoint
     *
     * Available at: http://host:port/cxf/CallCardService?wsdl
     */
    @Bean
    public Endpoint callCardServiceEndpoint() {
        EndpointImpl endpoint = new EndpointImpl(springBus(), callCardService);
        endpoint.publish("/CallCardService");

        // Enable FastInfoset (binary XML) and GZIP compression
        // These are typically configured via @FastInfoset and @GZIP annotations on the service implementation

        return endpoint;
    }

    /**
     * CallCard Statistics SOAP Service Endpoint
     *
     * Available at: http://host:port/cxf/CallCardStatisticsService?wsdl
     */
    @Bean
    public Endpoint callCardStatisticsServiceEndpoint() {
        EndpointImpl endpoint = new EndpointImpl(springBus(), callCardStatisticsService);
        endpoint.publish("/CallCardStatisticsService");

        // Enable FastInfoset (binary XML) and GZIP compression
        // These are typically configured via @FastInfoset and @GZIP annotations on the service implementation

        return endpoint;
    }
}
