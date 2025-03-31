# Stage 1: Build dengan Gradle
FROM gradle:8.4-jdk21-alpine AS builder
WORKDIR /app

# Copy file yang dibutuhkan untuk build
COPY build.gradle .
COPY settings.gradle .
COPY gradlew .
COPY gradle/wrapper/gradle-wrapper.properties gradle/wrapper/
COPY src ./src

# Build aplikasi
RUN ./gradlew clean bootJar --no-daemon

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