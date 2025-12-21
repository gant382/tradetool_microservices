package com.saicon.games.callcard.util;

import java.io.Serializable;

/**
 * Event Transfer Object for event-related operations.
 * Contains constants for event properties and attributes.
 *
 * @author Talos Maind Platform
 * @since 2025-12-21
 */
public class EventTO implements Serializable {
    private static final long serialVersionUID = 1L;

    // Event property keys
    public static final String PROPERTY_STATUS = "status";
    public static final String PROPERTY_ITEM_ID = "itemId";
    public static final String PROPERTY_ITEM_TYPE_ID = "itemTypeId";
    public static final String PROPERTY_QUANTITY = "quantity";
    public static final String PROPERTY_TYPE = "type";
    public static final String PROPERTY_UNIT_TYPE_ID = "unitTypeId";
    public static final String PROPERTY_REF_ITEM_ID = "refItemId";
    public static final String PROPERTY_FROM_USER_ID = "fromUserId";
    public static final String PROPERTY_DATE = "date";

    private String id;
    private String status;
    private String userId;
    private String applicationId;
    private String gameTypeId;
    private Boolean runInNewDBTransaction;
    private String eventProperties;
    private Integer clientTypeId;
    private Integer eventTypeId;

    public EventTO() {
    }

    public EventTO(String id, String status) {
        this.id = id;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getGameTypeId() {
        return gameTypeId;
    }

    public void setGameTypeId(String gameTypeId) {
        this.gameTypeId = gameTypeId;
    }

    public Boolean getRunInNewDBTransaction() {
        return runInNewDBTransaction;
    }

    public void setRunInNewDBTransaction(Boolean runInNewDBTransaction) {
        this.runInNewDBTransaction = runInNewDBTransaction;
    }

    public String getEventProperties() {
        return eventProperties;
    }

    public void setEventProperties(String eventProperties) {
        this.eventProperties = eventProperties;
    }

    public Integer getClientTypeId() {
        return clientTypeId;
    }

    public void setClientTypeId(Integer clientTypeId) {
        this.clientTypeId = clientTypeId;
    }

    public Integer getEventTypeId() {
        return eventTypeId;
    }

    public void setEventTypeId(Integer eventTypeId) {
        this.eventTypeId = eventTypeId;
    }
}
