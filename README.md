# Resource Booking System

A secure RESTful Resource Booking System built with Spring Boot, Java 17+, Spring Security, JWT, JPA/Hibernate, and MySQL.

The system allows users to view resources and create/manage their reservations, while administrators have full access to manage resources and reservations.

## Features

- JWT-based authentication
- BCrypt password hashing
- Role-based access control with `ADMIN` and `USER`
- Stateless Spring Security
- Resource CRUD operations
- Reservation creation and management
- Reservation ownership enforcement
- Reservation statuses: `PENDING`, `CONFIRMED`, `CANCELLED`
- Resource-based reservation price calculation
- Decimal pricing using `BigDecimal`
- Reservation filtering by status, minimum price, and maximum price
- Pagination using `page` and `size`
- Optional sorting
- Request and business validation
- Centralized exception handling
- Standardized API responses using `ApiResponse` and `ApiError`
- MySQL database integration using JPA/Hibernate
- Swagger/OpenAPI documentation
- Seed users for testing
- Integration and security tests

## Technology Stack

| Technology | Purpose |
|---|---|
| Java 17+ | Programming language |
| Spring Boot | Backend framework |
| Spring Security | Authentication and authorization |
| JWT | Stateless authentication |
| Spring Data JPA | Database persistence |
| Hibernate | ORM |
| MySQL | Relational database |
| Maven | Build tool |
| Lombok | Boilerplate reduction |
| Swagger/OpenAPI | API documentation |
| JUnit | Testing |
| Mockito | Unit testing support |

## Architecture

The application follows a layered architecture:

```text
Client
   |
   v
Controller
   |
   v
Service
   |
   v
Repository
   |
   v
MySQL
```

Security flow:

```text
POST /api/auth/login
        |
        v
Authentication
        |
        v
JWT Token
        |
        v
Authorization: Bearer <token>
        |
        v
JwtAuthenticationFilter
        |
        v
Spring Security
        |
        v
Protected API
```

## Project Structure

```text
src/
├── main/
│   ├── java/
│   │   └── com/resourcebooking/
│   │       ├── common/
│   │       │   ├── constant/
│   │       │   │   └── ErrorCode.java
│   │       │   │
│   │       │   └── response/
│   │       │       ├── ApiError.java
│   │       │       └── ApiResponse.java
│   │       │
│   │       ├── config/
│   │       │   ├── DataInitializer.java
│   │       │   ├── JacksonConfig.java
│   │       │   ├── OpenApiConfig.java
│   │       │   └── SecurityConfig.java
│   │       │
│   │       ├── controller/
│   │       │   ├── AuthController.java
│   │       │   ├── ResourceController.java
│   │       │   └── ReservationController.java
│   │       │
│   │       ├── dto/
│   │       │   ├── auth/
│   │       │   ├── resource/
│   │       │   └── reservation/
│   │       │
│   │       ├── entity/
│   │       │   ├── User.java
│   │       │   ├── Resource.java
│   │       │   └── Reservation.java
│   │       │
│   │       ├── enums/
│   │       │   ├── Role.java
│   │       │   └── ReservationStatus.java
│   │       │
│   │       ├── exception/
│   │       │   ├── GlobalExceptionHandler.java
│   │       │   ├── ResourceNotFoundException.java
│   │       │   ├── DuplicateResourceException.java
│   │       │   └── ...
│   │       │
│   │       ├── repository/
│   │       │   ├── UserRepository.java
│   │       │   ├── ResourceRepository.java
│   │       │   └── ReservationRepository.java
│   │       │
│   │       ├── security/
│   │       │   ├── JwtAuthenticationFilter.java
│   │       │   ├── JwtService.java
│   │       │   └── ...
│   │       │
│   │       ├── service/
│   │       │   ├── AuthService.java
│   │       │   ├── ResourceService.java
│   │       │   └── ReservationService.java
│   │       │
│   │       ├── specification/
│   │       │   └── ReservationSpecification.java
│   │       │
│   │       └── ResourceBookingSystemApplication.java
│   │
│   └── resources/
│       └── application.yaml
│
└── test/
    └── java/
        └── com/resourcebooking/
            ├── controller/
            │   ├── AuthIntegrationTest.java
            │   ├── ResourceIntegrationTest.java
            │   └── ReservationIntegrationTest.java
            │
            └── security/
                └── SecurityIntegrationTest.java


## Prerequisites

Install:

- Java 17 or later
- Maven
- MySQL 8.x
- Git
- IntelliJ IDEA, Eclipse, or another Java IDE

Verify Java:

```bash
java -version
```

Verify Maven:

```bash
mvn -version
```

## Database Setup

Create the MySQL database:

```sql
CREATE DATABASE resource_booking_db;
```

The application uses JPA/Hibernate to create/update the required tables.

Main tables:

```text
users
resources
reservations
```

## Configuration

Configure your database and JWT properties in `applicatio.yaml`.

Recommended environment variables:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
JWT_SECRET
```

Example:

```text
DB_URL=jdbc:mysql://localhost:3306/resource_booking_db
DB_USERNAME=root
DB_PASSWORD=your_mysql_password
JWT_SECRET=your_secure_jwt_secret
```

Do not commit real database passwords or JWT secrets to Git.

If your project currently uses a different JWT property name, keep the property name expected by your `JwtService`/security configuration.

## Running the Application

Clone the repository:

```bash
git clone <repository-url>
```

Navigate to the project:

```bash
cd resource-booking-system
```

Build the project:

```bash
mvn clean install
```

Run the application:

```bash
mvn spring-boot:run
```

The application runs on:

```text
http://localhost:8080
```

## Seed Users

Test users are created by `DataInitializer`.

Use the credentials configured in your `DataInitializer.java`.

Typical example:

| Role | Username | Password |
|---|---|---|
| ADMIN | admin | Admin@123 |
| USER | user | User@123 |

Passwords are stored using BCrypt hashing.

> Always use the actual credentials configured in `DataInitializer.java`.

## Authentication

Login endpoint:

```http
POST /api/auth/login
```

Example request:

```json
{
  "username": "user",
  "password": "User@123"
}
```

The successful response contains a JWT token.

Use the token for protected APIs:

```http
Authorization: Bearer <JWT_TOKEN>
```

## Authorization

### USER

A USER can:

- Login
- View resources
- Create reservations
- View their own reservations
- Access only their own reservation data

A USER cannot:

- Create resources
- Update resources
- Delete resources
- Access another user's reservations
- Perform ADMIN-only operations

### ADMIN

An ADMIN can:

- Login
- Create resources
- View resources
- Update resources
- Delete resources
- View all reservations
- Manage reservations
- Update reservation status

## Resource Management

Resources contain:

- Name
- Description
- Location
- Capacity
- Price
- Availability
- Created timestamp
- Updated timestamp

Example:

```json
{
  "name": "Conference Room A",
  "description": "Large conference room",
  "location": "Pune",
  "capacity": 20,
  "price": 500.00,
  "available": true
}
```

Resource names are protected by a database unique constraint and service-level duplicate validation.

## Reservation Management

A reservation request contains:

```json
{
  "resourceId": 1,
  "startTime": "2026-09-10T10:00:00",
  "endTime": "2026-09-10T12:00:00"
}
```

The client does not provide:

```text
userId
price
status
```

The backend determines these values.

### Reservation Ownership

The authenticated user is taken from the JWT/Spring Security authentication context.

```text
JWT
 |
 v
Authenticated User
 |
 v
Reservation.user
```

This prevents a USER from creating a reservation on behalf of another user.

A USER can access only their own reservations.

An ADMIN can access all reservations.

## Reservation Pricing

The reservation price is not accepted from the client.

The backend calculates the price using the resource's configured price and reservation duration.

Example:

```text
Resource price = 500.00
Duration       = 2 hours

Reservation price = calculated by backend
```

Reservation prices are stored using `BigDecimal` with decimal database precision.

## Reservation Status

Supported statuses:

```text
PENDING
CONFIRMED
CANCELLED
```

New reservations are created with the default status:

```text
PENDING
```

Status management is controlled by the backend and authorized users.

## Reservation Validation

The application validates:

- Resource ID is required
- Start time is required
- End time is required
- Start time must be in the future
- End time must be in the future
- End time must be after start time
- Resource must exist
- Resource must be available
- Reservation business rules must be satisfied

Invalid requests return appropriate HTTP status codes and standardized error responses.

## Reservation Filtering

Reservation results support filtering by status:

```http
GET /api/reservations?status=PENDING
```

Minimum price:

```http
GET /api/reservations?minPrice=100
```

Maximum price:

```http
GET /api/reservations?maxPrice=1000
```

Combined filters:

```http
GET /api/reservations?status=PENDING&minPrice=100&maxPrice=1000
```

## Pagination

Reservation results support pagination using `page` and `size`.

Example:

```http
GET /api/reservations?page=0&size=10
```

Where:

```text
page = page number
size = number of records per page
```

## Sorting

Sorting is optional.

Example:

```http
GET /api/reservations?page=0&size=10&sort=price,desc
```

Another example:

```http
GET /api/reservations?page=0&size=10&sort=startTime,asc
```

Use the actual supported sort fields exposed by the application.

## Standard API Response

Successful API responses use `ApiResponse<T>`.

Example:

```json
{
  "success": true,
  "message": "Login successful",
  "data": {},
  "error": null,
  "timestamp": "2026-08-29T10:00:00Z"
}
```

Error responses use `ApiError`.

Example:

```json
{
  "success": false,
  "message": "Resource not found",
  "data": null,
  "error": {
    "code": "RESOURCE_NOT_FOUND",
    "details": null
  },
  "timestamp": "2026-08-29T10:00:00Z"
}
```

## HTTP Status Codes

| Status | Meaning |
|---|---|
| 200 OK | Successful request |
| 201 CREATED | Resource successfully created |
| 400 BAD_REQUEST | Invalid request or validation failure |
| 401 UNAUTHORIZED | Authentication required or failed |
| 403 FORBIDDEN | Authenticated user does not have permission |
| 404 NOT_FOUND | Requested resource does not exist |
| 409 CONFLICT | Duplicate/conflicting resource |
| 500 INTERNAL_SERVER_ERROR | Unexpected server error |

## Exception Handling

The application uses centralized exception handling through:

```text
GlobalExceptionHandler
```

Application exceptions are converted into standardized `ApiResponse` and `ApiError` responses.

Examples include:

- Resource not found
- Duplicate resource
- Validation errors
- Authentication failures
- Authorization failures
- Invalid reservation requests

## Swagger / OpenAPI

Swagger UI is available at:

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

Swagger can be used to explore and test the REST APIs.

For protected endpoints, authenticate using the JWT token through Swagger's authorization mechanism.

## Testing

The project contains integration and security tests covering:

- Authentication
- JWT security
- Resource APIs
- Reservation APIs
- Role-based authorization
- Reservation ownership
- Protected endpoints

Run all tests:

```bash
mvn clean test
```

Run complete Maven verification:

```bash
mvn clean verify
```

Build the application:

```bash
mvn clean package
```

Expected result:

```text
BUILD SUCCESS
```

## Recommended API Test Flow

```text
1. Start MySQL
        |
        v
2. Start Spring Boot application
        |
        v
3. Login as USER
        |
        v
4. Copy JWT token
        |
        v
5. View resources
        |
        v
6. Create reservation
        |
        v
7. View own reservations
        |
        v
8. Login as ADMIN
        |
        v
9. Create/update/delete resources
        |
        v
10. View all reservations
        |
        v
11. Manage reservation status
```

## Security

The application implements:

- JWT authentication
- BCrypt password hashing
- Stateless authentication
- Role-based authorization
- Protected REST endpoints
- JWT-based user identification
- Reservation ownership validation
- ADMIN/USER access restrictions
- Server-side reservation price calculation
- No client-controlled reservation ownership
- No client-controlled reservation status during creation

## Git Security

Do not commit sensitive or generated files.

Recommended `.gitignore` entries:

```gitignore
target/
.idea/
*.iml
.env
*.log
application-local.properties
```

Do not commit:

- Database passwords
- JWT secrets
- `.env` files containing secrets
- Local configuration containing credentials

## Future Enhancements

Possible future improvements:

- Refresh tokens
- Email notifications
- Reservation cancellation policies
- Resource categories
- Audit logging
- Redis caching
- Rate limiting
- Docker support
- CI/CD pipeline
- Flyway/Liquibase database migrations
- Production monitoring and centralized logging

## Author

Resource Booking System

Backend developed using:

- Java
- Spring Boot
- Spring Security
- JWT
- Spring Data JPA
- Hibernate
- MySQL
