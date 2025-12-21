package com.saicon.games.callcard.ws.dto;

import java.io.Serializable;
import com.saicon.games.callcard.util.EventType;

/**
 * Transfer object for CallCard system events.
 */
public class EventTO implements Serializable {
    private static final long serialVersionUID = 1L;

    // Event property constants
    public static final String PROPERTY_TYPE = "PROPERTY_TYPE";
    public static final String PROPERTY_UNIT_TYPE_ID = "PROPERTY_UNIT_TYPE_ID";
    public static final String PROPERTY_REF_ITEM_ID = "PROPERTY_REF_ITEM_ID";
    public static final String PROPERTY_STATUS = "PROPERTY_STATUS";
    public static final String PROPERTY_FROM_USER_ID = "PROPERTY_FROM_USER_ID";
    public static final String PROPERTY_DATE = "PROPERTY_DATE";

    private String eventId;
    private EventType eventType;
    private String entityId;
    private String entityType;
    private String userId;
    private String userGroupId;

    public EventTO() {
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserGroupId() {
        return userGroupId;
    }

    public void setUserGroupId(String userGroupId) {
        this.userGroupId = userGroupId;
    }
}
