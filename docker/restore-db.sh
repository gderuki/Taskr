#!/bin/bash
# Database restore script for Docker PostgreSQL

set -e

# Check if backup file is provided
if [ -z "$1" ]; then
    echo "Usage: $0 <backup-file.sql.gz>"
    echo "Example: $0 ./backups/taskr_db_20260105_120000.sql.gz"
    exit 1
fi

BACKUP_FILE="$1"

# Check if file exists
if [ ! -f "$BACKUP_FILE" ]; then
    echo "Error: Backup file not found: $BACKUP_FILE"
    exit 1
fi

# Configuration
CONTAINER_NAME="${DB_CONTAINER:-taskr-postgres}"
DB_NAME="${DB_NAME:-taskr_db}"
DB_USER="${DB_USER:-postgres}"

echo "WARNING: This will restore the database and may overwrite existing data!"
echo "Container: $CONTAINER_NAME"
echo "Database: $DB_NAME"
echo "Backup file: $BACKUP_FILE"
read -p "Continue? (y/N): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Restore cancelled"
    exit 0
fi

echo "Restoring database..."

# Decompress and restore
gunzip -c "$BACKUP_FILE" | docker exec -i "$CONTAINER_NAME" psql -U "$DB_USER" -d "$DB_NAME"

echo "Database restored successfully from: $BACKUP_FILE"
