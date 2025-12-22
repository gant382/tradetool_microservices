package com.saicon.games.entities.shared;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Country entity - Stub implementation for location management
 * Contains country information
 */
@Entity
@Table(name = "COUNTRY")
public class Country implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "COUNTRY_ID", nullable = false, columnDefinition = "nvarchar(10)")
    private String id;

    @Column(name = "COUNTRY_NAME", nullable = false, columnDefinition = "nvarchar(100)")
    private String countryName;

    @Column(name = "COUNTRY_CODE", columnDefinition = "nvarchar(5)")
    private String countryCode;

    public Country() {
    }

    public Country(String id, String countryName) {
        this.id = id;
        this.countryName = countryName;
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

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Country)) {
            return false;
        }
        Country other = (Country) object;
        if ((this.id == null && other.id != null) ||
            (this.id != null && !this.id.equalsIgnoreCase(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Country{" +
                "id='" + id + '\'' +
                ", countryName='" + countryName + '\'' +
                '}';
    }
}
