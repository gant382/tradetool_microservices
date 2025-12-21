package com.saicon.games.core.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * SalesOrderDetails entity - Stub implementation for ERP integration
 * Contains line item details for sales orders
 */
@Entity
@Table(name = "SALES_ORDER_DETAILS")
public class SalesOrderDetails implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "SALES_ORDER_DETAILS_ID", nullable = false, columnDefinition = "uniqueidentifier")
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "com.saicon.games.callcard.entity.util.UUIDGenerator")
    private String salesOrderDetailsId;

    @Column(name = "SALES_ORDER_ID", nullable = false, columnDefinition = "uniqueidentifier")
    private String salesOrderId;

    @Column(name = "PRODUCT_ID", nullable = false, columnDefinition = "uniqueidentifier")
    private String productId;

    @Column(name = "QUANTITY", nullable = false)
    private int quantity;

    @Column(name = "DATE_CREATED", columnDefinition = "datetime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;

    @Column(name = "LAST_UPDATED", columnDefinition = "datetime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdated;

    @Column(name = "ITEM_ID", columnDefinition = "uniqueidentifier")
    private String itemId;

    @Column(name = "ITEM_TYPE_ID")
    private Integer itemTypeId;

    @Column(name = "ITEM_PRICE", precision = 19, scale = 4)
    private BigDecimal itemPrice;

    public SalesOrderDetails() {
    }

    public SalesOrderDetails(String salesOrderDetailsId, String salesOrderId, String productId, int quantity) {
        this.salesOrderDetailsId = salesOrderDetailsId;
        this.salesOrderId = salesOrderId;
        this.productId = productId;
        this.quantity = quantity;
    }

    public String getSalesOrderDetailsId() {
        return salesOrderDetailsId;
    }

    public void setSalesOrderDetailsId(String salesOrderDetailsId) {
        this.salesOrderDetailsId = salesOrderDetailsId;
    }

    public String getSalesOrderId() {
        return salesOrderId;
    }

    public void setSalesOrderId(String salesOrderId) {
        this.salesOrderId = salesOrderId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
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

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public Integer getItemTypeId() {
        return itemTypeId;
    }

    public void setItemTypeId(Integer itemTypeId) {
        this.itemTypeId = itemTypeId;
    }

    public BigDecimal getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(BigDecimal itemPrice) {
        this.itemPrice = itemPrice;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (salesOrderDetailsId != null ? salesOrderDetailsId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof SalesOrderDetails)) {
            return false;
        }
        SalesOrderDetails other = (SalesOrderDetails) object;
        if ((this.salesOrderDetailsId == null && other.salesOrderDetailsId != null) ||
            (this.salesOrderDetailsId != null && !this.salesOrderDetailsId.equalsIgnoreCase(other.salesOrderDetailsId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "SalesOrderDetails{" +
                "salesOrderDetailsId='" + salesOrderDetailsId + '\'' +
                ", salesOrderId='" + salesOrderId + '\'' +
                ", productId='" + productId + '\'' +
                ", quantity=" + quantity +
                '}';
    }
}
