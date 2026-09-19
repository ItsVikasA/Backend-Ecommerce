# Auth App Backend

Spring Boot REST API for full-stack authentication application.

## Features

- User registration and login
- JWT authentication
- Password encryption (BCrypt)
- MySQL database integration
- Bean validation
- CORS configuration
- API retry handling
- Comprehensive error responses

## Tech Stack

- Spring Boot 3.3.5
- Spring Security
- Spring Data JPA
- MySQL 8
- JWT (JSON Web Tokens)
- Maven

## Environment Variables

### Required (No defaults)
- `JWT_SECRET`: Secret key for JWT signing (min 256 bits, base64 encoded)
- `DB_PASSWORD`: Database password

### Optional (Have defaults)
- `DB_HOST`: Database host (default: localhost)
- `DB_PORT`: Database port (default: 3306)
- `DB_NAME`: Database name (default: auth_app_db)
- `DB_USERNAME`: Database username (default: root)
- `JWT_EXPIRATION`: Token expiration in milliseconds (default: 3600000 = 1 hour)
- `CORS_ALLOWED_ORIGINS`: Allowed frontend origins (default: http://localhost:5173)

### Production-specific
- `DATABASE_URL`: Full JDBC URL for Aiven MySQL
- `DATABASE_USER`: Aiven database username
- `DATABASE_PASSWORD`: Aiven database password
- `PORT`: Server port (default: 8080)
- `SPRING_PROFILES_ACTIVE`: Set to `prod` for production
- `CORS_ORIGINS`: Production frontend URLs

## Local Development

### 1. Setup MySQL Database
```bash
# Create database
mysql -u root -p
CREATE DATABASE auth_app_db;
USE auth_app_db;

# Run schema
source database/schema.sql;
```

### 2. Configure Environment Variables
Create `.env` file in backend directory:
```
DB_PASSWORD=root
JWT_SECRET=your-local-secret-key-at-least-32-bytes-long
```

### 3. Run Application
```bash
mvn spring-boot:run
```

Server runs on http://localhost:8081

## Build

```bash
mvn clean package
```

JAR file created in `target/` directory.

## Run JAR

```bash
java -jar target/auth-app-0.0.1-SNAPSHOT.jar
```

## API Endpoints

### Authentication
- `POST /api/auth/signup` - Register new user
- `POST /api/auth/login` - Login user

### User Management (Protected)
- `GET /api/users/me` - Get current user profile
- `PUT /api/users/me` - Update current user profile
- `DELETE /api/users/me` - Delete current user account

## Request/Response Examples

### Signup
```json
POST /api/auth/signup
{
  "username": "john_doe",
  "email": "john@example.com",
  "phone": "+911234567890",
  "password": "SecurePass123!"
}

Response 201:
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "username": "john_doe"
}
```

### Login
```json
POST /api/auth/login
{
  "username": "john_doe",
  "password": "SecurePass123!"
}

Response 200:
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "username": "john_doe"
}
```

### Get Profile
```
GET /api/users/me
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...

Response 200:
{
  "username": "john_doe",
  "email": "john@example.com",
  "phone": "+911234567890",
  "createdAt": "2026-09-17T10:30:00"
}
```

## Testing

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=UserControllerTest

# Run with coverage
mvn test jacoco:report
```

## Database Schema

```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(15) NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## Deployment

### Render

1. Create Web Service
2. Connect GitHub repository
3. Configure:
   - **Build Command**: `./build.sh`
   - **Start Command**: `java -jar target/auth-app-0.0.1-SNAPSHOT.jar`
   - **Environment**: Set all required variables

See `DEPLOYMENT-GUIDE.md` for detailed instructions.

## Security

- Passwords hashed with BCrypt (strength 10)
- JWT tokens for stateless authentication
- CORS protection
- Input validation on all endpoints
- SQL injection prevention (JPA)
- XSS protection

## License

MIT
