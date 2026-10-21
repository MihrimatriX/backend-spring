#!/bin/bash

# E-commerce Backend Health Check Script
# This script performs comprehensive health checks on the application

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
BASE_URL="http://localhost:8080"
TIMEOUT=10
RETRY_COUNT=3
RETRY_DELAY=5

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

# Health check functions
check_http_endpoint() {
    local endpoint="$1"
    local expected_status="$2"
    local description="$3"
    
    log_info "Checking $description..."
    
    for i in $(seq 1 $RETRY_COUNT); do
        response=$(curl -s -o /dev/null -w "%{http_code}" --max-time $TIMEOUT "$BASE_URL$endpoint" 2>/dev/null || echo "000")
        
        if [ "$response" = "$expected_status" ]; then
            log_success "$description is healthy (HTTP $response)"
            return 0
        else
            log_warning "$description check attempt $i failed (HTTP $response)"
            if [ $i -lt $RETRY_COUNT ]; then
                sleep $RETRY_DELAY
            fi
        fi
    done
    
    log_error "$description is unhealthy (HTTP $response)"
    return 1
}

check_database_connection() {
    log_info "Checking database connection..."
    
    # Check if PostgreSQL container is running
    if ! docker ps --format "table {{.Names}}" | grep -q "ecommerce-postgres"; then
        log_error "PostgreSQL container is not running"
        return 1
    fi
    
    # Check database connectivity
    if docker exec ecommerce-postgres pg_isready -U ecommerce_user -d ecommerce > /dev/null 2>&1; then
        log_success "Database connection is healthy"
        return 0
    else
        log_error "Database connection failed"
        return 1
    fi
}

check_redis_connection() {
    log_info "Checking Redis connection..."
    
    # Check if Redis container is running
    if ! docker ps --format "table {{.Names}}" | grep -q "ecommerce-redis"; then
        log_error "Redis container is not running"
        return 1
    fi
    
    # Check Redis connectivity
    if docker exec ecommerce-redis redis-cli ping > /dev/null 2>&1; then
        log_success "Redis connection is healthy"
        return 0
    else
        log_error "Redis connection failed"
        return 1
    fi
}

check_application_metrics() {
    log_info "Checking application metrics..."
    
    # Check Prometheus metrics endpoint
    if check_http_endpoint "/actuator/prometheus" "200" "Prometheus metrics endpoint"; then
        # Check if custom metrics are present
        metrics_response=$(curl -s --max-time $TIMEOUT "$BASE_URL/actuator/prometheus" 2>/dev/null || echo "")
        
        if echo "$metrics_response" | grep -q "ecommerce_"; then
            log_success "Custom e-commerce metrics are available"
        else
            log_warning "Custom e-commerce metrics not found"
        fi
        
        return 0
    else
        return 1
    fi
}

check_monitoring_services() {
    log_info "Checking monitoring services..."
    
    local prometheus_healthy=false
    local grafana_healthy=false
    
    # Check Prometheus
    if curl -s --max-time $TIMEOUT "http://localhost:9090/-/healthy" > /dev/null 2>&1; then
        log_success "Prometheus is healthy"
        prometheus_healthy=true
    else
        log_error "Prometheus is unhealthy"
    fi
    
    # Check Grafana
    if curl -s --max-time $TIMEOUT "http://localhost:3000/api/health" > /dev/null 2>&1; then
        log_success "Grafana is healthy"
        grafana_healthy=true
    else
        log_error "Grafana is unhealthy"
    fi
    
    if [ "$prometheus_healthy" = true ] && [ "$grafana_healthy" = true ]; then
        return 0
    else
        return 1
    fi
}

check_api_endpoints() {
    log_info "Checking API endpoints..."
    
    local endpoints_healthy=true
    
    # Check public endpoints
    if ! check_http_endpoint "/api/test/health" "200" "Health endpoint"; then
        endpoints_healthy=false
    fi
    
    if ! check_http_endpoint "/api/product" "200" "Products endpoint"; then
        endpoints_healthy=false
    fi
    
    if ! check_http_endpoint "/api/category" "200" "Categories endpoint"; then
        endpoints_healthy=false
    fi
    
    # Check Swagger UI
    if ! check_http_endpoint "/swagger-ui.html" "200" "Swagger UI"; then
        endpoints_healthy=false
    fi
    
    if [ "$endpoints_healthy" = true ]; then
        log_success "All API endpoints are healthy"
        return 0
    else
        log_error "Some API endpoints are unhealthy"
        return 1
    fi
}

check_system_resources() {
    log_info "Checking system resources..."
    
    # Check Docker container resources
    local containers=$(docker ps --format "{{.Names}}" | grep "ecommerce")
    
    for container in $containers; do
        local cpu_usage=$(docker stats --no-stream --format "{{.CPUPerc}}" "$container" | sed 's/%//')
        local memory_usage=$(docker stats --no-stream --format "{{.MemUsage}}" "$container" | cut -d'/' -f1 | sed 's/[^0-9.]//g')
        
        log_info "Container $container - CPU: ${cpu_usage}%, Memory: ${memory_usage}MB"
        
        # Check if CPU usage is too high
        if (( $(echo "$cpu_usage > 80" | bc -l) )); then
            log_warning "High CPU usage detected for $container: ${cpu_usage}%"
        fi
        
        # Check if memory usage is too high (assuming 1GB limit)
        if (( $(echo "$memory_usage > 800" | bc -l) )); then
            log_warning "High memory usage detected for $container: ${memory_usage}MB"
        fi
    done
    
    log_success "System resource check completed"
    return 0
}

# Main health check function
main() {
    log_info "Starting comprehensive health check..."
    log_info "Base URL: $BASE_URL"
    log_info "Timeout: ${TIMEOUT}s"
    log_info "Retry count: $RETRY_COUNT"
    echo ""
    
    local overall_healthy=true
    
    # Core application health
    if ! check_http_endpoint "/actuator/health" "200" "Application health endpoint"; then
        overall_healthy=false
    fi
    
    # Database health
    if ! check_database_connection; then
        overall_healthy=false
    fi
    
    # Redis health
    if ! check_redis_connection; then
        overall_healthy=false
    fi
    
    # API endpoints
    if ! check_api_endpoints; then
        overall_healthy=false
    fi
    
    # Application metrics
    if ! check_application_metrics; then
        overall_healthy=false
    fi
    
    # Monitoring services
    if ! check_monitoring_services; then
        overall_healthy=false
    fi
    
    # System resources
    check_system_resources
    
    echo ""
    if [ "$overall_healthy" = true ]; then
        log_success "All health checks passed! System is healthy."
        exit 0
    else
        log_error "Some health checks failed! System may be unhealthy."
        exit 1
    fi
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --url)
            BASE_URL="$2"
            shift 2
            ;;
        --timeout)
            TIMEOUT="$2"
            shift 2
            ;;
        --retries)
            RETRY_COUNT="$2"
            shift 2
            ;;
        --help)
            echo "Usage: $0 [OPTIONS]"
            echo "Options:"
            echo "  --url URL           Base URL for health checks [default: http://localhost:8080]"
            echo "  --timeout SECONDS   Timeout for HTTP requests [default: 10]"
            echo "  --retries COUNT     Number of retries for failed checks [default: 3]"
            echo "  --help              Show this help message"
            exit 0
            ;;
        *)
            log_error "Unknown option: $1"
            exit 1
            ;;
    esac
done

# Run main function
main
