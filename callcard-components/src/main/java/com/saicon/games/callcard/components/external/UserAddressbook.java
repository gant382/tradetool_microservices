package com.saicon.games.callcard.components.external;

/**
 * Stub class for UserAddressbook entity from addressbook subsystem.
 * This is a placeholder to allow CallCard microservice to compile without
 * full addressbook dependencies. The actual implementation resides in the addressbook module.
 */
public class UserAddressbook {
    // Stub implementation - add fields/methods as needed for compilation
    private String userAddressbookId;
    private String userId;
    private String addressbookId;
    private Addressbook addressbook;

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

    public Addressbook getAddressbook() {
        return addressbook;
    }

    public void setAddressbook(Addressbook addressbook) {
        this.addressbook = addressbook;
    }
}
