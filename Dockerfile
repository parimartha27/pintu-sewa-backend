# Stage 1: Build
FROM gradle:8.4-jdk21-alpine AS builder
WORKDIR /app

# Copy hanya file yang diperlukan untuk resolve dependencies dulu
COPY build.gradle .
COPY settings.gradle .
COPY gradlew .
COPY gradle/wrapper gradle/wrapper
RUN chmod +x gradlew

# Download dependencies (memanfaatkan layer caching)
RUN ./gradlew dependencies --no-daemon

# Copy seluruh source code
COPY src ./src

# Build dengan skip tests
RUN ./gradlew clean build -x test --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=production
ENTRYPOINT ["java", "-jar", "app.jar"]