package com.saicon.games.callcard.components.external;

/**
 * Stub class for State entity from addressbook subsystem.
 * This is a placeholder to allow CallCard microservice to compile without
 * full addressbook dependencies. The actual implementation resides in the addressbook module.
 */
public class State {
    // Stub implementation - add fields/methods as needed for compilation
    private int stateId;
    private String stateName;
    private String stateCode;
    private Country countryId;

    public int getStateId() {
        return stateId;
    }

    public void setStateId(int stateId) {
        this.stateId = stateId;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public String getStateCode() {
        return stateCode;
    }

    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }

    public Country getCountryId() {
        return countryId;
    }

    public void setCountryId(Country countryId) {
        this.countryId = countryId;
    }
}
