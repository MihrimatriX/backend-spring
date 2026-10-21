#!/bin/bash

# E-commerce Backend Deployment Script
# This script deploys the application using Docker Compose

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
ENVIRONMENT="prod"
COMPOSE_FILE="docker/docker-compose.yml"
BACKUP_DIR="./backups"
LOG_DIR="./logs"

# Functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --env)
            ENVIRONMENT="$2"
            shift 2
            ;;
        --backup)
            CREATE_BACKUP=true
            shift
            ;;
        --rollback)
            ROLLBACK=true
            shift
            ;;
        --help)
            echo "Usage: $0 [OPTIONS]"
            echo "Options:"
            echo "  --env ENV          Environment (dev/prod) [default: prod]"
            echo "  --backup           Create backup before deployment"
            echo "  --rollback         Rollback to previous version"
            echo "  --help             Show this help message"
            exit 0
            ;;
        *)
            log_error "Unknown option: $1"
            exit 1
            ;;
    esac
done

# Set compose file based on environment
if [ "$ENVIRONMENT" = "dev" ]; then
    COMPOSE_FILE="docker/docker-compose.dev.yml"
fi

log_info "Starting deployment process"
log_info "Environment: $ENVIRONMENT"
log_info "Compose file: $COMPOSE_FILE"

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    log_error "Docker is not running. Please start Docker and try again."
    exit 1
fi

# Check if Docker Compose is available
if ! command -v docker-compose &> /dev/null; then
    log_error "Docker Compose is not installed or not in PATH"
    exit 1
fi

# Create necessary directories
mkdir -p "$BACKUP_DIR" "$LOG_DIR"

# Backup function
create_backup() {
    log_info "Creating backup..."
    BACKUP_TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
    BACKUP_FILE="$BACKUP_DIR/backup_$BACKUP_TIMESTAMP.tar.gz"
    
    # Backup volumes
    docker-compose -f "$COMPOSE_FILE" exec -T postgres pg_dump -U ecommerce_user ecommerce > "$BACKUP_DIR/postgres_backup_$BACKUP_TIMESTAMP.sql" 2>/dev/null || true
    
    # Backup application logs
    docker-compose -f "$COMPOSE_FILE" logs > "$BACKUP_DIR/logs_backup_$BACKUP_TIMESTAMP.log" 2>/dev/null || true
    
    log_success "Backup created: $BACKUP_FILE"
}

# Rollback function
rollback() {
    log_warning "Rolling back to previous version..."
    
    # Stop current services
    docker-compose -f "$COMPOSE_FILE" down
    
    # Restore from latest backup
    LATEST_BACKUP=$(ls -t "$BACKUP_DIR"/postgres_backup_*.sql 2>/dev/null | head -n1)
    if [ -n "$LATEST_BACKUP" ]; then
        log_info "Restoring database from: $LATEST_BACKUP"
        docker-compose -f "$COMPOSE_FILE" up -d postgres
        sleep 10
        docker-compose -f "$COMPOSE_FILE" exec -T postgres psql -U ecommerce_user -d ecommerce < "$LATEST_BACKUP"
    fi
    
    # Start previous version
    docker-compose -f "$COMPOSE_FILE" up -d
    
    log_success "Rollback completed"
    exit 0
}

# Handle rollback
if [ "$ROLLBACK" = true ]; then
    rollback
fi

# Create backup if requested
if [ "$CREATE_BACKUP" = true ]; then
    create_backup
fi

# Pull latest images
log_info "Pulling latest images..."
docker-compose -f "$COMPOSE_FILE" pull

# Stop existing services
log_info "Stopping existing services..."
docker-compose -f "$COMPOSE_FILE" down

# Remove old images (optional)
log_info "Cleaning up old images..."
docker image prune -f

# Start services
log_info "Starting services..."
docker-compose -f "$COMPOSE_FILE" up -d

# Wait for services to be ready
log_info "Waiting for services to be ready..."
sleep 30

# Health check
log_info "Performing health check..."
MAX_RETRIES=30
RETRY_COUNT=0

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
        log_success "Application is healthy"
        break
    fi
    
    RETRY_COUNT=$((RETRY_COUNT + 1))
    log_info "Health check attempt $RETRY_COUNT/$MAX_RETRIES failed, retrying in 10 seconds..."
    sleep 10
done

if [ $RETRY_COUNT -eq $MAX_RETRIES ]; then
    log_error "Health check failed after $MAX_RETRIES attempts"
    log_error "Deployment may have failed. Check logs:"
    docker-compose -f "$COMPOSE_FILE" logs --tail=50
    exit 1
fi

# Show service status
log_info "Service status:"
docker-compose -f "$COMPOSE_FILE" ps

# Show logs
log_info "Recent logs:"
docker-compose -f "$COMPOSE_FILE" logs --tail=20

log_success "Deployment completed successfully!"
log_info "Application is available at:"
log_info "  - API: http://localhost:8080"
log_info "  - Swagger UI: http://localhost:8080/swagger-ui.html"
log_info "  - Prometheus: http://localhost:9090"
log_info "  - Grafana: http://localhost:3000 (admin/admin123)"
log_info "  - H2 Console (dev): http://localhost:8082"

# Save deployment info
DEPLOYMENT_INFO="$LOG_DIR/deployment_$(date +"%Y%m%d_%H%M%S").log"
{
    echo "Deployment completed at: $(date)"
    echo "Environment: $ENVIRONMENT"
    echo "Compose file: $COMPOSE_FILE"
    echo "Docker images:"
    docker images --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}\t{{.CreatedAt}}"
    echo ""
    echo "Service status:"
    docker-compose -f "$COMPOSE_FILE" ps
} > "$DEPLOYMENT_INFO"

log_info "Deployment information saved to: $DEPLOYMENT_INFO"
