# Events Management Service

A Spring Boot microservice for managing events with CEP validation and ticket integration.

## rchitecture

This is the **Events Service** that handles:
- **Event Management** - Event creation, updating, listing, and cancellation
- **CEP Integration** - Address validation using ViaCEP API
- **Ticket Integration** - Validates ticket existence before allowing event cancellation

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
cd Challenge3_tickets_events/events
```

### 2. Run the Events Service
```bash
./mvnw spring-boot:run
```
The Events Service will be available at: `http://localhost:8080`

## Database Configuration

The service uses MongoDB Atlas cloud database. The connection is already configured in the `application.properties` file.

## External API Integration

The service integrates with **ViaCEP API** for Brazilian postal code (CEP) validation and address information retrieval.

## API Endpoints

### Events Management

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `GET` | `/api/v1/events` | List all events (with pagination and filters) | No |
| `GET` | `/api/v1/events/{id}` | Get event by ID | No |
| `GET` | `/api/v1/events/search` | Search events by name (with pagination) | No |
| `POST` | `/api/v1/events` | Create new event | No |
| `PUT` | `/api/v1/events/{id}` | Update event | No |
| `PATCH` | `/api/v1/events/{id}/cancel` | Cancel event | No |
| `PATCH` | `/api/v1/events/{id}/reactivate` | Reactivate cancelled event | No |

### Query Parameters

#### GET /api/v1/events
- `canceled`: Filter by cancellation status (true/false)
- `page`: Page number for pagination
- `size`: Page size (default: 10)
- `sort`: Sort field (default: eventDate)
- `direction`: Sort direction (ASC/DESC, default: ASC)

#### GET /api/v1/events/search
- `name`: Event name to search (required)
- `page`: Page number for pagination
- `size`: Page size (default: 10)
- `sort`: Sort field (default: eventDate)
- `direction`: Sort direction (ASC/DESC, default: ASC)

## Request/Response Examples

### Create Event
```bash
POST /api/v1/events
Content-Type: application/json

{
  "eventName": "Rock Festival 2025",
  "eventDate": "2025-12-15T20:00:00",
  "eventDescription": "Amazing rock festival with international bands",
  "cep": "01310-100"
}
```

### Update Event
```bash
PUT /api/v1/events/{id}
Content-Type: application/json

{
  "eventName": "Updated Rock Festival 2025",
  "eventDate": "2025-12-20T19:00:00",
  "eventDescription": "Updated amazing rock festival",
  "cep": "01310-200"
}
```

### List Events with Pagination
```bash
GET /api/v1/events?page=0&size=5&sort=eventName&direction=ASC
```

### Search Events by Name
```bash
GET /api/v1/events/search?name=Rock&page=0&size=10
```

### Cancel Event
```bash
PATCH /api/v1/events/{id}/cancel
```

### Reactivate Event
```bash
PATCH /api/v1/events/{id}/reactivate
```

## Response Structure

### Event Response
```json
{
  "id": "64f1a2b3c9e4f5d6e7a8b9c0",
  "eventName": "Rock Festival 2025",
  "eventDate": "2025-12-15T20:00:00",
  "eventDescription": "Amazing rock festival",
  "eventLocation": {
    "logradouro": "Avenida Paulista",
    "bairro": "Bela Vista",
    "localidade": "São Paulo",
    "uf": "SP",
    "cep": "01310-100"
  },
  "canceled": false,
  "createdAt": "2025-01-15T10:30:00",
  "updatedAt": "2025-01-15T10:30:00"
}
```

### Paginated Response
```json
{
  "events": [
    {
      "id": "64f1a2b3c9e4f5d6e7a8b9c0",
      "eventName": "Rock Festival 2025",
      "eventDate": "2025-12-15T20:00:00",
      "eventDescription": "Amazing rock festival",
      "eventLocation": {
        "logradouro": "Avenida Paulista",
        "bairro": "Bela Vista",
        "localidade": "São Paulo",
        "uf": "SP",
        "cep": "01310-100"
      },
      "canceled": false,
      "createdAt": "2025-01-15T10:30:00",
      "updatedAt": "2025-01-15T10:30:00"
    }
  ],
  "currentPage": 0,
  "totalPages": 5,
  "totalElements": 50,
  "pageSize": 10,
  "hasNext": true,
  "hasPrevious": false
}
```

## Key Features

### CEP Validation
- Integrates with ViaCEP API to validate Brazilian postal codes
- Automatically retrieves and stores complete address information
- Validates CEP format and existence before event creation/update

### Event Status Management
- Events can be cancelled and reactivated
- Cancellation validation checks for active tickets via Tickets Service
- Status tracking with timestamps

### Search and Filtering
- Search events by name with partial matching
- Filter events by cancellation status
- Comprehensive pagination support
- Flexible sorting options

### Ticket Integration
- Before cancelling an event, validates if there are active tickets
- Uses OpenFeign client to communicate with Tickets Service
- Prevents cancellation of events with active tickets

## Error Handling

The service provides comprehensive error handling with appropriate HTTP status codes:

- `400 Bad Request`: Invalid CEP format or event data
- `404 Not Found`: Event not found
- `409 Conflict`: Event name already exists or cancellation not allowed
- `503 Service Unavailable`: ViaCEP API or Tickets Service unavailable

### Error Response Examples

```json
{
  "status": "error",
  "message": "Event name 'Rock Festival 2025' already exists",
  "timestamp": "2025-01-15T10:30:00"
}
```

```json
{
  "status": "error",
  "message": "Cannot cancel event. There are 15 active tickets for this event",
  "timestamp": "2025-01-15T10:30:00"
}
```

## Dependencies

- **Spring Boot Starter Web** - REST API framework
- **Spring Boot Starter Data MongoDB** - MongoDB integration
- **Spring Cloud OpenFeign** - HTTP client for external APIs
- **Spring Boot Starter Validation** - Request validation
- **Lombok** - Code generation for POJOs