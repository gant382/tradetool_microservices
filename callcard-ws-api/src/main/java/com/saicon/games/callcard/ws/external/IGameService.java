package com.saicon.games.callcard.ws.external;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

/**
 * Stub interface for TALOS Core IGameService
 * This represents an external SOAP service that the microservice will call
 *
 * The actual implementation will be provided by a SOAP client pointing to
 * the TALOS Core platform Game_Server_WS deployment.
 */
@WebService(targetNamespace = "http://ws.games.saicon.com/", name = "GameService")
public interface IGameService {

    @WebMethod(operationName = "ping")
    String ping();

    // Add other methods as needed based on actual usage
    // This is a stub - methods will be added as requirements are discovered
}
