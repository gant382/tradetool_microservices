package com.saicon.games.metadata.dto;

import java.io.Serializable;

public class MetadataKeyDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String metadataKeyId;
    private String metadataKeyName;
    private int itemTypeId;
    private String dataTypeName;

    public MetadataKeyDTO() {
    }

    public MetadataKeyDTO(String metadataKeyId, String metadataKeyName, int itemTypeId) {
        this.metadataKeyId = metadataKeyId;
        this.metadataKeyName = metadataKeyName;
        this.itemTypeId = itemTypeId;
    }

    public String getMetadataKeyId() {
        return metadataKeyId;
    }

    public void setMetadataKeyId(String metadataKeyId) {
        this.metadataKeyId = metadataKeyId;
    }

    public String getMetadataKeyName() {
        return metadataKeyName;
    }

    public void setMetadataKeyName(String metadataKeyName) {
        this.metadataKeyName = metadataKeyName;
    }

    public int getItemTypeId() {
        return itemTypeId;
    }

    public void setItemTypeId(int itemTypeId) {
        this.itemTypeId = itemTypeId;
    }

    public String getDataTypeName() {
        return dataTypeName;
    }

    public void setDataTypeName(String dataTypeName) {
        this.dataTypeName = dataTypeName;
    }

    @Override
    public String toString() {
        return "MetadataKeyDTO{" +
                "metadataKeyId='" + metadataKeyId + '\'' +
                ", metadataKeyName='" + metadataKeyName + '\'' +
                ", itemTypeId=" + itemTypeId +
                '}';
    }
}
