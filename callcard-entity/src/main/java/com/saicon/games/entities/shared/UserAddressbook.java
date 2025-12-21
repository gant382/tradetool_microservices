package com.saicon.games.entities.shared;

import javax.persistence.*;
import java.io.Serializable;

/**
 * UserAddressbook entity - Stub implementation for addressbook integration
 * Maps users to their addressbook entries
 */
@Entity
@Table(name = "USER_ADDRESSBOOK")
public class UserAddressbook implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "USER_ADDRESSBOOK_ID", nullable = false, columnDefinition = "uniqueidentifier")
    private String userAddressbookId;

    @Column(name = "USER_ID", nullable = false, columnDefinition = "uniqueidentifier")
    private String userId;

    @Column(name = "ADDRESSBOOK_ID", nullable = false, columnDefinition = "uniqueidentifier")
    private String addressbookId;

    public UserAddressbook() {
    }

    public UserAddressbook(String userAddressbookId, String userId, String addressbookId) {
        this.userAddressbookId = userAddressbookId;
        this.userId = userId;
        this.addressbookId = addressbookId;
    }

    public String getUserAddressbookId() {
        return userAddressbookId;
    }

    public void setUserAddressbookId(String userAddressbookId) {
        this.userAddressbookId = userAddressbookId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAddressbookId() {
        return addressbookId;
    }

    public void setAddressbookId(String addressbookId) {
        this.addressbookId = addressbookId;
    }

    /**
     * Stub method - returns the related Addressbook entity
     * In a full implementation, this would be a @ManyToOne relationship
     * @return Stub Addressbook object
     */
    public Addressbook getAddressbook() {
        // Stub - would normally fetch from database via JPA relationship
        Addressbook addressbook = new Addressbook();
        addressbook.setAddressbookId(this.addressbookId);
        return addressbook;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (userAddressbookId != null ? userAddressbookId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof UserAddressbook)) {
            return false;
        }
        UserAddressbook other = (UserAddressbook) object;
        if ((this.userAddressbookId == null && other.userAddressbookId != null) ||
            (this.userAddressbookId != null && !this.userAddressbookId.equalsIgnoreCase(other.userAddressbookId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "UserAddressbook{" +
                "userAddressbookId='" + userAddressbookId + '\'' +
                ", userId='" + userId + '\'' +
                ", addressbookId='" + addressbookId + '\'' +
                '}';
    }
}
