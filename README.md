# Event Management System

A microservices-based event management system built with Spring Boot, featuring event creation and ticket management capabilities.

## Services

This project consists of two main microservices:

- **Events Service** - Manages event creation, updates, and cancellation
- **Tickets Service** - Handles ticket purchases and user management

## API Documentation

Both services provide interactive API documentation via Swagger UI:

### Local Development

- **Events API**: http://localhost:8080/swagger-ui/index.html#/
- **Tickets API**: http://localhost:8081/swagger-ui/index.html#/

### AWS Production

The services are deployed on AWS EC2 instances:

- **Events Service**: http://ec2-18-222-140-58.us-east-2.compute.amazonaws.com:8080
- **Tickets Service**: http://ec2-3-143-221-128.us-east-2.compute.amazonaws.com:8081

## Getting Started

Each service has its own README with detailed setup instructions:

- [Events Service README](./events/README.md)
- [Tickets Service README](./ticket/README.md)

## Technology Stack

- Spring Boot 3.5.4
- MongoDB Atlas
- Spring Security with JWT
- SpringDoc OpenAPI (Swagger)
- Spring Cloud OpenFeign

## Architecture

The system follows a microservices architecture where:

- Events Service manages event lifecycle
- Tickets Service handles user authentication and ticket operations
- Services communicate via REST APIs using OpenFeign clients
- Each service has its own MongoDB database