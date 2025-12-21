#!/usr/bin/env pwsh
# PowerShell script to apply all compilation fixes to callcard modules
# Run from tradetool_middleware directory

Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host " CallCard Microservice - Compilation Fixes Applicator" -ForegroundColor Cyan
Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host ""

$ErrorActionPreference = "Stop"

# Phase 1: Fix ws-api module
Write-Host "PHASE 1: Fixing ws-api module..." -ForegroundColor Yellow
Write-Host ""

# 1.1 Fix UserEngagementDTO
Write-Host "1. Updating UserEngagementDTO.java..." -ForegroundColor Green
$targetPath = "callcard-ws-api/src/main/java/com/saicon/games/callcard/ws/dto/UserEngagementDTO.java"
if (Test-Path "UserEngagementDTO_FIXED.java") {
    Copy-Item "UserEngagementDTO_FIXED.java" $targetPath -Force
    Write-Host "   ✓ UserEngagementDTO updated" -ForegroundColor Green
} else {
    Write-Host "   ✗ UserEngagementDTO_FIXED.java not found!" -ForegroundColor Red
    exit 1
}

# 1.2 Fix TemplateUsageDTO
Write-Host "2. Updating TemplateUsageDTO.java..." -ForegroundColor Green
$targetPath = "callcard-ws-api/src/main/java/com/saicon/games/callcard/ws/dto/TemplateUsageDTO.java"
if (Test-Path "TemplateUsageDTO_FIXED.java") {
    Copy-Item "TemplateUsageDTO_FIXED.java" $targetPath -Force
    Write-Host "   ✓ TemplateUsageDTO updated" -ForegroundColor Green
} else {
    Write-Host "   ✗ TemplateUsageDTO_FIXED.java not found!" -ForegroundColor Red
    exit 1
}

# 1.3 Fix ExceptionTypeTO
Write-Host "3. Updating ExceptionTypeTO.java..." -ForegroundColor Green
$exceptionFile = "callcard-ws-api/src/main/java/com/saicon/games/callcard/exception/ExceptionTypeTO.java"
$content = Get-Content $exceptionFile -Raw
if (-not ($content -match "GENERIC_ERROR")) {
    $content = $content -replace 'public static final String GENERIC = "9999";',
                                  'public static final String GENERIC = "9999";
    public static final String GENERIC_ERROR = "9999"; // Alias for GENERIC'
    Set-Content $exceptionFile -Value $content -NoNewline
    Write-Host "   ✓ Added GENERIC_ERROR constant" -ForegroundColor Green
} else {
    Write-Host "   ✓ GENERIC_ERROR already exists" -ForegroundColor Cyan
}

# 1.4 Rebuild ws-api
Write-Host ""
Write-Host "4. Rebuilding callcard-ws-api..." -ForegroundColor Green
Push-Location callcard-ws-api
mvn clean install -DskipTests -U
$wsApiResult = $LASTEXITCODE
Pop-Location

if ($wsApiResult -ne 0) {
    Write-Host "   ✗ callcard-ws-api build FAILED!" -ForegroundColor Red
    exit 1
}
Write-Host "   ✓ callcard-ws-api BUILD SUCCESS" -ForegroundColor Green

# Phase 2: Fix components module
Write-Host ""
Write-Host "PHASE 2: Fixing components module..." -ForegroundColor Yellow
Write-Host ""

# 2.1 Create external interfaces directory
Write-Host "5. Creating external interfaces..." -ForegroundColor Green
$externalDir = "callcard-components/src/main/java/com/saicon/games/callcard/components/external"
if (-not (Test-Path $externalDir)) {
    New-Item -ItemType Directory -Path $externalDir -Force | Out-Null
    Write-Host "   ✓ Created external directory" -ForegroundColor Green
}

# 2.2 Copy ISalesOrderManagement
if (Test-Path "ISalesOrderManagement.java") {
    Copy-Item "ISalesOrderManagement.java" "$externalDir/ISalesOrderManagement.java" -Force
    Write-Host "   ✓ ISalesOrderManagement.java created" -ForegroundColor Green
} else {
    Write-Host "   ✗ ISalesOrderManagement.java not found!" -ForegroundColor Red
    exit 1
}

# 2.3 Copy IAddressbookManagement
if (Test-Path "IAddressbookManagement.java") {
    Copy-Item "IAddressbookManagement.java" "$externalDir/IAddressbookManagement.java" -Force
    Write-Host "   ✓ IAddressbookManagement.java created" -ForegroundColor Green
} else {
    Write-Host "   ✗ IAddressbookManagement.java not found!" -ForegroundColor Red
    exit 1
}

# 2.4 Add missing methods to ErpDynamicQueryManager
Write-Host "6. Updating ErpDynamicQueryManager.java..." -ForegroundColor Green
$erpDynamicFile = "callcard-components/src/main/java/com/saicon/games/callcard/components/ErpDynamicQueryManager.java"
$content = Get-Content $erpDynamicFile -Raw

if (-not ($content -match "public List<Object\[\]> listCallCards\(")) {
    # Add listCallCards method before the closing brace
    $newMethod = @"

    /**
     * List call cards with specified criteria (stub implementation)
     */
    public List<Object[]> listCallCards(
        String gameTypeId,
        List<String> callCardTemplateIds,
        String callCardStatus,
        String userGroupId,
        String applicationId,
        boolean includeCallCardTemplateData,
        boolean includeCallCardMetadata,
        boolean includeCallCardReferencedUsers,
        String sortOrder,
        int offset,
        int limit
    ) {
        // Stub implementation
        return new java.util.ArrayList<>();
    }

    /**
     * List call card templates with specified criteria (stub implementation)
     */
    public List<Object[]> listCallCardTemplates(
        String gameTypeId,
        String userGroupId,
        List<String> templateIds,
        boolean activeOnly,
        Boolean includeMetadata,
        String sortOrder,
        int offset,
        int limit
    ) {
        // Stub implementation
        return new java.util.ArrayList<>();
    }
}
"@
    $content = $content -replace '\}[\s]*$', $newMethod
    Set-Content $erpDynamicFile -Value $content -NoNewline
    Write-Host "   ✓ Added listCallCards and listCallCardTemplates methods" -ForegroundColor Green
} else {
    Write-Host "   ✓ Methods already exist" -ForegroundColor Cyan
}

# 2.5 Add missing methods to ErpNativeQueryManager
Write-Host "7. Updating ErpNativeQueryManager.java..." -ForegroundColor Green
$erpNativeFile = "callcard-components/src/main/java/com/saicon/games/callcard/components/ErpNativeQueryManager.java"
$content = Get-Content $erpNativeFile -Raw

if (-not ($content -match "public List<Object\[\]> executeNativeQuery\(")) {
    # Add methods before the closing brace
    $newMethods = @"

    /**
     * Execute native query with parameters (stub implementation)
     */
    public List<Object[]> executeNativeQuery(String query, String[] paramNames, Object[] paramValues) {
        // Stub implementation
        return new java.util.ArrayList<>();
    }

    /**
     * List call card ref user indexes previous values summary (stub implementation)
     */
    public List<Object[]> listCallCardRefUserIndexesPreviousValuesSummary(
        String userGroupId,
        List<String> callCardIds,
        List<String> metadataKeys,
        Integer limit,
        List<Integer> previousVisitsCounts,
        boolean includeGeoInfo
    ) {
        // Stub implementation
        return new java.util.ArrayList<>();
    }
}
"@
    $content = $content -replace '\}[\s]*$', $newMethods
    Set-Content $erpNativeFile -Value $content -NoNewline
    Write-Host "   ✓ Added executeNativeQuery and listCallCardRefUserIndexesPreviousValuesSummary methods" -ForegroundColor Green
} else {
    Write-Host "   ✓ Methods already exist" -ForegroundColor Cyan
}

# Phase 3: Rebuild all modules
Write-Host ""
Write-Host "PHASE 3: Final rebuild of all modules..." -ForegroundColor Yellow
Write-Host ""

Write-Host "8. Building callcard-entity..." -ForegroundColor Green
Push-Location callcard-entity
mvn clean install -DskipTests
$entityResult = $LASTEXITCODE
Pop-Location

if ($entityResult -ne 0) {
    Write-Host "   ✗ callcard-entity build FAILED!" -ForegroundColor Red
    exit 1
}
Write-Host "   ✓ callcard-entity BUILD SUCCESS" -ForegroundColor Green

Write-Host "9. Building callcard-ws-api..." -ForegroundColor Green
Push-Location callcard-ws-api
mvn clean install -DskipTests
$wsApiResult = $LASTEXITCODE
Pop-Location

if ($wsApiResult -ne 0) {
    Write-Host "   ✗ callcard-ws-api build FAILED!" -ForegroundColor Red
    exit 1
}
Write-Host "   ✓ callcard-ws-api BUILD SUCCESS" -ForegroundColor Green

Write-Host "10. Building callcard-components..." -ForegroundColor Green
Push-Location callcard-components
mvn clean install -DskipTests
$componentsResult = $LASTEXITCODE
Pop-Location

if ($componentsResult -ne 0) {
    Write-Host "   ✗ callcard-components build FAILED!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Review the build output above for remaining errors." -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "==================================================================" -ForegroundColor Green
Write-Host " SUCCESS! All callcard modules compiled successfully!" -ForegroundColor Green
Write-Host "==================================================================" -ForegroundColor Green
Write-Host ""
Write-Host "Summary:" -ForegroundColor Cyan
Write-Host "  ✓ callcard-entity: BUILD SUCCESS" -ForegroundColor Green
Write-Host "  ✓ callcard-ws-api: BUILD SUCCESS" -ForegroundColor Green
Write-Host "  ✓ callcard-components: BUILD SUCCESS" -ForegroundColor Green
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host "  1. Review any warnings in the build output" -ForegroundColor White
Write-Host "  2. Run integration tests if available" -ForegroundColor White
Write-Host "  3. Implement proper business logic for stub methods" -ForegroundColor White
Write-Host ""
