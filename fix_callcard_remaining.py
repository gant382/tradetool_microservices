#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Fix remaining compilation errors in CallCardManagement.java"""

import re
import sys

def fix_remaining_errors(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # Fix line 2207 & 2235: SalesOrderDTO constructor - use correct signature
    # Constructor: SalesOrderDTO(String salesOrderId, String userId, int status, Date dateCreated)
    content = re.sub(
        r'salerOrderToCreate = new SalesOrderDTO\(callCardRefUser\.getCallCardRefUserId\(\), Constants\.ITEM_TYPE_CALL_CARD_REFUSER, salesOrders\.get\(0\)\.getSalesOrderStatus\(\), callCardRefUser\.getStartDate\(\)\);',
        'salerOrderToCreate = new SalesOrderDTO(callCardRefUser.getCallCardRefUserId(), callCardRefUser.getSourceUserId().getUserId(), salesOrders.get(0).getSalesOrderStatus(), callCardRefUser.getStartDate());',
        content
    )

    content = re.sub(
        r'salerOrderToCreate = new SalesOrderDTO\(callCardRefUser\.getCallCardRefUserId\(\), Constants\.ITEM_TYPE_CALL_CARD_REFUSER, SalesOrderStatus\.SUBMITTED\.getStatusId\(\), callCardRefUser\.getStartDate\(\)\);',
        'salerOrderToCreate = new SalesOrderDTO(callCardRefUser.getCallCardRefUserId(), callCardRefUser.getSourceUserId().getUserId(), SalesOrderStatus.SUBMITTED.getStatusId(), callCardRefUser.getStartDate());',
        content
    )

    # Fix line 2239: SalesOrderDetailsDTO constructor
    # Constructor: SalesOrderDetailsDTO(String salesOrderDetailsId, String salesOrderId, String productId, int quantity)
    content = re.sub(
        r'SalesOrderDetailsDTO salesOrderDetailsDTOToAdd = new SalesOrderDetailsDTO\(\s*null,\s*item\.getItemId\(\),\s*item\.getItemId\(\),\s*item\.getItemTypeId\(\),',
        'SalesOrderDetailsDTO salesOrderDetailsDTOToAdd = new SalesOrderDetailsDTO(\n                                                            null,\n                                                            null,\n                                                            item.getItemId(),\n                                                            Integer.parseInt(attribute.getAmount()),',
        content,
        flags=re.DOTALL
    )

    # Fix lines 2306, 2307, 2309: Handle primitive double properly
    # The issue is comparing primitives with getQuantity() which might return null
    # We need to handle the comparison more carefully
    content = re.sub(
        r'for \(SalesOrderDetails detail : revisedSalesOrder\.getSalesOrderDetails\(\)\) \{\s*for \(SalesOrderDetailsDTO detailDTO : salerOrderToCreate\.getSalesOrderDetailsDTOList\(\)\) \{\s*if \(detail\.getItemId\(\)\.equalsIgnoreCase\(detailDTO\.getItemId\(\)\)\) \{\s*BigDecimal detailPrice = detail\.getItemPrice\(\);\s*BigDecimal dtoPrice = null;\s*if \(detailDTO != null && detailDTO\.getItemPrice\(\) != null\)\s*dtoPrice = detailDTO\.getItemPrice\(\)\.toBigDecimal\(\);\s*if \(detail\.getQuantity\(\) != null && detailDTO\.getQuantity\(\) != null && !detail\.getQuantity\(\)\.equals\(detailDTO\.getQuantity\(\)\)\) \{',
        'for (SalesOrderDetails detail : revisedSalesOrder.getSalesOrderDetails()) {\n                                for (SalesOrderDetailsDTO detailDTO : salerOrderToCreate.getSalesOrderDetailsDTOList()) {\n                                    if (detail.getItemId().equalsIgnoreCase(detailDTO.getItemId())) {\n                                        BigDecimal detailPrice = detail.getItemPrice();\n                                        BigDecimal dtoPrice = null;\n                                        if (detailDTO != null && detailDTO.getItemPrice() != null)\n                                            dtoPrice = detailDTO.getItemPrice().toBigDecimal();\n\n                                        Integer detailQty = detail.getQuantity();\n                                        Integer dtoQty = detailDTO.getQuantity();\n                                        if ((detailQty != null && dtoQty != null && !detailQty.equals(dtoQty))) {',
        content,
        flags=re.DOTALL
    )

    # Fix line 2338: addSalesOrderDetails signature
    # Signature: Object addSalesOrderDetails(String orderId, String productId, int quantity, Double unitPrice, Double totalPrice, String notes)
    content = re.sub(
        r'Object salesOrderDetailsObj = salesOrderManagement\.addSalesOrderDetails\(\s*salesOrder\.getSalesOrderId\(\),\s*detailDTO\.getItemId\(\),\s*detailDTO\.getItemTypeId\(\),\s*detailDTO\.getQuantity\(\),',
        'Object salesOrderDetailsObj = salesOrderManagement.addSalesOrderDetails(\n                                    salesOrder.getSalesOrderId(),\n                                    detailDTO.getItemId(),\n                                    detailDTO.getQuantity(),\n                                    null,\n                                    null,',
        content,
        flags=re.DOTALL
    )

    # Also need to complete the addSalesOrderDetails call
    content = re.sub(
        r'Object salesOrderDetailsObj = salesOrderManagement\.addSalesOrderDetails\(\s*salesOrder\.getSalesOrderId\(\),\s*detailDTO\.getItemId\(\),\s*detailDTO\.getQuantity\(\),\s*null,\s*null,\s*null',
        'Object salesOrderDetailsObj = salesOrderManagement.addSalesOrderDetails(\n                                    salesOrder.getSalesOrderId(),\n                                    detailDTO.getItemId(),\n                                    detailDTO.getQuantity(),\n                                    null,\n                                    null,\n                                    null',
        content
    )

    # Fix line 3341: ItemTypes cast
    content = re.sub(
        r'salesToIndex\.setItemTypeId\(\(Object\) invoiceDetails\.getItemTypeId\(\)\);',
        'Object itemTypeObj = invoiceDetails.getItemTypeId();\n                    salesToIndex.setItemTypeId(itemTypeObj);',
        content
    )

    # Write fixed content
    with open(filepath, 'w', encoding='utf-8', newline='') as f:
        f.write(content)

    print("Fixed remaining compilation errors in CallCardManagement.java")

if __name__ == '__main__':
    fix_remaining_errors('C:/Users/dimit/tradetool_middleware/callcard-components/src/main/java/com/saicon/games/callcard/components/impl/CallCardManagement.java')
