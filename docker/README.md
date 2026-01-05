# Database Backup Scripts

**Quick Start:**
- **Windows:** Use `backup-db.ps1` / `restore-db.ps1`
- **Linux/macOS:** Use `backup-db.sh` / `restore-db.sh`
- **Windows (no PowerShell):** Use `backup-db.bat` / `restore-db.bat`

## Usage

### Backup
```powershell
# Windows PowerShell
.\docker\backup-db.ps1

# Linux/macOS
./docker/backup-db.sh

# Windows Batch
docker\backup-db.bat
```

### Restore
```powershell
# Windows PowerShell
.\docker\restore-db.ps1 -BackupFile backups\taskr_db_20260105_120000.sql.zip

# Linux/macOS
./docker/restore-db.sh backups/taskr_db_20260105_120000.sql.gz

# Windows Batch
docker\restore-db.bat backups\taskr_db_20260105_120000.sql.zip
```

## Features
- Automatic timestamp-based naming
- Compression (ZIP on Windows, gzip on Linux/macOS)
- Auto-cleanup of backups older than 7 days (PowerShell/Bash only)
- Interactive confirmation for restore operations

## Requirements
- Docker running with `taskr-postgres` container
- Database credentials in `.env` file
