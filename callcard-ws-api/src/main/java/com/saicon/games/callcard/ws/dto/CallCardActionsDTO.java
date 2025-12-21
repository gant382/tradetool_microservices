package com.saicon.games.callcard.ws.dto;

import com.saicon.games.callcard.util.DTOParam;

import java.util.List;

/**
 * Created by user101 on 12/2/2016.
 */
public class CallCardActionsDTO {

    @DTOParam(1)
    private List<CallCardActionItemDTO> actionItems;

    @DTOParam(2)
    private int itemTypeId;

    @DTOParam(3)
    private boolean mandatory;

    public CallCardActionsDTO() {
    }

    public CallCardActionsDTO(List<CallCardActionItemDTO> actionItems, int itemTypeId, boolean mandatory) {
        this.actionItems = actionItems;
        this.itemTypeId = itemTypeId;
        this.mandatory = mandatory;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public int getItemTypeId() {
        return itemTypeId;
    }

    public void setItemTypeId(int itemTypeId) {
        this.itemTypeId = itemTypeId;
    }

    public List<CallCardActionItemDTO> getActionItems() {
        return actionItems;
    }

    public void setActionItems(List<CallCardActionItemDTO> actionItems) {
        this.actionItems = actionItems;
    }
}
