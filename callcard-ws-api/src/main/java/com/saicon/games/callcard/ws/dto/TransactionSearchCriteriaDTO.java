package com.saicon.games.callcard.ws.dto;

import com.saicon.games.callcard.util.DTOParam;

import java.io.Serializable;
import java.util.Date;

/**
 * DTO for advanced transaction search criteria.
 * Supports filtering by multiple dimensions for audit queries.
 *
 * @author Talos Maind Platform
 * @since 2025-12-21
 */
public class TransactionSearchCriteriaDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @DTOParam(1)
    private String callCardId;

    @DTOParam(2)
    private Integer userId;

    @DTOParam(3)
    private Integer userGroupId;  // Required for multi-tenant isolation

    @DTOParam(4)
    private String transactionType;

    @DTOParam(5)
    private Date dateFrom;

    @DTOParam(6)
    private Date dateTo;

    @DTOParam(7)
    private String sessionId;

    @DTOParam(8)
    private String ipAddress;

    @DTOParam(9)
    private Integer pageNumber;  // For pagination (0-based)

    @DTOParam(10)
    private Integer pageSize;    // Number of records per page

    @DTOParam(11)
    private String sortBy;       // Field to sort by (timestamp, transactionType, etc.)

    @DTOParam(12)
    private String sortDirection; // ASC or DESC

    // Constructors

    public TransactionSearchCriteriaDTO() {
        this.pageNumber = 0;
        this.pageSize = 50;
        this.sortBy = "timestamp";
        this.sortDirection = "DESC";
    }

    public TransactionSearchCriteriaDTO(Integer userGroupId) {
        this();
        this.userGroupId = userGroupId;
    }

    // Getters and Setters

    public String getCallCardId() {
        return callCardId;
    }

    public void setCallCardId(String callCardId) {
        this.callCardId = callCardId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getUserGroupId() {
        return userGroupId;
    }

    public void setUserGroupId(Integer userGroupId) {
        this.userGroupId = userGroupId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public Date getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
    }

    public Date getDateTo() {
        return dateTo;
    }

    public void setDateTo(Date dateTo) {
        this.dateTo = dateTo;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }

    @Override
    public String toString() {
        return "TransactionSearchCriteriaDTO{" +
                "callCardId='" + callCardId + '\'' +
                ", userId=" + userId +
                ", userGroupId=" + userGroupId +
                ", transactionType='" + transactionType + '\'' +
                ", dateFrom=" + dateFrom +
                ", dateTo=" + dateTo +
                ", pageNumber=" + pageNumber +
                ", pageSize=" + pageSize +
                '}';
    }
}
