package com.saicon.games.callcard.ws.dto;

import com.saicon.games.callcard.util.DTOParam;

import java.util.List;

/**
 * Created by user101 on 12/2/2016.
 */
public class CallCardActionItemDTO {

    @DTOParam(1)
    private String itemId;

    @DTOParam(2)
    private int itemTypeId;

    @DTOParam(3)
    private List<CallCardActionItemAttributesDTO> attributes;

    @DTOParam(4)
    private boolean mandatory;

    @DTOParam(5)
    private int categoryId;

    public CallCardActionItemDTO() {
    }

    public CallCardActionItemDTO(String itemId, int itemTypeId, List<CallCardActionItemAttributesDTO> attributes, int categoryId, boolean mandatory) {
        this.itemId = itemId;
        this.itemTypeId = itemTypeId;
        this.attributes = attributes;
        this.mandatory = mandatory;
        this.categoryId = categoryId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public List<CallCardActionItemAttributesDTO> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<CallCardActionItemAttributesDTO> attributes) {
        this.attributes = attributes;
    }

    public int getItemTypeId() {
        return itemTypeId;
    }

    public void setItemTypeId(int itemTypeId) {
        this.itemTypeId = itemTypeId;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }
}
