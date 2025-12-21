package com.saicon.games.callcard.components.external;

/**
 * Stub class for Addressbook entity from addressbook subsystem.
 * This is a placeholder to allow CallCard microservice to compile without
 * full addressbook dependencies. The actual implementation resides in the addressbook module.
 */
public class Addressbook {
    // Stub implementation - add fields/methods as needed for compilation
    private String addressbookId;
    private String userId;
    private String address;
    private String city;
    private State state;
    private String country;
    private String postalCode;
    private Double latitude;
    private Double longitude;

    public String getAddressbookId() {
        return addressbookId;
    }

    public void setAddressbookId(String addressbookId) {
        this.addressbookId = addressbookId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
