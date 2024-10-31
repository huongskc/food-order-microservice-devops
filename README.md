# Spring Boot Microservices Project

A modern, scalable food delivery system built with Spring Boot microservices architecture.

## 🏗️ Architecture Overview

This project implements a microservices-based architecture using Spring Boot and Spring Cloud, designed for scalability and maintainability.

![Architecture Diagram](./screenshot/architecture.png)

## 🚀 Key Components

### Backend Services

- **API Gateway** (Port: 8080)
  - Single entry point for all client requests
  - Request routing and load balancing
  - Built with Spring Cloud Gateway

- **Eureka Service Discovery** (Port: 8761)
  - Service registration and discovery
  - Load balancing support
  - Health monitoring

- **User Service** (Port: 8081)
  - User management and authentication
  - Profile management
  - JWT-based security

- **Restaurant Service** (Port: 8082)
  - Restaurant information management
  - Menu management
  - Image handling for restaurants and menu items

- **Order Service** (Port: 8083)
  - Order processing and management
  - Integration with restaurant and user services
  - Order status tracking

- **Notification Service** (Port: 8084)
  - Email notifications
  - Kafka-based event processing
  - Template-based email generation

### Frontend

- **Angular Application** (Port: 4200)
  - Modern UI built with Angular 17
  - Responsive design
  - JWT authentication integration

## 🛠️ Technologies Used

- **Backend**
  - Java 21
  - Spring Boot 3.3.4
  - Spring Cloud 2023.0.3
  - Spring Security + JWT
  - MySQL
  - Apache Kafka
  - OpenAPI (Swagger)

- **Frontend**
  - Angular 17
  - TypeScript
  - RxJS
  - TailwindCSS

- **DevOps & Tools**
  - Docker
  - Maven
  - Git

## 📋 Prerequisites

- Java 21
- Node.js 18+
- MySQL 8+
- Docker
- Maven
- Kafka

## 🚀 Installation

1. **Clone the repository:**
```bash
https://github.com/Vanhuyne/food-order-microservice.git
```
2. **Navigate to the project directory:**
3. **Build and package each component with Maven**


