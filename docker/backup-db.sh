#!/bin/bash
# Database backup script for Docker PostgreSQL

set -e

# Configuration
CONTAINER_NAME="${DB_CONTAINER:-taskr-postgres}"
DB_NAME="${DB_NAME:-taskr_db}"
DB_USER="${DB_USER:-postgres}"
BACKUP_DIR="${BACKUP_DIR:-./backups}"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_FILE="${BACKUP_DIR}/taskr_db_${TIMESTAMP}.sql"

# Create backup directory if it doesn't exist
mkdir -p "$BACKUP_DIR"

echo "Starting database backup..."
echo "Container: $CONTAINER_NAME"
echo "Database: $DB_NAME"
echo "Backup file: $BACKUP_FILE"

# Create backup using pg_dump
docker exec -t "$CONTAINER_NAME" pg_dump -U "$DB_USER" -d "$DB_NAME" --clean --if-exists > "$BACKUP_FILE"

# Compress the backup
gzip "$BACKUP_FILE"
echo "Backup completed: ${BACKUP_FILE}.gz"

# Optional: Remove backups older than 7 days
find "$BACKUP_DIR" -name "taskr_db_*.sql.gz" -type f -mtime +7 -delete
echo "Old backups cleaned up (kept last 7 days)"

# Show backup size
ls -lh "${BACKUP_FILE}.gz"
