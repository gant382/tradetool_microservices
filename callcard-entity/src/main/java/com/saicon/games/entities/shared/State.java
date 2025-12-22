package com.saicon.games.entities.shared;

import javax.persistence.*;
import java.io.Serializable;

/**
 * State entity - Stub implementation for location management
 * Contains state/province information
 */
@Entity
@Table(name = "STATE")
public class State implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "STATE_ID", nullable = false)
    private int stateId;

    @Column(name = "STATE_NAME", nullable = false, columnDefinition = "nvarchar(100)")
    private String stateName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COUNTRY_ID", referencedColumnName = "COUNTRY_ID")
    private Country countryId;

    public State() {
    }

    public State(int stateId, String stateName) {
        this.stateId = stateId;
        this.stateName = stateName;
    }

    public int getStateId() {
        return stateId;
    }

    public void setStateId(int stateId) {
        this.stateId = stateId;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public Country getCountryId() {
        return countryId;
    }

    public void setCountryId(Country countryId) {
        this.countryId = countryId;
    }

    @Override
    public int hashCode() {
        return stateId;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof State)) {
            return false;
        }
        State other = (State) object;
        return this.stateId == other.stateId;
    }

    @Override
    public String toString() {
        return "State{" +
                "stateId='" + stateId + '\'' +
                ", stateName='" + stateName + '\'' +
                '}';
    }
}
