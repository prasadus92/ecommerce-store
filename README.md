# E-commerce Store API

[![CI/CD Pipeline](https://github.com/prasadus92/ecommerce-store/actions/workflows/ci.yml/badge.svg)](https://github.com/prasadus92/ecommerce-store/actions/workflows/ci.yml)
[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A production-grade RESTful API for e-commerce operations built with modern Java and Spring Boot best practices.

## Features

- **Product Management** - Full CRUD operations with SKU uniqueness, soft-delete support, and optimistic locking
- **Order Processing** - Create orders with multiple items, status workflow management, and order cancellation
- **Price Integrity** - Product price changes don't affect historical order totals (prices frozen at order time)
- **Pagination & Sorting** - All list endpoints support pagination, sorting, and filtering
- **Caching** - Caffeine-based caching for improved read performance
- **API Documentation** - Interactive OpenAPI 3.0 documentation with Swagger UI
- **Health Monitoring** - Spring Actuator endpoints with Prometheus metrics
- **Containerization** - Multi-stage Docker build with health checks

## Tech Stack

| Category | Technology |
|----------|------------|
| Language | Java 21 (LTS) |
| Framework | Spring Boot 3.3.5 |
| Database | PostgreSQL 16 / H2 (dev) |
| ORM | Spring Data JPA / Hibernate |
| Build | Maven |
| API Docs | springdoc-openapi 2.6 |
| Caching | Caffeine |
| Testing | JUnit 5, Testcontainers, REST Assured |
| CI/CD | GitHub Actions |
| Container | Docker with multi-stage builds |

## Quick Start

### Prerequisites

- Java 21+
- Maven 3.9+
- Docker & Docker Compose (optional, for containerized deployment)

### Development Mode (H2 Database)

```bash
# Clone the repository
git clone https://github.com/prasadus92/ecommerce-store.git
cd ecommerce-store

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`

### Production Mode (Docker Compose)

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f ecommerce-api

# Stop services
docker-compose down
```

## API Documentation

Once the application is running, access the interactive API documentation:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/api-docs

## API Endpoints

### Products

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/products` | List all products (paginated) |
| `GET` | `/api/v1/products/{id}` | Get product by ID |
| `POST` | `/api/v1/products` | Create a new product |
| `PUT` | `/api/v1/products/{id}` | Update a product |
| `DELETE` | `/api/v1/products/{id}` | Deactivate a product |

### Orders

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/orders` | List all orders (paginated, filterable) |
| `GET` | `/api/v1/orders/{id}` | Get order by ID |
| `POST` | `/api/v1/orders` | Place a new order |
| `PATCH` | `/api/v1/orders/{id}/status` | Update order status |
| `POST` | `/api/v1/orders/{id}/cancel` | Cancel an order |

### Example Requests

**Create a Product:**
```bash
curl -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "iPhone 15 Pro",
    "description": "Latest flagship smartphone",
    "price": 999.99,
    "sku": "IPHONE-15-PRO"
  }'
```

**Place an Order:**
```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "buyerEmail": "customer@example.com",
    "items": [
      {"productId": "uuid-here", "quantity": 2}
    ]
  }'
```

**Get Orders by Date Range:**
```bash
curl "http://localhost:8080/api/v1/orders?startDate=2024-01-01&endDate=2024-12-31"
```

## Project Structure

```
src/
├── main/
│   ├── java/com/ecommerce/store/
│   │   ├── api/
│   │   │   ├── controller/     # REST controllers
│   │   │   ├── dto/            # Request/Response DTOs (Java records)
│   │   │   ├── exception/      # Global exception handling
│   │   │   └── mapper/         # MapStruct mappers
│   │   ├── config/             # Configuration classes
│   │   └── domain/
│   │       ├── entity/         # JPA entities
│   │       ├── exception/      # Domain exceptions
│   │       ├── repository/     # Spring Data repositories
│   │       └── service/        # Business logic
│   └── resources/
│       ├── application.yml             # Development config
│       └── application-production.yml  # Production config
└── test/
    └── java/com/ecommerce/store/
        ├── domain/service/     # Unit tests
        └── integration/        # Integration tests with Testcontainers
```

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_HOST` | Database host | `postgres` |
| `DB_PORT` | Database port | `5432` |
| `DB_NAME` | Database name | `ecommerce_db` |
| `DB_USER` | Database username | `ecommerce_user` |
| `DB_PASSWORD` | Database password | `secret` |
| `SERVER_PORT` | Application port | `8080` |
| `JAVA_OPTS` | JVM options | See Dockerfile |

### Profiles

- **default** - Development mode with H2 in-memory database
- **production** - Production mode with PostgreSQL
- **test** - Test configuration with Testcontainers

## Testing

```bash
# Run all tests
mvn test

# Run with coverage report
mvn verify

# Coverage report location
open target/site/jacoco/index.html
```

### Test Categories

- **Unit Tests** - Service layer tests with mocked dependencies
- **Integration Tests** - Full API tests using Testcontainers (PostgreSQL)

## Monitoring

### Actuator Endpoints

| Endpoint | Description |
|----------|-------------|
| `/actuator/health` | Application health status |
| `/actuator/info` | Application information |
| `/actuator/metrics` | Application metrics |
| `/actuator/prometheus` | Prometheus-format metrics |

### Health Check Response

```json
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "diskSpace": {"status": "UP"}
  }
}
```

## Architecture Decisions

### Why Java Records for DTOs?
- Immutable by default, preventing accidental mutation
- Concise syntax with automatic getters, equals, hashCode, toString
- Clear separation between mutable entities and immutable API contracts

### Why MapStruct?
- Compile-time code generation (no runtime reflection overhead)
- Type-safe mapping with compile-time validation
- Clean separation between domain and API layers

### Why Caffeine Cache?
- High-performance, near-optimal caching
- Memory-efficient with configurable eviction policies
- Built-in statistics for monitoring cache effectiveness

### Why Testcontainers?
- Real database testing without mocks
- Consistent test environment across local and CI
- Eliminates "works on my machine" issues

## Error Handling

The API uses RFC 7807 Problem Details for error responses:

```json
{
  "type": "https://api.ecommerce.com/errors/not-found",
  "title": "Resource Not Found",
  "status": 404,
  "detail": "Product not found with identifier: abc123",
  "timestamp": "2024-01-15T10:30:00Z",
  "resourceType": "Product",
  "identifier": "abc123"
}
```

## Security Considerations

For production deployment, consider adding:

1. **Authentication** - OAuth2 + JWT via Spring Security
2. **Rate Limiting** - Using Spring Cloud Gateway or Bucket4j
3. **Input Validation** - Already implemented with Bean Validation
4. **HTTPS** - Configure TLS in reverse proxy (nginx/traefik)
5. **CORS** - Configure allowed origins for web clients

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- [Spring Boot](https://spring.io/projects/spring-boot)
- [springdoc-openapi](https://springdoc.org/)
- [Testcontainers](https://www.testcontainers.org/)
- [MapStruct](https://mapstruct.org/)
