#!/usr/bin/env python3
"""Add throws BusinessLayerException to interface method declarations"""

file_path = r"C:\Users\dimit\tradetool_middleware\callcard-components\src\main\java\com\saicon\games\callcard\components\ICallCardManagement.java"

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

changes = 0

# Fix the 5 interface method declarations
fixes = [
    ('    CallCardStatsDTO getOverallCallCardStatistics(String userGroupId, Date dateFrom, Date dateTo);',
     '    CallCardStatsDTO getOverallCallCardStatistics(String userGroupId, Date dateFrom, Date dateTo) throws BusinessLayerException;'),

    ('    TemplateUsageDTO getTemplateUsageStatistics(String templateId, String userGroupId, Date dateFrom, Date dateTo);',
     '    TemplateUsageDTO getTemplateUsageStatistics(String templateId, String userGroupId, Date dateFrom, Date dateTo) throws BusinessLayerException;'),

    ('    UserEngagementDTO getUserEngagementStatistics(String userId, String userGroupId, Date dateFrom, Date dateTo);',
     '    UserEngagementDTO getUserEngagementStatistics(String userId, String userGroupId, Date dateFrom, Date dateTo) throws BusinessLayerException;'),

    ('    Long getActiveUsersCount(String userGroupId, Date dateFrom, Date dateTo);',
     '    Long getActiveUsersCount(String userGroupId, Date dateFrom, Date dateTo) throws BusinessLayerException;'),

    ('    List<UserEngagementDTO> getAllUserEngagementStatistics(String userGroupId, Date dateFrom, Date dateTo, Integer limit);',
     '    List<UserEngagementDTO> getAllUserEngagementStatistics(String userGroupId, Date dateFrom, Date dateTo, Integer limit) throws BusinessLayerException;'),
]

for old, new in fixes:
    if old in content:
        content = content.replace(old, new)
        changes += 1
        print(f"✓ Added throws to interface method: {old[:70]}...")

if changes > 0:
    with open(file_path, 'w', encoding='utf-8', newline='') as f:
        f.write(content)
    print(f"\n✅ SUCCESS: Added throws to {changes} interface methods")
else:
    print("❌ No methods matched")
