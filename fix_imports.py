#!/usr/bin/env python3
"""
Fix import errors in CallCard REST resource files.
Applies the following mappings:
- com.saicon.games.commons.exceptions.* → com.saicon.games.callcard.exception.*
- com.saicon.games.ws.commons.* → com.saicon.games.callcard.ws.response.*
- com.saicon.callCard.dto.* → com.saicon.games.callcard.ws.dto.*
- Remove or comment out ITalosResource interface references
- Fix package declaration for CallCardResources.java
"""

import os
import re

# File paths
base_path = "C:/Users/dimit/tradetool_middleware/callcard-ws-api/src/main/java/com/saicon/games/callcard/resources"
files = [
    "CallCardResources.java",
    "CallCardStatisticsResources.java",
    "CallCardTransactionResources.java",
    "SimplifiedCallCardResources.java"
]

def fix_imports(content, filename):
    """Apply import fixes to file content"""

    # Fix package declaration for CallCardResources.java
    if filename == "CallCardResources.java":
        content = re.sub(
            r'package com\.saicon\.talos\.services;',
            'package com.saicon.games.callcard.resources;',
            content
        )

    # Fix DTO imports: com.saicon.callCard.dto.* → com.saicon.games.callcard.ws.dto.*
    content = re.sub(
        r'import com\.saicon\.callCard\.dto\.',
        'import com.saicon.games.callcard.ws.dto.',
        content
    )

    # Fix exception imports: com.saicon.games.commons.exceptions.* → com.saicon.games.callcard.exception.*
    content = re.sub(
        r'import com\.saicon\.games\.commons\.exceptions\.',
        'import com.saicon.games.callcard.exception.',
        content
    )

    # Fix response imports: com.saicon.games.ws.commons.* → com.saicon.games.callcard.ws.response.*
    content = re.sub(
        r'import com\.saicon\.games\.ws\.commons\.',
        'import com.saicon.games.callcard.ws.response.',
        content
    )

    # Remove ITalosResource interface reference
    content = re.sub(
        r'(implements\s+ICallCardResources)\s*,\s*ITalosResource',
        r'\1 /* , ITalosResource */',
        content
    )

    return content

def main():
    """Fix imports in all resource files"""
    for filename in files:
        filepath = os.path.join(base_path, filename)

        print(f"Processing {filename}...")

        try:
            # Read file
            with open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()

            # Apply fixes
            fixed_content = fix_imports(content, filename)

            # Write back
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(fixed_content)

            print(f"  ✓ Fixed {filename}")

        except Exception as e:
            print(f"  ✗ Error processing {filename}: {e}")

    print("\nDone!")

if __name__ == "__main__":
    main()
