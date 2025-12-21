package com.saicon.games.callcard.ws.dto;

import com.saicon.games.callcard.util.DTOParam;
import com.saicon.multiplayer.dto.KeyValueDTO;

import java.util.Date;
import java.util.List;

/**
 * Created by user101 on 12/2/2016.
 */
public class CallCardRefUserDTO {
    // STATUS
    public static final int BUY = 1;
    public static final int SELL = 2;
    public static final int RETURN = 3;
    public static final int CREDIT_SELL = 4;
    public static final int INDIRECT_BUY = 5;
    public static final int UNSCHEDULED_SELL = 6;
    public static final int ORDER = 7;
    public static final int UNSCHEDULED_ORDER = 8;

    public static String DEFAULT_REF_USER_ID = "DefaultRefUserID";

    // Additional RefUser Info Keys
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";

    @DTOParam(1)
    private String callCardRefUserId;

    @DTOParam(2)
    private String refUserId;

    @DTOParam(3)
    private List<CallCardActionsDTO> actions;

    @DTOParam(4)
    private Date startDate;

    @DTOParam(5)
    private Date endDate;

    @DTOParam(6)
    private boolean mandatory;

    @DTOParam(7)
    private String comment;

    @DTOParam(8)
    private Integer status;

    @DTOParam(9)
    private Date lastUpdated;

    @DTOParam(10)
    private boolean active;

    @DTOParam(11)
    private String refNo;

    @DTOParam(12)
    private String sourceUserId;

    @DTOParam(13)
    private List<KeyValueDTO> additionalRefUserInfo;

    public CallCardRefUserDTO() {
    }

    public CallCardRefUserDTO(String refUserId, List<CallCardActionsDTO> actions, Date startDate, Date endDate, boolean mandatory) {
        this.refUserId = refUserId;
        this.actions = actions;
        this.startDate = startDate;
        this.endDate = endDate;
        this.mandatory = mandatory;
    }

    public CallCardRefUserDTO(String callCardRefUserId, String refUserId, List<CallCardActionsDTO> actions, Date startDate, Date endDate, boolean mandatory, String comment) {
        this.callCardRefUserId = callCardRefUserId;
        this.refUserId = refUserId;
        this.actions = actions;
        this.startDate = startDate;
        this.endDate = endDate;
        this.mandatory = mandatory;
        this.comment = comment;
    }

    public CallCardRefUserDTO(String callCardRefUserId, String refUserId, List<CallCardActionsDTO> actions, Date startDate, Date endDate, boolean mandatory, String comment, Integer status, Date lastUpdated, String refNo, String sourceUserId, List<KeyValueDTO> additionalRefUserInfo, boolean active) {
        this.callCardRefUserId = callCardRefUserId;
        this.refUserId = refUserId;
        this.actions = actions;
        this.startDate = startDate;
        this.endDate = endDate;
        this.mandatory = mandatory;
        this.comment = comment;
        this.status = status;
        this.lastUpdated = lastUpdated;
        this.active = active;
        this.refNo = refNo;
        this.sourceUserId = sourceUserId;
        this.additionalRefUserInfo = additionalRefUserInfo;
    }

    public String getRefUserId() {
        return refUserId;
    }

    public void setRefUserId(String refUserId) {
        this.refUserId = refUserId;
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

    public List<CallCardActionsDTO> getActions() {
        return actions;
    }

    public void setActions(List<CallCardActionsDTO> actions) {
        this.actions = actions;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCallCardRefUserId() {
        return callCardRefUserId;
    }

    public void setCallCardRefUserId(String callCardRefUserId) {
        this.callCardRefUserId = callCardRefUserId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getRefNo() {
        return refNo;
    }

    public void setRefNo(String refNo) {
        this.refNo = refNo;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getSourceUserId() {
        return sourceUserId;
    }

    public void setSourceUserId(String sourceUserId) {
        this.sourceUserId = sourceUserId;
    }

    public List<KeyValueDTO> getAdditionalRefUserInfo() {
        return additionalRefUserInfo;
    }

    public void setAdditionalRefUserInfo(List<KeyValueDTO> additionalRefUserInfo) {
        this.additionalRefUserInfo = additionalRefUserInfo;
    }
}
