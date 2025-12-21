package com.saicon.games.callcard.ws.external;

/**
 * Basic user DTO from TALOS Core platform
 * Stub for external service integration
 */
public class BasicUserDTO {
    private String userId;
    private String userGroupId;
    private String username;
    private String email;

    public BasicUserDTO() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserGroupId() {
        return userGroupId;
    }

    public void setUserGroupId(String userGroupId) {
        this.userGroupId = userGroupId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
