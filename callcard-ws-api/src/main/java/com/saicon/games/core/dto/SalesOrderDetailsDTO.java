package com.saicon.games.core.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * SalesOrderDetailsDTO - Data Transfer Object for Sales Order Details
 * Used for API communication of line items
 */
public class SalesOrderDetailsDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String salesOrderDetailsId;
    private String salesOrderId;
    private String productId;
    private int quantity;
    private Date dateCreated;
    private Date lastUpdated;
    private double itemPrice;

    // Additional fields for extended functionality
    private String itemId;
    private Integer itemTypeId;

    public SalesOrderDetailsDTO() {
    }

    public SalesOrderDetailsDTO(String salesOrderDetailsId, String salesOrderId, String productId, int quantity) {
        this.salesOrderDetailsId = salesOrderDetailsId;
        this.salesOrderId = salesOrderId;
        this.productId = productId;
        this.quantity = quantity;
    }

    public SalesOrderDetailsDTO(String salesOrderDetailsId, String salesOrderId, String productId, int quantity, Date dateCreated) {
        this.salesOrderDetailsId = salesOrderDetailsId;
        this.salesOrderId = salesOrderId;
        this.productId = productId;
        this.quantity = quantity;
        this.dateCreated = dateCreated;
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

    public double getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(double itemPrice) {
        this.itemPrice = itemPrice;
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

    @Override
    public String toString() {
        return "SalesOrderDetailsDTO{" +
                "salesOrderDetailsId='" + salesOrderDetailsId + '\'' +
                ", salesOrderId='" + salesOrderId + '\'' +
                ", productId='" + productId + '\'' +
                ", quantity=" + quantity +
                '}';
    }
}
