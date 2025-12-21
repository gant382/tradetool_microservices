package com.saicon.games.callcard.components.external;

/**
 * Stub class for City entity from addressbook/location subsystem.
 * This is a placeholder to allow CallCard microservice to compile without
 * full addressbook dependencies. The actual implementation resides in the addressbook module.
 */
public class City {
    // Stub implementation - add fields/methods as needed for compilation
    private String cityId;
    private String cityName;
    private String stateId;

    public City() {
    }

    public City(String cityId, String cityName, String stateId) {
        this.cityId = cityId;
        this.cityName = cityName;
        this.stateId = stateId;
    }

    public String getCityId() {
        return cityId;
    }

    public void setCityId(String cityId) {
        this.cityId = cityId;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getStateId() {
        return stateId;
    }

    public void setStateId(String stateId) {
        this.stateId = stateId;
    }
}
