package com.saicon.resources.dto;

import java.io.Serializable;

/**
 * Stub DTO for resources - extracted from gameserver_v3
 */
public class ResourceDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String resourceId;
    private String resourceType;
    private String resourceName;
    private String resourceValue;

    public ResourceDTO() {
    }

    public ResourceDTO(String resourceId, String resourceType, String resourceName, String resourceValue) {
        this.resourceId = resourceId;
        this.resourceType = resourceType;
        this.resourceName = resourceName;
        this.resourceValue = resourceValue;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getResourceValue() {
        return resourceValue;
    }

    public void setResourceValue(String resourceValue) {
        this.resourceValue = resourceValue;
    }
}
