# TalentRadar User Management Service - Technical Documentation

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Technology Stack](#technology-stack)
4. [Component Architecture](#component-architecture)
5. [Data Models](#data-models)
6. [API Endpoints](#api-endpoints)
7. [Authentication & Security](#authentication--security)
8. [Session Management](#session-management)
9. [Event-Driven Architecture](#event-driven-architecture)
10. [RabbitMQ Integration](#rabbitmq-integration)
11. [Email Service](#email-service)
12. [Database Design](#database-design)
13. [Deployment](#deployment)
14. [Configuration](#configuration)
15. [Testing Strategy](#testing-strategy)

## Overview

The TalentRadar User Management Service is a microservice built with Spring Boot 3.5.3 that provides comprehensive user management capabilities including authentication, authorization, session management, and user lifecycle management. The service is designed to work within a microservices architecture and integrates with external services through message queues and REST APIs.

### Key Features

- **User Authentication & Authorization**: JWT-based authentication with role-based access control
- **User Registration Flow**: Invitation-based registration with email notifications
- **Session Management**: Redis-based session storage with tracking capabilities
- **Event-Driven Communication**: RabbitMQ integration for service-to-service communication
- **Email Notifications**: SMTP-based email service for user invitations
- **Service Discovery**: Eureka client integration for microservices architecture

## Architecture

### High-Level Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           Client Applications                              │
└─────────────────────┬───────────────────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                        API Gateway / Load Balancer                        │
└─────────────────────┬───────────────────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                    TalentRadar User Management Service                    │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────────────────┐ │
│  │   Controllers   │  │   Services      │  │   Security & Filters        │ │
│  │                 │  │                 │  │                             │ │
│  │ • AuthController│  │ • UserService   │  │ • JwtUtils                  │ │
│  │ • UserController│  │ • AuthService   │  │ • HeaderAuthFilter          │ │
│  │ • RoleController│  │ • SessionService│  │ • CustomAuthEntryPoint      │ │
│  │ • SessionController│ • EmailService  │  │ • SecurityConfig            │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────────────────┘ │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────────────────┐ │
│  │   Repositories  │  │   Models        │  │   Configuration             │ │
│  │                 │  │                 │  │                             │ │
│  │ • UserRepository│  │ • User          │  │ • SecurityConfig            │ │
│  │ • RoleRepository│  │ • Role          │  │ • RabbitConfig              │ │
│  │ • SessionRepository│ • Session       │  │ • RedisConfig               │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────────────────┘ │
└─────────────────────┬───────────────────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              External Services                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────────┐ │
│  │ PostgreSQL  │  │    Redis    │  │  RabbitMQ   │  │  SMTP Server    │ │
│  │   Database  │  │   Sessions  │  │   Events    │  │   Emails        │ │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Component Interaction Flow

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Client    │───▶│  Controller │───▶│   Service   │───▶│ Repository  │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
       │                   │                   │                   │
       │                   ▼                   ▼                   ▼
       │            ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
       │            │   Security  │    │   Business  │    │   Database  │
       │            │   Filters   │    │   Logic     │    │   Layer     │
       │            └─────────────┘    └─────────────┘    └─────────────┘
       │                   │                   │                   │
       ▼                   ▼                   ▼                   ▼
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Response  │◀───│   Response  │◀───│   Response  │◀───│   Response  │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
```

## Technology Stack

### Core Framework

- **Spring Boot**: 3.5.3
- **Java**: 21
- **Maven**: Build tool and dependency management

### Database & Storage

- **PostgreSQL**: Primary database for user data
- **Redis**: Session storage and caching
- **Spring Data JPA**: ORM framework

### Security

- **Spring Security**: Authentication and authorization
- **JWT (JSON Web Tokens)**: Stateless authentication
- **BCrypt**: Password hashing

### Messaging & Communication

- **RabbitMQ**: Message broker for event-driven communication
- **Spring AMQP**: RabbitMQ integration

### Email Service

- **Jakarta Mail**: SMTP email functionality
- **Spring Boot Mail**: Email configuration

### Documentation & Testing

- **JUnit 5**: Unit testing
- **Mockito**: Mocking framework

### Microservices

- **Spring Cloud**: Service discovery (Eureka client)
- **Spring Cloud Config**: Configuration management

## Component Architecture

### 1. Controllers Layer

#### AuthController

- **Purpose**: Handles authentication and registration endpoints
- **Key Endpoints**:
  - `POST /api/v1/auth/login`: User login
  - `POST /api/v1/auth/invite`: Initiate user registration
  - `PATCH /api/v1/auth/complete-registration`: Complete registration
  - `GET /api/v1/auth/validate-registration-token`: Validate registration token

#### UserController

- **Purpose**: User management operations
- **Key Endpoints**:
  - `GET /api/v1/users/me`: Get current user profile
  - `GET /api/v1/users`: Get all users (admin only)

#### SessionController

- **Purpose**: Session management operations
- **Key Endpoints**:
  - `GET /api/v1/admin/sessions`: Get active sessions
  - `DELETE /api/v1/admin/sessions/{sessionId}`: Revoke session
  - `GET /api/v1/admin/sessions/filter`: Filter sessions

#### RoleController

- **Purpose**: Role management operations
- **Key Endpoints**:
  - `GET /api/v1/roles`: Get all roles

### 2. Services Layer

#### AuthenticationService

- **Purpose**: Handles user authentication logic
- **Key Methods**:
  - `login(LoginRequestDto)`: Authenticate user and generate JWT
  - **Dependencies**: AuthenticationManager, JwtUtils

#### UserService

- **Purpose**: Core user management business logic
- **Key Methods**:
  - `initiateRegistration(InviteUserRequest)`: Start registration process
  - `completeRegistration(String token, CompleteRegistrationRequest)`: Complete registration
  - `getMe(UUID userId)`: Get user profile
  - `getAllUsers(int page, int size, UUID roleId)`: Get paginated users
  - `validateRegistrationToken(String token)`: Validate registration token
- **Dependencies**: UserRepository, RoleRepository, PasswordEncoder, EmailService, RabbitTemplate

#### SessionService

- **Purpose**: Session management and tracking
- **Key Methods**:
  - `getActiveSessions(Pageable)`: Get active sessions
  - `revokeSessionById(String sessionId)`: Revoke specific session
  - `filterSessions(UUID userId, String dateString, Pageable)`: Filter sessions
- **Dependencies**: UserSessionRepository, SessionMapper, SessionRepository

#### EmailService (Interface)

- **Implementations**:
  - `SmtpEmailService`: Production email service using SMTP
  - `ConsoleEmailService`: Development email service (console output)

### 3. Security Layer

#### JwtUtils

- **Purpose**: JWT token generation and validation
- **Key Methods**:
  - `generateJwtTokenFromUserId(CustomUserDetails)`: Generate JWT token
  - `key()`: Get signing key for JWT

#### HeaderAuthenticationFilter

- **Purpose**: Custom authentication filter using headers
- **Key Headers**:
  - `X-User-Id`: User ID
  - `X-User-Email`: User email
  - `X-User-FullName`: User full name
  - `X-User-Role`: User role

#### SecurityConfig

- **Purpose**: Spring Security configuration
- **Key Features**:
  - Stateless session management
  - JWT-based authentication
  - Role-based authorization
  - Custom authentication provider

### 4. Data Layer

#### Repositories

- **UserRepository**: User data access
- **RoleRepository**: Role data access
- **UserSessionRepository**: Session data access

#### Models

- **User**: Core user entity with relationships
- **Role**: Role entity with user relationship
- **Session**: Session tracking entity

## Data Models

### User Entity

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String username;
    private String fullName;
    private String email;

    @ManyToOne
    @JoinColumn(name = "role", nullable = false)
    private Role role;

    private String password;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Session> sessions;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### Role Entity

```java
@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "role_name", nullable = false, unique = true)
    private String roleName;

    @OneToMany(mappedBy = "role")
    private List<User> users;

    private LocalDateTime created_at;
}
```

### Session Entity

```java
@Entity
@Table(name = "sessions")
public class Session {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String sessionId;
    private String ipAddress;
    private String deviceInfo;
    private LocalDateTime createdAt;
    private boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
```

## API Endpoints

### Authentication Endpoints

```
POST /api/v1/auth/login
POST /api/v1/auth/invite
PATCH /api/v1/auth/complete-registration
GET /api/v1/auth/validate-registration-token
```

### User Management Endpoints

```
GET /api/v1/users/me
GET /api/v1/users?page=0&size=10&roleId=uuid
```

### Session Management Endpoints

```
GET /api/v1/admin/sessions?page=0&size=10
DELETE /api/v1/admin/sessions/{sessionId}
GET /api/v1/admin/sessions/filter?userId=uuid&date=YYYY-MM-DD
```

### Role Management Endpoints

```
GET /api/v1/roles
```

## Authentication & Security

### JWT Authentication Flow

```
1. User submits credentials
2. AuthenticationService validates credentials
3. JwtUtils generates JWT token
4. Token returned to client
5. Client includes token in subsequent requests
6. HeaderAuthenticationFilter validates token
7. Security context established
```

### Security Configuration

- **Stateless Sessions**: No server-side session storage
- **JWT Tokens**: Secure token-based authentication
- **Role-Based Access Control**: `@PreAuthorize` annotations
- **Password Encryption**: BCrypt password hashing
- **CORS Configuration**: Disabled for API-only service

### Authentication Headers

The service uses custom headers for authentication:

- `X-User-Id`: User identifier
- `X-User-Email`: User email address
- `X-User-FullName`: User's full name
- `X-User-Role`: User's role

## Session Management

### Session Storage

- **Primary Storage**: Redis for session data
- **Database Storage**: PostgreSQL for session metadata
- **Session Timeout**: 30 minutes (configurable)

### Session Tracking

- **IP Address**: Track user's IP address
- **Device Information**: User agent and device details
- **Active Status**: Track session activity
- **Creation Time**: Session creation timestamp

### Session Operations

- **Create**: Automatic session creation on login
- **List**: Get active sessions with pagination
- **Filter**: Filter by user ID and date
- **Revoke**: Manually revoke sessions

## Event-Driven Architecture

### RabbitMQ Integration

The service uses RabbitMQ for event-driven communication:

#### Queues

- `user-created-event`: User creation events
- `user-updated-event`: User update events

#### Event Types

```java
public enum EventType {
    USER_CREATED,
    USER_UPDATED,
    USER_DELETED
}

public enum EventRole {
    DEVELOPER,
    MANAGER
}
```

#### Event Flow

```
1. User creation/update triggers event
2. UserService creates UserCreatedEvent
3. RabbitTemplate sends event to queue
4. Other services consume events
5. Event contains user details and metadata
```

### Event Structure

```java
public class UserCreatedEvent {
    private EventType eventType;
    private UUID userId;
    private UUID managerId; // For developers
    private String fullName;
    private String username;
    private String email;
    private EventRole role;
    private LocalDateTime timestamp;
    private String eventId;
    private String source;
}
```

## RabbitMQ Integration

### RabbitMQ Architecture Overview

The TalentRadar User Management Service integrates RabbitMQ as its primary message broker for event-driven communication between microservices. This enables loose coupling and asynchronous communication patterns.

### RabbitMQ Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    RabbitMQ Message Broker                                │
│                                                                             │
│  ┌─────────────────┐                    ┌─────────────────┐                │
│  │   Exchange      │                    │   Exchange      │                │
│  │  (Direct)       │                    │  (Direct)       │                │
│  └─────────────────┘                    └─────────────────┘                │
│           │                                        │                       │
│           ▼                                        ▼                       │
│  ┌─────────────────┐                    ┌─────────────────┐                │
│  │   Queue         │                    │   Queue         │                │
│  │ user-created-   │                    │ user-updated-   │                │
│  │ event           │                    │ event           │                │
│  └─────────────────┘                    └─────────────────┘                │
│           │                                        │                       │
│           ▼                                        ▼                       │
│  ┌─────────────────┐                    ┌─────────────────┐                │
│  │   Consumer      │                    │   Consumer      │                │
│  │ (Other Services)│                    │ (Other Services)│                │
│  └─────────────────┘                    └─────────────────┘                │
└─────────────────────────────────────────────────────────────────────────────┘
                                    ▲
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                User Management Service                                     │
│                                                                             │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────────────┐ │
│  │   UserService   │───▶│  RabbitTemplate │───▶│   Event Publishing      │ │
│  │                 │    │                 │    │                         │ │
│  │ • User Creation │    │ • Connection    │    │ • UserCreatedEvent      │ │
│  │ • User Update   │    │ • Channel Mgmt  │    │ • UserUpdatedEvent      │ │
│  │ • Event Trigger │    │ • Message Send  │    │ • JSON Serialization    │ │
│  └─────────────────┘    └─────────────────┘    └─────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
```

### RabbitMQ Configuration

#### Queue Definitions

```java
@Configuration
public class RabbitConfig {
    public static final String QUEUE_USER_CREATED = "user-created-event";
    public static final String QUEUE_USER_UPDATED = "user-updated-event";

    @Bean
    public Queue userCreatedQueue() {
        return new Queue(QUEUE_USER_CREATED, true); // durable queue
    }

    @Bean
    public Queue userUpdatedQueue() {
        return new Queue(QUEUE_USER_UPDATED, true); // durable queue
    }
}
```

#### Message Converter Configuration

```java
@Bean
public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
}

@Bean
public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
        Jackson2JsonMessageConverter messageConverter) {
    RabbitTemplate template = new RabbitTemplate(connectionFactory);
    template.setMessageConverter(messageConverter);
    return template;
}
```

### Event Publishing Patterns

#### 1. User Creation Events

**Developer User Creation:**

```java
// When a DEVELOPER role user is created
UserCreatedEvent userCreatedEvent = new UserCreatedEvent().builder()
    .eventType(EventType.USER_CREATED)
    .userId(savedUser.getId())
    .managerId(managerUsers.get(0).getId()) // Assign to first available manager
    .fullName(savedUser.getFullName())
    .username(savedUser.getUsername())
    .email(savedUser.getEmail())
    .role(EventRole.DEVELOPER)
    .timestamp(LocalDateTime.now())
    .eventId(UUID.randomUUID().toString())
    .source("user-service")
    .build();

rabbitTemplate.convertAndSend(RabbitConfig.QUEUE_USER_CREATED, userCreatedEvent);
```

**Manager User Creation:**

```java
// When a MANAGER role user is created
UserCreatedEvent userCreatedEvent = new UserCreatedEvent().builder()
    .eventType(EventType.USER_CREATED)
    .userId(savedUser.getId())
    .fullName(savedUser.getFullName())
    .username(savedUser.getUsername())
    .email(savedUser.getEmail())
    .role(EventRole.MANAGER)
    .timestamp(LocalDateTime.now())
    .eventId(UUID.randomUUID().toString())
    .source("user-service")
    .build();

rabbitTemplate.convertAndSend(RabbitConfig.QUEUE_USER_CREATED, userCreatedEvent);
```

#### 2. User Update Events

**User Registration Completion:**

```java
private void fireEventWhenUserUpdated(User user) {
    UserCreatedEvent userUpdatedEvent = new UserCreatedEvent().builder()
        .eventType(EventType.USER_UPDATED)
        .userId(user.getId())
        .fullName(user.getFullName())
        .username(user.getUsername())
        .eventId(UUID.randomUUID().toString())
        .timestamp(LocalDateTime.now())
        .source("user-service")
        .build();

    rabbitTemplate.convertAndSend(RabbitConfig.QUEUE_USER_UPDATED, userUpdatedEvent);
    log.info("User updated event sent for user: {}", user.getEmail());
}
```

### Event Data Structure

#### UserCreatedEvent Class

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreatedEvent implements Serializable {
    private EventType eventType; // USER_CREATED, USER_UPDATED, USER_DELETED
    private UUID userId;
    private UUID managerId; // Only for DEVELOPER role
    private String fullName;
    private String username;
    private String email;
    private EventRole role; // DEVELOPER, MANAGER
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    private String eventId;
    private String source;
}
```

#### Event Types

```java
public enum EventType {
    USER_CREATED, USER_UPDATED, USER_DELETED
}

public enum EventRole {
    DEVELOPER, MANAGER
}
```

### Event Flow Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   User Action   │───▶│  UserService    │───▶│  Event Creation │
│                 │    │                 │    │                 │
│ • Registration  │    │ • Business Logic│    │ • UserCreatedEvent│
│ • Profile Update│    │ • Validation    │    │ • UserUpdatedEvent│
│ • Role Change   │    │ • Database Save │    │ • JSON Serialize │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │
                              ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  RabbitTemplate │───▶│  RabbitMQ      │───▶│  Other Services │
│                 │    │                 │    │                 │
│ • Send Message  │    │ • Queue Storage│    │ • Event Consumer│
│ • JSON Convert  │    │ • Message Route│    │ • Business Logic│
│ • Error Handle  │    │ • Durability   │    │ • Database Update│
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### Event Publishing Triggers

#### 1. User Registration Initiation

- **Trigger**: Admin invites new user
- **Event**: `USER_CREATED`
- **Queue**: `user-created-event`
- **Data**: User details, role, manager assignment (for developers)

#### 2. User Registration Completion

- **Trigger**: User completes registration with password
- **Event**: `USER_UPDATED`
- **Queue**: `user-updated-event`
- **Data**: Updated user details, activation status

### Message Reliability Features

#### 1. Durable Queues

```java
// Queues are configured as durable to survive broker restarts
return new Queue(QUEUE_USER_CREATED, true); // durable = true
```

#### 2. JSON Message Serialization

```java
// Messages are serialized as JSON for cross-platform compatibility
@Bean
public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
}
```

#### 3. Error Handling

```java
// Logging for successful event publishing
log.info("Developer created event sent for user: {}", savedUser.getEmail());
log.info("Manager created event sent for user: {}", savedUser.getEmail());
log.info("User updated event sent for user: {}", user.getEmail());
```

### Integration Points

#### 1. Service Discovery

- RabbitMQ integrates with Spring Cloud for service discovery
- Connection factory uses service discovery for broker endpoints

#### 2. Configuration Management

- RabbitMQ configuration managed through Spring Cloud Config
- Environment-specific connection parameters

#### 3. Monitoring and Logging

- Event publishing logged for audit trails
- Connection status monitored
- Message delivery confirmation

### Consumer Services (External)

The following services are expected to consume these events:

#### 1. Notification Service

- **Consumes**: `user-created-event`, `user-updated-event`
- **Purpose**: Send welcome emails, profile update notifications
- **Actions**: Email notifications, SMS alerts

#### 2. Analytics Service

- **Consumes**: `user-created-event`, `user-updated-event`
- **Purpose**: Track user metrics, generate reports
- **Actions**: Update analytics dashboards, generate insights

#### 3. Project Management Service

- **Consumes**: `user-created-event` (DEVELOPER role)
- **Purpose**: Assign developers to projects
- **Actions**: Create project assignments, update team structures

#### 4. Audit Service

- **Consumes**: All events
- **Purpose**: Maintain audit logs
- **Actions**: Store audit records, compliance reporting

## Email Service

### Email Service Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   UserService   │───▶│  EmailService   │───▶│  SMTP Server    │
│                 │    │   (Interface)   │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │
                              ▼
                       ┌─────────────────┐
                       │ ConsoleEmailService │
                       │   (Development) │
                       └─────────────────┘
```

### Email Types

- **Registration Invitation**: HTML email with registration link
- **Token Expiration**: 10 minutes for registration tokens

### Email Implementation

- **Production**: `SmtpEmailService` with SMTP configuration
- **Development**: `ConsoleEmailService` with console output
- **Profile-based**: Automatic selection based on Spring profile

## Database Design

### Database Schema

```sql
-- Users table
CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(255) UNIQUE,
    full_name VARCHAR(255),
    email VARCHAR(255) NOT NULL UNIQUE,
    role UUID NOT NULL REFERENCES roles(id),
    password VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Roles table
CREATE TABLE roles (
    id UUID PRIMARY KEY,
    role_name VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP
);

-- Sessions table
CREATE TABLE sessions (
    id UUID PRIMARY KEY,
    session_id VARCHAR(255),
    ip_address VARCHAR(255),
    device_info TEXT,
    created_at TIMESTAMP,
    is_active BOOLEAN,
    user_id UUID REFERENCES users(id)
);
```

### Database Relationships

- **User → Role**: Many-to-One (Each user has one role)
- **Role → User**: One-to-Many (Each role can have multiple users)
- **User → Session**: One-to-Many (Each user can have multiple sessions)
- **Session → User**: Many-to-One (Each session belongs to one user)

## Deployment

### Docker Support

The service includes Docker configuration:

- **Dockerfile**: Multi-stage build for optimized container
- **docker-compose.yml**: Local development setup

### Environment Configuration

- **Spring Profiles**: `prod` and development profiles
- **External Configuration**: Spring Cloud Config integration
- **Environment Variables**: Database, Redis, RabbitMQ, SMTP configuration

### Service Discovery

- **Eureka Client**: Integration with Netflix Eureka
- **Service Registration**: Automatic service registration
- **Health Checks**: Built-in health check endpoints (if implemented)

## Configuration

### Application Properties

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

### Required Environment Variables

- `JWT_SECRET`: JWT signing secret
- `SPRING_DATA_REDIS_HOSTNAME`: Redis host
- `SPRING_DATA_REDIS_PORT`: Redis port
- `SPRING_RABBITMQ_HOST`: RabbitMQ host
- `SPRING_RABBITMQ_PORT`: RabbitMQ port
- `SPRING_RABBITMQ_USERNAME`: RabbitMQ username
- `SPRING_RABBITMQ_PASSWORD`: RabbitMQ password
- `SPRING_MAIL_HOST`: SMTP host
- `SPRING_MAIL_PORT`: SMTP port
- `SPRING_MAIL_USERNAME`: SMTP username
- `SPRING_MAIL_PASSWORD`: SMTP password

## Testing Strategy

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

### Testing Technologies

- **JUnit 5**: Unit testing framework
- **Mockito**: Mocking framework
- **Spring Boot Test**: Integration testing

### Test Coverage

- **Unit Tests**: Service layer business logic
- **Integration Tests**: Controller endpoints
- **Exception Tests**: Custom exception handling
- **Security Tests**: Authentication and authorization

## Key Features Summary

### Security Features

- ✅ JWT-based authentication
- ✅ Role-based access control
- ✅ Password encryption (BCrypt)
- ✅ Stateless session management
- ✅ Custom authentication filters

### User Management Features

- ✅ User registration with email invitations
- ✅ User profile management
- ✅ Role-based user categorization
- ✅ User status tracking (Active/Inactive)

### Session Management Features

- ✅ Redis-based session storage
- ✅ Session tracking and monitoring
- ✅ Session revocation capabilities
- ✅ Session filtering and pagination

### Integration Features

- ✅ RabbitMQ event publishing
- ✅ SMTP email notifications
- ✅ Eureka service discovery
- ✅ Spring Cloud Config integration

### Development Features

- ✅ Comprehensive test coverage
- ✅ Docker containerization
- ✅ Environment-based configuration

This technical documentation provides a comprehensive overview of the TalentRadar User Management Service architecture, components, and interactions. The service is designed to be scalable, secure, and maintainable within a microservices ecosystem.
