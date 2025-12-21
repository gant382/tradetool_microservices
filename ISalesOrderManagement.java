package com.saicon.games.callcard.components.external;

import java.util.Date;

/**
 * Sales Order Management interface for CallCard microservice.
 * COPY TO: callcard-components/src/main/java/com/saicon/games/callcard/components/external/ISalesOrderManagement.java
 *
 * Provides stub interface for sales order operations.
 * Implementations will integrate with ERP system.
 */
public interface ISalesOrderManagement {

    /**
     * Add a new sales order
     */
    Object addSalesOrder(
        String orderId,
        String userId,
        String userGroupId,
        Date orderDate,
        Date deliveryDate,
        String orderStatus,
        String paymentMethod,
        String shippingAddress,
        String notes,
        Double totalAmount,
        Double taxAmount,
        Double shippingCost,
        String currency,
        int orderType,
        Date createdDate,
        String createdBy,
        String modifiedBy,
        Date modifiedDate,
        String externalReference,
        Integer priority
    );

    /**
     * Create a sales order revision
     */
    Object createSalesOrderRevision(
        String orderId,
        Date revisionDate,
        Date deliveryDate,
        String orderStatus,
        String paymentMethod,
        String shippingAddress,
        String notes,
        Double totalAmount,
        Double taxAmount,
        Double shippingCost,
        String currency,
        Date createdDate,
        String createdBy,
        String externalReference,
        Integer revisionNumber
    );

    /**
     * Add sales order details/line items
     */
    Object addSalesOrderDetails(
        String orderId,
        String productId,
        int quantity,
        Double unitPrice,
        Double totalPrice,
        String notes
    );
}
