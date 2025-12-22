#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Fix remaining BusinessLayerException declarations"""

import re
import sys

file_path = r"C:/Users/dimit/tradetool_middleware/callcard-components/src/main/java/com/saicon/games/callcard/components/impl/CallCardManagement.java"

try:
    with open(file_path, 'r', encoding='utf-8') as f:
        lines = f.readlines()
except Exception as e:
    print(f"Error reading file: {e}")
    sys.exit(1)

changes_made = 0
modified_lines = []

for i, line in enumerate(lines):
    original_line = line

    # Check if this line is a method signature that needs fixing
    # Pattern: method declaration ending with ) { but not having throws BusinessLayerException
    if ((' getCallCardTemplate(' in line or
         ' submitTransactions(' in line or
         ' createOrUpdateAdditionalRefUserInfo(' in line or
         ' addCallCardIndexes(' in line or
         ' createOrUpdateSimplifiedCallCardIndexes(' in line or
         ' getNewOrPendingCallCard(' in line or
         ' getCallCardTemplateByMetadataKeys(' in line) and
        ') {' in line and
        'throws BusinessLayerException' not in line):

        # Replace ') {' with ') throws BusinessLayerException {'
        line = line.replace(') {', ') throws BusinessLayerException {')
        changes_made += 1
        print(f"Line {i+1}: {original_line.strip()[:80]}")
        print(f"   ->: {line.strip()[:80]}")
        print()

    modified_lines.append(line)

if changes_made > 0:
    try:
        with open(file_path, 'w', encoding='utf-8', newline='') as f:
            f.writelines(modified_lines)
        print(f"✓ Successfully updated {changes_made} method signatures!")
    except Exception as e:
        print(f"✗ Error writing file: {e}")
        sys.exit(1)
else:
    print("✗ No changes needed - all methods already have throws declaration or not found.")
