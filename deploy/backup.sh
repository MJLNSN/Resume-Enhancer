#!/bin/bash

# Resume Enhancer Backup Script
# Backs up database, uploaded files, and configuration

set -e

# Configuration
APP_DIR="/home/ec2-user/resume-enhancer"
BACKUP_DIR="/home/ec2-user/backups"
DB_NAME="resume_enhancer_prod"
DB_USER="resume_user"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
RETENTION_DAYS=7

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}🗄️ Resume Enhancer Backup Script${NC}"
echo "=================================="

# Create backup directory
mkdir -p "$BACKUP_DIR"

# Function to log messages
log() {
    echo -e "${GREEN}[$(date '+%Y-%m-%d %H:%M:%S')]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1" >&2
}

warn() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Check if PostgreSQL is running
if ! pg_isready -h localhost -p 5432 -U "$DB_USER" > /dev/null 2>&1; then
    error "PostgreSQL is not accessible. Cannot perform backup."
    exit 1
fi

log "Starting backup process..."

# Backup PostgreSQL database
log "Backing up PostgreSQL database..."
PGPASSWORD=${POSTGRES_PASSWORD} pg_dump -h localhost -U "$DB_USER" -d "$DB_NAME" \
    --no-owner --no-privileges --clean --if-exists \
    > "$BACKUP_DIR/database_$TIMESTAMP.sql"

if [[ $? -eq 0 ]]; then
    log "Database backup completed: database_$TIMESTAMP.sql"
    gzip "$BACKUP_DIR/database_$TIMESTAMP.sql"
    log "Database backup compressed"
else
    error "Database backup failed"
    exit 1
fi

# Backup uploaded files (if using local storage)
if [[ -d "$APP_DIR/uploads" ]]; then
    log "Backing up uploaded files..."
    tar -czf "$BACKUP_DIR/uploads_$TIMESTAMP.tar.gz" -C "$APP_DIR" uploads/
    log "Uploads backup completed: uploads_$TIMESTAMP.tar.gz"
else
    log "No local uploads directory found (likely using S3)"
fi

# Backup configuration files
log "Backing up configuration files..."
tar -czf "$BACKUP_DIR/config_$TIMESTAMP.tar.gz" -C "$APP_DIR" \
    --exclude=".env" \
    .env.template logs/ 2>/dev/null || true
log "Configuration backup completed: config_$TIMESTAMP.tar.gz"

# Create backup manifest
log "Creating backup manifest..."
cat > "$BACKUP_DIR/manifest_$TIMESTAMP.txt" << EOF
Resume Enhancer Backup Manifest
Generated: $(date)
Backup ID: $TIMESTAMP

Files included:
- database_$TIMESTAMP.sql.gz (PostgreSQL dump)
- config_$TIMESTAMP.tar.gz (Configuration files)
$([ -f "$BACKUP_DIR/uploads_$TIMESTAMP.tar.gz" ] && echo "- uploads_$TIMESTAMP.tar.gz (Uploaded files)")

System Information:
- Hostname: $(hostname)
- OS: $(uname -a)
- Backup Size: $(du -sh "$BACKUP_DIR"/*_$TIMESTAMP* | awk '{sum+=$1} END {print sum "B"}' 2>/dev/null || echo "Unknown")

To restore:
1. Restore database: gunzip -c database_$TIMESTAMP.sql.gz | psql -h localhost -U $DB_USER -d $DB_NAME
2. Restore config: tar -xzf config_$TIMESTAMP.tar.gz -C $APP_DIR
3. Restore uploads: tar -xzf uploads_$TIMESTAMP.tar.gz -C $APP_DIR (if applicable)
EOF

log "Backup manifest created: manifest_$TIMESTAMP.txt"

# Clean up old backups
log "Cleaning up backups older than $RETENTION_DAYS days..."
find "$BACKUP_DIR" -name "*_*.sql.gz" -mtime +$RETENTION_DAYS -delete 2>/dev/null || true
find "$BACKUP_DIR" -name "*_*.tar.gz" -mtime +$RETENTION_DAYS -delete 2>/dev/null || true
find "$BACKUP_DIR" -name "manifest_*.txt" -mtime +$RETENTION_DAYS -delete 2>/dev/null || true

# Calculate backup size
BACKUP_SIZE=$(du -sh "$BACKUP_DIR"/*_$TIMESTAMP* 2>/dev/null | awk '{sum+=$1} END {print sum}' || echo "0")

log "✅ Backup completed successfully!"
echo ""
echo "📊 Backup Summary:"
echo "=================="
echo "Timestamp: $TIMESTAMP"
echo "Location: $BACKUP_DIR"
echo "Total Size: ${BACKUP_SIZE}B"
echo "Retention: $RETENTION_DAYS days"
echo ""
echo "📋 Files created:"
ls -lh "$BACKUP_DIR"/*_$TIMESTAMP*

# Upload to S3 if AWS CLI is configured
if command -v aws &> /dev/null && [[ -n "${S3_BACKUP_BUCKET:-}" ]]; then
    log "Uploading backup to S3..."
    aws s3 sync "$BACKUP_DIR" "s3://$S3_BACKUP_BUCKET/backups/" \
        --exclude "*" --include "*_$TIMESTAMP*" \
        --storage-class STANDARD_IA
    log "Backup uploaded to S3: s3://$S3_BACKUP_BUCKET/backups/"
fi

log "🎉 Backup process completed!"
