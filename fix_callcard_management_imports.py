#!/usr/bin/env python3
"""
Script to fix imports in CallCardManagement.java
Applies comprehensive import mappings and comments out non-existent imports.
"""

import re
import os

# File path
file_path = r"C:\Users\dimit\tradetool_middleware\callcard-components\src\main\java\com\saicon\games\callcard\components\impl\CallCardManagement.java"

def fix_imports(content):
    """Apply import fixes to file content"""

    # 1. Fix package declaration
    content = re.sub(
        r'package com\.saicon\.games\.core\.components\.impl;',
        'package com.saicon.games.callcard.components.impl;',
        content
    )

    # 2. Fix entity imports - Application
    content = re.sub(
        r'import com\.saicon\.application\.entities\.Application;',
        'import com.saicon.games.entities.shared.Application;',
        content
    )

    # 3. Fix entity imports - Users
    content = re.sub(
        r'import com\.saicon\.user\.entities\.Users;',
        'import com.saicon.games.entities.shared.Users;',
        content
    )

    # 4. Fix entity imports - ItemTypes
    content = re.sub(
        r'import com\.saicon\.generic\.entities\.ItemTypes;',
        'import com.saicon.games.entities.shared.ItemTypes;',
        content
    )

    # 5. Fix exception imports
    content = re.sub(
        r'import com\.saicon\.games\.commons\.exceptions\.BusinessLayerException;',
        'import com.saicon.games.callcard.exception.BusinessLayerException;',
        content
    )
    content = re.sub(
        r'import com\.saicon\.games\.commons\.exceptions\.ExceptionTypeTO;',
        'import com.saicon.games.callcard.exception.ExceptionTypeTO;',
        content
    )

    # 6. Fix utility imports
    content = re.sub(
        r'import com\.saicon\.games\.commons\.utilities\.Assert;',
        'import com.saicon.games.callcard.util.Assert;',
        content
    )
    content = re.sub(
        r'import com\.saicon\.games\.commons\.utilities\.UUIDUtilities;',
        'import com.saicon.games.callcard.util.UUIDUtilities;',
        content
    )

    # 7. Fix DAO import
    content = re.sub(
        r'import com\.saicon\.games\.entities\.common\.IGenericDAO;',
        'import com.saicon.games.callcard.dao.IGenericDAO;',
        content
    )

    # 8. Fix component imports
    content = re.sub(
        r'import com\.saicon\.games\.components\.ErpDynamicQueryManager;',
        'import com.saicon.games.callcard.components.ErpDynamicQueryManager;',
        content
    )
    content = re.sub(
        r'import com\.saicon\.games\.components\.ErpNativeQueryManager;',
        'import com.saicon.games.callcard.components.ErpNativeQueryManager;',
        content
    )
    content = re.sub(
        r'import com\.saicon\.games\.core\.components\.ICallCardManagement;',
        'import com.saicon.games.callcard.components.ICallCardManagement;',
        content
    )
    content = re.sub(
        r'import com\.saicon\.games\.core\.components\.ISalesOrderManagement;',
        'import com.saicon.games.callcard.components.external.ISalesOrderManagement;',
        content
    )
    content = re.sub(
        r'import com\.saicon\.games\.core\.components\.addressbook\.IAddressbookManagement;',
        'import com.saicon.games.callcard.components.external.IAddressbookManagement;',
        content
    )
    content = re.sub(
        r'import com\.saicon\.games\.core\.components\.appsettings\.IAppSettingsComponent;',
        'import com.saicon.games.callcard.components.external.IAppSettingsComponent;',
        content
    )
    content = re.sub(
        r'import com\.saicon\.games\.core\.components\.metadata\.IMetadataComponent;',
        'import com.saicon.games.callcard.components.external.IMetadataComponent;',
        content
    )
    content = re.sub(
        r'import com\.saicon\.games\.core\.components\.authentication\.IUserMetadataComponent;',
        'import com.saicon.games.callcard.components.external.IUserMetadataComponent;',
        content
    )

    # 9. Fix DTO imports - change from com.saicon.callCard.dto to com.saicon.games.callcard.ws.dto
    content = re.sub(
        r'import com\.saicon\.callCard\.dto\.\*;',
        'import com.saicon.games.callcard.ws.dto.*;',
        content
    )

    # 10. Fix entity wildcard import - replace with specific CallCard entity imports
    content = re.sub(
        r'import com\.saicon\.games\.entities\.\*;',
        'import com.saicon.games.callcard.entity.*;',
        content
    )

    # 11. Comment out imports that don't have equivalents (stubs needed)
    stub_imports = [
        (r'import com\.saicon\.addressbook\.entities\.\*;',
         '// TODO: stub needed - import com.saicon.addressbook.entities.*;'),
        (r'import com\.saicon\.appsettings\.enums\.ScopeType;',
         '// TODO: stub needed - import com.saicon.appsettings.enums.ScopeType;'),
        (r'import com\.saicon\.ecommerce\.dto\.ItemStatisticsDTO;',
         '// TODO: stub needed - import com.saicon.ecommerce.dto.ItemStatisticsDTO;'),
        (r'import com\.saicon\.ecommerce\.dto\.SolrBrandProductDTO;',
         '// TODO: stub needed - import com.saicon.ecommerce.dto.SolrBrandProductDTO;'),
        (r'import com\.saicon\.games\.appsettings\.dto\.AppSettingsDTO;',
         '// TODO: stub needed - import com.saicon.games.appsettings.dto.AppSettingsDTO;'),
        (r'import com\.saicon\.games\.client\.data\.DecimalDTO;',
         '// TODO: stub needed - import com.saicon.games.client.data.DecimalDTO;'),
        (r'import com\.saicon\.games\.client\.data\.MetadataKeyDTO;',
         '// TODO: stub needed - import com.saicon.games.client.data.MetadataKeyDTO;'),
        (r'import com\.saicon\.games\.common\.EventType;',
         '// TODO: stub needed - import com.saicon.games.common.EventType;'),
        (r'import com\.saicon\.games\.common\.SortOrderTypes;',
         '// TODO: stub needed - import com.saicon.games.common.SortOrderTypes;'),
        (r'import com\.saicon\.games\.core\.components\.events\.observers\.GeneratedEventsDispatcher;',
         '// TODO: stub needed - import com.saicon.games.core.components.events.observers.GeneratedEventsDispatcher;'),
        (r'import com\.saicon\.games\.core\.components\.util\.solr\.SolrClient;',
         '// TODO: stub needed - import com.saicon.games.core.components.util.solr.SolrClient;'),
        (r'import com\.saicon\.games\.invoice\.entities\.InvoiceDetails;',
         '// TODO: stub needed - import com.saicon.games.invoice.entities.InvoiceDetails;'),
        (r'import com\.saicon\.games\.metadata\.dto\.MetadataDTO;',
         '// TODO: stub needed - import com.saicon.games.metadata.dto.MetadataDTO;'),
        (r'import com\.saicon\.games\.salesorder\.entities\.SalesOrder;',
         '// TODO: stub needed - import com.saicon.games.salesorder.entities.SalesOrder;'),
        (r'import com\.saicon\.games\.salesorder\.entities\.SalesOrderDetails;',
         '// TODO: stub needed - import com.saicon.games.salesorder.entities.SalesOrderDetails;'),
        (r'import com\.saicon\.games\.salesorder\.entities\.enums\.SalesOrderStatus;',
         '// TODO: stub needed - import com.saicon.games.salesorder.entities.enums.SalesOrderStatus;'),
        (r'import com\.saicon\.games\.util\.Constants;',
         '// TODO: stub needed - import com.saicon.games.util.Constants;'),
        (r'import com\.saicon\.games\.ws\.common\.to\.EventTO;',
         '// TODO: stub needed - import com.saicon.games.ws.common.to.EventTO;'),
        (r'import com\.saicon\.invoice\.dto\.InvoiceDTO;',
         '// TODO: stub needed - import com.saicon.invoice.dto.InvoiceDTO;'),
        (r'import com\.saicon\.multiplayer\.dto\.KeyValueDTO;',
         '// TODO: stub needed - import com.saicon.multiplayer.dto.KeyValueDTO;'),
        (r'import com\.saicon\.salesOrder\.dto\.SalesOrderDTO;',
         '// TODO: stub needed - import com.saicon.salesOrder.dto.SalesOrderDTO;'),
        (r'import com\.saicon\.salesOrder\.dto\.SalesOrderDetailsDTO;',
         '// TODO: stub needed - import com.saicon.salesOrder.dto.SalesOrderDetailsDTO;'),
    ]

    for pattern, replacement in stub_imports:
        content = re.sub(pattern, replacement, content)

    return content

def main():
    """Fix imports in CallCardManagement.java"""
    print(f"Processing {file_path}...")

    try:
        # Create backup
        backup_path = file_path + '.backup'

        # Read file
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()

        # Create backup
        with open(backup_path, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"  ✓ Backup created: {backup_path}")

        # Apply fixes
        fixed_content = fix_imports(content)

        # Write back
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(fixed_content)

        print(f"  ✓ Import fixes applied successfully!")
        print(f"\nSummary:")
        print(f"  - Fixed package declaration")
        print(f"  - Remapped entity imports (Application, Users, ItemTypes)")
        print(f"  - Remapped exception imports")
        print(f"  - Remapped utility imports (Assert, UUIDUtilities)")
        print(f"  - Remapped DAO imports")
        print(f"  - Remapped component imports")
        print(f"  - Remapped DTO imports")
        print(f"  - Commented out 22 stub imports that need implementation")

    except Exception as e:
        print(f"  ✗ Error processing file: {e}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    main()
