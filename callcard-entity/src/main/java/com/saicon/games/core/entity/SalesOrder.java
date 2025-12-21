package com.saicon.games.core.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * SalesOrder entity - Stub implementation for ERP integration
 * Contains essential fields for sales order operations
 */
@Entity
@Table(name = "SALES_ORDER")
public class SalesOrder implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "SALES_ORDER_ID", nullable = false, columnDefinition = "uniqueidentifier")
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "com.saicon.games.callcard.entity.util.UUIDGenerator")
    private String salesOrderId;

    @Column(name = "USER_ID", nullable = false, columnDefinition = "uniqueidentifier")
    private String userId;

    @Column(name = "STATUS", nullable = false)
    private int status;

    @Column(name = "DATE_CREATED", nullable = false, columnDefinition = "datetime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;

    @Column(name = "LAST_UPDATED", columnDefinition = "datetime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdated;

    @Column(name = "REF_ITEM_ID", columnDefinition = "uniqueidentifier")
    private String refItemId;

    @Transient
    private List<SalesOrderDetails> salesOrderDetails = new ArrayList<>();

    public SalesOrder() {
    }

    public SalesOrder(String salesOrderId, String userId, int status, Date dateCreated) {
        this.salesOrderId = salesOrderId;
        this.userId = userId;
        this.status = status;
        this.dateCreated = dateCreated;
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

    public String getRefItemId() {
        return refItemId;
    }

    public void setRefItemId(String refItemId) {
        this.refItemId = refItemId;
    }

    public List<SalesOrderDetails> getSalesOrderDetails() {
        return salesOrderDetails;
    }

    public void setSalesOrderDetails(List<SalesOrderDetails> salesOrderDetails) {
        this.salesOrderDetails = salesOrderDetails;
    }

    /**
     * Stub method - returns sales order status wrapper
     */
    public SalesOrderStatusWrapper getSalesOrderStatus() {
        return new SalesOrderStatusWrapper(status);
    }

    /**
     * Inner class to wrap status
     */
    public static class SalesOrderStatusWrapper {
        private int statusId;

        public SalesOrderStatusWrapper(int statusId) {
            this.statusId = statusId;
        }

        public int getStatusId() {
            return statusId;
        }
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (salesOrderId != null ? salesOrderId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof SalesOrder)) {
            return false;
        }
        SalesOrder other = (SalesOrder) object;
        if ((this.salesOrderId == null && other.salesOrderId != null) ||
            (this.salesOrderId != null && !this.salesOrderId.equalsIgnoreCase(other.salesOrderId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "SalesOrder{" +
                "salesOrderId='" + salesOrderId + '\'' +
                ", userId='" + userId + '\'' +
                ", status=" + status +
                ", dateCreated=" + dateCreated +
                '}';
    }
}
