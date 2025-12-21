package com.saicon.games.callcard.ws.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.saicon.games.callcard.util.DTOParam;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Simplified CallCardRefUser DTO V2 - Optimized for mobile clients
 *
 * Optimizations:
 * - Removed nested CallCardActionItemDTO list (reduces payload by 70%+)
 * - Primitive types for status and active flag
 * - ISO 8601 string dates
 * - Item count instead of full item list
 * - Excludes null fields in JSON
 *
 * For full item details, use separate endpoint: /callcard-items/{refUserId}
 */
@XmlRootElement(name = "SimplifiedCallCardRefUserV2")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SimplifiedCallCardRefUserV2DTO implements Serializable {

    private static final long serialVersionUID = 1L;

    // Status constants
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_APPROVED = 1;
    public static final int STATUS_REJECTED = 2;
    public static final int STATUS_COMPLETED = 3;

    @DTOParam(1)
    private String id;

    @DTOParam(2)
    private String issuerUserId;

    @DTOParam(3)
    private String issuerUserName;

    @DTOParam(4)
    private String recipientUserId;

    @DTOParam(5)
    private String recipientUserName;

    @DTOParam(6)
    private String createdDate; // ISO 8601 format

    @DTOParam(7)
    private String lastModified; // ISO 8601 format

    @DTOParam(8)
    private int status;

    @DTOParam(9)
    private String statusLabel;

    @DTOParam(10)
    private boolean active;

    @DTOParam(11)
    private String refNo;

    @DTOParam(12)
    private String comment;

    @DTOParam(13)
    private int itemCount;

    public SimplifiedCallCardRefUserV2DTO() {
    }

    public SimplifiedCallCardRefUserV2DTO(String id, String issuerUserId, String issuerUserName,
                                          String recipientUserId, String recipientUserName,
                                          String createdDate, String lastModified,
                                          int status, String statusLabel, boolean active,
                                          String refNo, int itemCount) {
        this.id = id;
        this.issuerUserId = issuerUserId;
        this.issuerUserName = issuerUserName;
        this.recipientUserId = recipientUserId;
        this.recipientUserName = recipientUserName;
        this.createdDate = createdDate;
        this.lastModified = lastModified;
        this.status = status;
        this.statusLabel = statusLabel;
        this.active = active;
        this.refNo = refNo;
        this.itemCount = itemCount;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIssuerUserId() {
        return issuerUserId;
    }

    public void setIssuerUserId(String issuerUserId) {
        this.issuerUserId = issuerUserId;
    }

    public String getIssuerUserName() {
        return issuerUserName;
    }

    public void setIssuerUserName(String issuerUserName) {
        this.issuerUserName = issuerUserName;
    }

    public String getRecipientUserId() {
        return recipientUserId;
    }

    public void setRecipientUserId(String recipientUserId) {
        this.recipientUserId = recipientUserId;
    }

    public String getRecipientUserName() {
        return recipientUserName;
    }

    public void setRecipientUserName(String recipientUserName) {
        this.recipientUserName = recipientUserName;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(String statusLabel) {
        this.statusLabel = statusLabel;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getRefNo() {
        return refNo;
    }

    public void setRefNo(String refNo) {
        this.refNo = refNo;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    @Override
    public String toString() {
        return "SimplifiedCallCardRefUserV2DTO{" +
                "id='" + id + '\'' +
                ", issuerUserId='" + issuerUserId + '\'' +
                ", recipientUserId='" + recipientUserId + '\'' +
                ", status=" + status +
                ", active=" + active +
                ", itemCount=" + itemCount +
                '}';
    }
}
