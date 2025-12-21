package com.saicon.games.callcard.components.external;

/**
 * Stub class for Postcode entity from addressbook/location subsystem.
 * This is a placeholder to allow CallCard microservice to compile without
 * full dependencies. The actual implementation resides in the addressbook module.
 */
public class Postcode {
    // Stub implementation - add fields/methods as needed for compilation
    private Integer postcodeId;
    private String postcode;
    private String city;
    private String stateCode;
    private String countryCode;

    public Integer getPostcodeId() {
        return postcodeId;
    }

    public void setPostcodeId(Integer postcodeId) {
        this.postcodeId = postcodeId;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStateCode() {
        return stateCode;
    }

    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
}
