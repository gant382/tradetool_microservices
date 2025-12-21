# Fix import errors in CallCard REST resource files
# PowerShell script to apply import mappings

$basePath = "C:\Users\dimit\tradetool_middleware\callcard-ws-api\src\main\java\com\saicon\games\callcard\resources"

# File to fix
$file1 = Join-Path $basePath "CallCardResources.java"
$file2 = Join-Path $basePath "CallCardStatisticsResources.java"

Write-Host "Fixing imports in CallCard resources..."

# Fix CallCardResources.java
Write-Host "`nProcessing CallCardResources.java..."
$content1 = Get-Content $file1 -Raw -Encoding UTF8

# Fix package declaration
$content1 = $content1 -replace 'package com\.saicon\.talos\.services;', 'package com.saicon.games.callcard.resources;'

# Fix DTO imports
$content1 = $content1 -replace 'import com\.saicon\.callCard\.dto\.', 'import com.saicon.games.callcard.ws.dto.'

# Fix exception imports
$content1 = $content1 -replace 'import com\.saicon\.games\.commons\.exceptions\.', 'import com.saicon.games.callcard.exception.'

# Fix response imports
$content1 = $content1 -replace 'import com\.saicon\.games\.ws\.commons\.', 'import com.saicon.games.callcard.ws.response.'

# Remove ITalosResource interface
$content1 = $content1 -replace '(implements\s+ICallCardResources)\s*,\s*ITalosResource', '$1 /* , ITalosResource */'

$content1 | Set-Content $file1 -Encoding UTF8 -NoNewline
Write-Host "  Fixed CallCardResources.java"

# Fix CallCardStatisticsResources.java
Write-Host "`nProcessing CallCardStatisticsResources.java..."
$content2 = Get-Content $file2 -Raw -Encoding UTF8

# Fix exception imports
$content2 = $content2 -replace 'import com\.saicon\.games\.commons\.exceptions\.', 'import com.saicon.games.callcard.exception.'

# Fix response imports
$content2 = $content2 -replace 'import com\.saicon\.games\.ws\.commons\.', 'import com.saicon.games.callcard.ws.response.'

$content2 | Set-Content $file2 -Encoding UTF8 -NoNewline
Write-Host "  Fixed CallCardStatisticsResources.java"

Write-Host "`nDone! All imports have been fixed."
