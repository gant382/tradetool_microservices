#!/usr/bin/env python3
"""Fix the final 3 compilation errors"""

import re

file_path = r"C:\Users\dimit\tradetool_middleware\callcard-components\src\main\java\com\saicon\games\callcard\components\impl\CallCardManagement.java"

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

changes = 0

# Fix 1 & 2: Lines 2270, 2460 - getValue() returns long, but method expects BigDecimal
# Change getValue() to toBigDecimal() to get BigDecimal instead of long
old1 = "attribute.getAmount().getValue() : 0L"
new1 = "attribute.getAmount().toBigDecimal() : BigDecimal.ZERO"
if old1 in content:
    count = content.count(old1)
    content = content.replace(old1, new1)
    changes += count
    print(f"✓ Fixed DecimalDTO getValue() to toBigDecimal() ({count} occurrences)")

# Fix 3: Line 3342 - Integer to ItemTypes conversion
# Use constructor instead of non-existent getItemTypeById()
old3 = "salesToIndex.setItemTypeId(ItemTypes.getItemTypeById(invoiceDetails.getItemTypeId()));"
new3 = "salesToIndex.setItemTypeId(invoiceDetails.getItemTypeId() != null ? new ItemTypes(invoiceDetails.getItemTypeId()) : null);"
if old3 in content:
    content = content.replace(old3, new3)
    changes += 1
    print("✓ Fixed Integer to ItemTypes conversion with constructor")

if changes > 0:
    with open(file_path, 'w', encoding='utf-8', newline='') as f:
        f.write(content)
    print(f"\n✅ SUCCESS: Applied {changes} fixes")
else:
    print("❌ No patterns matched")
