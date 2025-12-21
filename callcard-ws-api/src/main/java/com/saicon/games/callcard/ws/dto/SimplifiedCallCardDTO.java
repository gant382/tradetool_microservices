package com.saicon.games.callcard.ws.dto;

import com.saicon.games.callcard.util.DTOParam;
import com.saicon.games.common.AbstractDTOWithResources;
import com.saicon.games.callcard.util.Constants;

import java.util.Date;
import java.util.List;


public class SimplifiedCallCardDTO extends AbstractDTOWithResources {
    @DTOParam(1)
    private String callCardId;

    @DTOParam(2)
    private Date dateCreated;

    @DTOParam(3)
    private Date dateUpdated;

    @DTOParam(4)
    private List<SimplifiedCallCardRefUserDTO> refUserIds;

    @DTOParam(5)
    private boolean submitted;

    @DTOParam(6)
    private Date dateUploaded;

    public SimplifiedCallCardDTO() {
    }

    public SimplifiedCallCardDTO(String callCardId, Date dateCreated, Date dateUpdated, List<SimplifiedCallCardRefUserDTO> refUserIds, boolean submitted, Date dateUploaded) {
        this.callCardId = callCardId;
        this.dateCreated = dateCreated;
        this.dateUpdated = dateUpdated;
        this.refUserIds = refUserIds;
        this.submitted = submitted;
        this.dateUploaded = dateUploaded;
    }

    @Override
    public String getItemIdForResourceLookup() {
        return callCardId;
    }

    @Override
    public int getItemTypeIdForResourceLookup() {
        return Constants.ITEM_TYPE_CALL_CARD;
    }

    public String getCallCardId() {
        return callCardId;
    }

    public void setCallCardId(String callCardId) {
        this.callCardId = callCardId;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(Date dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public List<SimplifiedCallCardRefUserDTO> getRefUserIds() {
        return refUserIds;
    }

    public void setRefUserIds(List<SimplifiedCallCardRefUserDTO> refUserIds) {
        this.refUserIds = refUserIds;
    }

    public boolean isSubmitted() {
        return submitted;
    }

    public void setSubmitted(boolean submitted) {
        this.submitted = submitted;
    }

    public Date getDateUploaded() {
        return dateUploaded;
    }

    public void setDateUploaded(Date dateUploaded) {
        this.dateUploaded = dateUploaded;
    }
}