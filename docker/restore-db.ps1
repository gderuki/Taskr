# Database restore script for Docker PostgreSQL (PowerShell)

param(
    [Parameter(Mandatory=$true)]
    [string]$BackupFile,
    [string]$ContainerName = "taskr-postgres",
    [string]$DbName = "taskr_db",
    [string]$DbUser = "admin"
)

$ErrorActionPreference = "Stop"

# Check if backup file exists
if (!(Test-Path $BackupFile)) {
    Write-Host "Error: Backup file not found: $BackupFile" -ForegroundColor Red
    Write-Host "`nUsage: .\restore-db.ps1 -BackupFile <path-to-backup>" -ForegroundColor Yellow
    Write-Host "Example: .\restore-db.ps1 -BackupFile backups\taskr_db_20260105_120000.sql.zip"
    exit 1
}

Write-Host "`nWARNING: This will restore the database and may overwrite existing data!" -ForegroundColor Yellow
Write-Host "Container: $ContainerName"
Write-Host "Database: $DbName"
Write-Host "Backup file: $BackupFile"
Write-Host ""

$Confirmation = Read-Host "Continue? (Y/N)"
if ($Confirmation -ne 'Y' -and $Confirmation -ne 'y') {
    Write-Host "Restore cancelled" -ForegroundColor Yellow
    exit 0
}

Write-Host "`nRestoring database..." -ForegroundColor Green

try {
    $SqlFile = $BackupFile
    $TempFile = $null

    # Check if file is compressed
    if ($BackupFile -match '\.zip$') {
        Write-Host "Decompressing backup..." -ForegroundColor Cyan
        $TempDir = Join-Path $env:TEMP "taskr_restore"
        if (Test-Path $TempDir) {
            Remove-Item $TempDir -Recurse -Force
        }
        New-Item -ItemType Directory -Path $TempDir | Out-Null
        Expand-Archive -Path $BackupFile -DestinationPath $TempDir -Force

        # Find the .sql file
        $SqlFile = Get-ChildItem -Path $TempDir -Filter "*.sql" | Select-Object -First 1 -ExpandProperty FullName
        $TempFile = $SqlFile
    }

    # Restore from SQL file
    Get-Content $SqlFile -Raw | docker exec -i $ContainerName psql -U $DbUser -d $DbName

    Write-Host "`nDatabase restored successfully from: $BackupFile" -ForegroundColor Green

    # Cleanup temp files
    if ($TempFile -and (Test-Path $TempFile)) {
        Remove-Item (Split-Path $TempFile) -Recurse -Force
    }
}
catch {
    Write-Host "`nRestore failed: $_" -ForegroundColor Red
    exit 1
}
