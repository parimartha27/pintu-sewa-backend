# Stage 1: Build dengan Gradle
FROM gradle:8.4-jdk21-alpine AS builder
WORKDIR /app

# Copy file build dan set permission
COPY . .
RUN chmod +x gradlew

# Build aplikasi (dengan cache untuk dependencies)
RUN ./gradlew clean build --no-daemon

# Stage 2: Runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy hasil build dari stage builder
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose port default Spring Boot
EXPOSE 8080

# Set environment variable untuk Spring Boot
ENV SPRING_PROFILES_ACTIVE=production

# Run aplikasi
ENTRYPOINT ["java", "-jar", "app.jar"]