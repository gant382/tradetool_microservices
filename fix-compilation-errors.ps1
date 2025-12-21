# PowerShell script to fix compilation errors in callcard modules

Write-Host "Starting compilation error fixes..." -ForegroundColor Green

# 1. Add GENERIC_ERROR to ExceptionTypeTO.java
Write-Host "`n1. Adding GENERIC_ERROR constant to ExceptionTypeTO..." -ForegroundColor Yellow
$exceptionFile = "callcard-ws-api/src/main/java/com/saicon/games/callcard/exception/ExceptionTypeTO.java"
$content = Get-Content $exceptionFile -Raw
if (-not ($content -match "GENERIC_ERROR")) {
    $content = $content -replace 'public static final String GENERIC = "9999";', 'public static final String GENERIC = "9999";
    public static final String GENERIC_ERROR = "9999"; // Alias for GENERIC'
    Set-Content $exceptionFile -Value $content -NoNewline
    Write-Host "Added GENERIC_ERROR constant" -ForegroundColor Green
} else {
    Write-Host "GENERIC_ERROR already exists" -ForegroundColor Cyan
}

# 2. Add isValidUUID overload to Assert.java
Write-Host "`n2. Adding isValidUUID(String, String) overload to Assert..." -ForegroundColor Yellow
$assertFile = "callcard-ws-api/src/main/java/com/saicon/games/callcard/util/Assert.java"
$content = Get-Content $assertFile -Raw
if (-not ($content -match "isValidUUID\(String value, String message\)")) {
    # Add the overload method before the last closing brace
    $newMethod = @"

    public static void isValidUUID(String uuid, String message) throws BusinessLayerException {
        if (uuid == null) {
            throw new BusinessLayerException(message, ExceptionTypeTO.GENERAL_ERROR);
        }
        try {
            UUID.fromString(uuid);
        } catch (IllegalArgumentException e) {
            throw new BusinessLayerException(message, ExceptionTypeTO.GENERAL_ERROR);
        }
    }
}
"@
    $content = $content -replace '}$', $newMethod
    Set-Content $assertFile -Value $content -NoNewline
    Write-Host "Added isValidUUID overload method" -ForegroundColor Green
} else {
    Write-Host "isValidUUID overload already exists" -ForegroundColor Cyan
}

# 3. Rebuild callcard-ws-api
Write-Host "`n3. Rebuilding callcard-ws-api..." -ForegroundColor Yellow
Push-Location callcard-ws-api
mvn clean install -DskipTests -U
$wsApiResult = $LASTEXITCODE
Pop-Location

if ($wsApiResult -eq 0) {
    Write-Host "callcard-ws-api rebuild successful!" -ForegroundColor Green
} else {
    Write-Host "callcard-ws-api rebuild failed!" -ForegroundColor Red
    exit 1
}

Write-Host "`nCompleted Phase 1 fixes (ws-api)" -ForegroundColor Green
Write-Host "Next: Run Phase 2 to fix components module" -ForegroundColor Cyan
