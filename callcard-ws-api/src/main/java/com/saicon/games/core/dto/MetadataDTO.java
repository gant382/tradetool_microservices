package com.saicon.games.core.dto;

import java.io.Serializable;

/**
 * MetadataDTO - Data Transfer Object for Metadata
 * Used for API communication of metadata attributes
 */
public class MetadataDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String metadataId;
    private String metadataKey;
    private String metadataValue;
    private String userId;
    private int itemTypeId;
    private String itemId;
    private String refItemId;
    private int refItemTypeId;

    public MetadataDTO() {
    }

    public MetadataDTO(String metadataKey, String metadataValue) {
        this.metadataKey = metadataKey;
        this.metadataValue = metadataValue;
    }

    public MetadataDTO(String metadataId, String metadataKey, String metadataValue, String userId, int itemTypeId) {
        this.metadataId = metadataId;
        this.metadataKey = metadataKey;
        this.metadataValue = metadataValue;
        this.userId = userId;
        this.itemTypeId = itemTypeId;
    }

    public String getMetadataId() {
        return metadataId;
    }

    public void setMetadataId(String metadataId) {
        this.metadataId = metadataId;
    }

    public String getMetadataKey() {
        return metadataKey;
    }

    public void setMetadataKey(String metadataKey) {
        this.metadataKey = metadataKey;
    }

    public String getMetadataValue() {
        return metadataValue;
    }

    public void setMetadataValue(String metadataValue) {
        this.metadataValue = metadataValue;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getItemTypeId() {
        return itemTypeId;
    }

    public void setItemTypeId(int itemTypeId) {
        this.itemTypeId = itemTypeId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getRefItemId() {
        return refItemId;
    }

    public void setRefItemId(String refItemId) {
        this.refItemId = refItemId;
    }

    public int getRefItemTypeId() {
        return refItemTypeId;
    }

    public void setRefItemTypeId(int refItemTypeId) {
        this.refItemTypeId = refItemTypeId;
    }

    @Override
    public String toString() {
        return "MetadataDTO{" +
                "metadataId='" + metadataId + '\'' +
                ", metadataKey='" + metadataKey + '\'' +
                ", metadataValue='" + metadataValue + '\'' +
                ", userId='" + userId + '\'' +
                ", itemTypeId=" + itemTypeId +
                '}';
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (metadataId != null ? metadataId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof MetadataDTO)) {
            return false;
        }
        MetadataDTO other = (MetadataDTO) object;
        if ((this.metadataId == null && other.metadataId != null) ||
            (this.metadataId != null && !this.metadataId.equals(other.metadataId))) {
            return false;
        }
        return true;
    }
}
