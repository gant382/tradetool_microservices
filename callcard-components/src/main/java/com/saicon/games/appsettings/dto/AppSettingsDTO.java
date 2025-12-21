package com.saicon.games.appsettings.dto;

import java.io.Serializable;

public class AppSettingsDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String settingKey;
    private String settingValue;
    private String gameTypeId;
    private String userGroupId;

    public AppSettingsDTO() {
    }

    public AppSettingsDTO(String settingKey, String settingValue, String gameTypeId, String userGroupId) {
        this.settingKey = settingKey;
        this.settingValue = settingValue;
        this.gameTypeId = gameTypeId;
        this.userGroupId = userGroupId;
    }

    public String getSettingKey() {
        return settingKey;
    }

    public void setSettingKey(String settingKey) {
        this.settingKey = settingKey;
    }

    public String getSettingValue() {
        return settingValue;
    }

    public void setSettingValue(String settingValue) {
        this.settingValue = settingValue;
    }

    public String getGameTypeId() {
        return gameTypeId;
    }

    public void setGameTypeId(String gameTypeId) {
        this.gameTypeId = gameTypeId;
    }

    public String getUserGroupId() {
        return userGroupId;
    }

    public void setUserGroupId(String userGroupId) {
        this.userGroupId = userGroupId;
    }

    @Override
    public String toString() {
        return "AppSettingsDTO{" +
                "settingKey='" + settingKey + '\'' +
                ", settingValue='" + settingValue + '\'' +
                ", gameTypeId='" + gameTypeId + '\'' +
                ", userGroupId='" + userGroupId + '\'' +
                '}';
    }
}
