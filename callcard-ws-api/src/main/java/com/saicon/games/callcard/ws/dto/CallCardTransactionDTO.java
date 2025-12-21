package com.saicon.games.callcard.ws.dto;

import com.saicon.games.callcard.util.DTOParam;

import java.io.Serializable;
import java.util.Date;

/**
 * DTO for CallCard Transaction History.
 * Represents a single audit trail entry for CallCard modifications.
 *
 * @author Talos Maind Platform
 * @since 2025-12-21
 */
public class CallCardTransactionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @DTOParam(1)
    private String transactionId;

    @DTOParam(2)
    private String callCardId;

    @DTOParam(3)
    private String transactionType;

    @DTOParam(4)
    private Integer userId;

    @DTOParam(5)
    private String userName;  // For display purposes

    @DTOParam(6)
    private Integer userGroupId;

    @DTOParam(7)
    private Date timestamp;

    @DTOParam(8)
    private String oldValue;  // JSON

    @DTOParam(9)
    private String newValue;  // JSON

    @DTOParam(10)
    private String description;

    @DTOParam(11)
    private String ipAddress;

    @DTOParam(12)
    private String sessionId;

    @DTOParam(13)
    private String metadata;  // JSON

    // Constructors

    public CallCardTransactionDTO() {
    }

    public CallCardTransactionDTO(String transactionId, String callCardId, String transactionType,
                                   Integer userId, Integer userGroupId, Date timestamp, String description) {
        this.transactionId = transactionId;
        this.callCardId = callCardId;
        this.transactionType = transactionType;
        this.userId = userId;
        this.userGroupId = userGroupId;
        this.timestamp = timestamp;
        this.description = description;
    }

    // Getters and Setters

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getCallCardId() {
        return callCardId;
    }

    public void setCallCardId(String callCardId) {
        this.callCardId = callCardId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getUserGroupId() {
        return userGroupId;
    }

    public void setUserGroupId(Integer userGroupId) {
        this.userGroupId = userGroupId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return "CallCardTransactionDTO{" +
                "transactionId='" + transactionId + '\'' +
                ", callCardId='" + callCardId + '\'' +
                ", transactionType='" + transactionType + '\'' +
                ", userId=" + userId +
                ", timestamp=" + timestamp +
                ", description='" + description + '\'' +
                '}';
    }
}
