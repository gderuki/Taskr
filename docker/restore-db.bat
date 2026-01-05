@echo off
REM Database restore script for Docker PostgreSQL (Windows)

setlocal

REM Check if backup file is provided
if "%~1"=="" (
    echo Usage: %0 ^<backup-file^>
    echo Example: %0 backups\taskr_db_20260105_120000.sql
    echo Example: %0 backups\taskr_db_20260105_120000.sql.zip
    exit /b 1
)

set BACKUP_FILE=%~1

REM Check if file exists
if not exist "%BACKUP_FILE%" (
    echo Error: Backup file not found: %BACKUP_FILE%
    exit /b 1
)

REM Configuration
set CONTAINER_NAME=taskr-postgres
set DB_NAME=taskr_db
set DB_USER=admin
set TEMP_SQL=%TEMP%\taskr_restore_temp.sql

echo.
echo WARNING: This will restore the database and may overwrite existing data!
echo Container: %CONTAINER_NAME%
echo Database: %DB_NAME%
echo Backup file: %BACKUP_FILE%
echo.
set /p CONFIRM="Continue? (Y/N): "
if /i not "%CONFIRM%"=="Y" (
    echo Restore cancelled
    exit /b 0
)

echo.
echo Restoring database...

REM Check if file is compressed
echo %BACKUP_FILE% | findstr /i "\.zip$" >nul
if %errorlevel% equ 0 (
    echo Decompressing backup...
    powershell -Command "Expand-Archive -Path '%BACKUP_FILE%' -DestinationPath '%TEMP%' -Force"
    REM Find the .sql file in temp
    for %%f in (%TEMP%\taskr_db_*.sql) do set TEMP_SQL=%%f
) else (
    set TEMP_SQL=%BACKUP_FILE%
)

REM Restore from SQL file
type "%TEMP_SQL%" | docker exec -i %CONTAINER_NAME% psql -U %DB_USER% -d %DB_NAME%

if %errorlevel% equ 0 (
    echo.
    echo Database restored successfully from: %BACKUP_FILE%
) else (
    echo.
    echo Restore failed!
    exit /b 1
)

REM Cleanup temp file if we decompressed
if not "%TEMP_SQL%"=="%BACKUP_FILE%" (
    del "%TEMP_SQL%" 2>nul
)
