FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .

# Pastikan gradlew dapat dieksekusi
RUN chmod +x gradlew

RUN echo "Building application..." && ./gradlew build -x test && echo "Build complete."

FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
