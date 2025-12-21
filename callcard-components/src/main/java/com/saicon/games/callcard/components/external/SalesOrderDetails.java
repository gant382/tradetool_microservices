package com.saicon.games.callcard.components.external;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Stub class for SalesOrderDetails entity from ERP subsystem.
 * This is a placeholder to allow CallCard microservice to compile without
 * full ERP dependencies. The actual implementation resides in the ERP module.
 */
public class SalesOrderDetails {
    // Stub implementation - add fields/methods as needed for compilation
    private String salesOrderDetailsId;
    private String salesOrderId;
    private String productId;
    private String itemId;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal itemPrice;
    private Date dateCreated;

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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public BigDecimal getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(BigDecimal itemPrice) {
        this.itemPrice = itemPrice;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }
}
