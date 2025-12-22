#!/usr/bin/env python3
"""Fix the final 2 BusinessLayerException errors by adding throws to interface and implementation"""

import os

# Fix interface file
interface_path = r"C:\Users\dimit\tradetool_middleware\callcard-components\src\main\java\com\saicon\games\callcard\components\ICallCardManagement.java"

with open(interface_path, 'r', encoding='utf-8') as f:
    interface_content = f.read()

interface_changes = 0

# Interface fixes
interface_fixes = [
    ('    List<TemplateUsageDTO> getTopTemplates(String userGroupId, Integer limit, Date dateFrom, Date dateTo);',
     '    List<TemplateUsageDTO> getTopTemplates(String userGroupId, Integer limit, Date dateFrom, Date dateTo) throws BusinessLayerException;'),

    ('    List<TemplateUsageDTO> getAllTemplateUsageStatistics(String userGroupId, Date dateFrom, Date dateTo);',
     '    List<TemplateUsageDTO> getAllTemplateUsageStatistics(String userGroupId, Date dateFrom, Date dateTo) throws BusinessLayerException;'),
]

for old, new in interface_fixes:
    if old in interface_content:
        interface_content = interface_content.replace(old, new)
        interface_changes += 1
        print(f"✓ Interface: Added throws to: {old[:60]}...")

if interface_changes > 0:
    with open(interface_path, 'w', encoding='utf-8', newline='') as f:
        f.write(interface_content)
    print(f"\n✅ Interface: Added throws to {interface_changes} methods")
else:
    print("❌ Interface: No methods matched")

# Fix implementation file
impl_path = r"C:\Users\dimit\tradetool_middleware\callcard-components\src\main\java\com\saicon\games\callcard\components\impl\CallCardManagement.java"

with open(impl_path, 'r', encoding='utf-8') as f:
    impl_content = f.read()

impl_changes = 0

# Implementation fixes
impl_fixes = [
    ('    public List<TemplateUsageDTO> getTopTemplates(String userGroupId, Integer limit, Date dateFrom, Date dateTo) {',
     '    public List<TemplateUsageDTO> getTopTemplates(String userGroupId, Integer limit, Date dateFrom, Date dateTo) throws BusinessLayerException {'),

    ('    public List<TemplateUsageDTO> getAllTemplateUsageStatistics(String userGroupId, Date dateFrom, Date dateTo) {',
     '    public List<TemplateUsageDTO> getAllTemplateUsageStatistics(String userGroupId, Date dateFrom, Date dateTo) throws BusinessLayerException {'),
]

for old, new in impl_fixes:
    if old in impl_content:
        impl_content = impl_content.replace(old, new)
        impl_changes += 1
        print(f"✓ Implementation: Added throws to: {old[:60]}...")

if impl_changes > 0:
    with open(impl_path, 'w', encoding='utf-8', newline='') as f:
        f.write(impl_content)
    print(f"\n✅ Implementation: Added throws to {impl_changes} methods")
else:
    print("❌ Implementation: No methods matched")

print(f"\n🎯 TOTAL CHANGES: {interface_changes + impl_changes}/4 methods updated")
