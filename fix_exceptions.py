#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Fix BusinessLayerException declarations in CallCardManagement.java"""

import re
import sys

file_path = r"C:/Users/dimit/tradetool_middleware/callcard-components/src/main/java/com/saicon/games/callcard/components/impl/CallCardManagement.java"

# Read file with UTF-8 encoding
try:
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
except Exception as e:
    print(f"Error reading file: {e}")
    sys.exit(1)

# Original content length
original_length = len(content)

# Define replacements - exact patterns to match
replacements = [
    (
        'private List<CallCardTemplate> getCallCardTemplateByMetadataKeys(String userId, String userGroupId, String gameTypeId, String callCardTemplateId, List<String> metadataKeys) {',
        'private List<CallCardTemplate> getCallCardTemplateByMetadataKeys(String userId, String userGroupId, String gameTypeId, String callCardTemplateId, List<String> metadataKeys) throws BusinessLayerException {'
    ),
    (
        'public CallCardDTO getNewOrPendingCallCard(String userId, String userGroupId, String gameTypeId, String applicationId, String callCardId, List<String> filterProperties) {',
        'public CallCardDTO getNewOrPendingCallCard(String userId, String userGroupId, String gameTypeId, String applicationId, String callCardId, List<String> filterProperties) throws BusinessLayerException {'
    ),
    (
        'public void addCallCardIndexes(String userId, String gameTypeId, String applicationId, CallCard callCard, List<CallCardGroupDTO> groups) {',
        'public void addCallCardIndexes(String userId, String gameTypeId, String applicationId, CallCard callCard, List<CallCardGroupDTO> groups) throws BusinessLayerException {'
    ),
    (
        'public void createOrUpdateSimplifiedCallCardIndexes(String userId, String gameTypeId, String applicationId, CallCard callCard, List<SimplifiedCallCardRefUserDTO> refUserIds) {',
        'public void createOrUpdateSimplifiedCallCardIndexes(String userId, String gameTypeId, String applicationId, CallCard callCard, List<SimplifiedCallCardRefUserDTO> refUserIds) throws BusinessLayerException {'
    ),
    (
        'public void addOrUpdateSimplifiedCallCard(String userGroupId, String gameTypeId, String applicationId, String userId, SimplifiedCallCardDTO callCard) {',
        'public void addOrUpdateSimplifiedCallCard(String userGroupId, String gameTypeId, String applicationId, String userId, SimplifiedCallCardDTO callCard) throws BusinessLayerException {'
    ),
    (
        'private CallCardTemplate getCallCardTemplateByMetadataProperty(String userGroupId, String gameTypeId, String userId) {',
        'private CallCardTemplate getCallCardTemplateByMetadataProperty(String userGroupId, String gameTypeId, String userId) throws BusinessLayerException {'
    ),
    (
        'private CallCardTemplate getCallCardTemplate(String userGroupId, String gameTypeId, String applicationId, String userId) {',
        'private CallCardTemplate getCallCardTemplate(String userGroupId, String gameTypeId, String applicationId, String userId) throws BusinessLayerException {'
    ),
    (
        'public void submitTransactions(String userId, String userGroupId, String gameTypeId, String applicationId, String indirectUserId, CallCardDTO callCardDTO) {',
        'public void submitTransactions(String userId, String userGroupId, String gameTypeId, String applicationId, String indirectUserId, CallCardDTO callCardDTO) throws BusinessLayerException {'
    ),
    (
        'private void createOrUpdateAdditionalRefUserInfo(String userId, List<KeyValueDTO> additionalRefUserInfo) {',
        'private void createOrUpdateAdditionalRefUserInfo(String userId, List<KeyValueDTO> additionalRefUserInfo) throws BusinessLayerException {'
    ),
    (
        'public CallCardStatsDTO getOverallCallCardStatistics(String userGroupId, Date dateFrom, Date dateTo) {',
        'public CallCardStatsDTO getOverallCallCardStatistics(String userGroupId, Date dateFrom, Date dateTo) throws BusinessLayerException {'
    ),
    (
        'public TemplateUsageDTO getTemplateUsageStatistics(String templateId, String userGroupId, Date dateFrom, Date dateTo) {',
        'public TemplateUsageDTO getTemplateUsageStatistics(String templateId, String userGroupId, Date dateFrom, Date dateTo) throws BusinessLayerException {'
    )
]

# Apply replacements
changes_made = 0
for old, new in replacements:
    if old in content:
        content = content.replace(old, new)
        changes_made += 1
        print(f"✓ Applied: {old[:60]}...")
    else:
        print(f"✗ NOT FOUND: {old[:60]}...")

# Write file with UTF-8 encoding (no BOM)
if changes_made > 0:
    try:
        with open(file_path, 'w', encoding='utf-8', newline='') as f:
            f.write(content)
        print(f"\n✓ Successfully updated {changes_made} method signatures!")
        print(f"  Original size: {original_length} bytes")
        print(f"  New size: {len(content)} bytes")
    except Exception as e:
        print(f"\n✗ Error writing file: {e}")
        sys.exit(1)
else:
    print("\n✗ No changes were made - patterns not found in file.")
    sys.exit(1)
