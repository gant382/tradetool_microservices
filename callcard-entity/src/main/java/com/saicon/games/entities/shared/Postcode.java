package com.saicon.games.entities.shared;

import javax.persistence.*;
import java.io.Serializable;

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
}
