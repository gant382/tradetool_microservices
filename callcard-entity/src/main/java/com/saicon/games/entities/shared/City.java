package com.saicon.games.entities.shared;

import javax.persistence.*;
import java.io.Serializable;

/**
 * City entity - Stub implementation for location management
 * Contains city information
 */
@Entity
@Table(name = "CITY")
public class City implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "CITY_ID", nullable = false)
    private int cityId;

    @Column(name = "CITY_NAME", nullable = false, columnDefinition = "nvarchar(100)")
    private String cityName;

    @Column(name = "POSTCODE_ID")
    private Integer postcodeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STATE_ID", referencedColumnName = "STATE_ID")
    private State stateId;

    public City() {
    }

    public City(int cityId, String cityName) {
        this.cityId = cityId;
        this.cityName = cityName;
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

    @Override
    public int hashCode() {
        return cityId;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof City)) {
            return false;
        }
        City other = (City) object;
        return this.cityId == other.cityId;
    }

    @Override
    public String toString() {
        return "City{" +
                "cityId='" + cityId + '\'' +
                ", cityName='" + cityName + '\'' +
                '}';
    }
}
