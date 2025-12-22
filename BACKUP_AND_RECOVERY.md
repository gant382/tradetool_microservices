# Backup and Disaster Recovery Procedures

**Document Version:** 1.0
**Last Updated:** 2025-12-22
**Maintained By:** DevOps/Infrastructure Team
**Classification:** Internal - Operational Procedures

---

## Table of Contents

1. [Overview](#overview)
2. [Database Backup Procedures](#database-backup-procedures)
3. [Application Backup Procedures](#application-backup-procedures)
4. [Recovery Time & Point Objectives](#recovery-time--point-objectives)
5. [Backup Testing Procedures](#backup-testing-procedures)
6. [Disaster Recovery Scenarios](#disaster-recovery-scenarios)
7. [Failover Procedures](#failover-procedures)
8. [Data Integrity Verification](#data-integrity-verification)
9. [Point-in-Time Recovery](#point-in-time-recovery)
10. [Emergency Contacts & Escalation](#emergency-contacts--escalation)
11. [Implementation Roadmap](#implementation-roadmap)

---

## Overview

### Purpose

This document defines comprehensive backup and disaster recovery (B&R) procedures for the tradetool_middleware platform, ensuring business continuity and data protection across all critical systems.

### Scope

- Microsoft SQL Server 2008+ databases
- Java Spring Boot 2.7.x applications (WAR/JAR deployments)
- Configuration files and environment variables
- Infrastructure configuration and state
- File storage and attachments

### Key Principles

- **RTO Target:** Maximum 4 hours for critical services
- **RPO Target:** Maximum 1 hour of data loss
- **Recovery Strategy:** Multi-layered backup approach (incremental, full, differential)
- **Testing:** Quarterly disaster recovery drills
- **Documentation:** Updated monthly, reviewed quarterly
- **Automation:** 95%+ automated backup and recovery processes

---

## Database Backup Procedures

### 1. SQL Server Full Backup

#### Weekly Full Backup Schedule

```
Frequency: Weekly (Sunday 02:00 UTC)
Retention: 4 weeks (28 days)
Backup Type: Full database backup
Backup Location: Primary: Local SSD, Secondary: Network share
Compression: Enabled (reduces storage by 40-60%)
Verification: Automatic CHECKSUM validation
```

#### Backup Script: `scripts/backup-full-database.sql`

```sql
-- Full Database Backup Script
-- Schedule: Sunday 02:00 UTC via SQL Agent Job

DECLARE @BackupPath NVARCHAR(500)
DECLARE @DatabaseName NVARCHAR(128) = 'chatbot_db'
DECLARE @BackupFileName NVARCHAR(500)
DECLARE @BackupDate NVARCHAR(20)
DECLARE @BackupTime NVARCHAR(20)

-- Generate timestamp
SET @BackupDate = CONVERT(NVARCHAR(20), GETDATE(), 112)
SET @BackupTime = REPLACE(CONVERT(NVARCHAR(20), GETDATE(), 108), ':', '')

-- Set backup path (adjust based on environment)
SET @BackupPath = 'C:\SQLBackups\' + @DatabaseName + '\Full\'

-- Create backup filename
SET @BackupFileName = @BackupPath + @DatabaseName + '_FULL_' + @BackupDate + '_' + @BackupTime + '.bak'

-- Create backup directory if not exists
EXEC xp_cmdshell 'md "' + @BackupPath + '"', no_output

-- Execute full backup with compression and CHECKSUM
BACKUP DATABASE @DatabaseName
TO DISK = @BackupFileName
WITH
    COMPRESSION,
    CHECKSUM,
    INIT,
    NAME = 'Full Backup of ' + @DatabaseName + ' ' + @BackupDate,
    DESCRIPTION = 'Weekly full backup',
    STATS = 10

-- Log backup operation
INSERT INTO dbo.BackupLog (DatabaseName, BackupType, BackupPath, BackupDate, Status)
VALUES (@DatabaseName, 'FULL', @BackupFileName, GETDATE(), 'SUCCESS')

PRINT 'Backup completed: ' + @BackupFileName
```

#### PowerShell Backup Script: `scripts/Backup-SqlDatabase.ps1`

```powershell
param(
    [Parameter(Mandatory=$true)]
    [string]$DatabaseName,

    [Parameter(Mandatory=$false)]
    [string]$BackupType = 'FULL',  # FULL, DIFFERENTIAL, LOG

    [Parameter(Mandatory=$false)]
    [string]$BackupPath = 'C:\SQLBackups\',

    [Parameter(Mandatory=$false)]
    [string]$SqlServer = 'localhost',

    [Parameter(Mandatory=$false)]
    [int]$RetentionDays = 28
)

# Enable error handling
$ErrorActionPreference = 'Stop'

# Create backup directory structure
$BackupDir = Join-Path $BackupPath $DatabaseName $BackupType
if (-not (Test-Path $BackupDir)) {
    New-Item -Path $BackupDir -ItemType Directory -Force | Out-Null
    Write-Host "Created backup directory: $BackupDir"
}

# Generate backup filename
$Timestamp = Get-Date -Format 'yyyyMMdd_HHmmss'
$BackupFile = Join-Path $BackupDir "$DatabaseName`_$BackupType`_$Timestamp.bak"

# Execute backup via SQL Server Management Objects
try {
    [System.Reflection.Assembly]::LoadWithPartialName("Microsoft.SqlServer.SMO") | Out-Null

    $SmoServer = New-Object Microsoft.SqlServer.Management.Smo.Server($SqlServer)
    $Database = $SmoServer.Databases[$DatabaseName]

    if (-not $Database) {
        throw "Database '$DatabaseName' not found on server '$SqlServer'"
    }

    # Configure backup settings
    $Backup = New-Object Microsoft.SqlServer.Management.Smo.Backup
    $Backup.Database = $DatabaseName
    $Backup.Action = switch ($BackupType) {
        'FULL' { [Microsoft.SqlServer.Management.Smo.BackupActionType]::Database }
        'DIFFERENTIAL' { [Microsoft.SqlServer.Management.Smo.BackupActionType]::DatabaseDifferential }
        'LOG' { [Microsoft.SqlServer.Management.Smo.BackupActionType]::Log }
    }

    $Backup.CompressionOption = [Microsoft.SqlServer.Management.Smo.BackupCompressionOptions]::On
    $Backup.Checksum = $true
    $Backup.Initialize = $true
    $Backup.PercentCompleteNotification = 10

    $BackupDevice = New-Object Microsoft.SqlServer.Management.Smo.BackupDeviceItem($BackupFile, 'File')
    $Backup.Devices.Add($BackupDevice)

    # Execute backup with progress tracking
    Write-Host "Starting $BackupType backup: $BackupFile" -ForegroundColor Green
    $StartTime = Get-Date

    $Backup.SqlBackup($SmoServer)

    $EndTime = Get-Date
    $Duration = ($EndTime - $StartTime).TotalSeconds

    # Verify backup file
    $BackupFileInfo = Get-Item $BackupFile
    $BackupSizeMB = [math]::Round($BackupFileInfo.Length / 1MB, 2)

    Write-Host "Backup completed successfully" -ForegroundColor Green
    Write-Host "File: $BackupFile" -ForegroundColor Green
    Write-Host "Size: $BackupSizeMB MB" -ForegroundColor Green
    Write-Host "Duration: $Duration seconds" -ForegroundColor Green

    # Log backup operation
    Add-Content -Path "$BackupPath\backup.log" -Value "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') | $DatabaseName | $BackupType | $BackupFile | $BackupSizeMB MB | $Duration sec | SUCCESS"

    # Cleanup old backups based on retention policy
    Get-ChildItem $BackupDir -Filter "*.bak" | Where-Object {
        $_.LastWriteTime -lt (Get-Date).AddDays(-$RetentionDays)
    } | Remove-Item -Force

    Write-Host "Retention cleanup completed (kept files from last $RetentionDays days)"

} catch {
    $ErrorMsg = $_.Exception.Message
    Write-Host "Backup failed: $ErrorMsg" -ForegroundColor Red
    Add-Content -Path "$BackupPath\backup.log" -Value "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') | $DatabaseName | $BackupType | FAILED | $ErrorMsg"
    exit 1
}
```

### 2. Differential Backup

#### Daily Differential Backup Schedule

```
Frequency: Daily (02:30 UTC, Monday-Saturday)
Runs After: Weekly full backup completes
Retention: 6 days
Backup Type: Incremental differential (only changed blocks)
Compression: Enabled
```

#### SQL Agent Job Configuration

```sql
-- Create SQL Agent job for differential backups
USE msdb
GO

EXEC msdb.dbo.sp_add_job
    @job_name = 'Backup_chatbot_db_Differential_Daily',
    @enabled = 1,
    @description = 'Daily differential backup for chatbot_db'
GO

EXEC msdb.dbo.sp_add_jobstep
    @job_name = 'Backup_chatbot_db_Differential_Daily',
    @step_name = 'Execute Differential Backup',
    @subsystem = 'TSQL',
    @command = N'
        DECLARE @BackupPath NVARCHAR(500) = ''C:\SQLBackups\chatbot_db\Differential\''
        DECLARE @BackupFileName NVARCHAR(500)
        DECLARE @BackupDate NVARCHAR(20) = CONVERT(NVARCHAR(20), GETDATE(), 112)
        DECLARE @BackupTime NVARCHAR(20) = REPLACE(CONVERT(NVARCHAR(20), GETDATE(), 108), '':'', '''')

        SET @BackupFileName = @BackupPath + ''chatbot_db_DIFF_'' + @BackupDate + ''_'' + @BackupTime + ''.bak''

        BACKUP DATABASE chatbot_db
        TO DISK = @BackupFileName
        WITH DIFFERENTIAL, COMPRESSION, CHECKSUM, INIT, STATS = 10
    '
GO

-- Schedule job for 02:30 UTC daily (Monday-Saturday)
EXEC msdb.dbo.sp_add_schedule
    @schedule_name = 'Daily_0230_UTC',
    @freq_type = 8,  -- Weekly
    @freq_interval = 62,  -- Monday-Saturday (2+4+8+16+32)
    @active_start_time = 023000
GO

EXEC msdb.dbo.sp_attach_schedule
    @job_name = 'Backup_chatbot_db_Differential_Daily',
    @schedule_name = 'Daily_0230_UTC'
GO
```

### 3. Transaction Log Backup

#### Hourly Log Backup Schedule

```
Frequency: Every hour (24/7)
Retention: 7 days
Backup Type: Transaction log (supports point-in-time recovery)
Recovery Model: FULL (required for log backups)
Compression: Enabled
```

#### Log Backup PowerShell Script: `scripts/Backup-TransactionLog.ps1`

```powershell
param(
    [Parameter(Mandatory=$true)]
    [string]$DatabaseName,

    [Parameter(Mandatory=$false)]
    [string]$BackupPath = 'C:\SQLBackups\',

    [Parameter(Mandatory=$false)]
    [string]$SqlServer = 'localhost',

    [Parameter(Mandatory=$false)]
    [int]$RetentionDays = 7
)

$ErrorActionPreference = 'Stop'

# Verify recovery model
[System.Reflection.Assembly]::LoadWithPartialName("Microsoft.SqlServer.SMO") | Out-Null
$SmoServer = New-Object Microsoft.SqlServer.Management.Smo.Server($SqlServer)
$Database = $SmoServer.Databases[$DatabaseName]

if ($Database.RecoveryModel -ne 'Full') {
    Write-Host "ERROR: Database is in $($Database.RecoveryModel) recovery model. Log backups require FULL recovery model." -ForegroundColor Red
    exit 1
}

# Create backup directory
$BackupDir = Join-Path $BackupPath $DatabaseName 'TransactionLog'
if (-not (Test-Path $BackupDir)) {
    New-Item -Path $BackupDir -ItemType Directory -Force | Out-Null
}

# Generate backup filename
$Timestamp = Get-Date -Format 'yyyyMMdd_HHmmss'
$BackupFile = Join-Path $BackupDir "$DatabaseName`_LOG_$Timestamp.trn"

try {
    $Backup = New-Object Microsoft.SqlServer.Management.Smo.Backup
    $Backup.Database = $DatabaseName
    $Backup.Action = [Microsoft.SqlServer.Management.Smo.BackupActionType]::Log
    $Backup.CompressionOption = [Microsoft.SqlServer.Management.Smo.BackupCompressionOptions]::On
    $Backup.Checksum = $true
    $Backup.Initialize = $true
    $Backup.PercentCompleteNotification = 20

    $BackupDevice = New-Object Microsoft.SqlServer.Management.Smo.BackupDeviceItem($BackupFile, 'File')
    $Backup.Devices.Add($BackupDevice)

    Write-Host "Backing up transaction log: $BackupFile"
    $Backup.SqlBackup($SmoServer)

    $BackupFileInfo = Get-Item $BackupFile
    $BackupSizeMB = [math]::Round($BackupFileInfo.Length / 1MB, 2)

    Write-Host "Transaction log backup completed: $BackupSizeMB MB" -ForegroundColor Green
    Add-Content -Path "$BackupPath\backup.log" -Value "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') | $DatabaseName | LOG | $BackupFile | $BackupSizeMB MB | SUCCESS"

    # Cleanup old logs
    Get-ChildItem $BackupDir -Filter "*.trn" | Where-Object {
        $_.LastWriteTime -lt (Get-Date).AddDays(-$RetentionDays)
    } | Remove-Item -Force

} catch {
    Write-Host "Log backup failed: $($_.Exception.Message)" -ForegroundColor Red
    Add-Content -Path "$BackupPath\backup.log" -Value "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') | $DatabaseName | LOG | FAILED | $($_.Exception.Message)"
    exit 1
}
```

### 4. Automated Backup Verification

#### Backup Header Verification Script: `scripts/Verify-BackupIntegrity.ps1`

```powershell
param(
    [Parameter(Mandatory=$true)]
    [string]$BackupFile
)

[System.Reflection.Assembly]::LoadWithPartialName("Microsoft.SqlServer.SMO") | Out-Null

try {
    $BackupRestore = New-Object Microsoft.SqlServer.Management.Smo.Restore

    # Read backup file header
    $BackupDeviceItem = New-Object Microsoft.SqlServer.Management.Smo.BackupDeviceItem($BackupFile, 'File')
    $BackupRestore.Devices.Add($BackupDeviceItem)

    # Get backup information
    $BackupDataTable = $BackupRestore.ReadBackupHeader([Microsoft.SqlServer.Management.Smo.Server]'localhost')

    Write-Host "Backup Information:"
    Write-Host "==================" -ForegroundColor Green

    foreach ($Row in $BackupDataTable) {
        Write-Host "Database Name: $($Row['DatabaseName'])"
        Write-Host "Backup Type: $($Row['BackupType'])"
        Write-Host "Backup Start Date: $($Row['BackupStartDate'])"
        Write-Host "Backup Finish Date: $($Row['BackupFinishDate'])"
        Write-Host "Compressed: $($Row['Compressed'])"
        Write-Host "Checksum: $($Row['HasBackupChecksums'])"
        Write-Host "Backup Size: $([math]::Round($Row['BackupSize'] / 1MB, 2)) MB"
    }

    Write-Host "Backup verification completed successfully" -ForegroundColor Green
    return $true

} catch {
    Write-Host "Backup verification failed: $($_.Exception.Message)" -ForegroundColor Red
    return $false
}
```

### 5. Off-Site Backup Replication

#### Network Share Replication: `scripts/Replicate-BackupOffsite.ps1`

```powershell
param(
    [Parameter(Mandatory=$true)]
    [string]$LocalBackupPath = 'C:\SQLBackups\',

    [Parameter(Mandatory=$true)]
    [string]$RemoteBackupPath = '\\backup-server\sql-backups\',

    [Parameter(Mandatory=$false)]
    [string]$RobocopyArgs = '/MIR /COPY:DAT /R:3 /W:5 /LOG+:$($RemoteBackupPath)sync.log'
)

Write-Host "Starting off-site backup replication..."
Write-Host "Source: $LocalBackupPath"
Write-Host "Target: $RemoteBackupPath"

# Verify network connectivity
if (-not (Test-Path $RemoteBackupPath)) {
    Write-Host "ERROR: Cannot access remote backup location" -ForegroundColor Red
    exit 1
}

# Execute robocopy for efficient sync
$RobocopyCommand = "robocopy `"$LocalBackupPath`" `"$RemoteBackupPath`" /MIR /COPY:DAT /R:3 /W:5 /LOG+:`"$($RemoteBackupPath)sync.log`""

Invoke-Expression $RobocopyCommand

if ($LASTEXITCODE -le 1) {
    Write-Host "Off-site replication completed successfully" -ForegroundColor Green
} else {
    Write-Host "Replication completed with warnings/errors (exit code: $LASTEXITCODE)" -ForegroundColor Yellow
}
```

---

## Application Backup Procedures

### 1. WAR/JAR File Backups

#### Build Artifact Archival

```
Location: C:\Applications\Backups\
Frequency: Every successful build deployment
Retention: 12 builds minimum, 30 days minimum
Format: WAR/JAR + version.txt + build-metadata.json
Verification: SHA-256 checksum
```

#### Backup Script: `scripts/Backup-ApplicationArtifacts.ps1`

```powershell
param(
    [Parameter(Mandatory=$true)]
    [string]$SourceWARPath = 'C:\Applications\Deploy\',

    [Parameter(Mandatory=$true)]
    [string]$BackupPath = 'C:\Applications\Backups\',

    [Parameter(Mandatory=$false)]
    [int]$RetentionDays = 30,

    [Parameter(Mandatory=$false)]
    [int]$MinimumVersions = 12
)

$ErrorActionPreference = 'Stop'

# Create backup directory
if (-not (Test-Path $BackupPath)) {
    New-Item -Path $BackupPath -ItemType Directory -Force | Out-Null
}

# Get all deployable artifacts
$Artifacts = Get-ChildItem $SourceWARPath -Filter "*.war" -ErrorAction SilentlyContinue
$Artifacts += Get-ChildItem $SourceWARPath -Filter "*.jar" -ErrorAction SilentlyContinue

foreach ($Artifact in $Artifacts) {
    try {
        $Timestamp = Get-Date -Format 'yyyyMMdd_HHmmss'
        $BackupDir = Join-Path $BackupPath $Artifact.BaseName

        if (-not (Test-Path $BackupDir)) {
            New-Item -Path $BackupDir -ItemType Directory -Force | Out-Null
        }

        $BackupFile = Join-Path $BackupDir "$($Artifact.BaseName)_$Timestamp$($Artifact.Extension)"

        # Copy artifact
        Copy-Item -Path $Artifact.FullName -Destination $BackupFile -Force

        # Create checksum
        $SHA256 = (Get-FileHash -Path $BackupFile -Algorithm SHA256).Hash
        Add-Content -Path "$BackupFile.sha256" -Value $SHA256

        # Create metadata
        $Metadata = @{
            BackupDate = Get-Date -Format 'yyyy-MM-dd HH:mm:ss'
            FileName = $Artifact.Name
            Size = $Artifact.Length
            SHA256 = $SHA256
            SourcePath = $Artifact.FullName
        } | ConvertTo-Json

        Add-Content -Path "$BackupFile.metadata.json" -Value $Metadata

        Write-Host "Backed up: $BackupFile" -ForegroundColor Green

        # Cleanup old backups (keep minimum versions)
        $AllBackups = Get-ChildItem $BackupDir -Filter "$($Artifact.BaseName)_*.war" -ErrorAction SilentlyContinue
        $AllBackups += Get-ChildItem $BackupDir -Filter "$($Artifact.BaseName)_*.jar" -ErrorAction SilentlyContinue

        if ($AllBackups.Count -gt $MinimumVersions) {
            $AllBackups | Sort-Object -Property LastWriteTime -Descending |
                Select-Object -Skip $MinimumVersions |
                Where-Object { $_.LastWriteTime -lt (Get-Date).AddDays(-$RetentionDays) } |
                Remove-Item -Force
        }

    } catch {
        Write-Host "Failed to backup artifact $($Artifact.Name): $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "Application artifact backup completed" -ForegroundColor Green
```

### 2. Configuration File Backups

#### Configuration Backup: `scripts/Backup-ConfigurationFiles.ps1`

```powershell
param(
    [Parameter(Mandatory=$true)]
    [string]$ConfigSourcePath = 'C:\Applications\Config\',

    [Parameter(Mandatory=$true)]
    [string]$BackupPath = 'C:\Applications\Backups\Config\',

    [Parameter(Mandatory=$false)]
    [int]$RetentionVersions = 20
)

$ErrorActionPreference = 'Stop'

if (-not (Test-Path $ConfigSourcePath)) {
    Write-Host "ERROR: Configuration source path not found: $ConfigSourcePath" -ForegroundColor Red
    exit 1
}

# Create backup directory
if (-not (Test-Path $BackupPath)) {
    New-Item -Path $BackupPath -ItemType Directory -Force | Out-Null
}

# Create timestamp-based subdirectory
$Timestamp = Get-Date -Format 'yyyyMMdd_HHmmss'
$BackupDir = Join-Path $BackupPath $Timestamp

New-Item -Path $BackupDir -ItemType Directory -Force | Out-Null

# Copy all configuration files
Get-ChildItem $ConfigSourcePath -Recurse -File | ForEach-Object {
    $RelativePath = $_.FullName.Substring($ConfigSourcePath.Length)
    $TargetFile = Join-Path $BackupDir $RelativePath
    $TargetDir = Split-Path $TargetFile

    if (-not (Test-Path $TargetDir)) {
        New-Item -Path $TargetDir -ItemType Directory -Force | Out-Null
    }

    Copy-Item -Path $_.FullName -Destination $TargetFile -Force
    Write-Host "Backed up: $RelativePath"
}

# Create backup manifest
$Manifest = @{
    BackupDate = Get-Date -Format 'yyyy-MM-dd HH:mm:ss'
    BackupPath = $BackupDir
    FileCount = (Get-ChildItem $BackupDir -Recurse -File).Count
    ConfigSourcePath = $ConfigSourcePath
} | ConvertTo-Json

Add-Content -Path "$BackupDir\manifest.json" -Value $Manifest

Write-Host "Configuration backup completed: $BackupDir" -ForegroundColor Green

# Cleanup old backups
Get-ChildItem $BackupPath -Directory | Sort-Object -Property CreationTime -Descending |
    Select-Object -Skip $RetentionVersions |
    Remove-Item -Recurse -Force

Write-Host "Cleanup completed - retained $RetentionVersions versions"
```

### 3. Environment Variable & Secrets Backup

#### Encrypted Secrets Backup: `scripts/Backup-Secrets.ps1`

```powershell
param(
    [Parameter(Mandatory=$true)]
    [string]$SecretsSourcePath = 'C:\Applications\Secrets\',

    [Parameter(Mandatory=$true)]
    [string]$BackupPath = 'C:\Applications\Backups\Secrets\',

    [Parameter(Mandatory=$true)]
    [string]$EncryptionCertThumbprint
)

$ErrorActionPreference = 'Stop'

if (-not (Test-Path $SecretsSourcePath)) {
    Write-Host "ERROR: Secrets source path not found" -ForegroundColor Red
    exit 1
}

# Create backup directory
if (-not (Test-Path $BackupPath)) {
    New-Item -Path $BackupPath -ItemType Directory -Force | Out-Null
}

# Get encryption certificate
$Cert = Get-ChildItem Cert:\LocalMachine\My | Where-Object { $_.Thumbprint -eq $EncryptionCertThumbprint }

if (-not $Cert) {
    Write-Host "ERROR: Encryption certificate not found: $EncryptionCertThumbprint" -ForegroundColor Red
    exit 1
}

# Backup and encrypt secrets
$Timestamp = Get-Date -Format 'yyyyMMdd_HHmmss'
$SecureBackupFile = Join-Path $BackupPath "secrets_$Timestamp.zip.encrypted"

# Create temporary zip of secrets
$TempZipFile = "$env:TEMP\secrets_$Timestamp.zip"
Compress-Archive -Path $SecretsSourcePath -DestinationPath $TempZipFile -Force

try {
    # Encrypt with certificate
    $FileBytes = [System.IO.File]::ReadAllBytes($TempZipFile)
    $EncryptedContent = $Cert.PublicKey.Key.Encrypt($FileBytes, $true)

    [System.IO.File]::WriteAllBytes($SecureBackupFile, $EncryptedContent)

    Write-Host "Encrypted secrets backup created: $SecureBackupFile" -ForegroundColor Green

    # Create manifest
    $Manifest = @{
        BackupDate = Get-Date -Format 'yyyy-MM-dd HH:mm:ss'
        EncryptionCertThumbprint = $EncryptionCertThumbprint
        BackupFile = $SecureBackupFile
    } | ConvertTo-Json

    Add-Content -Path "$SecureBackupFile.manifest" -Value $Manifest

} finally {
    # Cleanup temporary file
    if (Test-Path $TempZipFile) {
        Remove-Item $TempZipFile -Force
    }
}

Write-Host "Secrets backup completed with encryption" -ForegroundColor Green
```

---

## Recovery Time & Point Objectives

### RTO Definition

| Service Tier | RTO | Notes |
|---|---|---|
| Critical (Production APIs) | 4 hours | Full service restoration from backup |
| High (Business Functions) | 8 hours | May involve manual configuration |
| Medium (Admin Functions) | 24 hours | Low impact on end users |
| Low (Development/Test) | 72 hours | Can use from older backups |

### RPO Definition

| Service Tier | RPO | Backup Frequency | Notes |
|---|---|---|---|
| Critical | 1 hour | Hourly transaction logs | Maximum 1 hour data loss acceptable |
| High | 4 hours | Differential + Logs | Maximum 4 hours data loss acceptable |
| Medium | 24 hours | Daily full backups | Maximum 24 hours data loss acceptable |
| Low | 7 days | Weekly backups | Long-term archival acceptable |

### Backup Schedule Summary

```
TRANSACTION LOGS (Hourly)
  ├─ 00:00, 01:00, 02:00, ... 23:00 UTC
  ├─ Retention: 7 days
  └─ Location: \\backup-server\logs\

FULL BACKUP (Weekly)
  ├─ Schedule: Sunday 02:00 UTC
  ├─ Retention: 4 weeks
  └─ Location: \\backup-server\full\

DIFFERENTIAL BACKUP (Daily)
  ├─ Schedule: Mon-Sat 02:30 UTC
  ├─ Retention: 6 days
  └─ Location: \\backup-server\differential\

APPLICATION ARTIFACTS
  ├─ Frequency: Every deployment
  ├─ Retention: 12 versions minimum / 30 days
  └─ Location: C:\Applications\Backups\

CONFIGURATION FILES
  ├─ Frequency: Daily at 03:00 UTC
  ├─ Retention: 20 versions
  └─ Location: C:\Applications\Backups\Config\
```

---

## Backup Testing Procedures

### 1. Automated Backup Validation

#### Daily Backup Verification Job: `scripts/Validate-DailyBackups.ps1`

```powershell
param(
    [Parameter(Mandatory=$true)]
    [string]$BackupPath = 'C:\SQLBackups\',

    [Parameter(Mandatory=$true)]
    [string]$LogPath = 'C:\SQLBackups\validation.log',

    [Parameter(Mandatory=$false)]
    [int]$MaxAgeHours = 25
)

$ErrorActionPreference = 'Continue'
$ValidationResults = @()

function Log-Validation {
    param([string]$Message, [string]$Level = 'INFO')
    $Timestamp = Get-Date -Format 'yyyy-MM-dd HH:mm:ss'
    $LogEntry = "[$Timestamp] [$Level] $Message"
    Add-Content -Path $LogPath -Value $LogEntry
    Write-Host $LogEntry
}

# Check backup file existence and freshness
Log-Validation "Starting daily backup validation"

$BackupDirs = Get-ChildItem $BackupPath -Directory

foreach ($Dir in $BackupDirs) {
    $LatestBackup = Get-ChildItem $Dir.FullName -Filter "*.bak" -File |
        Sort-Object -Property LastWriteTime -Descending |
        Select-Object -First 1

    if (-not $LatestBackup) {
        Log-Validation "No backup found in $($Dir.Name)" 'ERROR'
        $ValidationResults += @{
            Database = $Dir.Name
            Status = 'FAILED'
            Reason = 'No backup file found'
        }
        continue
    }

    $AgeHours = ((Get-Date) - $LatestBackup.LastWriteTime).TotalHours

    if ($AgeHours -gt $MaxAgeHours) {
        Log-Validation "Backup for $($Dir.Name) is $AgeHours hours old (max: $MaxAgeHours)" 'WARNING'
        $ValidationResults += @{
            Database = $Dir.Name
            Status = 'WARNING'
            Reason = "Backup age exceeds threshold ($AgeHours hours)"
        }
        continue
    }

    # Verify backup file integrity
    try {
        $BackupRestore = New-Object Microsoft.SqlServer.Management.Smo.Restore
        $BackupDeviceItem = New-Object Microsoft.SqlServer.Management.Smo.BackupDeviceItem($LatestBackup.FullName, 'File')
        $BackupRestore.Devices.Add($BackupDeviceItem)

        $BackupDataTable = $BackupRestore.ReadBackupHeader([Microsoft.SqlServer.Management.Smo.Server]'localhost')

        if ($BackupDataTable -and $BackupDataTable.Rows.Count -gt 0) {
            $Row = $BackupDataTable.Rows[0]
            $Size = [math]::Round($Row['BackupSize'] / 1MB, 2)

            Log-Validation "Backup OK: $($Dir.Name) | Size: $Size MB | Age: $([math]::Round($AgeHours, 2)) hours"

            $ValidationResults += @{
                Database = $Dir.Name
                Status = 'SUCCESS'
                Size = "$Size MB"
                Age = "$([math]::Round($AgeHours, 2)) hours"
            }
        }
    } catch {
        Log-Validation "Backup header validation failed for $($Dir.Name): $($_.Exception.Message)" 'ERROR'
        $ValidationResults += @{
            Database = $Dir.Name
            Status = 'FAILED'
            Reason = $_.Exception.Message
        }
    }
}

# Generate summary report
Log-Validation ""
Log-Validation "========== VALIDATION SUMMARY =========="

$SuccessCount = ($ValidationResults | Where-Object { $_.Status -eq 'SUCCESS' }).Count
$WarningCount = ($ValidationResults | Where-Object { $_.Status -eq 'WARNING' }).Count
$FailureCount = ($ValidationResults | Where-Object { $_.Status -eq 'FAILED' }).Count

Log-Validation "Success: $SuccessCount | Warnings: $WarningCount | Failures: $FailureCount"
Log-Validation "========================================="

# Alert if failures detected
if ($FailureCount -gt 0) {
    Log-Validation "ALERT: Backup validation failures detected!" 'ERROR'
    # Trigger notification (email, webhook, etc.)
    exit 1
}

exit 0
```

### 2. Quarterly Full Restore Test

#### Restore Test Procedure: `scripts/Test-FullRestore.ps1`

```powershell
param(
    [Parameter(Mandatory=$true)]
    [string]$SourceDatabase = 'chatbot_db',

    [Parameter(Mandatory=$true)]
    [string]$RestoreDatabase = 'chatbot_db_restore_test',

    [Parameter(Mandatory=$true)]
    [string]$BackupFile,

    [Parameter(Mandatory=$false)]
    [string]$SqlServer = 'localhost',

    [Parameter(Mandatory=$false)]
    [string]$ReportPath = 'C:\SQLBackups\restore-test-reports\'
)

$ErrorActionPreference = 'Stop'

if (-not (Test-Path $ReportPath)) {
    New-Item -Path $ReportPath -ItemType Directory -Force | Out-Null
}

$TestDate = Get-Date -Format 'yyyyMMdd_HHmmss'
$ReportFile = Join-Path $ReportPath "restore_test_$TestDate.txt"

function Report-Status {
    param([string]$Message, [string]$Level = 'INFO')
    $Timestamp = Get-Date -Format 'yyyy-MM-dd HH:mm:ss'
    $ReportEntry = "[$Timestamp] [$Level] $Message"
    Add-Content -Path $ReportFile -Value $ReportEntry
    Write-Host $ReportEntry
}

try {
    Report-Status "Starting restore test from backup: $BackupFile"

    [System.Reflection.Assembly]::LoadWithPartialName("Microsoft.SqlServer.SMO") | Out-Null
    $SmoServer = New-Object Microsoft.SqlServer.Management.Smo.Server($SqlServer)

    # Check if test database already exists
    if ($SmoServer.Databases[$RestoreDatabase]) {
        Report-Status "Dropping existing test database: $RestoreDatabase"
        $SmoServer.Databases[$RestoreDatabase].Drop()
        Start-Sleep -Seconds 5
    }

    # Perform restore
    Report-Status "Initiating restore operation..."
    $RestoreDB = New-Object Microsoft.SqlServer.Management.Smo.Restore
    $RestoreDB.Database = $RestoreDatabase
    $RestoreDB.NoRecovery = $false
    $RestoreDB.ReplaceDatabase = $true
    $RestoreDB.PercentCompleteNotification = 10

    $BackupDevice = New-Object Microsoft.SqlServer.Management.Smo.BackupDeviceItem($BackupFile, 'File')
    $RestoreDB.Devices.Add($BackupDevice)

    $StartTime = Get-Date
    $RestoreDB.SqlRestore($SmoServer)
    $EndTime = Get-Date
    $Duration = ($EndTime - $StartTime).TotalSeconds

    Report-Status "Restore completed successfully in $Duration seconds"

    # Verify restored database
    Report-Status "Verifying restored database integrity..."

    # Check database is online
    $RestoredDb = $SmoServer.Databases[$RestoreDatabase]
    if ($RestoredDb.Status -eq 'Normal') {
        Report-Status "Database status: ONLINE"
    } else {
        Report-Status "Database status: $($RestoredDb.Status)" 'WARNING'
    }

    # Count tables
    $TableCount = $RestoredDb.Tables.Count
    Report-Status "Tables found: $TableCount"

    # Run DBCC CHECKDB
    Report-Status "Running DBCC CHECKDB for logical consistency..."
    $QueryResults = $SmoServer.ConnectionContext.ExecuteWithResults("DBCC CHECKDB([$RestoreDatabase]) NO_INFOMSGS")

    if ($QueryResults.Tables[0].Rows.Count -eq 0) {
        Report-Status "DBCC CHECKDB: No errors found"
    } else {
        Report-Status "DBCC CHECKDB: Errors detected" 'WARNING'
        $QueryResults.Tables[0] | ForEach-Object {
            Report-Status "Error: $($_)" 'WARNING'
        }
    }

    # Sample data verification
    Report-Status "Performing sample data verification..."
    $SampleQuery = @"
    SELECT
        (SELECT COUNT(*) FROM sys.tables) as TableCount,
        (SELECT COUNT(*) FROM sys.views) as ViewCount,
        (SELECT COUNT(*) FROM sys.procedures) as ProcedureCount
"@

    $SampleResults = $SmoServer.ConnectionContext.ExecuteWithResults($SampleQuery)
    $Row = $SampleResults.Tables[0].Rows[0]
    Report-Status "Database objects: Tables=$($Row['TableCount']), Views=$($Row['ViewCount']), Procedures=$($Row['ProcedureCount'])"

    Report-Status "========== RESTORE TEST SUCCESSFUL ==========" 'SUCCESS'

    # Cleanup
    Report-Status "Cleaning up test database..."
    $SmoServer.Databases[$RestoreDatabase].Drop()

    Report-Status "Restore test completed successfully"
    return $true

} catch {
    Report-Status "Restore test failed: $($_.Exception.Message)" 'ERROR'
    Report-Status "Stack trace: $($_.Exception.StackTrace)" 'ERROR'

    # Attempt cleanup
    try {
        if ($SmoServer.Databases[$RestoreDatabase]) {
            $SmoServer.Databases[$RestoreDatabase].Drop()
        }
    } catch {
        Report-Status "Cleanup failed: $($_.Exception.Message)" 'ERROR'
    }

    return $false
}
```

### 3. Monthly Application Restore Drill

#### Application Restore Test: `scripts/Test-ApplicationRestore.ps1`

```powershell
param(
    [Parameter(Mandatory=$true)]
    [string]$BackupArtifactPath = 'C:\Applications\Backups\',

    [Parameter(Mandatory=$true)]
    [string]$TestDeployPath = 'C:\Applications\Test\',

    [Parameter(Mandatory=$false)]
    [string]$ReportPath = 'C:\Applications\Backups\restore-reports\'
)

$ErrorActionPreference = 'Continue'

if (-not (Test-Path $ReportPath)) {
    New-Item -Path $ReportPath -ItemType Directory -Force | Out-Null
}

$TestDate = Get-Date -Format 'yyyyMMdd_HHmmss'
$ReportFile = Join-Path $ReportPath "app_restore_test_$TestDate.txt"

function Report-Status {
    param([string]$Message)
    $Timestamp = Get-Date -Format 'yyyy-MM-dd HH:mm:ss'
    $Entry = "[$Timestamp] $Message"
    Add-Content -Path $ReportFile -Value $Entry
    Write-Host $Entry
}

Report-Status "Starting application restore test..."
Report-Status "Backup Source: $BackupArtifactPath"
Report-Status "Test Target: $TestDeployPath"

# Get latest backup artifact for each application
$Applications = Get-ChildItem $BackupArtifactPath -Directory

foreach ($AppDir in $Applications) {
    $LatestArtifact = Get-ChildItem $AppDir.FullName -Include "*.war", "*.jar" |
        Sort-Object -Property LastWriteTime -Descending |
        Select-Object -First 1

    if (-not $LatestArtifact) {
        Report-Status "No artifact found for application: $($AppDir.Name)"
        continue
    }

    try {
        Report-Status "Testing restore for: $($AppDir.Name)"

        # Verify checksum
        $ChecksumFile = "$($LatestArtifact.FullName).sha256"
        if (Test-Path $ChecksumFile) {
            $StoredChecksum = Get-Content $ChecksumFile
            $CalculatedChecksum = (Get-FileHash -Path $LatestArtifact.FullName -Algorithm SHA256).Hash

            if ($StoredChecksum -eq $CalculatedChecksum) {
                Report-Status "  Checksum verification: PASSED"
            } else {
                Report-Status "  Checksum verification: FAILED"
                continue
            }
        }

        # Verify artifact integrity
        if ($LatestArtifact.Extension -eq '.war' -or $LatestArtifact.Extension -eq '.jar') {
            # Try to list contents
            $TestZip = Add-Type -AssemblyName System.IO.Compression
            $ZipArchive = [System.IO.Compression.ZipFile]::OpenRead($LatestArtifact.FullName)
            $EntryCount = $ZipArchive.Entries.Count
            $ZipArchive.Dispose()

            Report-Status "  Archive integrity: PASSED ($EntryCount entries)"
        }

        # Verify metadata
        $MetadataFile = "$($LatestArtifact.FullName).metadata.json"
        if (Test-Path $MetadataFile) {
            $Metadata = Get-Content $MetadataFile | ConvertFrom-Json
            Report-Status "  Metadata found: BackupDate=$($Metadata.BackupDate)"
        }

        Report-Status "  Restore test for $($AppDir.Name): PASSED"

    } catch {
        Report-Status "  Restore test for $($AppDir.Name): FAILED - $($_.Exception.Message)"
    }
}

Report-Status "========== APPLICATION RESTORE TEST COMPLETED =========="
```

---

## Disaster Recovery Scenarios

### Scenario 1: Database Corruption

#### Description
Production database experiences logical corruption (corrupted tables/indexes) detectable by DBCC CHECKDB.

#### Detection
- DBCC CHECKDB error alerts
- Application data integrity exceptions
- Unexpected query results

#### Recovery Steps

```powershell
# Step 1: Assess damage
DBCC CHECKDB(chatbot_db) -- identifies corruption
DBCC CHECKDB(chatbot_db, REPAIR_ALLOW_DATA_LOSS) -- in single user mode

# Step 2: If corruption is not repairable
# Use point-in-time recovery from transaction logs

# Step 3: Restore to latest good backup
# See "Point-in-Time Recovery" section
```

#### Recovery Script: `scripts/Recover-DatabaseCorruption.ps1`

```powershell
param(
    [Parameter(Mandatory=$true)]
    [string]$CorruptedDatabase,

    [Parameter(Mandatory=$true)]
    [string]$BackupFile,

    [Parameter(Mandatory=$false)]
    [datetime]$RecoveryPointTime = (Get-Date).AddHours(-1)
)

# 1. Backup current corrupted database for forensics
$ForensicsBackup = "C:\SQLBackups\forensics\$CorruptedDatabase`_corrupted_$(Get-Date -Format 'yyyyMMdd_HHmmss').bak"
# ... backup script here ...

# 2. Kill all connections to database
EXEC msdb.dbo.sp_who2 -- identify connections
-- Use Transact-SQL to kill SPIDs

# 3. Restore from last good backup
# See Restore-Database function below

# 4. Apply transaction logs to recover to recovery point
# See point-in-time recovery section
```

### Scenario 2: Server Disk Failure

#### Description
Primary application server disk fails, all local data and applications are lost.

#### Recovery Steps

1. **Provision replacement hardware** (15 minutes)
   - Physical replacement of disk or VM disk
   - Verify network connectivity

2. **Restore OS from image** (30 minutes)
   - Deploy base OS image
   - Install required middleware (Java, SQL Server JDBC drivers)

3. **Deploy application from backup** (20 minutes)
   - Restore latest WAR/JAR from backup
   - Restore configuration files

4. **Verify connectivity** (10 minutes)
   - Database connectivity test
   - Health check endpoints

#### Recovery Procedure: `scripts/Recover-ServerDiskFailure.ps1`

```powershell
param(
    [Parameter(Mandatory=$true)]
    [string]$ApplicationName,

    [Parameter(Mandatory=$true)]
    [string]$BackupArtifactPath = 'C:\Applications\Backups\',

    [Parameter(Mandatory=$true)]
    [string]$DeployPath = 'C:\Applications\Deploy\'
)

Write-Host "Server Disk Failure Recovery Procedure"
Write-Host "======================================="

# Step 1: Verify OS and prerequisites
Write-Host "[Step 1] Verifying operating system and prerequisites..."
$JavaVersion = java -version 2>&1
if (-not $JavaVersion) {
    Write-Host "ERROR: Java not installed"
    exit 1
}
Write-Host "Java version detected: OK"

# Step 2: Restore application artifact
Write-Host "[Step 2] Restoring application artifact..."
$LatestArtifact = Get-ChildItem "$BackupArtifactPath\$ApplicationName" -Include "*.war", "*.jar" |
    Sort-Object -Property LastWriteTime -Descending |
    Select-Object -First 1

if (-not $LatestArtifact) {
    Write-Host "ERROR: No backup artifact found"
    exit 1
}

Copy-Item -Path $LatestArtifact.FullName -Destination $DeployPath -Force
Write-Host "Artifact deployed: $($LatestArtifact.Name)"

# Step 3: Restore configuration
Write-Host "[Step 3] Restoring configuration files..."
$ConfigBackup = Get-ChildItem "$BackupArtifactPath\Config" -Directory |
    Sort-Object -Property CreationTime -Descending |
    Select-Object -First 1

if ($ConfigBackup) {
    Copy-Item -Path "$($ConfigBackup.FullName)\*" -Destination 'C:\Applications\Config\' -Recurse -Force
    Write-Host "Configuration restored"
}

# Step 4: Verify connectivity
Write-Host "[Step 4] Verifying database connectivity..."
$ConnectionTest = Test-NetConnection -ComputerName 'sql-server' -Port 1433
if ($ConnectionTest.TcpTestSucceeded) {
    Write-Host "Database connectivity: OK"
} else {
    Write-Host "WARNING: Database connectivity check failed"
}

# Step 5: Start application
Write-Host "[Step 5] Starting application..."
# Application startup specific to your setup
Write-Host "Application startup initiated"

Write-Host "Recovery procedure completed"
```

### Scenario 3: Data Center Failure

#### Description
Entire data center becomes unavailable (power outage, network failure, catastrophic event).

#### Recovery Strategy

```
Time: 0-30 min       | Detect failure, activate recovery plan
Time: 30-90 min      | Provision infrastructure at alternate site
Time: 90-180 min     | Restore databases from off-site backups
Time: 180-240 min    | Deploy applications and verify
Time: 240+ min       | Failover traffic to recovery site
```

#### DR Failover Procedure: `scripts/Execute-DRFailover.ps1`

```powershell
param(
    [Parameter(Mandatory=$true)]
    [ValidateSet('FULL', 'PARTIAL')]
    [string]$FailoverType = 'FULL',

    [Parameter(Mandatory=$true)]
    [string]$DRSiteConfiguration = 'C:\DR\dr-config.json'
)

Write-Host "Initiating DR Failover - Type: $FailoverType"
Write-Host "Configuration: $DRSiteConfiguration"

# Load DR configuration
$DRConfig = Get-Content $DRSiteConfiguration | ConvertFrom-Json

# Step 1: Notify stakeholders
Write-Host "[Step 1/5] Notifying stakeholders..."
Send-DRNotification -Recipients $DRConfig.NotificationRecipients -Message "DR failover initiated"

# Step 2: Restore databases at DR site
Write-Host "[Step 2/5] Restoring databases from off-site backups..."
foreach ($Database in $DRConfig.CriticalDatabases) {
    Restore-DatabaseFromOffsite -Database $Database -TargetServer $DRConfig.DRSiteServer
}

# Step 3: Deploy applications
Write-Host "[Step 3/5] Deploying applications..."
foreach ($Application in $DRConfig.CriticalApplications) {
    Deploy-ApplicationArtifact -Application $Application -TargetServer $DRConfig.DRSiteServer
}

# Step 4: Verify functionality
Write-Host "[Step 4/5] Verifying system functionality..."
Test-DRSiteHealth -HealthCheckEndpoint $DRConfig.HealthCheckEndpoint

# Step 5: Switch DNS
Write-Host "[Step 5/5] Switching DNS to DR site..."
Update-DNSRecord -RecordName $DRConfig.ApplicationDNS -TargetIP $DRConfig.DRSiteIP

Write-Host "DR Failover completed successfully"
```

### Scenario 4: Ransomware Attack

#### Description
Ransomware encrypts database and application files, requiring recovery from known-clean backups.

#### Immediate Actions

```
1. ISOLATE: Disconnect infected servers from network immediately
2. ASSESS: Determine extent of encryption/damage
3. PRESERVE: Preserve evidence for forensics and law enforcement
4. NOTIFY: Activate incident response team and notify stakeholders
5. RECOVER: Restore from backups known to be clean (pre-infection)
6. VERIFY: Perform thorough security scans before bringing systems online
```

#### Recovery Script: `scripts/Recover-RansomwareAttack.ps1`

```powershell
param(
    [Parameter(Mandatory=$true)]
    [datetime]$InfectionTime,

    [Parameter(Mandatory=$true)]
    [string]$BackupPath = 'C:\SQLBackups\'
)

Write-Host "Ransomware Recovery Procedure"
Write-Host "============================"

# Step 1: Find clean backups (before infection time)
Write-Host "[Step 1] Identifying clean backups..."
$CleanBackups = Get-ChildItem $BackupPath -Recurse -Include "*.bak" |
    Where-Object { $_.LastWriteTime -lt $InfectionTime } |
    Sort-Object -Property LastWriteTime -Descending

if (-not $CleanBackups) {
    Write-Host "ERROR: No clean backups found before infection time"
    exit 1
}

$LatestCleanBackup = $CleanBackups | Select-Object -First 1
Write-Host "Latest clean backup: $($LatestCleanBackup.FullName) ($(Get-Date $LatestCleanBackup.LastWriteTime))"

# Step 2: Isolate and disable ransomware persistence
Write-Host "[Step 2] Disabling suspicious services and scheduled tasks..."
# Review and disable any suspicious services/tasks discovered during incident response

# Step 3: Restore from clean backup
Write-Host "[Step 3] Restoring database from clean backup..."
Restore-DatabaseFromBackup -BackupFile $LatestCleanBackup.FullName -TargetDatabase 'chatbot_db'

# Step 4: Restore applications
Write-Host "[Step 4] Restoring applications..."
# Deploy latest clean application artifacts

# Step 5: Security hardening
Write-Host "[Step 5] Applying security hardening..."
# - Update antivirus signatures
# - Patch vulnerabilities
# - Review and strengthen access controls
# - Enable encryption on all backups

# Step 6: Restore data from transaction logs
Write-Host "[Step 6] Recovering recent transactions (if available)..."
# Apply transaction logs up to infection time to minimize data loss

Write-Host "Recovery from ransomware attack completed"
Write-Host "Next: Conduct security audit and implement preventive measures"
```

---

## Failover Procedures

### Active-Passive Failover (Standard Setup)

#### Configuration

```
PRODUCTION (Active)          STANDBY (Passive)
├─ Primary DB Server         ├─ Standby DB Server
├─ Primary App Servers       ├─ Standby App Servers
├─ Active DNS Record         └─ Inactive DNS Record
└─ Monitored 24/7            └─ Regular data sync
```

#### Manual Failover Steps

1. **Verify Primary Failure** (5 minutes)
   ```powershell
   Test-Connection -ComputerName 'prod-db-server'
   Invoke-RestMethod http://prod-app:5000/api/health
   ```

2. **Verify Standby Readiness** (5 minutes)
   ```powershell
   # Check standby database is in sync
   SELECT * FROM sys.databases WHERE name = 'chatbot_db'

   # Check application deployments
   Get-ChildItem C:\Applications\Deploy\
   ```

3. **Initiate Failover** (30 minutes)
   ```powershell
   # Restore database (if from backups)
   Restore-DatabaseFromBackup -BackupFile $LatestBackup

   # Start applications
   net start "App Service Name"
   ```

4. **Update DNS** (15 minutes)
   ```powershell
   # Update DNS record to point to standby
   Set-DNSRecord -Name 'api.platform.com' -Target '192.168.2.50'
   ```

5. **Verify Service** (15 minutes)
   ```powershell
   Test-ServiceHealth -ServiceEndpoint 'http://api.platform.com/api/health'
   ```

### Automated Failover (Optional - Future Enhancement)

For automated failover using Windows Failover Clustering or SQL Server AlwaysOn:

```powershell
# Example: Enable SQL Server AlwaysOn Availability Group
Enable-SqlAlwaysOn -Path 'SQLSERVER:\SQL\InstanceName'

# Create availability group
New-SqlAvailabilityGroup -Name 'chatbot_db_ag' `
    -Path 'SQLSERVER:\SQL\PrimaryInstance' `
    -AvailabilityReplicas $replicas
```

---

## Data Integrity Verification

### 1. DBCC CHECKDB Procedure

#### Weekly Integrity Check: `scripts/Verify-DatabaseIntegrity.ps1`

```powershell
param(
    [Parameter(Mandatory=$true)]
    [string]$DatabaseName,

    [Parameter(Mandatory=$true)]
    [string]$SqlServer = 'localhost',

    [Parameter(Mandatory=$false)]
    [string]$ReportPath = 'C:\SQLBackups\integrity-reports\'
)

$ErrorActionPreference = 'Stop'

if (-not (Test-Path $ReportPath)) {
    New-Item -Path $ReportPath -ItemType Directory -Force | Out-Null
}

$ReportDate = Get-Date -Format 'yyyyMMdd_HHmmss'
$ReportFile = Join-Path $ReportPath "dbcc_checkdb_$DatabaseName`_$ReportDate.txt"

Write-Host "Starting database integrity check for: $DatabaseName"
Write-Host "Report: $ReportFile"

[System.Reflection.Assembly]::LoadWithPartialName("Microsoft.SqlServer.SMO") | Out-Null

try {
    $SmoServer = New-Object Microsoft.SqlServer.Management.Smo.Server($SqlServer)
    $Database = $SmoServer.Databases[$DatabaseName]

    if (-not $Database) {
        throw "Database not found: $DatabaseName"
    }

    # Execute DBCC CHECKDB with output capture
    $QueryResults = $SmoServer.ConnectionContext.ExecuteWithResults(@"
        DBCC CHECKDB('[$DatabaseName]') WITH NO_INFOMSGS
"@)

    Add-Content -Path $ReportFile -Value "Database Integrity Check Report"
    Add-Content -Path $ReportFile -Value "==============================="
    Add-Content -Path $ReportFile -Value "Database: $DatabaseName"
    Add-Content -Path $ReportFile -Value "Server: $SqlServer"
    Add-Content -Path $ReportFile -Value "Check Time: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
    Add-Content -Path $ReportFile -Value ""

    # Check for errors
    if ($QueryResults.Tables[0].Rows.Count -eq 0) {
        Add-Content -Path $ReportFile -Value "Result: NO ERRORS FOUND"
        Write-Host "DBCC CHECKDB: No errors detected" -ForegroundColor Green
    } else {
        Add-Content -Path $ReportFile -Value "Result: ERRORS DETECTED"
        Add-Content -Path $ReportFile -Value ""

        $QueryResults.Tables[0] | ForEach-Object {
            $ErrorMsg = $_[0].ToString()
            Add-Content -Path $ReportFile -Value "ERROR: $ErrorMsg"
            Write-Host "ERROR: $ErrorMsg" -ForegroundColor Red
        }
    }

    # Additional database health checks
    Add-Content -Path $ReportFile -Value ""
    Add-Content -Path $ReportFile -Value "Additional Health Metrics:"
    Add-Content -Path $ReportFile -Value "--------------------------"

    $HealthQuery = @"
    SELECT
        DB_NAME() as DatabaseName,
        (SELECT COUNT(*) FROM sys.tables) as TableCount,
        (SELECT COUNT(*) FROM sys.indexes) as IndexCount,
        (SELECT SUM(size) * 8 / 1024 FROM sys.database_files) as SizeGB
"@

    $HealthResults = $SmoServer.ConnectionContext.ExecuteWithResults($HealthQuery)
    $HealthRow = $HealthResults.Tables[0].Rows[0]

    Add-Content -Path $ReportFile -Value "Tables: $($HealthRow['TableCount'])"
    Add-Content -Path $ReportFile -Value "Indexes: $($HealthRow['IndexCount'])"
    Add-Content -Path $ReportFile -Value "Size: $($HealthRow['SizeGB']) GB"

    Write-Host "Database integrity check completed successfully"
    return $true

} catch {
    Add-Content -Path $ReportFile -Value "ERROR: $($_.Exception.Message)"
    Write-Host "Database integrity check failed: $($_.Exception.Message)" -ForegroundColor Red
    return $false
}
```

### 2. Table-Level Validation

#### Validate Critical Tables: `scripts/Validate-CriticalTables.ps1`

```powershell
param(
    [Parameter(Mandatory=$true)]
    [string]$DatabaseName,

    [Parameter(Mandatory=$true)]
    [array]$TableNames,  # List of critical tables to validate

    [Parameter(Mandatory=$false)]
    [string]$SqlServer = 'localhost'
)

Write-Host "Validating critical tables..."

[System.Reflection.Assembly]::LoadWithPartialName("Microsoft.SqlServer.SMO") | Out-Null
$SmoServer = New-Object Microsoft.SqlServer.Management.Smo.Server($SqlServer)
$Database = $SmoServer.Databases[$DatabaseName]

$ValidationResults = @()

foreach ($TableName in $TableNames) {
    try {
        $Table = $Database.Tables[$TableName]

        if (-not $Table) {
            Write-Host "Table not found: $TableName" -ForegroundColor Red
            $ValidationResults += @{ Table = $TableName; Status = 'NOT_FOUND' }
            continue
        }

        # Check row count
        $RowCount = $SmoServer.ConnectionContext.ExecuteWithResults(
            "SELECT COUNT(*) as RowCount FROM [$DatabaseName].dbo.[$TableName]"
        ).Tables[0].Rows[0]['RowCount']

        # Check for NULL in primary key
        $PkColumns = $Table.PrimaryKeyColumns
        if ($PkColumns.Count -gt 0) {
            $PkCheck = $SmoServer.ConnectionContext.ExecuteWithResults(
                "SELECT COUNT(*) as NullCount FROM [$DatabaseName].dbo.[$TableName] WHERE $($PkColumns[0]) IS NULL"
            ).Tables[0].Rows[0]['NullCount']

            if ($PkCheck -gt 0) {
                Write-Host "Table $TableName has NULL values in primary key!" -ForegroundColor Red
                $ValidationResults += @{ Table = $TableName; Status = 'PK_NULL'; RowCount = $RowCount }
            } else {
                Write-Host "Table $TableName OK: $RowCount rows" -ForegroundColor Green
                $ValidationResults += @{ Table = $TableName; Status = 'OK'; RowCount = $RowCount }
            }
        }

    } catch {
        Write-Host "Validation failed for $TableName`: $($_.Exception.Message)" -ForegroundColor Red
        $ValidationResults += @{ Table = $TableName; Status = 'ERROR'; Error = $_.Exception.Message }
    }
}

return $ValidationResults
```

---

## Point-in-Time Recovery

### PITR Using Transaction Logs

#### Restore to Specific Point in Time: `scripts/Restore-PointInTime.ps1`

```powershell
param(
    [Parameter(Mandatory=$true)]
    [string]$DatabaseName,

    [Parameter(Mandatory=$true)]
    [datetime]$RecoveryTime,  # Target recovery point

    [Parameter(Mandatory=$true)]
    [string]$FullBackupFile,  # Latest full backup before recovery time

    [Parameter(Mandatory=$false)]
    [string]$SqlServer = 'localhost',

    [Parameter(Mandatory=$false)]
    [string]$TransactionLogPath = 'C:\SQLBackups\chatbot_db\TransactionLog\'
)

$ErrorActionPreference = 'Stop'

Write-Host "Point-in-Time Recovery Procedure"
Write-Host "================================="
Write-Host "Database: $DatabaseName"
Write-Host "Target Recovery Time: $RecoveryTime"
Write-Host "Full Backup: $FullBackupFile"

[System.Reflection.Assembly]::LoadWithPartialName("Microsoft.SqlServer.SMO") | Out-Null
$SmoServer = New-Object Microsoft.SqlServer.Management.Smo.Server($SqlServer)

try {
    # Step 1: Restore full backup
    Write-Host "[Step 1] Restoring full backup..."
    $RestoreDB = New-Object Microsoft.SqlServer.Management.Smo.Restore
    $RestoreDB.Database = $DatabaseName
    $RestoreDB.NoRecovery = $true  # Important: leave in NORECOVERY for log application
    $RestoreDB.ReplaceDatabase = $true

    $BackupDevice = New-Object Microsoft.SqlServer.Management.Smo.BackupDeviceItem($FullBackupFile, 'File')
    $RestoreDB.Devices.Add($BackupDevice)

    $RestoreDB.SqlRestore($SmoServer)
    Write-Host "Full backup restored (NORECOVERY mode)"

    # Step 2: Find and apply transaction logs
    Write-Host "[Step 2] Applying transaction logs..."

    $TransactionLogs = Get-ChildItem $TransactionLogPath -Filter "*.trn" -File |
        Sort-Object -Property LastWriteTime

    $FullBackupTime = (Get-Item $FullBackupFile).LastWriteTime
    $AppliedLogCount = 0

    foreach ($LogFile in $TransactionLogs) {
        if ($LogFile.LastWriteTime -le $RecoveryTime) {
            Write-Host "Applying log: $($LogFile.Name)"

            $RestoreLog = New-Object Microsoft.SqlServer.Management.Smo.Restore
            $RestoreLog.Database = $DatabaseName
            $RestoreLog.Action = [Microsoft.SqlServer.Management.Smo.RestoreActionType]::Log
            $RestoreLog.NoRecovery = $true

            $LogDevice = New-Object Microsoft.SqlServer.Management.Smo.BackupDeviceItem($LogFile.FullName, 'File')
            $RestoreLog.Devices.Add($LogDevice)

            $RestoreLog.SqlRestore($SmoServer)
            $AppliedLogCount++
        } else {
            break  # Stop at first log after recovery time
        }
    }

    Write-Host "Applied $AppliedLogCount transaction logs"

    # Step 3: Recover to point in time
    Write-Host "[Step 3] Completing recovery to point in time..."

    $RecoverLog = New-Object Microsoft.SqlServer.Management.Smo.Restore
    $RecoverLog.Database = $DatabaseName
    $RecoverLog.Action = [Microsoft.SqlServer.Management.Smo.RestoreActionType]::Log
    $RecoverLog.ToPointInTime = $RecoveryTime

    # Create a dummy backup device (required for recovery)
    $DummyDevice = New-Object Microsoft.SqlServer.Management.Smo.BackupDeviceItem("Dummy", 'File')
    $RecoverLog.Devices.Add($DummyDevice)

    $RecoverLog.SqlRestore($SmoServer)
    Write-Host "Database recovered to: $RecoveryTime"

    # Step 4: Verify recovery
    Write-Host "[Step 4] Verifying database..."

    $Database = $SmoServer.Databases[$DatabaseName]
    if ($Database.Status -eq 'Normal') {
        Write-Host "Database Status: ONLINE" -ForegroundColor Green
    } else {
        Write-Host "Database Status: $($Database.Status)" -ForegroundColor Yellow
    }

    Write-Host "Point-in-time recovery completed successfully" -ForegroundColor Green

} catch {
    Write-Host "Recovery failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Stack Trace: $($_.Exception.StackTrace)"
    exit 1
}
```

#### PITR with Tail Log Backup

```powershell
# Before performing recovery, capture the tail of the transaction log
# (changes made after the last transaction log backup)

BACKUP LOG chatbot_db TO DISK = 'C:\SQLBackups\chatbot_db\TransactionLog\tail_log.trn'
    WITH NO_TRUNCATE, INIT

# Then follow the PITR procedure above
```

---

## Emergency Contacts & Escalation

### Incident Escalation Matrix

#### Level 1: On-Call Support (Response Time: 30 minutes)

| Role | Contact | Escalation Trigger |
|------|---------|-------------------|
| Database Administrator | [Primary DBA Phone] | Database unavailable / Corruption detected |
| Application Admin | [App Admin Phone] | Application service down |
| Network Admin | [Network Admin Phone] | Network connectivity issue |

#### Level 2: Engineering Management (Response Time: 1 hour)

| Role | Contact | Escalation Trigger |
|------|---------|-------------------|
| Database Engineering Lead | [Lead Email] | Data loss / Recovery > 2 hours |
| Infrastructure Manager | [Manager Email] | Multiple system failures |

#### Level 3: Executive / Crisis Management (Response Time: 2 hours)

| Role | Contact | Escalation Trigger |
|------|---------|-------------------|
| VP Engineering | [VP Email] | Expected RTO > 4 hours |
| CTO / Chief Architect | [CTO Email] | Data center failure |
| Company Leadership | [Executive Email] | Potential data breach / ransomware |

### Emergency Contacts List

```
PRIMARY CONTACT (24/7):
  Name: [DBA Name]
  Phone: [Primary Phone]
  Email: [Email]
  Backup Phone: [Backup Phone]

ESCALATION CONTACTS:
  Database Team Lead: [Name] - [Phone]
  Infrastructure Manager: [Name] - [Phone]
  VP Engineering: [Name] - [Phone]
  CTO: [Name] - [Phone]

EXTERNAL SUPPORT:
  Microsoft SQL Server Support: +1-XXX-XXX-XXXX
  Vendor Account Manager: [Contact]
  Backup Solution Provider: [Contact]
  Cloud Provider Support: [Contact]
```

### Incident Response Checklist

```
IMMEDIATE ACTIONS (First 15 minutes):
[ ] Identify and confirm the incident
[ ] Page on-call responder
[ ] Initiate incident war room (conference call, Slack channel)
[ ] Document incident timeline and initial observations
[ ] Identify potential impact (systems affected, users impacted, data at risk)

ASSESSMENT (15-30 minutes):
[ ] Gather diagnostic information (logs, error messages, recent changes)
[ ] Verify backup integrity and availability
[ ] Estimate recovery time objective (RTO) and data loss (RPO)
[ ] Determine if recovery from backups is required
[ ] Assess if escalation to Level 2 is needed

RECOVERY (30 minutes - 4 hours):
[ ] Execute recovery procedure appropriate to scenario
[ ] Monitor recovery progress and adjust as needed
[ ] Verify data integrity after recovery
[ ] Restore dependent services in correct order
[ ] Conduct functional testing before announcing service restoration

POST-INCIDENT (After recovery):
[ ] Document incident details and root cause analysis
[ ] Identify preventive measures to avoid recurrence
[ ] Update procedures/runbooks as needed
[ ] Conduct post-incident review meeting
[ ] Update stakeholders with final incident report
```

---

## Implementation Roadmap

### Phase 1: Foundation (Months 1-2)

- [x] Define backup strategy and schedules
- [ ] Implement SQL Server full and differential backups
- [ ] Implement transaction log backups
- [ ] Set up off-site backup replication
- [ ] Create backup automation scripts
- [ ] Establish backup verification procedures
- [ ] Document RTO/RPO for all services

### Phase 2: Automation & Testing (Months 2-3)

- [ ] Automate backup scripts in SQL Server Agent
- [ ] Implement daily backup validation
- [ ] Create quarterly restore test schedule
- [ ] Develop application artifact backup system
- [ ] Implement configuration file versioning
- [ ] Create encrypted secrets backup process
- [ ] Test all disaster recovery scenarios

### Phase 3: Monitoring & Alerting (Months 3-4)

- [ ] Deploy backup job monitoring
- [ ] Configure failure alerts (email, SMS, Slack)
- [ ] Create backup dashboard for visibility
- [ ] Implement automated backup integrity checks
- [ ] Set up RTO/RPO tracking and reporting
- [ ] Create runbook for each disaster scenario

### Phase 4: Optimization & Hardening (Months 4-6)

- [ ] Analyze backup performance and optimize
- [ ] Implement incremental backup strategy
- [ ] Deploy off-site DR site (if budget allows)
- [ ] Implement automated failover testing
- [ ] Review and update all documentation
- [ ] Conduct full DR drill with stakeholders

---

## Revision History

| Version | Date | Changes | Author |
|---------|------|---------|--------|
| 1.0 | 2025-12-22 | Initial comprehensive B&R procedures | DevOps Team |
| 1.1 | TBD | Update with Phase 1 implementation results | TBD |
| 2.0 | TBD | Automated failover implementation | TBD |

---

## Appendix A: Backup Storage Configuration

### Primary Backup Storage

```
Location: C:\SQLBackups\
Total Capacity: 2 TB SSD
Allocation:
  - Full Backups: 800 GB
  - Differential Backups: 400 GB
  - Transaction Logs: 400 GB
  - Reserved: 400 GB

Maintenance:
  - Daily: Clean incomplete backups
  - Weekly: Verify storage health
  - Monthly: Capacity review and archival
```

### Secondary Backup Storage (Off-Site)

```
Location: \\backup-server\sql-backups\ (Network Share)
Total Capacity: 5 TB
Replication: Daily via Robocopy at 03:00 UTC
Encryption: AES-256 for sensitive backups
Retention: 12 months for archives
```

---

## Appendix B: Backup Automation Scheduling

### SQL Server Agent Jobs

```sql
-- View all backup jobs
SELECT * FROM msdb.dbo.sysjobs WHERE name LIKE '%Backup%'

-- View job execution history
SELECT job_id, step_id, last_run_date, last_run_time, last_run_outcome
FROM msdb.dbo.sysjobhistory
WHERE job_id = @job_id
ORDER BY last_run_date DESC, last_run_time DESC
```

### Windows Task Scheduler

```powershell
# List all backup tasks
Get-ScheduledTask -TaskName "*Backup*"

# Create scheduled backup task
Register-ScheduledTask -TaskName "Backup-TransactionLog-Hourly" `
    -Action (New-ScheduledTaskAction -Execute "powershell.exe" `
        -Argument "-ExecutionPolicy Bypass -File C:\Scripts\Backup-TransactionLog.ps1") `
    -Trigger (New-ScheduledTaskTrigger -Hourly -At 00:00)
```

---

**Document Classification:** Internal - Operational Procedures
**Last Reviewed:** 2025-12-22
**Next Review Date:** 2026-03-22 (Quarterly)
**Owner:** DevOps / Infrastructure Team
