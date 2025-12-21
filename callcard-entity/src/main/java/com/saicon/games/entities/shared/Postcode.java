package com.saicon.games.entities.shared;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "POSTCODE")
public class Postcode implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "POSTCODE_ID")
    private Integer postcodeId;

    @Column(name = "CODE")
    private String code;

    @Column(name = "CITY")
    private String city;

    @Column(name = "REGION")
    private String region;

    public Postcode() {
    }

    public Integer getPostcodeId() {
        return postcodeId;
    }

    public void setPostcodeId(Integer postcodeId) {
        this.postcodeId = postcodeId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * Stub method - returns list of cities associated with this postcode
     * In a full implementation, this would be a @OneToMany relationship
     * @return Stub list of City objects
     */
    public List<City> getCities() {
        // Stub - would normally fetch from database via JPA relationship
        List<City> cities = new ArrayList<>();
        if (city != null) {
            City cityObj = new City();
            cityObj.setCityName(city);
            cities.add(cityObj);
        }
        return cities;
    }

    /**
     * Stub method - returns the country ID for this postcode
     * @return Empty string (no country association in this stub)
     */
    @SuppressWarnings("unused")
    public String getCountryId() {
        // Stub - would normally be a foreign key to Country table
        return "";
    }
}
