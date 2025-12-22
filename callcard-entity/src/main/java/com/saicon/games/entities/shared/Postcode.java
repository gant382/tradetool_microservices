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
    private String cityName;

    @Column(name = "REGION")
    private String region;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "POSTCODE_ID", referencedColumnName = "POSTCODE_ID", insertable = false, updatable = false)
    private List<City> cities;

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

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * Returns list of cities associated with this postcode
     * @return List of City objects
     */
    public List<City> getCities() {
        return cities != null ? cities : new ArrayList<>();
    }

    public void setCities(List<City> cities) {
        this.cities = cities;
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
