package com.saicon.games.callcard.ws.dto;

import com.saicon.games.callcard.util.DTOParam;
import com.saicon.games.common.AbstractDTOWithResources;
import com.saicon.games.callcard.util.Constants;

import java.util.List;


public class CallCardGroupDTO extends AbstractDTOWithResources {

    public static int STOCK_DATA_GROUP = -1;
    public static int UNASSIGNED_POS_GROUP = 0;

    @DTOParam(1)
    private int groupId;

    @DTOParam(2)
    private List<CallCardRefUserDTO> refUserIds;

    @DTOParam(3)
    private String callCardTemplateId;

    public CallCardGroupDTO() {
    }

    public CallCardGroupDTO(int groupId, List<CallCardRefUserDTO> refUserIds, String callCardTemplateId) {
        this.groupId = groupId;
        this.refUserIds = refUserIds;
        this.callCardTemplateId = callCardTemplateId;
    }

    public List<CallCardRefUserDTO> getRefUserIds() {
        return refUserIds;
    }

    public void setRefUserIds(List<CallCardRefUserDTO> refUserIds) {
        this.refUserIds = refUserIds;
    }

    @Override
    public String getItemIdForResourceLookup() {
        return (String.valueOf(groupId) + "_" + callCardTemplateId);
    }

    @Override
    public int getItemTypeIdForResourceLookup() {
        return Constants.ITEM_TYPE_CALL_CARD_POS_GROUP;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getCallCardTemplateId() {
        return callCardTemplateId;
    }

    public void setCallCardTemplateId(String callCardTemplateId) {
        this.callCardTemplateId = callCardTemplateId;
    }
}