package com.saicon.games.callcard.ws.dto;

import com.saicon.games.callcard.util.DTOParam;

import java.util.Date;
import java.util.List;

/**
 * Created by user101 on 12/2/2016.
 */
public class SimplifiedCallCardRefUserDTO {

    public static final int BUY = 1;
    public static final int SELL = 2;
    public static final int RETURN = 3;
    public static final int CREDIT_SELL = 4;
    public static final int INDIRECT_BUY = 5;

    public static String DEFAULT_REF_USER_ID = "DefaultRefUserID";

    @DTOParam(1)
    private String callCardRefUserId;

    @DTOParam(2)
    private String issuerUserId;

    @DTOParam(3)
    private List<CallCardActionItemDTO> items;

    @DTOParam(4)
    private Date dateCreated;

    @DTOParam(5)
    private Date dateUpdated;

    @DTOParam(6)
    private String recipientUserId;

    @DTOParam(7)
    private String comment;

    @DTOParam(8)
    private Integer status;

    @DTOParam(9)
    private String refNo;

    @DTOParam(10)
    private boolean active;

    public SimplifiedCallCardRefUserDTO() {
    }

    public SimplifiedCallCardRefUserDTO(String callCardRefUserId, String issuerUserId, List<CallCardActionItemDTO> items, Date dateCreated, Date dateUpdated, String recipientUserId, String comment, Integer status, String refNo, boolean active) {
        this.callCardRefUserId = callCardRefUserId;
        this.issuerUserId = issuerUserId;
        this.items = items;
        this.dateCreated = dateCreated;
        this.dateUpdated = dateUpdated;
        this.recipientUserId = recipientUserId;
        this.comment = comment;
        this.status = status;
        this.refNo = refNo;
        this.active = active;
    }

    public String getIssuerUserId() {
        return issuerUserId;
    }

    public void setIssuerUserId(String issuerUserId) {
        this.issuerUserId = issuerUserId;
    }

    public List<CallCardActionItemDTO> getItems() {
        return items;
    }

    public void setItems(List<CallCardActionItemDTO> items) {
        this.items = items;
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

    public String getRecipientUserId() {
        return recipientUserId;
    }

    public void setRecipientUserId(String recipientUserId) {
        this.recipientUserId = recipientUserId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRefNo() {
        return refNo;
    }

    public void setRefNo(String refNo) {
        this.refNo = refNo;
    }

    public String getCallCardRefUserId() {
        return callCardRefUserId;
    }

    public void setCallCardRefUserId(String callCardRefUserId) {
        this.callCardRefUserId = callCardRefUserId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
