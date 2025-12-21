package com.saicon.games.entities.shared;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Addressbook entity - Stub implementation for addressbook integration
 * Contains address information
 */
@Entity
@Table(name = "ADDRESSBOOK")
public class Addressbook implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ADDRESSBOOK_ID", nullable = false, columnDefinition = "uniqueidentifier")
    private String addressbookId;

    @Column(name = "STREET", nullable = true, columnDefinition = "nvarchar(255)")
    private String street;

    @Column(name = "POSTCODE_ID", nullable = true, columnDefinition = "uniqueidentifier")
    private String postcodeId;

    @Column(name = "CITY_ID", nullable = true, columnDefinition = "uniqueidentifier")
    private String cityId;

    @Column(name = "STATE_ID", nullable = true, columnDefinition = "uniqueidentifier")
    private String stateId;

    @Column(name = "COUNTRY_ID", nullable = true, columnDefinition = "uniqueidentifier")
    private String countryId;

    public Addressbook() {
    }

    public Addressbook(String addressbookId, String street, String postcodeId, String cityId, String stateId, String countryId) {
        this.addressbookId = addressbookId;
        this.street = street;
        this.postcodeId = postcodeId;
        this.cityId = cityId;
        this.stateId = stateId;
        this.countryId = countryId;
    }

    public String getAddressbookId() {
        return addressbookId;
    }

    public void setAddressbookId(String addressbookId) {
        this.addressbookId = addressbookId;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getPostcodeId() {
        return postcodeId;
    }

    public void setPostcodeId(String postcodeId) {
        this.postcodeId = postcodeId;
    }

    public String getCityId() {
        return cityId;
    }

    public void setCityId(String cityId) {
        this.cityId = cityId;
    }

    public String getStateId() {
        return stateId;
    }

    public void setStateId(String stateId) {
        this.stateId = stateId;
    }

    public String getCountryId() {
        return countryId;
    }

    public void setCountryId(String countryId) {
        this.countryId = countryId;
    }

    /**
     * Stub method - returns latitude coordinate
     * @return Stub latitude (0.0)
     */
    public Double getLatitude() {
        // Stub - would normally be stored in database
        return 0.0;
    }

    /**
     * Stub method - returns longitude coordinate
     * @return Stub longitude (0.0)
     */
    public Double getLongitude() {
        // Stub - would normally be stored in database
        return 0.0;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (addressbookId != null ? addressbookId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Addressbook)) {
            return false;
        }
        Addressbook other = (Addressbook) object;
        if ((this.addressbookId == null && other.addressbookId != null) ||
            (this.addressbookId != null && !this.addressbookId.equalsIgnoreCase(other.addressbookId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Addressbook{" +
                "addressbookId='" + addressbookId + '\'' +
                ", street='" + street + '\'' +
                ", postcodeId='" + postcodeId + '\'' +
                ", cityId='" + cityId + '\'' +
                ", stateId='" + stateId + '\'' +
                ", countryId='" + countryId + '\'' +
                '}';
    }
}
