#!/usr/bin/env python3
"""
Fix imports in CallCard service layer files according to the new package structure.
"""

import re
import sys
from pathlib import Path

# Import mappings
MAPPINGS = {
    # Package declarations
    r'^package com\.saicon\.games\.ws;': 'package com.saicon.games.callcard.service;',

    # Exception imports
    r'import com\.saicon\.games\.commons\.exceptions\.': 'import com.saicon.games.callcard.exception.',

    # DTO imports
    r'import com\.saicon\.callCard\.dto\.': 'import com.saicon.games.callcard.ws.dto.',
    r'import com\.saicon\.ecommerce\.dto\.': 'import com.saicon.games.callcard.ws.dto.',

    # Response/commons imports
    r'import com\.saicon\.games\.ws\.commons\.': 'import com.saicon.games.callcard.ws.response.',
    r'import com\.saicon\.games\.ws\.data\.': 'import com.saicon.games.callcard.ws.response.',
    r'import com\.saicon\.games\.ecommerce\.data\.': 'import com.saicon.games.callcard.ws.response.',

    # Components imports
    r'import com\.saicon\.games\.core\.components\.ICallCardManagement;': 'import com.saicon.games.callcard.components.ICallCardManagement;',
    r'import com\.saicon\.games\.core\.components\.usersession\.IUserSessionManagement;': 'import com.saicon.games.callcard.components.external.IUserSessionManagement;',
    r'import com\.saicon\.games\.core\.components\.': 'import com.saicon.games.callcard.components.',

    # WebService endpointInterface
    r'endpointInterface = "com\.saicon\.games\.ws\.ICallCardService"': 'endpointInterface = "com.saicon.games.callcard.ws.ICallCardService"',
}

# Additional import to add after LoggerFactory for CallCardService
ADDITIONAL_IMPORT_CALLCARDSERVICE = 'import com.saicon.games.callcard.ws.ICallCardService;'


def fix_file(filepath: Path) -> bool:
    """Fix imports in a single file. Returns True if changes were made."""
    print(f"Processing: {filepath}")

    try:
        content = filepath.read_text(encoding='utf-8')
        original_content = content

        # Apply all mappings
        for pattern, replacement in MAPPINGS.items():
            content = re.sub(pattern, replacement, content, flags=re.MULTILINE)

        # For CallCardService.java, add missing import after LoggerFactory
        if filepath.name == 'CallCardService.java':
            if 'import com.saicon.games.callcard.ws.ICallCardService;' not in content:
                content = content.replace(
                    'import org.slf4j.LoggerFactory;',
                    f'import org.slf4j.LoggerFactory;\nimport com.saicon.games.callcard.ws.ICallCardService;'
                )

        # Check if changes were made
        if content != original_content:
            filepath.write_text(content, encoding='utf-8')
            print(f"  ✓ Fixed imports in {filepath.name}")
            return True
        else:
            print(f"  - No changes needed for {filepath.name}")
            return False

    except Exception as e:
        print(f"  ✗ Error processing {filepath.name}: {e}")
        return False


def main():
    """Main function to process all service files."""
    service_dir = Path(r'C:/Users/dimit/tradetool_middleware/callcard-service/src/main/java/com/saicon/games/callcard/service')

    if not service_dir.exists():
        print(f"Error: Service directory not found: {service_dir}")
        sys.exit(1)

    # Process all Java files in the service directory
    files = [
        'CallCardService.java',
        'CallCardStatisticsService.java',
        'CallCardTransactionService.java',
        'SimplifiedCallCardService.java'
    ]

    fixed_count = 0
    for filename in files:
        filepath = service_dir / filename
        if filepath.exists():
            if fix_file(filepath):
                fixed_count += 1
        else:
            print(f"Warning: File not found: {filename}")

    print(f"\n{'='*60}")
    print(f"Summary: Fixed {fixed_count} file(s)")
    print(f"{'='*60}")


if __name__ == '__main__':
    main()
