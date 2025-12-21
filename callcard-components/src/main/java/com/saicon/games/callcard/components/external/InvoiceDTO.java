package com.saicon.games.callcard.components.external;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Stub class for InvoiceDTO from ERP subsystem.
 * This is a placeholder to allow CallCard microservice to compile without
 * full ERP dependencies. The actual implementation resides in the ERP module.
 */
public class InvoiceDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    // Invoice status constants
    public static final int SUBMITTED = 1;

    private String invoiceId;
    private String userId;
    private Date invoiceDate;
    private BigDecimal totalAmount;
    private String status;
    private List<InvoiceDetails> details;

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(Date invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<InvoiceDetails> getDetails() {
        return details;
    }

    public void setDetails(List<InvoiceDetails> details) {
        this.details = details;
    }
}
