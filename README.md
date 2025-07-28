# TalentRadar User Management Service

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.3-green.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7.2-red.svg)](https://redis.io/)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.12-orange.svg)](https://www.rabbitmq.com/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

A comprehensive user management microservice built with Spring Boot that provides authentication, authorization, session management, and user lifecycle management capabilities for the TalentRadar platform.

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Quick Start](#quick-start)
- [API Documentation](#api-documentation)
- [Development Setup](#development-setup)
- [Deployment](#deployment)
- [Configuration](#configuration)
- [Testing](#testing)
- [Contributing](#contributing)
- [License](#license)

## 🚀 Overview

The TalentRadar User Management Service is a microservice designed to handle all user-related operations in a distributed system architecture. It provides secure authentication, role-based authorization, session management, and user lifecycle management with event-driven communication capabilities.

### Key Capabilities

- **🔐 Secure Authentication**: JWT-based authentication with role-based access control
- **👥 User Management**: Complete user lifecycle from invitation to deactivation
- **📧 Email Notifications**: Automated email invitations and notifications
- **🔄 Session Management**: Redis-based session tracking and management
- **📡 Event-Driven**: RabbitMQ integration for service-to-service communication

## ✨ Features

### Authentication & Authorization

- JWT-based stateless authentication
- Role-based access control (ADMIN, MANAGER, DEVELOPER)
- Password encryption with BCrypt
- Custom authentication filters
- Session management and revocation

### User Management

- Invitation-based user registration
- Email notifications for user invitations
- User profile management
- Role assignment and management
- User status tracking (Active/Inactive)

### Session Management

- Redis-based session storage
- Session tracking with device information
- Session filtering and pagination
- Session revocation capabilities
- IP address and device tracking

### Event-Driven Communication

- RabbitMQ integration
- User creation/update events
- Service-to-service communication
- Event logging and monitoring

### Email Service

- SMTP-based email notifications
- HTML email templates
- Development console email service
- Profile-based email service selection

## 🛠️ Technology Stack

| Component          | Technology           | Version |
| ------------------ | -------------------- | ------- |
| **Framework**      | Spring Boot          | 3.5.3   |
| **Language**       | Java                 | 21      |
| **Database**       | PostgreSQL           | 15+     |
| **Cache/Session**  | Redis                | 7.2+    |
| **Message Broker** | RabbitMQ             | 3.12+   |
| **Security**       | JWT, Spring Security | Latest  |
| **Email**          | Jakarta Mail         | Latest  |
| **Build Tool**     | Maven                | 3.9+    |
| **Container**      | Docker               | Latest  |

## 🚀 Quick Start

### Prerequisites

- Java 21 or higher
- Maven 3.9 or higher
- PostgreSQL 15 or higher
- Redis 7.2 or higher
- RabbitMQ 3.12 or higher
- Docker (optional)

### Local Development Setup

1. **Clone the repository**

   ```bash
   git clone https://github.com/your-org/talentradar-user-management-service-rw.git
   cd talentradar-user-management-service-rw
   ```

2. **Set up environment variables**

   ```bash
   # Create .env file
   cp .env.example .env

   # Edit .env file with your configuration
   nano .env
   ```

3. **Start required services with Docker**

   ```bash
   # Start Redis
   docker-compose up -d redis

   # Start PostgreSQL (if not using local installation)
   docker run --name postgres -e POSTGRES_PASSWORD=password -e POSTGRES_DB=user_service -p 5432:5432 -d postgres:15

   # Start RabbitMQ (if not using local installation)
   docker run --name rabbitmq -p 5672:5672 -p 15672:15672 -d rabbitmq:3.12-management
   ```

4. **Run the application**

   ```bash
   # Using Maven
   mvn spring-boot:run

   # Or build and run
   mvn clean package
   java -jar target/user_service-0.0.1-SNAPSHOT.jar
   ```

5. **Access the application**
   - **Application**: http://localhost:8090

## 📚 API Documentation

### Base URL

```
http://localhost:8090/api/v1
```

### Authentication Endpoints

#### Login

```http
POST /auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

#### Invite User

```http
POST /auth/invite
Content-Type: application/json

{
  "email": "newuser@example.com",
  "roleId": "uuid-of-role"
}
```

#### Complete Registration

```http
PATCH /auth/complete-registration?token=registration-token
Content-Type: application/json

{
  "fullName": "John Doe",
  "password": "newpassword123",
  "confirmPassword": "newpassword123"
}
```

#### Validate Registration Token

```http
GET /auth/validate-registration-token?token=registration-token
```

### User Management Endpoints

#### Get Current User

```http
GET /users/me
X-User-Id: user-uuid
```

#### Get All Users (Admin Only)

```http
GET /users?page=0&size=10&roleId=uuid
Authorization: Bearer jwt-token
```

### Session Management Endpoints

#### Get Active Sessions

```http
GET /admin/sessions?page=0&size=10
Authorization: Bearer jwt-token
```

#### Revoke Session

```http
DELETE /admin/sessions/{sessionId}
Authorization: Bearer jwt-token
```

#### Filter Sessions

```http
GET /admin/sessions/filter?userId=uuid&date=YYYY-MM-DD
Authorization: Bearer jwt-token
```

### Role Management Endpoints

#### Get All Roles

```http
GET /roles
Authorization: Bearer jwt-token
```

## 🔧 Development Setup

### Environment Variables

Create a `.env` file in the root directory:

```env
# Database Configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/user_service
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=password

# Redis Configuration
SPRING_DATA_REDIS_HOSTNAME=localhost
SPRING_DATA_REDIS_PORT=6379

# RabbitMQ Configuration
SPRING_RABBITMQ_HOST=localhost
SPRING_RABBITMQ_PORT=5672
SPRING_RABBITMQ_USERNAME=guest
SPRING_RABBITMQ_PASSWORD=guest
SPRING_RABBITMQ_VIRTUAL_HOST=/

# JWT Configuration
JWT_SECRET=your-super-secret-jwt-key-here
SPRING_SECURITY_JWT_EXPIRATION_MS=3600000

# Email Configuration
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your-email@gmail.com
SPRING_MAIL_PASSWORD=your-app-password

# Application Configuration
SPRING_APPLICATION_NAME=user-service
SPRING_PROFILES_ACTIVE=dev
SERVER_PORT=8090

# Registration Token Configuration
APP_REGISTRATION_TOKEN_SECRET=your-registration-token-secret
APP_REGISTRATION_TOKEN_EXPIRATION_MS=600000
APP_BASE_URL=http://localhost:3000

# Email Notification Configuration
APP_NOTIFICATION_EMAIL_FROM=noreply@talentradar.com
APP_NOTIFICATION_EMAIL_SUBJECT_PREFIX=[TalentRadar]
```

### Database Setup

1. **Create PostgreSQL database**

   ```sql
   CREATE DATABASE user_service;
   CREATE USER user_service_user WITH PASSWORD 'password';
   GRANT ALL PRIVILEGES ON DATABASE user_service TO user_service_user;
   ```

2. **Run database migrations** (if using Flyway or similar)
   ```bash
   mvn flyway:migrate
   ```

### IDE Setup

#### IntelliJ IDEA

1. Import as Maven project
2. Set Java 21 as project SDK
3. Enable annotation processing for Lombok and MapStruct
4. Configure run configuration with environment variables

#### VS Code

1. Install Java Extension Pack
2. Install Spring Boot Extension Pack
3. Configure launch.json for debugging

## 🚀 Deployment

### Docker Deployment

1. **Build production image**

   ```bash
   docker build -t talentradar-user-service:latest .
   ```

2. **Run with environment variables**
   ```bash
   docker run -d \
     --name user-service \
     -p 8090:8090 \
     -e SPRING_PROFILES_ACTIVE=prod \
     -e SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/user_service \
     -e SPRING_DATA_REDIS_HOSTNAME=redis \
     -e SPRING_RABBITMQ_HOST=rabbitmq \
     talentradar-user-service:latest
   ```

### Production Considerations

- **Security**: Use strong JWT secrets and database passwords
- **Monitoring**: Integrate with your preferred monitoring tools
- **Logging**: Configure centralized logging
- **Backup**: Set up database backups
- **SSL/TLS**: Configure HTTPS for production
- **Load Balancing**: Use proper load balancer configuration

## ⚙️ Configuration

### Application Properties

The application uses Spring Boot's configuration system with the following key properties:

```yaml
spring:
  application:
    name: user-service
  profiles:
    active: prod
  session:
    store-type: redis
    timeout: 30m
  security:
    jwt:
      secret: ${JWT_SECRET}
      expirationMs: 3600000
  data:
    redis:
      host: ${SPRING_DATA_REDIS_HOSTNAME}
      port: ${SPRING_DATA_REDIS_PORT}
  rabbitmq:
    host: ${SPRING_RABBITMQ_HOST}
    port: ${SPRING_RABBITMQ_PORT}
    username: ${SPRING_RABBITMQ_USERNAME}
    password: ${SPRING_RABBITMQ_PASSWORD}
  mail:
    host: ${SPRING_MAIL_HOST}
    port: ${SPRING_MAIL_PORT}
    username: ${SPRING_MAIL_USERNAME}
    password: ${SPRING_MAIL_PASSWORD}
```

### Profiles

- **dev**: Development profile with console email service
- **prod**: Production profile with SMTP email service

## 🧪 Testing

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=UserServiceTest
```

### Test Structure

```
src/test/java/com/talentradar/user_service/
├── controller/
│   ├── AuthControllerTest.java
│   ├── UserControllerTest.java
│   ├── RoleControllerTest.java
│   └── SessionControllerTest.java
├── service/
│   ├── AuthenticationServiceTest.java
│   ├── UserServiceTest.java
│   ├── SessionServiceTest.java
│   └── EmailServiceTest.java
└── exception/
    ├── AppExceptionTest.java
    ├── InvalidTokenExceptionTest.java
    └── ResourceNotFoundExceptionTest.java
```

### Test Coverage

The project includes comprehensive test coverage for:

- ✅ Unit tests for service layer
- ✅ Integration tests for controllers
- ✅ Exception handling tests
- ✅ Security configuration tests

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. **Fork the repository**
2. **Create a feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. **Make your changes**
4. **Add tests for new functionality**
5. **Run the test suite**
   ```bash
   mvn test
   ```
6. **Commit your changes**
   ```bash
   git commit -m "feat: add new feature"
   ```
7. **Push to your branch**
   ```bash
   git push origin feature/your-feature-name
   ```
8. **Create a Pull Request**

### Code Style

- Follow Java coding conventions
- Use meaningful variable and method names
- Add comments for complex logic
- Ensure proper exception handling
- Write unit tests for new features

### Commit Message Format

We use [Conventional Commits](https://www.conventionalcommits.org/):

```
type(scope): description

[optional body]

[optional footer]
```

Types: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

For support and questions:

- **Email**: support@talentradar.com
- **Documentation**: [Technical Documentation](TECHNICAL_DOCUMENTATION.md)
- **Issues**: [GitHub Issues](https://github.com/your-org/talentradar-user-management-service-rw/issues)

## 🙏 Acknowledgments

- **Spring Boot Team** for the excellent framework
- **AmaliTech Training Academy** for the learning opportunity
- **Open Source Community** for the amazing tools and libraries

---

**Made with ❤️ by the TalentRadar Team**
