# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/restaurant-service-0.0.1-SNAPSHOT.jar ./restaurant-service.jar
RUN mkdir -p /app/src/main/resources/uploads
RUN addgroup -S appuser
RUN adduser -S appuser -G appuser
RUN chown -R appuser:appuser /app/src/main/resources/uploads
USER appuser
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "./restaurant-service.jar"]