@echo off
REM Database backup script for Docker PostgreSQL (Windows)

setlocal

REM Configuration
set CONTAINER_NAME=taskr-postgres
set DB_NAME=taskr_db
set DB_USER=admin
set BACKUP_DIR=backups

REM Create timestamp
for /f "tokens=2-4 delims=/ " %%a in ('date /t') do (set mydate=%%c%%a%%b)
for /f "tokens=1-2 delims=/:" %%a in ("%TIME%") do (set mytime=%%a%%b)
set mytime=%mytime: =0%
set TIMESTAMP=%mydate%_%mytime%
set BACKUP_FILE=%BACKUP_DIR%\taskr_db_%TIMESTAMP%.sql

REM Create backup directory if it doesn't exist
if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"

echo Starting database backup...
echo Container: %CONTAINER_NAME%
echo Database: %DB_NAME%
echo Backup file: %BACKUP_FILE%

REM Create backup using pg_dump
docker exec -t %CONTAINER_NAME% pg_dump -U %DB_USER% -d %DB_NAME% --clean --if-exists > "%BACKUP_FILE%"

if %errorlevel% equ 0 (
    echo Backup completed: %BACKUP_FILE%
    echo.

    REM Show backup size
    dir "%BACKUP_FILE%"

    REM Optional: Compress using PowerShell (Windows 10+)
    powershell -Command "Compress-Archive -Path '%BACKUP_FILE%' -DestinationPath '%BACKUP_FILE%.zip' -Force"
    if %errorlevel% equ 0 (
        echo Compressed backup: %BACKUP_FILE%.zip
        del "%BACKUP_FILE%"
    )
) else (
    echo Backup failed!
    exit /b 1
)

echo.
echo Backup completed successfully!
