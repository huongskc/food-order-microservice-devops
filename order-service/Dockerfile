# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/order-service-0.0.1-SNAPSHOT.jar ./order-service.jar
RUN addgroup -S appuser
RUN adduser -S appuser -G appuser
USER appuser
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "./order-service.jar"]