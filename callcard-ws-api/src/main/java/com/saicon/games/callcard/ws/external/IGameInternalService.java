package com.saicon.games.callcard.ws.external;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

/**
 * Stub interface for TALOS Core IGameInternalService
 * This represents an external SOAP service that the microservice will call
 *
 * The actual implementation will be provided by a SOAP client pointing to
 * the TALOS Core platform Game_Server_WS deployment.
 */
@WebService(targetNamespace = "http://ws.games.internal.saicon.com/", name = "GameInternalService")
public interface IGameInternalService {

    /**
     * Get user session information by session ID
     * @param userSessionId The session ID
     * @return UserSessionDTOS with user and session details
     */
    @WebMethod(operationName = "getUserSession")
    UserSessionDTOS getUserSession(@WebParam(name = "userSessionId") String userSessionId);

    /**
     * Get active user session by session ID
     * @param userSessionId The session ID
     * @return UserSessionDTOS with user and session details
     */
    @WebMethod(operationName = "getActiveUserSession")
    UserSessionDTOS getActiveUserSession(@WebParam(name = "userSessionId") String userSessionId);

    // Add other methods as needed based on actual usage
    // This is a stub - methods will be added as requirements are discovered
}
