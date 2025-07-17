# Spring Boot Development Dockerfile
FROM openjdk:21-jdk-slim

# Set working directory
WORKDIR /app

# Install Maven (for dependency management and hot reload)
RUN apt-get update && \
    apt-get install -y maven && \
    rm -rf /var/lib/apt/lists/*

# Copy Maven files first for better layer caching
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Download dependencies (this layer will be cached unless pom.xml changes)
RUN mvn dependency:go-offline -B

# Run tests
RUN mvn test


# Copy source code
COPY src ./src

# Expose application port and debug port
EXPOSE 8081

# Run the application with hot reload enabled
CMD ["mvn", "spring-boot:run"]