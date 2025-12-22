#!/usr/bin/env python3
"""Fix the final 7 BusinessLayerException errors by adding throws declarations"""

import re

file_path = r"C:\Users\dimit\tradetool_middleware\callcard-components\src\main\java\com\saicon\games\callcard\components\impl\CallCardManagement.java"

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

changes = 0

# Error lines: 3679, 3762, 3866, 3941, 3982, 4055, 4128
# Find the methods containing these lines and add throws BusinessLayerException

# Method signatures to fix (found by searching around error lines)
fixes = [
    # getUserEngagementStatistics - around line 3768
    ('    public UserEngagementDTO getUserEngagementStatistics(String userId, String userGroupId, Date dateFrom, Date dateTo) {',
     '    public UserEngagementDTO getUserEngagementStatistics(String userId, String userGroupId, Date dateFrom, Date dateTo) throws BusinessLayerException {'),

    # getActiveUsersCount - around line 3947
    ('    public Long getActiveUsersCount(String userGroupId, Date dateFrom, Date dateTo) {',
     '    public Long getActiveUsersCount(String userGroupId, Date dateFrom, Date dateTo) throws BusinessLayerException {'),

    # getAllUserEngagementStatistics - around line 3988
    ('    public List<UserEngagementDTO> getAllUserEngagementStatistics(String userGroupId, Date dateFrom, Date dateTo, Integer limit) {',
     '    public List<UserEngagementDTO> getAllUserEngagementStatistics(String userGroupId, Date dateFrom, Date dateTo, Integer limit) throws BusinessLayerException {'),

    # getOverallCallCardStatistics - around line 4055
    ('    public CallCardStatsDTO getOverallCallCardStatistics(String userGroupId, Date dateFrom, Date dateTo) {',
     '    public CallCardStatsDTO getOverallCallCardStatistics(String userGroupId, Date dateFrom, Date dateTo) throws BusinessLayerException {'),

    # getTemplateUsageStatistics - around line 4128
    ('    public TemplateUsageDTO getTemplateUsageStatistics(String templateId, String userGroupId, Date dateFrom, Date dateTo) {',
     '    public TemplateUsageDTO getTemplateUsageStatistics(String templateId, String userGroupId, Date dateFrom, Date dateTo) throws BusinessLayerException {'),
]

for old, new in fixes:
    if old in content:
        content = content.replace(old, new)
        changes += 1
        print(f"✓ Added throws to: {old.strip()[:60]}...")

if changes > 0:
    with open(file_path, 'w', encoding='utf-8', newline='') as f:
        f.write(content)
    print(f"\n✅ SUCCESS: Added throws BusinessLayerException to {changes} methods")
else:
    print("❌ No methods matched")
