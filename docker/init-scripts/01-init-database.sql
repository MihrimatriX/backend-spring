-- Initialize E-commerce Database
-- This script runs when PostgreSQL container starts for the first time

-- Create database if not exists (already created by POSTGRES_DB)
-- CREATE DATABASE IF NOT EXISTS ecommerce;

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Set timezone
SET timezone = 'UTC';

-- Create indexes for better performance (will be created by JPA, but good to have)
-- These will be created by Flyway migrations later

-- Grant permissions
GRANT ALL PRIVILEGES ON DATABASE ecommerce TO ecommerce_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO ecommerce_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO ecommerce_user;

-- Log initialization
DO $$
BEGIN
    RAISE NOTICE 'E-commerce database initialized successfully';
END $$;
