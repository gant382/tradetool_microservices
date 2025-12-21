package com.saicon.games.callcard.ws.dto;

import com.saicon.games.callcard.util.DTOParam;
import com.saicon.games.common.AbstractDTOWithResources;
import com.saicon.games.commons.utilities.UUIDUtilities;
import com.saicon.games.callcard.util.Constants;

import java.util.Comparator;
import java.util.Date;
import java.util.List;


public class CallCardDTO extends AbstractDTOWithResources{
    @DTOParam(1)
    private String callCardId;

    @DTOParam(2)
    private Date startDate;

    @DTOParam(3)
    private Date endDate;

    @DTOParam(4)
    private List<CallCardGroupDTO> groupIds;

    @DTOParam(5)
    private boolean submitted;

    @DTOParam(6)
    private String comments;

    @DTOParam(7)
    private Date lastUpdated;

    @DTOParam(8)
    private String callCardTemplateId;

    private String internalRefNo;

    public CallCardDTO(){
    }

    public CallCardDTO(String callCardId, Date startDate, Date endDate) {
        this.callCardId = callCardId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public CallCardDTO(String callCardId, Date startDate, Date endDate, List<CallCardGroupDTO> groupIds) {
        this.callCardId = callCardId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.groupIds = groupIds;
    }

    public CallCardDTO(String callCardId, Date startDate, Date endDate, List<CallCardGroupDTO> groupIds, boolean submitted, Date lastUpdated) {
        this.callCardId = callCardId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.groupIds = groupIds;
        this.submitted = submitted;
        this.lastUpdated = lastUpdated;
    }

    public CallCardDTO(String callCardId, Date startDate, Date endDate, List<CallCardGroupDTO> groupIds, boolean submitted, String comments, Date lastUpdated, String callCardTemplateId) {
        this.callCardId = callCardId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.groupIds = groupIds;
        this.submitted = submitted;
        this.comments = comments;
        this.lastUpdated = lastUpdated;
        this.callCardTemplateId = callCardTemplateId;
    }

    @Override
    public String getItemIdForResourceLookup() {
        return callCardTemplateId;
    }

    @Override
    public int getItemTypeIdForResourceLookup() {
        return Constants.ITEM_TYPE_CALL_CARD_TEMPLATE;
    }


    public String getCallCardId() {
        return callCardId;
    }

    public void setCallCardId(String callCardId) {
        this.callCardId = callCardId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public boolean isSubmitted() {
        return submitted;
    }

    public void setSubmitted(boolean submitted) {
        this.submitted = submitted;
    }

    public List<CallCardGroupDTO> getGroupIds() {
        return groupIds;
    }

    public void setGroupIds(List<CallCardGroupDTO> groupIds) {
        this.groupIds = groupIds;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getInternalRefNo() {
        return internalRefNo;
    }

    public void setInternalRefNo(String internalRefNo) {
        this.internalRefNo = internalRefNo;
    }

    public String getCallCardTemplateId() {
        return callCardTemplateId;
    }

    public void setCallCardTemplateId(String callCardTemplateId) {
        this.callCardTemplateId = callCardTemplateId;
    }


    public static class ExistingCallCardFirst implements Comparator<CallCardDTO> {
        @Override
        public int compare(CallCardDTO cc1, CallCardDTO cc2) {
            return UUIDUtilities.isValidUUID(cc1.getCallCardId()) ? -1 : 1;
        }
    }

}