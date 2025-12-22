package com.saicon.games.callcard.components.external;

import com.saicon.games.core.dto.SalesOrderDetailsDTO;
import java.math.BigDecimal;
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
    SalesOrder addSalesOrder(
        String createdByUserId,
        String fromUserId,
        String toUserId,
        Date dateCreated,
        Date dateUpdated,
        Object param6,
        Object param7,
        Object param8,
        String comments,
        Object param10,
        Object param11,
        Object param12,
        Object param13,
        int salesOrderStatus,
        Date dateSubmitted,
        Object param16,
        Object param17,
        Object param18,
        String refItemId,
        Integer refItemTypeId
    );

    /**
     * Create a sales order revision
     */
    SalesOrder createSalesOrderRevision(
        String createdByUserId,
        Date dateCreated,
        Date dateUpdated,
        Object param4,
        Object param5,
        Object param6,
        String comments,
        Object param8,
        Object param9,
        Object param10,
        Object param11,
        Date dateSubmitted,
        String salesOrderId,
        String refItemId,
        Integer refItemTypeId
    );

    /**
     * Add sales order details/line items
     */
    SalesOrderDetailsDTO addSalesOrderDetails(
        String salesOrderId,
        String itemId,
        Integer itemTypeId,
        Object param4,
        BigDecimal itemPrice,
        Object param6,
        Object param7,
        Object param8,
        Object param9,
        Object param10,
        Integer quantity,
        Object param12,
        Object param13,
        Object param14,
        Date dateCreated,
        Object param16,
        Object param17
    );
}
