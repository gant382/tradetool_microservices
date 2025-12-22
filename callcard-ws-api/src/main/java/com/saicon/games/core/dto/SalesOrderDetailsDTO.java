package com.saicon.games.core.dto;

import com.saicon.games.client.data.DecimalDTO;
import java.io.Serializable;
import java.math.BigDecimal;
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
    private DecimalDTO itemPrice;

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

    /**
     * Full constructor for CallCard management (18 parameters)
     */
    public SalesOrderDetailsDTO(Object param1, Object param2, String itemId, Integer itemTypeId,
                                Object param5, DecimalDTO itemPrice, Object param7, Object param8,
                                Object param9, Object param10, Object param11, Integer quantity,
                                Object param13, Object param14, Object param15, Object param16,
                                Object param17, Object param18) {
        this.itemId = itemId;
        this.itemTypeId = itemTypeId;
        this.itemPrice = itemPrice;
        this.quantity = quantity != null ? quantity : 0;
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

    public DecimalDTO getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(DecimalDTO itemPrice) {
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
