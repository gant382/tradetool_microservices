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
    @Column(name = "CITY_ID", nullable = false, columnDefinition = "uniqueidentifier")
    private String cityId;

    @Column(name = "CITY_NAME", nullable = false, columnDefinition = "nvarchar(100)")
    private String cityName;

    public City() {
    }

    public City(String cityId, String cityName) {
        this.cityId = cityId;
        this.cityName = cityName;
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

    /**
     * Stub method - returns the state ID for this city
     * In a full implementation, this would be a @ManyToOne relationship to State
     * @return Stub state ID (empty string)
     */
    public String getStateId() {
        // Stub - would normally be a foreign key to State table
        return "";
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (cityId != null ? cityId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof City)) {
            return false;
        }
        City other = (City) object;
        if ((this.cityId == null && other.cityId != null) ||
            (this.cityId != null && !this.cityId.equalsIgnoreCase(other.cityId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "City{" +
                "cityId='" + cityId + '\'' +
                ", cityName='" + cityName + '\'' +
                '}';
    }
}
