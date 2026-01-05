# Database backup script for Docker PostgreSQL (PowerShell)

param(
    [string]$ContainerName = "taskr-postgres",
    [string]$DbName = "taskr_db",
    [string]$DbUser = "admin",
    [string]$BackupDir = "backups"
)

$ErrorActionPreference = "Stop"

# Create timestamp
$Timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$BackupFile = Join-Path $BackupDir "taskr_db_$Timestamp.sql"

# Create backup directory if it doesn't exist
if (!(Test-Path $BackupDir)) {
    New-Item -ItemType Directory -Path $BackupDir | Out-Null
}

Write-Host "Starting database backup..." -ForegroundColor Green
Write-Host "Container: $ContainerName"
Write-Host "Database: $DbName"
Write-Host "Backup file: $BackupFile"

try {
    # Create backup using pg_dump
    docker exec -t $ContainerName pg_dump -U $DbUser -d $DbName --clean --if-exists | Out-File -Encoding UTF8 $BackupFile

    Write-Host "`nBackup completed: $BackupFile" -ForegroundColor Green

    # Compress the backup
    $ZipFile = "$BackupFile.zip"
    Compress-Archive -Path $BackupFile -DestinationPath $ZipFile -Force
    Remove-Item $BackupFile

    Write-Host "Compressed backup: $ZipFile" -ForegroundColor Green

    # Show backup size
    $FileInfo = Get-Item $ZipFile
    Write-Host "`nBackup size: $([math]::Round($FileInfo.Length / 1MB, 2)) MB" -ForegroundColor Cyan

    # Optional: Remove backups older than 7 days
    $OldBackups = Get-ChildItem -Path $BackupDir -Filter "taskr_db_*.sql.zip" | Where-Object { $_.LastWriteTime -lt (Get-Date).AddDays(-7) }
    if ($OldBackups) {
        Write-Host "`nCleaning up old backups (>7 days):" -ForegroundColor Yellow
        $OldBackups | ForEach-Object {
            Write-Host "  Removing: $($_.Name)"
            Remove-Item $_.FullName
        }
    }

    Write-Host "`nBackup completed successfully!" -ForegroundColor Green
}
catch {
    Write-Host "`nBackup failed: $_" -ForegroundColor Red
    exit 1
}
