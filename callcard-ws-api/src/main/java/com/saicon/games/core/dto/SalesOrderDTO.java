package com.saicon.games.core.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * SalesOrderDTO - Data Transfer Object for Sales Order
 * Used for API communication
 */
public class SalesOrderDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String salesOrderId;
    private String userId;
    private int status;
    private Date dateCreated;
    private Date lastUpdated;
    private java.util.List<SalesOrderDetailsDTO> salesOrderDetailsDTOList;

    // Additional fields for extended functionality
    private String createdByUserId;
    private String fromUserId;
    private String toUserId;
    private Date dateUpdated;
    private String comments;
    private Date dateSubmitted;
    private String refItemId;
    private Integer refItemTypeId;

    public SalesOrderDTO() {
    }

    public SalesOrderDTO(String salesOrderId, String userId, int status, Date dateCreated) {
        this.salesOrderId = salesOrderId;
        this.userId = userId;
        this.status = status;
        this.dateCreated = dateCreated;
    }

    public SalesOrderDTO(String salesOrderId, String userId, int status, Date dateCreated, Date lastUpdated) {
        this.salesOrderId = salesOrderId;
        this.userId = userId;
        this.status = status;
        this.dateCreated = dateCreated;
        this.lastUpdated = lastUpdated;
    }

    /**
     * Full constructor for CallCard management
     */
    public SalesOrderDTO(String salesOrderId, String createdByUserId, String fromUserId, String toUserId,
                         Date dateCreated, Date dateUpdated, Object param7, int param8, Object param9,
                         String comments, Object param11, Object param12, Object param13, Object param14,
                         int salesOrderStatus, Date dateSubmitted, Object param17, Object param18, Object param19,
                         java.util.List<SalesOrderDetailsDTO> salesOrderDetailsDTOList, String refItemId, Integer refItemTypeId) {
        this.salesOrderId = salesOrderId;
        this.createdByUserId = createdByUserId;
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.dateCreated = dateCreated;
        this.dateUpdated = dateUpdated;
        this.comments = comments;
        this.status = salesOrderStatus;
        this.dateSubmitted = dateSubmitted;
        this.salesOrderDetailsDTOList = salesOrderDetailsDTOList;
        this.refItemId = refItemId;
        this.refItemTypeId = refItemTypeId;
    }

    public String getSalesOrderId() {
        return salesOrderId;
    }

    public void setSalesOrderId(String salesOrderId) {
        this.salesOrderId = salesOrderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public java.util.List<SalesOrderDetailsDTO> getSalesOrderDetailsDTOList() {
        return salesOrderDetailsDTOList;
    }

    public void setSalesOrderDetailsDTOList(java.util.List<SalesOrderDetailsDTO> salesOrderDetailsDTOList) {
        this.salesOrderDetailsDTOList = salesOrderDetailsDTOList;
    }

    public int getSalesOrderStatus() {
        return status;
    }

    public void setSalesOrderStatus(int status) {
        this.status = status;
    }

    public String getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(String createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public Date getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(Date dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Date getDateSubmitted() {
        return dateSubmitted;
    }

    public void setDateSubmitted(Date dateSubmitted) {
        this.dateSubmitted = dateSubmitted;
    }

    public String getRefItemId() {
        return refItemId;
    }

    public void setRefItemId(String refItemId) {
        this.refItemId = refItemId;
    }

    public Integer getRefItemTypeId() {
        return refItemTypeId;
    }

    public void setRefItemTypeId(Integer refItemTypeId) {
        this.refItemTypeId = refItemTypeId;
    }

    @Override
    public String toString() {
        return "SalesOrderDTO{" +
                "salesOrderId='" + salesOrderId + '\'' +
                ", userId='" + userId + '\'' +
                ", status=" + status +
                ", dateCreated=" + dateCreated +
                '}';
    }
}
