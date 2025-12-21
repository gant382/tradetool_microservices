package com.saicon.games.callcard.ws.external;

/**
 * DTO for user session information from TALOS Core platform
 * Stub for external service integration
 */
public class UserSessionDTOS {
    private String userSessionId;
    private String gameTypeId;
    private String applicationId;
    private BasicUserDTO user;

    public UserSessionDTOS() {
    }

    public String getUserSessionId() {
        return userSessionId;
    }

    public void setUserSessionId(String userSessionId) {
        this.userSessionId = userSessionId;
    }

    public String getGameTypeId() {
        return gameTypeId;
    }

    public void setGameTypeId(String gameTypeId) {
        this.gameTypeId = gameTypeId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public BasicUserDTO getUser() {
        return user;
    }

    public void setUser(BasicUserDTO user) {
        this.user = user;
    }
}
