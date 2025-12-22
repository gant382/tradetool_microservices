package com.saicon.games.callcard.components.external;

import com.saicon.games.core.util.SalesOrderStatus;
import java.util.Date;
import java.util.List;

/**
 * Stub class for SalesOrder entity from ERP subsystem.
 * This is a placeholder to allow CallCard microservice to compile without
 * full ERP dependencies. The actual implementation resides in the ERP module.
 */
public class SalesOrder {
    // Stub implementation - add fields/methods as needed for compilation
    private String salesOrderId;
    private String userId;
    private Date dateCreated;
    private String status;
    private SalesOrderStatus salesOrderStatus;
    private List<SalesOrderDetails> salesOrderDetails;

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

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public SalesOrderStatus getSalesOrderStatus() {
        return salesOrderStatus;
    }

    public void setSalesOrderStatus(SalesOrderStatus salesOrderStatus) {
        this.salesOrderStatus = salesOrderStatus;
    }

    public List<SalesOrderDetails> getSalesOrderDetails() {
        return salesOrderDetails;
    }

    public void setSalesOrderDetails(List<SalesOrderDetails> salesOrderDetails) {
        this.salesOrderDetails = salesOrderDetails;
    }

    private String refItemId;

    public String getRefItemId() {
        return refItemId;
    }

    public void setRefItemId(String refItemId) {
        this.refItemId = refItemId;
    }
}
