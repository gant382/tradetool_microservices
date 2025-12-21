package com.saicon.games.callcard.components.external;

/**
 * Stub interface for User Session Management.
 * Session validation is handled by TALOS Core external SOAP call.
 */
public interface IUserSessionManagement {
    
    /**
     * Validate a session ID.
     * @param sessionId The session ID to validate
     * @return true if session is valid
     */
    default boolean validateSession(String sessionId) {
        throw new UnsupportedOperationException(
            "Session validation should use GameInternalServiceClient for TALOS Core integration");
    }
}
