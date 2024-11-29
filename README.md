# Spring Boot JWT Authentication Project

## Overview
This is a Spring Boot project implementing JWT (JSON Web Token) authentication with comprehensive security features, including role-based access control, custom authentication, and API documentation.

## Features
- JWT-based authentication and authorization
- Role-based access control
- Custom authentication and authorization filters
- Stateless session management
- PostgreSQL database integration
- Swagger/OpenAPI documentation
- CORS support
- BCrypt password encryption

## Prerequisites
- Java 8
- Maven
- Docker
- PostgreSQL

## Technologies Used
- Spring Boot
- Spring Security
- JWT
- PostgreSQL
- Swagger/OpenAPI
- Lombok
- Jackson

## Getting Started

### 1. Clone the Repository
```bash
git clone https://github.com/yogigan/Spring-Boot-JWT.git
cd Spring-Boot-JWT
```

### 2. Database Setup
This project uses Docker Compose for easy database management:

```bash
docker-compose up -d
```

#### Docker Compose Configuration
- **Database**: PostgreSQL 14.1
- **Port**: 5432
- **Default Credentials**:
  - Username: `postgres`
  - Password: `root`
  - Database: `db_jwt`

### 3. Run the Application
```bash
./mvnw clean install
./mvnw spring-boot:run
```

## Authentication Endpoints

### Default Credentials
- **Username**: `admin`
- **Password**: `toor`

### API Documentation
- **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`
- **API Docs**: `http://localhost:8080/v3/api-docs`

## Security Configuration
- Stateless session management
- CORS enabled
- JWT-based authentication
- Role-based access control
- Custom authentication and authorization filters

### Allowed Paths
- `/api/v1/login`
- `/api/v1/registration/**`
- `/api/v1/session/**`
- Swagger and API documentation paths

### Secured Admin Paths
- `/api/v1/user/**`
- `/api/v1/role/**`

## Customization

### Password Encryption
Uses BCrypt for secure password hashing.

### CORS Configuration
Configured to allow all origins, methods, and headers. Adjust in `WebSecurityConfiguration` as needed.

## Error Handling
- Custom access denied handler
- Detailed error responses for authentication failures

## Logging
Utilizes SLF4J for logging authentication and security events.

## Troubleshooting
- Ensure PostgreSQL is running
- Check database connection settings
- Verify JWT configuration
- Review security filter chains

## Future Improvements
- Implement refresh token mechanism
- Add more comprehensive unit and integration tests
- Implement more granular role-based permissions
