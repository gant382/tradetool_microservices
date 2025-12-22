package com.saicon.games.callcard.components.external;

/**
 * Stub class for City entity from addressbook/location subsystem.
 * This is a placeholder to allow CallCard microservice to compile without
 * full addressbook dependencies. The actual implementation resides in the addressbook module.
 */
public class City {
    // Stub implementation - add fields/methods as needed for compilation
    private int cityId;
    private String cityName;
    private State stateId;

    public City() {
    }

    public City(int cityId, String cityName, State stateId) {
        this.cityId = cityId;
        this.cityName = cityName;
        this.stateId = stateId;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public State getStateId() {
        return stateId;
    }

    public void setStateId(State stateId) {
        this.stateId = stateId;
    }
}
