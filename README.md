# Microservices Architecture Project

A Spring Boot microservices application demonstrating service discovery, configuration management, API gateway, and inter-service communication using OpenFeign.

## Architecture Overview

This project implements a complete microservices ecosystem with the following components:

### Infrastructure Services

- **Eureka Discovery Service** (Port 8761) - Service registry and discovery
- **Config Service** (Port 8888) - Centralized configuration management
- **Gateway Service** (Port 8080) - API Gateway with routing

### Business Services

- **Customer Service** (Port 8081) - Customer management operations
- **Inventory Service** (Port 8082) - Product inventory management
- **Billing Service** (Port 8083) - Billing operations with inter-service communication

### Database

- **PostgreSQL** - Single database with separate schemas per service

## Technology Stack

- **Spring Boot 4.0.0** - Main framework
- **Spring Cloud 2025.1.0** - Microservices components
- **Java 17** - Programming language
- **PostgreSQL** - Database
- **Maven** - Build tool
- **Docker & Docker Compose** - Containerization

## Quick Start

### Prerequisites

- Docker and Docker Compose installed
- Java 17 (for local development)
- Maven (for local development)

### Running with Docker Compose

1. **Build the application:**

   ```bash
   mvn clean package -DskipTests
   ```

2. **Start all services:**

   ```bash
   docker-compose up -d
   ```

3. **Check service status:**
   ```bash
   docker-compose ps
   ```

### Service URLs

- **API Gateway:** http://localhost:8080
- **Eureka Dashboard:** http://localhost:8761
- **Config Service:** http://localhost:8888
- **Customer Service:** http://localhost:8081
- **Inventory Service:** http://localhost:8082
- **Billing Service:** http://localhost:8083

## API Endpoints

### Customer Service

```
GET    /api/customers           - Get all customers
POST   /api/customers           - Create customer
GET    /api/customers/{id}      - Get customer by ID
PUT    /api/customers/{id}      - Update customer
DELETE /api/customers/{id}      - Delete customer
GET    /api/customers/email/{email} - Get customer by email
```

### Inventory Service

```
GET    /api/products            - Get all products
POST   /api/products            - Create product
GET    /api/products/{id}       - Get product by ID
PUT    /api/products/{id}       - Update product
DELETE /api/products/{id}       - Delete product
GET    /api/products/search/{name} - Search products by name
GET    /api/products/available  - Get available products
```

### Billing Service

```
GET    /api/bills               - Get all bills
POST   /api/bills               - Create bill
GET    /api/bills/{id}          - Get bill with customer and product details
DELETE /api/bills/{id}          - Delete bill
GET    /api/bills/customer/{customerId} - Get bills by customer
```

## Testing the Application

### 1. Create a Customer

```bash
curl -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -d '{"name":"John Doe","email":"john@example.com","phone":"123-456-7890","address":"123 Main St"}'
```

### 2. Create a Product

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Laptop","description":"Gaming Laptop","price":999.99,"quantity":10}'
```

### 3. Create a Bill

```bash
curl -X POST http://localhost:8080/api/bills \
  -H "Content-Type: application/json" \
  -d '{"customerId":1,"productId":1,"quantity":2}'
```

## Development Setup

### Local Development

1. **Start PostgreSQL locally** or use Docker:

   ```bash
   docker run -d --name postgres -p 5432:5432 -e POSTGRES_PASSWORD=justForWork postgres:15
   ```

2. **Run services in order:**

   ```bash
   # Terminal 1 - Eureka
   cd eureka-discovery && mvn spring-boot:run

   # Terminal 2 - Config Service
   cd config-service && mvn spring-boot:run

   # Terminal 3 - Customer Service
   cd customer-service && mvn spring-boot:run

   # Terminal 4 - Inventory Service
   cd inventory-service && mvn spring-boot:run

   # Terminal 5 - Billing Service
   cd billing-service && mvn spring-boot:run

   # Terminal 6 - Gateway
   cd gateway-service && mvn spring-boot:run
   ```

### Database Schemas

- `customer_schema` - Customer service tables
- `inventory_schema` - Inventory service tables
- `billing_schema` - Billing service tables

## Configuration

Services use Spring Cloud Config for centralized configuration management. Configuration files are located in `config-service/src/main/resources/configs/`.

### Environment Profiles

- **dev** - Development environment (default)
- **prod** - Production environment
- **docker** - Docker container environment

## Monitoring

- **Eureka Dashboard:** Monitor service registration and health
- **Health Endpoints:** Available at `/actuator/health` for each service
- **Gateway Routes:** Available at `/actuator/gateway/routes`

## Stopping the Application

```bash
docker-compose down
```

To remove volumes:

```bash
docker-compose down -v
```

## Troubleshooting

### Common Issues

1. **Services not registering with Eureka:** Check if Eureka is running and accessible
2. **Database connection errors:** Verify PostgreSQL is running and schemas are created
3. **Port conflicts:** Ensure no other applications are using the required ports

### Logs

```bash
# View logs for specific service
docker-compose logs [service-name]

# Follow logs in real-time
docker-compose logs -f [service-name]
```

## Project Structure

```
pratique1-architecture-micro-services/
├── eureka-discovery/          # Service discovery
├── config-service/            # Configuration management
├── customer-service/          # Customer operations
├── inventory-service/         # Product operations
├── billing-service/           # Billing operations
├── gateway-service/           # API Gateway
├── docker-compose.yml         # Container orchestration
├── init-db.sql               # Database initialization
└── pom.xml                   # Parent POM
```
