# Deployment Guide

This guide provides comprehensive instructions for deploying the E-commerce Backend application in different environments.

## 📋 Table of Contents

- [Prerequisites](#prerequisites)
- [Environment Setup](#environment-setup)
- [Development Deployment](#development-deployment)
- [Production Deployment](#production-deployment)
- [Docker Deployment](#docker-deployment)
- [Database Migration](#database-migration)
- [Monitoring Setup](#monitoring-setup)
- [Health Checks](#health-checks)
- [Troubleshooting](#troubleshooting)

## 🔧 Prerequisites

### System Requirements
- **OS**: Linux/macOS/Windows
- **RAM**: Minimum 4GB, Recommended 8GB+
- **CPU**: Minimum 2 cores, Recommended 4+ cores
- **Storage**: Minimum 20GB free space
- **Network**: Ports 8080, 5432, 6379, 9090, 3000 available

### Software Requirements
- **Java**: 17 or higher
- **Maven**: 3.6+
- **Docker**: 20.10+
- **Docker Compose**: 2.0+
- **Git**: Latest version

### External Services
- **PostgreSQL**: 15+ (for production)
- **Redis**: 7+ (for production)
- **Domain/SSL**: For production deployment

## 🌍 Environment Setup

### Environment Variables

Create a `.env` file in the project root:

```bash
# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=ecommerce
DB_USERNAME=ecommerce_user
DB_PASSWORD=secure_password

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379

# JWT Configuration
JWT_SECRET=your-256-bit-secret-key-for-production-use-only
JWT_EXPIRATION=86400000

# CORS Configuration
ALLOWED_ORIGINS=http://localhost:3000,http://localhost:8080

# Rate Limiting
RATE_LIMIT_CAPACITY=1000
RATE_LIMIT_REFILL=1000
RATE_LIMIT_DURATION=1

# Server Configuration
SERVER_PORT=8080
```

### SSL/TLS Configuration (Production)

For production deployment, configure SSL certificates:

```bash
# SSL Certificate paths
SSL_CERT_PATH=/path/to/certificate.crt
SSL_KEY_PATH=/path/to/private.key
SSL_CHAIN_PATH=/path/to/chain.crt
```

## 🚀 Development Deployment

### Option 1: Docker Compose (Recommended)

```bash
# Clone repository
git clone <repository-url>
cd backend-spring

# Start development environment
docker-compose -f docker/docker-compose.dev.yml up -d

# Check status
docker-compose -f docker/docker-compose.dev.yml ps

# View logs
docker-compose -f docker/docker-compose.dev.yml logs -f
```

### Option 2: Local Development

```bash
# Install dependencies
mvn clean install

# Start H2 database (if not using Docker)
# H2 will start automatically with the application

# Run application
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Or with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev -Dspring-boot.run.arguments="--server.port=8081"
```

### Development URLs
- **Application**: http://localhost:8081
- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **H2 Console**: http://localhost:8082
- **Actuator Health**: http://localhost:8081/actuator/health

## 🏭 Production Deployment

### Option 1: Docker Compose (Recommended)

```bash
# Build application
./scripts/build.sh --profile prod --tag latest

# Deploy with backup
./scripts/deploy.sh --env prod --backup

# Check deployment status
./scripts/health-check.sh
```

### Option 2: Manual Deployment

#### 1. Build Application
```bash
# Clean and build
mvn clean package -Pprod -DskipTests

# Create deployment directory
mkdir -p /opt/ecommerce-backend
cp target/*.jar /opt/ecommerce-backend/
```

#### 2. Database Setup
```bash
# Create PostgreSQL database
sudo -u postgres createdb ecommerce
sudo -u postgres createuser ecommerce_user
sudo -u postgres psql -c "ALTER USER ecommerce_user PASSWORD 'secure_password';"
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE ecommerce TO ecommerce_user;"

# Run migrations
mvn flyway:migrate -Pprod
```

#### 3. Redis Setup
```bash
# Install Redis
sudo apt-get install redis-server

# Configure Redis
sudo systemctl enable redis-server
sudo systemctl start redis-server
```

#### 4. Application Configuration
```bash
# Create application configuration
cat > /opt/ecommerce-backend/application-prod.yml << EOF
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ecommerce
    username: ecommerce_user
    password: secure_password
  redis:
    host: localhost
    port: 6379

server:
  port: 8080

jwt:
  secret: your-production-secret-key
  expiration: 86400000
EOF
```

#### 5. Systemd Service
```bash
# Create systemd service
sudo cat > /etc/systemd/system/ecommerce-backend.service << EOF
[Unit]
Description=E-commerce Backend Application
After=network.target postgresql.service redis.service

[Service]
Type=simple
User=ecommerce
Group=ecommerce
WorkingDirectory=/opt/ecommerce-backend
ExecStart=/usr/bin/java -jar -Dspring.profiles.active=prod /opt/ecommerce-backend/ecommerce-backend-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=10
Environment=JAVA_OPTS="-Xms512m -Xmx1024m"

[Install]
WantedBy=multi-user.target
EOF

# Enable and start service
sudo systemctl daemon-reload
sudo systemctl enable ecommerce-backend
sudo systemctl start ecommerce-backend
```

## 🐳 Docker Deployment

### Production Docker Compose

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: ecommerce
      POSTGRES_USER: ecommerce_user
      POSTGRES_PASSWORD: secure_password
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"
    networks:
      - ecommerce-network

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    networks:
      - ecommerce-network

  app:
    build:
      context: .
      dockerfile: docker/Dockerfile
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DB_HOST: postgres
      DB_PORT: 5432
      DB_NAME: ecommerce
      DB_USERNAME: ecommerce_user
      DB_PASSWORD: secure_password
      REDIS_HOST: redis
      REDIS_PORT: 6379
    ports:
      - "8080:8080"
    depends_on:
      - postgres
      - redis
    networks:
      - ecommerce-network

volumes:
  postgres_data:
  redis_data:

networks:
  ecommerce-network:
    driver: bridge
```

### Docker Commands

```bash
# Build image
docker build -t ecommerce-backend:latest .

# Run container
docker run -d \
  --name ecommerce-backend \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_HOST=localhost \
  -e DB_PASSWORD=secure_password \
  ecommerce-backend:latest

# View logs
docker logs -f ecommerce-backend

# Stop container
docker stop ecommerce-backend
docker rm ecommerce-backend
```

## 🗄️ Database Migration

### Flyway Migration

```bash
# Check migration status
mvn flyway:info

# Run migrations
mvn flyway:migrate

# Validate migrations
mvn flyway:validate

# Repair migrations (if needed)
mvn flyway:repair
```

### Manual Migration

```bash
# Connect to database
psql -h localhost -U ecommerce_user -d ecommerce

# Run migration scripts
\i src/main/resources/db/migration/V1__initial_schema.sql
\i src/main/resources/db/migration/V2__add_performance_indexes.sql
\i src/main/resources/db/migration/V3__seed_initial_data.sql
```

## 📊 Monitoring Setup

### Prometheus Configuration

```yaml
# prometheus.yml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'ecommerce-backend'
    static_configs:
      - targets: ['localhost:8080']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 10s
```

### Grafana Dashboard

1. Access Grafana: http://localhost:3000
2. Login: admin/admin123
3. Import dashboard from `docker/monitoring/grafana/dashboards/`
4. Configure Prometheus datasource

### Monitoring Commands

```bash
# Check Prometheus targets
curl http://localhost:9090/api/v1/targets

# Check application metrics
curl http://localhost:8080/actuator/prometheus

# Check health status
curl http://localhost:8080/actuator/health
```

## 🔍 Health Checks

### Automated Health Check

```bash
# Run comprehensive health check
./scripts/health-check.sh

# Check specific endpoint
./scripts/health-check.sh --url http://your-domain.com

# Custom timeout and retries
./scripts/health-check.sh --timeout 30 --retries 5
```

### Manual Health Checks

```bash
# Application health
curl -f http://localhost:8080/actuator/health

# Database connectivity
docker exec ecommerce-postgres pg_isready -U ecommerce_user -d ecommerce

# Redis connectivity
docker exec ecommerce-redis redis-cli ping

# Prometheus health
curl -f http://localhost:9090/-/healthy

# Grafana health
curl -f http://localhost:3000/api/health
```

## 🔧 Troubleshooting

### Common Issues

#### 1. Database Connection Issues
```bash
# Check database status
docker-compose logs postgres

# Test connection
docker exec ecommerce-postgres psql -U ecommerce_user -d ecommerce -c "SELECT 1;"

# Check network connectivity
docker network ls
docker network inspect ecommerce-network
```

#### 2. Redis Connection Issues
```bash
# Check Redis status
docker-compose logs redis

# Test Redis connection
docker exec ecommerce-redis redis-cli ping

# Check Redis configuration
docker exec ecommerce-redis redis-cli config get "*"
```

#### 3. Application Startup Issues
```bash
# Check application logs
docker-compose logs app

# Check JVM memory
docker stats ecommerce-backend

# Check port availability
netstat -tulpn | grep :8080
```

#### 4. Migration Issues
```bash
# Check migration status
mvn flyway:info

# Repair migrations
mvn flyway:repair

# Manual migration
docker exec ecommerce-postgres psql -U ecommerce_user -d ecommerce -f /path/to/migration.sql
```

### Log Analysis

```bash
# Application logs
docker-compose logs -f app

# Database logs
docker-compose logs -f postgres

# Redis logs
docker-compose logs -f redis

# All services logs
docker-compose logs -f
```

### Performance Issues

```bash
# Check resource usage
docker stats

# Check database performance
docker exec ecommerce-postgres psql -U ecommerce_user -d ecommerce -c "SELECT * FROM pg_stat_activity;"

# Check Redis performance
docker exec ecommerce-redis redis-cli info stats
```

### Security Issues

```bash
# Check security headers
curl -I http://localhost:8080/api/test/health

# Check rate limiting
for i in {1..10}; do curl http://localhost:8080/api/test/health; done

# Check JWT token
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" http://localhost:8080/api/order
```

## 📞 Support

For deployment issues:
1. Check the logs first
2. Verify all prerequisites
3. Test connectivity between services
4. Check resource usage
5. Review configuration files
6. Create an issue with detailed information

## 🔄 Rollback Procedures

### Docker Rollback
```bash
# Stop current deployment
docker-compose down

# Restore from backup
docker-compose -f docker/docker-compose.backup.yml up -d

# Or rollback using deploy script
./scripts/deploy.sh --rollback
```

### Manual Rollback
```bash
# Stop application
sudo systemctl stop ecommerce-backend

# Restore database backup
sudo -u postgres psql ecommerce < backup.sql

# Start previous version
sudo systemctl start ecommerce-backend
```
