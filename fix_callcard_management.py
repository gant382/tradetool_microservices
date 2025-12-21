#!/usr/bin/env python3
"""
Fix imports in CallCardManagement.java for the new microservice package structure.
"""

import re
from pathlib import Path

file_path = Path(r'C:/Users/dimit/tradetool_middleware/callcard-components/src/main/java/com/saicon/games/callcard/components/impl/CallCardManagement.java')

# Import mappings
MAPPINGS = [
    # Package declaration
    (r'^package com\.saicon\.games\.core\.components\.impl;',
     'package com.saicon.games.callcard.components.impl;'),

    # DTOs
    (r'import com\.saicon\.callCard\.dto\.\*;',
     'import com.saicon.games.callcard.ws.dto.*;'),
    (r'import com\.saicon\.ecommerce\.dto\.ItemStatisticsDTO;',
     'import com.saicon.games.callcard.ws.dto.ItemStatisticsDTO;'),
    (r'import com\.saicon\.ecommerce\.dto\.SolrBrandProductDTO;',
     '// TODO: import com.saicon.games.callcard.ws.dto.SolrBrandProductDTO; // Stub needed'),

    # Exceptions
    (r'import com\.saicon\.games\.commons\.exceptions\.BusinessLayerException;',
     'import com.saicon.games.callcard.exception.BusinessLayerException;'),
    (r'import com\.saicon\.games\.commons\.exceptions\.ExceptionTypeTO;',
     'import com.saicon.games.callcard.exception.ExceptionTypeTO;'),

    # Components interface
    (r'import com\.saicon\.games\.core\.components\.ICallCardManagement;',
     'import com.saicon.games.callcard.components.ICallCardManagement;'),

    # External component stubs
    (r'import com\.saicon\.games\.core\.components\.ISalesOrderManagement;',
     'import com.saicon.games.callcard.components.external.ISalesOrderManagement;'),
    (r'import com\.saicon\.games\.core\.components\.addressbook\.IAddressbookManagement;',
     'import com.saicon.games.callcard.components.external.IAddressbookManagement;'),
    (r'import com\.saicon\.games\.core\.components\.appsettings\.IAppSettingsComponent;',
     'import com.saicon.games.callcard.components.external.IAppSettingsComponent;'),
    (r'import com\.saicon\.games\.core\.components\.metadata\.IMetadataComponent;',
     'import com.saicon.games.callcard.components.external.IMetadataComponent;'),
    (r'import com\.saicon\.games\.core\.components\.authentication\.IUserMetadataComponent;',
     'import com.saicon.games.callcard.components.external.IUserMetadataComponent;'),

    # Query managers
    (r'import com\.saicon\.games\.components\.ErpDynamicQueryManager;',
     'import com.saicon.games.callcard.components.ErpDynamicQueryManager;'),
    (r'import com\.saicon\.games\.components\.ErpNativeQueryManager;',
     'import com.saicon.games.callcard.components.ErpNativeQueryManager;'),

    # DAO
    (r'import com\.saicon\.games\.entities\.common\.IGenericDAO;',
     'import com.saicon.games.callcard.dao.IGenericDAO;'),

    # Entities
    (r'import com\.saicon\.games\.entities\.\*;',
     'import com.saicon.games.callcard.entity.*;'),
    (r'import com\.saicon\.user\.entities\.Users;',
     'import com.saicon.games.entities.shared.Users;'),
    (r'import com\.saicon\.generic\.entities\.ItemTypes;',
     'import com.saicon.games.entities.shared.ItemTypes;'),
    (r'import com\.saicon\.application\.entities\.Application;',
     'import com.saicon.games.entities.shared.Application;'),

    # External entity stubs - comment out for now
    (r'import com\.saicon\.addressbook\.entities\.\*;',
     '// TODO: Addressbook entities stub needed\n// import com.saicon.addressbook.entities.*;'),
    (r'import com\.saicon\.games\.invoice\.entities\.InvoiceDetails;',
     '// TODO: InvoiceDetails entity stub needed'),
    (r'import com\.saicon\.games\.salesorder\.entities\.SalesOrder;',
     '// TODO: SalesOrder entities stub needed'),
    (r'import com\.saicon\.games\.salesorder\.entities\.SalesOrderDetails;',
     '// SalesOrderDetails stub needed'),
    (r'import com\.saicon\.games\.salesorder\.entities\.enums\.SalesOrderStatus;',
     '// SalesOrderStatus enum stub needed'),

    # External DTOs - comment out for now
    (r'import com\.saicon\.games\.appsettings\.dto\.AppSettingsDTO;',
     '// TODO: import com.saicon.games.callcard.ws.dto.AppSettingsDTO;'),
    (r'import com\.saicon\.games\.client\.data\.DecimalDTO;',
     '// TODO: import com.saicon.games.callcard.ws.dto.DecimalDTO;'),
    (r'import com\.saicon\.games\.client\.data\.MetadataKeyDTO;',
     '// TODO: import com.saicon.games.callcard.ws.dto.MetadataKeyDTO;'),
    (r'import com\.saicon\.games\.metadata\.dto\.MetadataDTO;',
     '// TODO: import com.saicon.games.callcard.ws.dto.MetadataDTO;'),
    (r'import com\.saicon\.invoice\.dto\.InvoiceDTO;',
     '// TODO: InvoiceDTO stub'),
    (r'import com\.saicon\.multiplayer\.dto\.KeyValueDTO;',
     '// TODO: KeyValueDTO stub'),
    (r'import com\.saicon\.salesOrder\.dto\.SalesOrderDTO;',
     '// TODO: SalesOrderDTO stub'),
    (r'import com\.saicon\.salesOrder\.dto\.SalesOrderDetailsDTO;',
     '// TODO: SalesOrderDetailsDTO stub'),

    # External utilities - keep as stubs
    (r'import com\.saicon\.games\.common\.EventType;',
     'import com.saicon.games.callcard.util.EventType;'),
    (r'import com\.saicon\.games\.common\.SortOrderTypes;',
     'import com.saicon.games.callcard.util.SortOrderTypes;'),
    (r'import com\.saicon\.games\.commons\.utilities\.Assert;',
     'import com.saicon.games.callcard.util.Assert;'),
    (r'import com\.saicon\.games\.commons\.utilities\.UUIDUtilities;',
     'import com.saicon.games.callcard.util.UUIDUtilities;'),
    (r'import com\.saicon\.games\.util\.Constants;',
     'import com.saicon.games.callcard.util.Constants;'),

    # External components to stub
    (r'import com\.saicon\.games\.core\.components\.events\.observers\.GeneratedEventsDispatcher;',
     'import com.saicon.games.callcard.components.external.GeneratedEventsDispatcher;'),
    (r'import com\.saicon\.games\.core\.components\.util\.solr\.SolrClient;',
     'import com.saicon.games.callcard.components.external.SolrClient;'),

    # EventTO
    (r'import com\.saicon\.games\.ws\.common\.to\.EventTO;',
     'import com.saicon.games.callcard.ws.dto.EventTO;'),

    # AppSettings enum
    (r'import com\.saicon\.appsettings\.enums\.ScopeType;',
     'import com.saicon.games.callcard.util.ScopeType;'),
]

def fix_imports():
    """Fix imports in CallCardManagement.java"""
    print(f"Processing: {file_path}")

    content = file_path.read_text(encoding='utf-8')
    original = content

    for pattern, replacement in MAPPINGS:
        content = re.sub(pattern, replacement, content, flags=re.MULTILINE)

    if content != original:
        file_path.write_text(content, encoding='utf-8')
        print("  Fixed imports in CallCardManagement.java")
    else:
        print("  No changes needed")

if __name__ == '__main__':
    fix_imports()
