package com.saicon.games.callcard.components.external;

/**
 * Stub class for Country entity from addressbook/location subsystem.
 * This is a placeholder to allow CallCard microservice to compile without
 * full addressbook dependencies. The actual implementation resides in the addressbook module.
 */
public class Country {
    private String id;
    private String countryName;
    private String countryCode;

    public Country() {
    }

    public Country(String id, String countryName, String countryCode) {
        this.id = id;
        this.countryName = countryName;
        this.countryCode = countryCode;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
}
