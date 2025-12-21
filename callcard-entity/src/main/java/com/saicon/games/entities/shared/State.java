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
    @Column(name = "STATE_ID", nullable = false, columnDefinition = "uniqueidentifier")
    private String stateId;

    @Column(name = "STATE_NAME", nullable = false, columnDefinition = "nvarchar(100)")
    private String stateName;

    public State() {
    }

    public State(String stateId, String stateName) {
        this.stateId = stateId;
        this.stateName = stateName;
    }

    public String getStateId() {
        return stateId;
    }

    public void setStateId(String stateId) {
        this.stateId = stateId;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    /**
     * Stub method - returns the country ID for this state
     * In a full implementation, this would be a @ManyToOne relationship to Country
     * @return Stub country ID (empty string)
     */
    public String getCountryId() {
        // Stub - would normally be a foreign key to Country table
        return "";
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (stateId != null ? stateId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof State)) {
            return false;
        }
        State other = (State) object;
        if ((this.stateId == null && other.stateId != null) ||
            (this.stateId != null && !this.stateId.equalsIgnoreCase(other.stateId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "State{" +
                "stateId='" + stateId + '\'' +
                ", stateName='" + stateName + '\'' +
                '}';
    }
}
