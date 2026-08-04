# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/eureka-service-0.0.1-SNAPSHOT.jar ./eureka-service.jar
RUN addgroup -S appuser
RUN adduser -S appuser -G appuser
USER appuser
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "./eureka-service.jar"]