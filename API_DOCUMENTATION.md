# API Documentation

This document provides comprehensive API documentation for the E-commerce Backend application.

## 📋 Table of Contents

- [Authentication](#authentication)
- [Products API](#products-api)
- [Categories API](#categories-api)
- [Orders API](#orders-api)
- [Users API](#users-api)
- [Addresses API](#addresses-api)
- [Payment Methods API](#payment-methods-api)
- [Reviews API](#reviews-api)
- [Favorites API](#favorites-api)
- [Notifications API](#notifications-api)
- [Security API](#security-api)
- [Health & Monitoring](#health--monitoring)
- [Error Handling](#error-handling)
- [Rate Limiting](#rate-limiting)

## 🔐 Authentication

### Register User
```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1234567890",
  "address": "123 Main St",
  "city": "New York",
  "postalCode": "10001"
}
```

**Response:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": 1,
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "isEmailVerified": false
  }
}
```

### Login User
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": 1,
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "isEmailVerified": false
  }
}
```

### Logout User
```http
POST /api/auth/logout
Authorization: Bearer <token>
```

**Response:**
```json
{
  "success": true,
  "message": "Logout successful",
  "data": "User logged out successfully"
}
```

## 📦 Products API

### Get All Products
```http
GET /api/product?page=0&size=10&sortBy=productName&sortOrder=asc&categoryId=1&minPrice=10&maxPrice=100&searchTerm=laptop
```

**Query Parameters:**
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 10)
- `sortBy` (optional): Sort field (default: productName)
- `sortOrder` (optional): Sort direction (asc/desc, default: asc)
- `categoryId` (optional): Filter by category
- `minPrice` (optional): Minimum price filter
- `maxPrice` (optional): Maximum price filter
- `searchTerm` (optional): Search in product name/description

**Response:**
```json
{
  "success": true,
  "message": "Products retrieved successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "productName": "MacBook Pro M3",
        "unitPrice": 1999.99,
        "unitInStock": 25,
        "quantityPerUnit": "1 piece",
        "categoryId": 1,
        "description": "Professional laptop for creators",
        "imageUrl": "https://example.com/images/macbook-pro.jpg",
        "discount": 5,
        "isActive": true,
        "createdAt": "2024-01-01T10:00:00",
        "updatedAt": "2024-01-01T10:00:00"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10,
      "sort": {
        "sorted": true,
        "unsorted": false
      }
    },
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true,
    "numberOfElements": 1
  }
}
```

### Get Product by ID
```http
GET /api/product/{id}
```

**Response:**
```json
{
  "success": true,
  "message": "Product retrieved successfully",
  "data": {
    "id": 1,
    "productName": "MacBook Pro M3",
    "unitPrice": 1999.99,
    "unitInStock": 25,
    "quantityPerUnit": "1 piece",
    "categoryId": 1,
    "description": "Professional laptop for creators",
    "imageUrl": "https://example.com/images/macbook-pro.jpg",
    "discount": 5,
    "isActive": true,
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-01T10:00:00"
  }
}
```

### Create Product (Admin)
```http
POST /api/product
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "productName": "iPhone 15 Pro",
  "unitPrice": 999.99,
  "unitInStock": 50,
  "quantityPerUnit": "1 piece",
  "categoryId": 1,
  "description": "Latest iPhone with advanced camera system",
  "imageUrl": "https://example.com/images/iphone15.jpg",
  "discount": 0
}
```

### Update Product (Admin)
```http
PUT /api/product/{id}
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "productName": "iPhone 15 Pro Max",
  "unitPrice": 1099.99,
  "unitInStock": 45,
  "quantityPerUnit": "1 piece",
  "categoryId": 1,
  "description": "Latest iPhone with advanced camera system and larger screen",
  "imageUrl": "https://example.com/images/iphone15-max.jpg",
  "discount": 5
}
```

### Delete Product (Admin)
```http
DELETE /api/product/{id}
Authorization: Bearer <admin-token>
```

## 🏷️ Categories API

### Get All Categories
```http
GET /api/category
```

**Response:**
```json
{
  "success": true,
  "message": "Categories retrieved successfully",
  "data": [
    {
      "id": 1,
      "categoryName": "Electronics",
      "description": "Electronic devices and gadgets",
      "imageUrl": "https://example.com/images/electronics.jpg",
      "isActive": true,
      "createdAt": "2024-01-01T10:00:00",
      "updatedAt": "2024-01-01T10:00:00"
    }
  ]
}
```

### Get Category by ID
```http
GET /api/category/{id}
```

### Create Category (Admin)
```http
POST /api/category
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "categoryName": "Books",
  "description": "Books and educational materials",
  "imageUrl": "https://example.com/images/books.jpg"
}
```

### Update Category (Admin)
```http
PUT /api/category/{id}
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "categoryName": "Educational Books",
  "description": "Educational books and learning materials",
  "imageUrl": "https://example.com/images/educational-books.jpg"
}
```

### Delete Category (Admin)
```http
DELETE /api/category/{id}
Authorization: Bearer <admin-token>
```

## 🛒 Orders API

### Get User Orders
```http
GET /api/order
Authorization: Bearer <token>
```

**Response:**
```json
{
  "success": true,
  "message": "Orders retrieved successfully",
  "data": [
    {
      "id": 1,
      "orderNumber": "ORD-2024-001",
      "userId": 1,
      "totalAmount": 1999.99,
      "status": "Pending",
      "shippingAddress": "123 Main St, New York, NY 10001",
      "billingAddress": "123 Main St, New York, NY 10001",
      "isActive": true,
      "createdAt": "2024-01-01T10:00:00",
      "updatedAt": "2024-01-01T10:00:00",
      "orderItems": [
        {
          "id": 1,
          "productId": 1,
          "quantity": 1,
          "unitPrice": 1999.99,
          "discount": 0.00
        }
      ]
    }
  ]
}
```

### Get Order by ID
```http
GET /api/order/{id}
Authorization: Bearer <token>
```

### Create Order
```http
POST /api/order
Authorization: Bearer <token>
Content-Type: application/json

{
  "shippingAddress": "123 Main St, New York, NY 10001",
  "billingAddress": "123 Main St, New York, NY 10001",
  "orderItems": [
    {
      "productId": 1,
      "quantity": 1
    },
    {
      "productId": 2,
      "quantity": 2
    }
  ]
}
```

### Update Order Status
```http
PUT /api/order/{id}/status
Authorization: Bearer <token>
Content-Type: application/json

{
  "status": "Shipped"
}
```

## 👤 Users API

### Get User Profile
```http
GET /api/user/profile
Authorization: Bearer <token>
```

**Response:**
```json
{
  "success": true,
  "message": "User profile retrieved successfully",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567890",
    "address": "123 Main St",
    "city": "New York",
    "postalCode": "10001",
    "isEmailVerified": false,
    "isActive": true,
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-01T10:00:00"
  }
}
```

### Update User Profile
```http
PUT /api/user/profile
Authorization: Bearer <token>
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Smith",
  "phoneNumber": "+1234567890",
  "address": "456 Oak Ave",
  "city": "Boston",
  "postalCode": "02101"
}
```

## 🏠 Addresses API

### Get User Addresses
```http
GET /api/address
Authorization: Bearer <token>
```

### Get Address by ID
```http
GET /api/address/{id}
Authorization: Bearer <token>
```

### Create Address
```http
POST /api/address
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "Home",
  "fullAddress": "123 Main Street, Apt 4B",
  "city": "Istanbul",
  "district": "Kadikoy",
  "postalCode": "34710",
  "country": "Turkey",
  "phoneNumber": "+1234567890",
  "isDefault": true
}
```

### Update Address
```http
PUT /api/address/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "Work",
  "fullAddress": "456 Business Avenue, Floor 10",
  "city": "Istanbul",
  "district": "Sisli",
  "postalCode": "34394",
  "country": "Turkey",
  "phoneNumber": "+1234567890",
  "isDefault": false
}
```

### Set Default Address
```http
PUT /api/address/{id}/default
Authorization: Bearer <token>
```

### Delete Address
```http
DELETE /api/address/{id}
Authorization: Bearer <token>
```

## 💳 Payment Methods API

### Get User Payment Methods
```http
GET /api/payment-method
Authorization: Bearer <token>
```

### Get Payment Method by ID
```http
GET /api/payment-method/{id}
Authorization: Bearer <token>
```

### Create Payment Method
```http
POST /api/payment-method
Authorization: Bearer <token>
Content-Type: application/json

{
  "type": "CreditCard",
  "cardHolderName": "John Doe",
  "cardNumber": "1234567890123456",
  "expiryMonth": 12,
  "expiryYear": 2025,
  "cvv": "123",
  "bankName": "Chase Bank",
  "isDefault": true
}
```

### Update Payment Method
```http
PUT /api/payment-method/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "type": "CreditCard",
  "cardHolderName": "John Smith",
  "cardNumber": "1234567890123456",
  "expiryMonth": 10,
  "expiryYear": 2026,
  "cvv": "123",
  "bankName": "Chase Bank",
  "isDefault": false
}
```

### Set Default Payment Method
```http
PUT /api/payment-method/{id}/default
Authorization: Bearer <token>
```

### Delete Payment Method
```http
DELETE /api/payment-method/{id}
Authorization: Bearer <token>
```

## ⭐ Reviews API

### Get Product Reviews
```http
GET /api/review/product/{productId}?page=0&size=10
```

### Get User Reviews
```http
GET /api/review/user
Authorization: Bearer <token>
```

### Create Review
```http
POST /api/review
Authorization: Bearer <token>
Content-Type: application/json

{
  "productId": 1,
  "rating": 5,
  "title": "Excellent product!",
  "comment": "This product exceeded my expectations. Highly recommended!"
}
```

### Update Review
```http
PUT /api/review/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "rating": 4,
  "title": "Good product",
  "comment": "Good quality but could be better."
}
```

### Delete Review
```http
DELETE /api/review/{id}
Authorization: Bearer <token>
```

## ❤️ Favorites API

### Get User Favorites
```http
GET /api/favorite
Authorization: Bearer <token>
```

### Add to Favorites
```http
POST /api/favorite
Authorization: Bearer <token>
Content-Type: application/json

{
  "productId": 1
}
```

### Remove from Favorites
```http
DELETE /api/favorite/{productId}
Authorization: Bearer <token>
```

## 🔔 Notifications API

### Get User Notifications
```http
GET /api/notification?page=0&size=10&isRead=false
Authorization: Bearer <token>
```

### Mark Notification as Read
```http
PUT /api/notification/{id}/read
Authorization: Bearer <token>
```

### Mark All Notifications as Read
```http
PUT /api/notification/read-all
Authorization: Bearer <token>
```

### Delete Notification
```http
DELETE /api/notification/{id}
Authorization: Bearer <token>
```

## 🔒 Security API

### Change Password
```http
PUT /api/security/change-password
Authorization: Bearer <token>
Content-Type: application/json

{
  "currentPassword": "oldpassword123",
  "newPassword": "newpassword123"
}
```

### Get Login History
```http
GET /api/security/login-history?page=0&size=10
Authorization: Bearer <token>
```

## 🏥 Health & Monitoring

### Application Health
```http
GET /actuator/health
```

**Response:**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "redis": {
      "status": "UP",
      "details": {
        "version": "7.0.0"
      }
    }
  }
}
```

### Prometheus Metrics
```http
GET /actuator/prometheus
```

### Application Info
```http
GET /actuator/info
```

## ❌ Error Handling

### Error Response Format
```json
{
  "success": false,
  "message": "Error description",
  "error": "ERROR_CODE",
  "timestamp": "2024-01-01T10:00:00Z",
  "path": "/api/product/999"
}
```

### Common Error Codes
- `400` - Bad Request
- `401` - Unauthorized
- `403` - Forbidden
- `404` - Not Found
- `409` - Conflict
- `422` - Unprocessable Entity
- `429` - Too Many Requests
- `500` - Internal Server Error

### Validation Errors
```json
{
  "success": false,
  "message": "Validation failed",
  "errors": [
    {
      "field": "email",
      "message": "Email is required"
    },
    {
      "field": "password",
      "message": "Password must be at least 8 characters"
    }
  ]
}
```

## 🚦 Rate Limiting

### Rate Limit Headers
- `X-Rate-Limit-Remaining`: Remaining requests
- `X-Rate-Limit-Reset`: Time until reset (seconds)
- `Retry-After`: Time to wait before retrying (seconds)

### Rate Limit Response
```json
{
  "error": "Rate limit exceeded. Please try again later."
}
```

## 🔑 Authentication Headers

All protected endpoints require the following header:
```http
Authorization: Bearer <jwt-token>
```

## 📝 Request/Response Examples

### Successful Response
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... }
}
```

### Error Response
```json
{
  "success": false,
  "message": "Operation failed",
  "error": "ERROR_CODE"
}
```

## 🌐 CORS Configuration

The API supports CORS for the following origins:
- `http://localhost:3000`
- `http://localhost:8080`
- Custom origins (configurable)

## 📊 API Versioning

Current API version: `v1`
- Base path: `/api`
- Version header: `API-Version: 1`

## 🔍 Testing

### Test Endpoints
```http
GET /api/test/health
GET /api/test/database
GET /api/test/redis
```

### Swagger UI
Access the interactive API documentation at:
```
http://localhost:8080/swagger-ui.html
```

## 📈 Performance

### Response Times
- Average response time: < 200ms
- 95th percentile: < 500ms
- 99th percentile: < 1000ms

### Rate Limits
- Default: 100 requests per minute
- Authenticated users: 1000 requests per minute
- Admin users: 5000 requests per minute
