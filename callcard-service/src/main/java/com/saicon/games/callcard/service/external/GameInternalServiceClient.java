package com.saicon.games.callcard.service.external;

import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SOAP client configuration for IGameInternalService.
 *
 * Creates a JAX-WS proxy client to communicate with the main Game Server
 * for authentication, session validation, and user management operations.
 *
 * Configuration properties:
 * - game.internal.service.url: WSDL URL for IGameInternalService
 *   Example: http://game-server:8080/Game_Server_WS/cxf/GAMEInternalService?wsdl
 */
@Configuration
public class GameInternalServiceClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameInternalServiceClient.class);

    @Value("${game.internal.service.url:http://localhost:8080/Game_Server_WS/cxf/GAMEInternalService}")
    private String gameInternalServiceUrl;

    @Value("${game.internal.service.timeout:30000}")
    private long timeout;

    /**
     * Create IGameInternalService SOAP client
     *
     * TODO: Replace with actual IGameInternalService interface when available
     */
    @Bean
    public Object gameInternalService() {
        LOGGER.info("Creating IGameInternalService SOAP client for URL: {}", gameInternalServiceUrl);

        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();

        // TODO: Set the actual service interface class
        // factory.setServiceClass(IGameInternalService.class);

        factory.setAddress(gameInternalServiceUrl);

        // Configure timeouts
        factory.getClientFactoryBean().getProperties().put("timeout", timeout);
        factory.getClientFactoryBean().getProperties().put("connectionTimeout", timeout);

        // Enable FastInfoset and GZIP if supported by server
        // factory.getFeatures().add(new org.apache.cxf.feature.FastInfosetFeature());
        // factory.getFeatures().add(new org.apache.cxf.transport.common.gzip.GZIPFeature());

        LOGGER.info("IGameInternalService client configured with timeout: {}ms", timeout);

        // TODO: Uncomment when IGameInternalService interface is available
        // return (IGameInternalService) factory.create();

        return factory.create();
    }
}
