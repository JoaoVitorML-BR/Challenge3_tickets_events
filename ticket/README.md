# Tickets & User Management Service

A Spring Boot microservice for managing users and tickets with JWT authentication and CPF validation.

## Architecture

This is the **Tickets Service** that handles:
- **User Management** - User registration, authentication, and profile management
- **Ticket Operations** - Ticket creation, listing, and cancellation with CPF validation

## Requirements

- **Java**: Version 21 or higher
- **Spring Boot**: Version 3.5.4
- **Spring Cloud**: Version 2025.0.0
- **Maven**: Version 3.6+
- **MongoDB Atlas**: Cloud database (configured)

## How to Run the Project

### 1. Clone the Repository
```bash
git clone https://github.com/JoaoVitorML-BR/Challenge3_tickets_events.git
cd Challenge3_tickets_events/ticket
```

### 2. Run the Tickets Service
```bash
./mvnw spring-boot:run
```
The Tickets Service will be available at: `http://localhost:8081`

## Database Configuration

The system uses MongoDB Atlas cloud database. The connection is already configured in the `application.properties` files for both services.

## Authentication

The system uses JWT (JSON Web Tokens) for authentication. Users must register and login to access protected endpoints.

## API Endpoints

### Tickets Service (Port 8081)

#### User Management
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `POST` | `/api/v1/users` | Register new user | No |
| `GET` | `/api/v1/users` | List all users (paginated) | Yes (ADMIN) |
| `GET` | `/api/v1/users/{id}` | Get user by ID | Yes (Own user or ADMIN) |
| `GET` | `/api/v1/users/cpf/{cpf}` | Get user by CPF | Yes (ADMIN) |
| `PUT` | `/api/v1/users/{id}` | Update user | Yes (Own user or ADMIN) |

#### Authentication
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `POST` | `/api/v1/auth/login` | User login | No |

#### Ticket Management
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `POST` | `/api/v1/tickets` | Create new ticket | Yes (CLIENT) |
| `GET` | `/api/v1/tickets/my-tickets` | Get user's tickets (paginated) | Yes (CLIENT) |
| `GET` | `/api/v1/tickets/{ticketId}` | Get ticket by ID | Yes (CLIENT) |
| `GET` | `/api/v1/tickets/cpf/{cpf}` | Get tickets by CPF | Yes (ADMIN) |
| `GET` | `/api/v1/tickets/status/{status}` | Get tickets by status | Yes (ADMIN) |
| `PUT` | `/api/v1/tickets/{ticketId}/cancel` | Cancel ticket | Yes (CLIENT - Own ticket) |
| `GET` | `/api/v1/tickets/event/{eventId}/check` | Check tickets for event | No |

## Request/Response Examples

### User Registration
```bash
POST /api/v1/users
Content-Type: application/json

{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "securepassword",
  "cpf": "123.456.789-00",
  "role": "ROLE_CLIENT"
}
```

### User Login
```bash
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "johndoe",
  "password": "securepassword"
}
```

### Create Ticket
```bash
POST /api/v1/tickets
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "eventName": "Rock Festival 2025",
  "cpf": "123.456.789-00"
}
```

## Security Features

- **JWT Authentication**: Secure token-based authentication
- **Role-based Authorization**: ADMIN and CLIENT roles with different permissions
- **CPF Validation**: Users can only create tickets with their registered CPF
- **Password Encryption**: BCrypt encryption for user passwords
- **Input Validation**: Comprehensive validation for all inputs

## User Roles

- **ROLE_ADMIN**: Full access to all endpoints, can manage events and view all users/tickets
- **ROLE_CLIENT**: Can create tickets, view own tickets, and update own profile

## Key Features

### CPF Validation
- Users must provide their registered CPF when creating tickets
- System validates that the provided CPF matches the user's registered CPF
- Prevents ticket fraud and ensures accountability

### Event-Ticket Integration
- Integrates with Events service to validate event existence before ticket creation
- OpenFeign client handles inter-service communication
- Fallback mechanisms for service availability

### Pagination Support
- All list endpoints support pagination
- Configurable page size and sorting options
- Efficient data retrieval for large datasets

## Error Handling

The system provides comprehensive error handling with appropriate HTTP status codes:

- `400 Bad Request`: Invalid input data or CPF mismatch
- `401 Unauthorized`: Authentication required
- `403 Forbidden`: Insufficient permissions
- `404 Not Found`: Resource not found
- `409 Conflict`: Duplicate data (username, email, CPF)
- `503 Service Unavailable`: External service unavailable
