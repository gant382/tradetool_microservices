# PowerShell script to fix imports in CallCardManagement.java
# Run this script to apply all import fixes

$filePath = "C:\Users\dimit\tradetool_middleware\callcard-components\src\main\java\com\saicon\games\callcard\components\impl\CallCardManagement.java"

Write-Host "Processing CallCardManagement.java..." -ForegroundColor Yellow

# Create backup
$backupPath = "$filePath.backup"
Copy-Item $filePath $backupPath -Force
Write-Host "✓ Backup created: $backupPath" -ForegroundColor Green

# Read file content
$content = Get-Content $filePath -Raw

# Apply fixes
Write-Host "Applying import fixes..." -ForegroundColor Yellow

# 1. Fix package declaration
$content = $content -replace 'package com\.saicon\.games\.core\.components\.impl;', 'package com.saicon.games.callcard.components.impl;'

# 2. Fix entity imports
$content = $content -replace 'import com\.saicon\.application\.entities\.Application;', 'import com.saicon.games.entities.shared.Application;'
$content = $content -replace 'import com\.saicon\.user\.entities\.Users;', 'import com.saicon.games.entities.shared.Users;'
$content = $content -replace 'import com\.saicon\.generic\.entities\.ItemTypes;', 'import com.saicon.games.entities.shared.ItemTypes;'

# 3. Fix exception imports
$content = $content -replace 'import com\.saicon\.games\.commons\.exceptions\.BusinessLayerException;', 'import com.saicon.games.callcard.exception.BusinessLayerException;'
$content = $content -replace 'import com\.saicon\.games\.commons\.exceptions\.ExceptionTypeTO;', 'import com.saicon.games.callcard.exception.ExceptionTypeTO;'

# 4. Fix utility imports
$content = $content -replace 'import com\.saicon\.games\.commons\.utilities\.Assert;', 'import com.saicon.games.callcard.util.Assert;'
$content = $content -replace 'import com\.saicon\.games\.commons\.utilities\.UUIDUtilities;', 'import com.saicon.games.callcard.util.UUIDUtilities;'

# 5. Fix DAO import
$content = $content -replace 'import com\.saicon\.games\.entities\.common\.IGenericDAO;', 'import com.saicon.games.callcard.dao.IGenericDAO;'

# 6. Fix component imports
$content = $content -replace 'import com\.saicon\.games\.components\.ErpDynamicQueryManager;', 'import com.saicon.games.callcard.components.ErpDynamicQueryManager;'
$content = $content -replace 'import com\.saicon\.games\.components\.ErpNativeQueryManager;', 'import com.saicon.games.callcard.components.ErpNativeQueryManager;'
$content = $content -replace 'import com\.saicon\.games\.core\.components\.ICallCardManagement;', 'import com.saicon.games.callcard.components.ICallCardManagement;'
$content = $content -replace 'import com\.saicon\.games\.core\.components\.ISalesOrderManagement;', 'import com.saicon.games.callcard.components.external.ISalesOrderManagement;'
$content = $content -replace 'import com\.saicon\.games\.core\.components\.addressbook\.IAddressbookManagement;', 'import com.saicon.games.callcard.components.external.IAddressbookManagement;'
$content = $content -replace 'import com\.saicon\.games\.core\.components\.appsettings\.IAppSettingsComponent;', 'import com.saicon.games.callcard.components.external.IAppSettingsComponent;'
$content = $content -replace 'import com\.saicon\.games\.core\.components\.metadata\.IMetadataComponent;', 'import com.saicon.games.callcard.components.external.IMetadataComponent;'
$content = $content -replace 'import com\.saicon\.games\.core\.components\.authentication\.IUserMetadataComponent;', 'import com.saicon.games.callcard.components.external.IUserMetadataComponent;'

# 7. Fix DTO imports
$content = $content -replace 'import com\.saicon\.callCard\.dto\.\*;', 'import com.saicon.games.callcard.ws.dto.*;'

# 8. Fix entity wildcard import
$content = $content -replace 'import com\.saicon\.games\.entities\.\*;', 'import com.saicon.games.callcard.entity.*;'

# 9. Comment out stub imports
$stubImports = @(
    'import com\.saicon\.addressbook\.entities\.\*;',
    'import com\.saicon\.appsettings\.enums\.ScopeType;',
    'import com\.saicon\.ecommerce\.dto\.ItemStatisticsDTO;',
    'import com\.saicon\.ecommerce\.dto\.SolrBrandProductDTO;',
    'import com\.saicon\.games\.appsettings\.dto\.AppSettingsDTO;',
    'import com\.saicon\.games\.client\.data\.DecimalDTO;',
    'import com\.saicon\.games\.client\.data\.MetadataKeyDTO;',
    'import com\.saicon\.games\.common\.EventType;',
    'import com\.saicon\.games\.common\.SortOrderTypes;',
    'import com\.saicon\.games\.core\.components\.events\.observers\.GeneratedEventsDispatcher;',
    'import com\.saicon\.games\.core\.components\.util\.solr\.SolrClient;',
    'import com\.saicon\.games\.invoice\.entities\.InvoiceDetails;',
    'import com\.saicon\.games\.metadata\.dto\.MetadataDTO;',
    'import com\.saicon\.games\.salesorder\.entities\.SalesOrder;',
    'import com\.saicon\.games\.salesorder\.entities\.SalesOrderDetails;',
    'import com\.saicon\.games\.salesorder\.entities\.enums\.SalesOrderStatus;',
    'import com\.saicon\.games\.util\.Constants;',
    'import com\.saicon\.games\.ws\.common\.to\.EventTO;',
    'import com\.saicon\.invoice\.dto\.InvoiceDTO;',
    'import com\.saicon\.multiplayer\.dto\.KeyValueDTO;',
    'import com\.saicon\.salesOrder\.dto\.SalesOrderDTO;',
    'import com\.saicon\.salesOrder\.dto\.SalesOrderDetailsDTO;'
)

foreach ($import in $stubImports) {
    $content = $content -replace $import, "// TODO: stub needed - $($import -replace '\\.', '.')"
}

# Write back
$content | Set-Content $filePath -NoNewline

Write-Host "✓ Import fixes applied successfully!" -ForegroundColor Green

Write-Host "`nSummary:" -ForegroundColor Cyan
Write-Host "  - Fixed package declaration" -ForegroundColor White
Write-Host "  - Remapped entity imports (Application, Users, ItemTypes)" -ForegroundColor White
Write-Host "  - Remapped exception imports (BusinessLayerException, ExceptionTypeTO)" -ForegroundColor White
Write-Host "  - Remapped utility imports (Assert, UUIDUtilities)" -ForegroundColor White
Write-Host "  - Remapped DAO imports (IGenericDAO)" -ForegroundColor White
Write-Host "  - Remapped component imports" -ForegroundColor White
Write-Host "  - Remapped DTO imports" -ForegroundColor White
Write-Host "  - Commented out 22 stub imports that need implementation" -ForegroundColor White

Write-Host "`nNext steps:" -ForegroundColor Yellow
Write-Host "  1. Review the file for any compilation errors" -ForegroundColor White
Write-Host "  2. Implement stubs for commented-out imports as needed" -ForegroundColor White
Write-Host "  3. Fix any methods that depend on stub classes" -ForegroundColor White
