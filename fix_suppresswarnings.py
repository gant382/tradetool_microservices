#!/usr/bin/env python3
"""Fix @SuppressWarnings placement for final 2 syntax errors"""

file_path = r"C:\Users\dimit\tradetool_middleware\callcard-components\src\main\java\com\saicon\games\callcard\components\impl\CallCardManagement.java"

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Remove incorrectly placed @SuppressWarnings before assignments
old1 = """        // indexesByRefUser = erpNativeQueryManager.listCallCardRefUserIndexesPreviousValues(callCardUserId, refUserIds, previousValuesSetting, activeCallCards);  // get a number of previous values
        @SuppressWarnings({"unchecked", "rawtypes"})
        indexesByRefUser = (Map) erpNativeQueryManager.listCallCardRefUserIndexesPreviousValuesSummary(callCardUserId, refUserIds, propertiesList, previousValuesSetting, recordsTypes, activeCallCards);"""
new1 = """        // indexesByRefUser = erpNativeQueryManager.listCallCardRefUserIndexesPreviousValues(callCardUserId, refUserIds, previousValuesSetting, activeCallCards);  // get a number of previous values
        indexesByRefUser = (Map) erpNativeQueryManager.listCallCardRefUserIndexesPreviousValuesSummary(callCardUserId, refUserIds, propertiesList, previousValuesSetting, recordsTypes, activeCallCards);"""

old2 = """        // detailsByRefUser = erpNativeQueryManager.listSalesOrderDetailsPreviousValues(callCardUserId, refUserIds, previousValuesSetting, activeCallCards, false); // get a number of previous values from Sales Order
        //detailsByRefUser = erpNativeQueryManager.listSalesOrderDetailsSummaries(callCardUserId, refUserIds, previousValuesSetting, activeCallCards, false); // get the summaries for a number of previous values from Sales Order
        @SuppressWarnings({"unchecked", "rawtypes"})
        detailsByRefUser = (Map) erpNativeQueryManager.listInvoiceDetailsSummaries(callCardUserId, refUserIds, previousValuesSetting, Arrays.asList(InvoiceDTO.SUBMITTED));"""
new2 = """        // detailsByRefUser = erpNativeQueryManager.listSalesOrderDetailsPreviousValues(callCardUserId, refUserIds, previousValuesSetting, activeCallCards, false); // get a number of previous values from Sales Order
        //detailsByRefUser = erpNativeQueryManager.listSalesOrderDetailsSummaries(callCardUserId, refUserIds, previousValuesSetting, activeCallCards, false); // get the summaries for a number of previous values from Sales Order
        detailsByRefUser = (Map) erpNativeQueryManager.listInvoiceDetailsSummaries(callCardUserId, refUserIds, previousValuesSetting, Arrays.asList(InvoiceDTO.SUBMITTED));"""

# Add @SuppressWarnings to variable declarations
old3 = """        Map<String, List<CallCardRefUserIndex>> indexesByRefUser;//< RefUserId, List of CallCardRefUserIndex>>
        Map<String, List<InvoiceDetails>> detailsByRefUser = new HashMap<>();//< RefUserId, List of SalesOrderDetails>>"""
new3 = """        @SuppressWarnings({"unchecked", "rawtypes"})
        Map<String, List<CallCardRefUserIndex>> indexesByRefUser;//< RefUserId, List of CallCardRefUserIndex>>
        @SuppressWarnings({"unchecked", "rawtypes"})
        Map<String, List<InvoiceDetails>> detailsByRefUser = new HashMap<>();//< RefUserId, List of SalesOrderDetails>>"""

content = content.replace(old1, new1)
content = content.replace(old2, new2)
content = content.replace(old3, new3)

with open(file_path, 'w', encoding='utf-8', newline='') as f:
    f.write(content)

print("✅ Fixed @SuppressWarnings placement")
