package com.saicon.games.callcard.components.external;

/**
 * Stub class for InvoiceDetails entity from ERP subsystem.
 * This is a placeholder to allow CallCard microservice to compile without
 * full ERP dependencies. The actual implementation resides in the ERP module.
 */
public class InvoiceDetails {
    // Stub implementation - add fields/methods as needed for compilation
    private String invoiceDetailsId;
    private String invoiceId;
    private Integer itemTypeId;
    private String itemId;
    private Integer quantity;

    public String getInvoiceDetailsId() {
        return invoiceDetailsId;
    }

    public void setInvoiceDetailsId(String invoiceDetailsId) {
        this.invoiceDetailsId = invoiceDetailsId;
    }

    public String getInvoiceId() {
        return invoiceId;
    }


    public Integer getItemTypeId() {
        return itemTypeId;
    }

    public void setItemTypeId(Integer itemTypeId) {
        this.itemTypeId = itemTypeId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
