#!/bin/bash

# E-commerce Backend Build Script
# This script builds the Spring Boot application and Docker images

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PROJECT_NAME="ecommerce-backend"
DOCKER_REGISTRY=""
DOCKER_TAG="latest"
BUILD_PROFILE="prod"

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
        --profile)
            BUILD_PROFILE="$2"
            shift 2
            ;;
        --tag)
            DOCKER_TAG="$2"
            shift 2
            ;;
        --registry)
            DOCKER_REGISTRY="$2"
            shift 2
            ;;
        --help)
            echo "Usage: $0 [OPTIONS]"
            echo "Options:"
            echo "  --profile PROFILE    Build profile (dev/prod) [default: prod]"
            echo "  --tag TAG           Docker image tag [default: latest]"
            echo "  --registry REGISTRY Docker registry URL"
            echo "  --help              Show this help message"
            exit 0
            ;;
        *)
            log_error "Unknown option: $1"
            exit 1
            ;;
    esac
done

log_info "Starting build process for $PROJECT_NAME"
log_info "Build profile: $BUILD_PROFILE"
log_info "Docker tag: $DOCKER_TAG"

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    log_error "Docker is not running. Please start Docker and try again."
    exit 1
fi

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    log_error "Maven is not installed or not in PATH"
    exit 1
fi

# Clean previous builds
log_info "Cleaning previous builds..."
mvn clean

# Run tests (skip for dev profile)
if [ "$BUILD_PROFILE" = "prod" ]; then
    log_info "Running tests..."
    mvn test
    if [ $? -ne 0 ]; then
        log_error "Tests failed. Build aborted."
        exit 1
    fi
    log_success "All tests passed"
else
    log_warning "Skipping tests for dev profile"
fi

# Build the application
log_info "Building application..."
mvn package -DskipTests -P$BUILD_PROFILE

if [ $? -ne 0 ]; then
    log_error "Maven build failed"
    exit 1
fi

log_success "Application built successfully"

# Build Docker image
log_info "Building Docker image..."
DOCKER_IMAGE_NAME="$PROJECT_NAME:$DOCKER_TAG"

if [ -n "$DOCKER_REGISTRY" ]; then
    DOCKER_IMAGE_NAME="$DOCKER_REGISTRY/$DOCKER_IMAGE_NAME"
fi

docker build -f docker/Dockerfile -t "$DOCKER_IMAGE_NAME" .

if [ $? -ne 0 ]; then
    log_error "Docker build failed"
    exit 1
fi

log_success "Docker image built successfully: $DOCKER_IMAGE_NAME"

# Show image information
log_info "Docker image information:"
docker images "$PROJECT_NAME" --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}\t{{.CreatedAt}}"

# Optional: Push to registry
if [ -n "$DOCKER_REGISTRY" ]; then
    read -p "Do you want to push the image to registry? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        log_info "Pushing image to registry..."
        docker push "$DOCKER_IMAGE_NAME"
        if [ $? -eq 0 ]; then
            log_success "Image pushed successfully to registry"
        else
            log_error "Failed to push image to registry"
            exit 1
        fi
    fi
fi

log_success "Build process completed successfully!"
log_info "To run the application:"
log_info "  Development: docker-compose -f docker/docker-compose.dev.yml up -d"
log_info "  Production:  docker-compose -f docker/docker-compose.yml up -d"
