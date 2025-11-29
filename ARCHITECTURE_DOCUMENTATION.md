# 🏗️ Microservices Architecture - Deep Documentation

## 📖 Table of Contents

1. [Introduction to Microservices](#1-introduction-to-microservices)
2. [Architecture Overview](#2-architecture-overview)
3. [Service Communication Flow](#3-service-communication-flow)
4. [Infrastructure Services (Detailed)](#4-infrastructure-services-detailed)
5. [Business Services (Detailed)](#5-business-services-detailed)
6. [How Services Discover Each Other](#6-how-services-discover-each-other)
7. [Inter-Service Communication with OpenFeign](#7-inter-service-communication-with-openfeign)
8. [API Gateway Pattern](#8-api-gateway-pattern)
9. [Centralized Configuration](#9-centralized-configuration)
10. [Database Strategy](#10-database-strategy)
11. [Request Flow Examples](#11-request-flow-examples)
12. [Key Concepts Summary](#12-key-concepts-summary)

---

## 1. Introduction to Microservices

### What are Microservices?

Microservices is an architectural style where an application is built as a **collection of small, independent services** that:

- Run in their own process
- Communicate via lightweight mechanisms (typically HTTP/REST)
- Are independently deployable
- Can be written in different programming languages
- Can use different data storage technologies

### Monolithic vs Microservices

```
┌─────────────────────────────────────────────────────────────────┐
│                    MONOLITHIC ARCHITECTURE                       │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                    Single Application                     │   │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐   │   │
│  │  │ Customer │ │ Inventory│ │ Billing  │ │   UI     │   │   │
│  │  │  Module  │ │  Module  │ │  Module  │ │  Module  │   │   │
│  │  └──────────┘ └──────────┘ └──────────┘ └──────────┘   │   │
│  │                    ↓                                      │   │
│  │              Single Database                              │   │
│  └─────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                  MICROSERVICES ARCHITECTURE                      │
│                                                                  │
│   ┌──────────┐   ┌──────────┐   ┌──────────┐   ┌──────────┐   │
│   │ Customer │   │ Inventory│   │ Billing  │   │ Gateway  │   │
│   │ Service  │   │ Service  │   │ Service  │   │ Service  │   │
│   └────┬─────┘   └────┬─────┘   └────┬─────┘   └──────────┘   │
│        │              │              │                         │
│        ↓              ↓              ↓                         │
│   ┌────────┐    ┌────────┐    ┌────────┐                      │
│   │  DB 1  │    │  DB 2  │    │  DB 3  │                      │
│   └────────┘    └────────┘    └────────┘                      │
└─────────────────────────────────────────────────────────────────┘
```

---

## 2. Architecture Overview

### Your Project Architecture

```
                                    ┌─────────────────┐
                                    │   Client/User   │
                                    │  (Browser/App)  │
                                    └────────┬────────┘
                                             │
                                             │ HTTP Requests
                                             ▼
┌────────────────────────────────────────────────────────────────────────┐
│                         GATEWAY SERVICE (Port 8080)                     │
│                    Single Entry Point for All Requests                  │
│                                                                         │
│  Routes:                                                                │
│    /api/customers/** → Customer Service                                │
│    /api/products/**  → Inventory Service                               │
│    /api/bills/**     → Billing Service                                 │
└────────────────────────────────────────────────────────────────────────┘
                                             │
                    ┌────────────────────────┼────────────────────────┐
                    │                        │                        │
                    ▼                        ▼                        ▼
┌──────────────────────────┐ ┌──────────────────────────┐ ┌──────────────────────────┐
│    CUSTOMER SERVICE      │ │    INVENTORY SERVICE     │ │     BILLING SERVICE      │
│       (Port 8081)        │ │       (Port 8082)        │ │       (Port 8083)        │
│                          │ │                          │ │                          │
│  - CRUD for Customers    │ │  - CRUD for Products     │ │  - Create Bills          │
│  - Customer data         │ │  - Inventory tracking    │ │  - Uses OpenFeign to     │
│                          │ │                          │ │    call other services   │
└───────────┬──────────────┘ └───────────┬──────────────┘ └───────────┬──────────────┘
            │                            │                            │
            │ Registers                  │ Registers                  │ Registers
            │                            │                            │
            └────────────────────────────┼────────────────────────────┘
                                         ▼
┌────────────────────────────────────────────────────────────────────────┐
│                     EUREKA DISCOVERY (Port 8761)                        │
│                      Service Registry & Discovery                       │
│                                                                         │
│  Registered Services:                                                   │
│    - CUSTOMER-SERVICE → localhost:8081                                 │
│    - INVENTORY-SERVICE → localhost:8082                                │
│    - BILLING-SERVICE → localhost:8083                                  │
│    - GATEWAY-SERVICE → localhost:8080                                  │
│    - CONFIG-SERVICE → localhost:8888                                   │
└────────────────────────────────────────────────────────────────────────┘
                                         ▲
                                         │
┌────────────────────────────────────────────────────────────────────────┐
│                      CONFIG SERVICE (Port 8888)                         │
│                    Centralized Configuration Server                     │
│                                                                         │
│  Provides configuration to all services:                               │
│    - Database connections                                              │
│    - Service-specific settings                                         │
│    - Environment-based configs (dev/prod/docker)                       │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 3. Service Communication Flow

### How Services Talk to Each Other

```
┌────────────────────────────────────────────────────────────────────────────┐
│                    INTER-SERVICE COMMUNICATION FLOW                         │
│                                                                             │
│  Example: Creating a Bill                                                   │
│                                                                             │
│  ┌──────────┐      ┌─────────────┐      ┌─────────────────┐               │
│  │  Client  │──1──▶│   Gateway   │──2──▶│ Billing Service │               │
│  └──────────┘      └─────────────┘      └───────┬─────────┘               │
│                                                  │                          │
│                    ┌─────────────────────────────┼──────────────────────┐  │
│                    │                             │                      │  │
│                    │    3. Ask Eureka:           │                      │  │
│                    │    "Where is                │                      │  │
│                    │     customer-service?"      │                      │  │
│                    │              │              │                      │  │
│                    │              ▼              │                      │  │
│                    │      ┌──────────────┐       │                      │  │
│                    │      │    EUREKA    │       │                      │  │
│                    │      │   Discovery  │       │                      │  │
│                    │      └──────┬───────┘       │                      │  │
│                    │             │               │                      │  │
│                    │    4. "It's at             │                      │  │
│                    │        localhost:8081"      │                      │  │
│                    │             │               │                      │  │
│                    │             ▼               │                      │  │
│                    │    5. Call Customer         │                      │  │
│                    │       Service via           │                      │  │
│                    │       OpenFeign             │                      │  │
│                    │              │              ▼                      │  │
│                    │              │      ┌───────────────┐              │  │
│                    │              └─────▶│   Customer    │              │  │
│                    │                     │   Service     │              │  │
│                    │                     └───────────────┘              │  │
│                    │                             │                      │  │
│                    │    6. Return Customer       │                      │  │
│                    │       Data                  │                      │  │
│                    └─────────────────────────────┴──────────────────────┘  │
└────────────────────────────────────────────────────────────────────────────┘
```

---

## 4. Infrastructure Services (Detailed)

### 4.1 Eureka Discovery Service

**Purpose:** Service Registry - keeps track of all running services and their locations.

**How it works:**

1. Each service **registers** itself with Eureka on startup
2. Each service sends **heartbeats** to Eureka to stay registered
3. Services can **query** Eureka to find other services
4. If a service stops sending heartbeats, Eureka **removes** it from the registry

```java
// EurekaDiscoveryApplication.java
@SpringBootApplication
@EnableEurekaServer  // This annotation makes it a Eureka Server
public class EurekaDiscoveryApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaDiscoveryApplication.class, args);
    }
}
```

**Configuration (application.yml):**

```yaml
spring:
  application:
    name: eureka-discovery

server:
  port: 8761

eureka:
  client:
    register-with-eureka: false # Don't register itself
    fetch-registry: false # Don't fetch registry (it IS the registry)
```

**Key Concepts:**

- **Service Registration:** Services tell Eureka "I exist at this address"
- **Service Discovery:** Services ask Eureka "Where is service X?"
- **Load Balancing:** If multiple instances exist, Eureka helps distribute requests

### 4.2 Config Service

**Purpose:** Centralized configuration management for all microservices.

**Benefits:**

- Single source of truth for configuration
- No need to restart services for config changes
- Environment-specific configurations (dev, prod, docker)
- Secure storage of sensitive data

```java
// ConfigServiceApplication.java
@SpringBootApplication
@EnableConfigServer  // This annotation makes it a Config Server
public class ConfigServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConfigServiceApplication.class, args);
    }
}
```

**Configuration (application.yml):**

```yaml
spring:
  application:
    name: config-service
  profiles:
    active: dev,native # 'native' means read from local filesystem
  cloud:
    config:
      server:
        native:
          search-locations: classpath:/configs # Where configs are stored

server:
  port: 8888
```

**Config Files Structure:**

```
config-service/src/main/resources/configs/
├── customer-service.yml    # Config for Customer Service
├── inventory-service.yml   # Config for Inventory Service
├── billing-service.yml     # Config for Billing Service
└── gateway-service.yml     # Config for Gateway Service
```

**Example Config (customer-service.yml):**

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/postgres?currentSchema=customer_schema
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:justForWork}
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

### 4.3 Gateway Service

**Purpose:** Single entry point for all client requests (API Gateway Pattern).

**Benefits:**

- Single URL for clients (no need to know individual service URLs)
- Centralized security, logging, rate limiting
- Request routing based on URL patterns
- Load balancing

```yaml
# gateway-service application.yml
spring:
  cloud:
    gateway:
      mvc:
        routes:
          - id: customer-service
            uri: lb://CUSTOMER-SERVICE # lb = Load Balanced via Eureka
            predicates:
              - Path=/api/customers/** # Route all /api/customers to Customer Service

          - id: inventory-service
            uri: lb://INVENTORY-SERVICE
            predicates:
              - Path=/api/products/**

          - id: billing-service
            uri: lb://BILLING-SERVICE
            predicates:
              - Path=/api/bills/**
```

**Routing Logic:**

```
Client Request                    Routed To
────────────────────────────────────────────────────────
GET  /api/customers/1      →     Customer Service (8081)
POST /api/products         →     Inventory Service (8082)
GET  /api/bills/5          →     Billing Service (8083)
```

---

## 5. Business Services (Detailed)

### 5.1 Customer Service (Port 8081)

**Responsibility:** Manage customer data

**Entity:**

```java
@Entity
@Table(name = "customers")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String address;
}
```

**REST Endpoints:**
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/customers` | Get all customers |
| GET | `/api/customers/{id}` | Get customer by ID |
| POST | `/api/customers` | Create new customer |
| PUT | `/api/customers/{id}` | Update customer |
| DELETE | `/api/customers/{id}` | Delete customer |

### 5.2 Inventory Service (Port 8082)

**Responsibility:** Manage product inventory

**Entity:**

```java
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer quantity;
}
```

**REST Endpoints:**
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/products` | Get all products |
| GET | `/api/products/{id}` | Get product by ID |
| POST | `/api/products` | Create new product |
| PUT | `/api/products/{id}` | Update product |
| DELETE | `/api/products/{id}` | Delete product |

### 5.3 Billing Service (Port 8083)

**Responsibility:** Handle billing operations, **communicates with other services**

**Entity:**

```java
@Entity
@Table(name = "bills")
public class Bill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long customerId;    // Reference to Customer (not JPA relation!)
    private Long productId;     // Reference to Product (not JPA relation!)
    private Integer quantity;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
}
```

**Key Point:** Notice that `customerId` and `productId` are **just IDs**, not JPA relationships. This is because Customer and Product exist in **different databases/schemas**!

---

## 6. How Services Discover Each Other

### The Service Discovery Process

```
┌─────────────────────────────────────────────────────────────────────────┐
│                     SERVICE DISCOVERY WORKFLOW                           │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  STEP 1: Service Startup & Registration                                 │
│  ─────────────────────────────────────────                              │
│                                                                          │
│    Customer Service starts                                              │
│           │                                                              │
│           ▼                                                              │
│    ┌──────────────────────────────────────────────────────────────┐    │
│    │  "Hello Eureka! I'm CUSTOMER-SERVICE at localhost:8081"      │    │
│    └───────────────────────────┬──────────────────────────────────┘    │
│                                │                                        │
│                                ▼                                        │
│                      ┌─────────────────┐                               │
│                      │     EUREKA      │                               │
│                      │    Registry:    │                               │
│                      │ ┌─────────────┐ │                               │
│                      │ │CUSTOMER-SVC │ │                               │
│                      │ │ :8081       │ │                               │
│                      │ └─────────────┘ │                               │
│                      └─────────────────┘                               │
│                                                                          │
│  STEP 2: Heartbeat (Every 30 seconds by default)                        │
│  ──────────────────────────────────────────────                         │
│                                                                          │
│    Customer Service ──"I'm still alive!"──▶ Eureka                      │
│                                                                          │
│  STEP 3: Service Discovery                                              │
│  ─────────────────────────                                              │
│                                                                          │
│    Billing Service needs to call Customer Service:                      │
│                                                                          │
│    Billing Service                                                      │
│           │                                                              │
│           │ "Eureka, where is CUSTOMER-SERVICE?"                        │
│           ▼                                                              │
│    ┌──────────────────┐                                                │
│    │      EUREKA      │ ──▶ "CUSTOMER-SERVICE is at localhost:8081"    │
│    └──────────────────┘                                                │
│           │                                                              │
│           ▼                                                              │
│    Billing Service now calls http://localhost:8081/api/customers/1      │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

### How Services Register with Eureka

**In each service's pom.xml:**

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

**In each service's application.yml:**

```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka # Eureka server URL
  instance:
    prefer-ip-address: true
```

---

## 7. Inter-Service Communication with OpenFeign

### What is OpenFeign?

OpenFeign is a **declarative HTTP client** that makes calling other REST services as simple as calling a local method.

### How it Works

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        OPENFEIGN MAGIC                                   │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  WITHOUT FEIGN (Traditional RestTemplate):                              │
│  ─────────────────────────────────────────                              │
│                                                                          │
│    RestTemplate restTemplate = new RestTemplate();                      │
│    String url = "http://customer-service/api/customers/" + id;          │
│    Customer customer = restTemplate.getForObject(url, Customer.class);  │
│                                                                          │
│  ────────────────────────────────────────────────────────────────────   │
│                                                                          │
│  WITH FEIGN (Declarative):                                              │
│  ─────────────────────────                                              │
│                                                                          │
│    // Just define an interface!                                         │
│    @FeignClient(name = "customer-service")                              │
│    public interface CustomerServiceClient {                             │
│        @GetMapping("/api/customers/{id}")                               │
│        CustomerDto getCustomerById(@PathVariable("id") Long id);        │
│    }                                                                     │
│                                                                          │
│    // Use it like a regular method call                                 │
│    Customer customer = customerServiceClient.getCustomerById(1L);       │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

### Your Project's Feign Clients

**1. Enable Feign in your Application:**

```java
@SpringBootApplication
@EnableFeignClients  // Enable Feign client support
public class BillingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BillingServiceApplication.class, args);
    }
}
```

**2. Define Feign Client Interface:**

```java
// CustomerServiceClient.java
@FeignClient(name = "customer-service")  // Service name in Eureka
public interface CustomerServiceClient {

    @GetMapping("/api/customers/{id}")
    CustomerDto getCustomerById(@PathVariable("id") Long id);
}
```

```java
// ProductServiceClient.java
@FeignClient(name = "inventory-service")  // Service name in Eureka
public interface ProductServiceClient {

    @GetMapping("/api/products/{id}")
    ProductDto getProductById(@PathVariable("id") Long id);
}
```

**3. Use Feign Client in Controller:**

```java
@RestController
@RequestMapping("/api/bills")
public class BillController {

    @Autowired
    private CustomerServiceClient customerServiceClient;  // Injected by Spring

    @Autowired
    private ProductServiceClient productServiceClient;    // Injected by Spring

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getBillById(@PathVariable Long id) {
        Bill bill = billRepository.findById(id);

        // Call Customer Service (looks like a local method call!)
        CustomerDto customer = customerServiceClient.getCustomerById(bill.getCustomerId());

        // Call Inventory Service (looks like a local method call!)
        ProductDto product = productServiceClient.getProductById(bill.getProductId());

        // Combine data from all services
        Map<String, Object> response = new HashMap<>();
        response.put("bill", bill);
        response.put("customer", customer);
        response.put("product", product);

        return ResponseEntity.ok(response);
    }
}
```

### What Happens Behind the Scenes

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    FEIGN CLIENT CALL BREAKDOWN                           │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  Your Code:                                                             │
│    customerServiceClient.getCustomerById(1L);                           │
│                                                                          │
│  What Feign Does:                                                       │
│                                                                          │
│  1. ┌─────────────────────────────────────────────────────────────┐    │
│     │ Look up "customer-service" in Eureka                        │    │
│     │ Result: ["localhost:8081", "server2:8081"]                  │    │
│     └─────────────────────────────────────────────────────────────┘    │
│                              │                                          │
│                              ▼                                          │
│  2. ┌─────────────────────────────────────────────────────────────┐    │
│     │ Load Balancer picks one: "localhost:8081"                   │    │
│     └─────────────────────────────────────────────────────────────┘    │
│                              │                                          │
│                              ▼                                          │
│  3. ┌─────────────────────────────────────────────────────────────┐    │
│     │ Build HTTP Request:                                         │    │
│     │   GET http://localhost:8081/api/customers/1                 │    │
│     └─────────────────────────────────────────────────────────────┘    │
│                              │                                          │
│                              ▼                                          │
│  4. ┌─────────────────────────────────────────────────────────────┐    │
│     │ Send HTTP Request & Receive Response                        │    │
│     └─────────────────────────────────────────────────────────────┘    │
│                              │                                          │
│                              ▼                                          │
│  5. ┌─────────────────────────────────────────────────────────────┐    │
│     │ Deserialize JSON Response to CustomerDto object             │    │
│     └─────────────────────────────────────────────────────────────┘    │
│                              │                                          │
│                              ▼                                          │
│     Return CustomerDto to your code                                     │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 8. API Gateway Pattern

### Why Use an API Gateway?

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    WITHOUT API GATEWAY (Bad Practice)                    │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│   Client needs to know ALL service URLs:                                │
│                                                                          │
│   ┌──────────┐                                                          │
│   │  Client  │───▶ http://customer-service:8081/api/customers          │
│   │          │───▶ http://inventory-service:8082/api/products          │
│   │          │───▶ http://billing-service:8083/api/bills               │
│   └──────────┘                                                          │
│                                                                          │
│   Problems:                                                             │
│   ❌ Client must track multiple URLs                                    │
│   ❌ No centralized security                                            │
│   ❌ Difficult to add rate limiting                                     │
│   ❌ CORS issues with multiple origins                                  │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│                    WITH API GATEWAY (Best Practice)                      │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│   Client only knows ONE URL:                                            │
│                                                                          │
│   ┌──────────┐         ┌─────────────┐                                 │
│   │  Client  │────────▶│   GATEWAY   │                                 │
│   │          │         │  :8080      │                                 │
│   └──────────┘         └──────┬──────┘                                 │
│                               │                                         │
│   http://gateway:8080/api/customers ──▶ Customer Service               │
│   http://gateway:8080/api/products  ──▶ Inventory Service              │
│   http://gateway:8080/api/bills     ──▶ Billing Service                │
│                                                                          │
│   Benefits:                                                             │
│   ✅ Single entry point                                                 │
│   ✅ Centralized authentication/authorization                          │
│   ✅ Rate limiting in one place                                        │
│   ✅ Request/Response logging                                          │
│   ✅ Load balancing                                                     │
│   ✅ SSL termination                                                    │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 9. Centralized Configuration

### How Config Server Works

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    CONFIGURATION FLOW                                    │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  STARTUP SEQUENCE:                                                      │
│                                                                          │
│  1. Customer Service starts                                             │
│           │                                                              │
│           ▼                                                              │
│  2. "I need my configuration!"                                          │
│           │                                                              │
│           │  GET http://config-service:8888/customer-service/default    │
│           ▼                                                              │
│  3. ┌────────────────────────────────────────────────────────────┐     │
│     │                    CONFIG SERVICE                          │     │
│     │                                                            │     │
│     │  Reads from: configs/customer-service.yml                 │     │
│     │                                                            │     │
│     │  Returns:                                                  │     │
│     │  {                                                         │     │
│     │    "spring.datasource.url": "jdbc:postgresql://...",      │     │
│     │    "spring.datasource.username": "postgres",              │     │
│     │    "spring.jpa.hibernate.ddl-auto": "update"              │     │
│     │  }                                                         │     │
│     └────────────────────────────────────────────────────────────┘     │
│           │                                                              │
│           ▼                                                              │
│  4. Customer Service applies the configuration and continues startup    │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

### Service Configuration

**In each service's application.yml:**

```yaml
spring:
  application:
    name: customer-service # Config Server uses this name to find the config file
  config:
    import: "configserver:http://config-service:8888" # Where to get config from
```

---

## 10. Database Strategy

### Database Per Service Pattern (Schema Isolation)

In this project, we use a **single PostgreSQL database** with **separate schemas** for each service.

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    POSTGRESQL DATABASE                                   │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│   ┌─────────────────────────────────────────────────────────────────┐  │
│   │                    customer_schema                               │  │
│   │   ┌─────────────────────────────────────────────────────────┐   │  │
│   │   │  customers table                                        │   │  │
│   │   │  - id, name, email, phone, address                      │   │  │
│   │   └─────────────────────────────────────────────────────────┘   │  │
│   │   Used by: Customer Service ONLY                               │  │
│   └─────────────────────────────────────────────────────────────────┘  │
│                                                                          │
│   ┌─────────────────────────────────────────────────────────────────┐  │
│   │                    inventory_schema                              │  │
│   │   ┌─────────────────────────────────────────────────────────┐   │  │
│   │   │  products table                                         │   │  │
│   │   │  - id, name, description, price, quantity               │   │  │
│   │   └─────────────────────────────────────────────────────────┘   │  │
│   │   Used by: Inventory Service ONLY                              │  │
│   └─────────────────────────────────────────────────────────────────┘  │
│                                                                          │
│   ┌─────────────────────────────────────────────────────────────────┐  │
│   │                    billing_schema                                │  │
│   │   ┌─────────────────────────────────────────────────────────┐   │  │
│   │   │  bills table                                            │   │  │
│   │   │  - id, customer_id, product_id, quantity, total_amount  │   │  │
│   │   └─────────────────────────────────────────────────────────┘   │  │
│   │   Used by: Billing Service ONLY                                │  │
│   └─────────────────────────────────────────────────────────────────┘  │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

**Why Schema Isolation?**

- Each service owns its data
- Services can't directly access other services' tables
- Changes to one schema don't affect others
- Easier to split into separate databases later

---

## 11. Request Flow Examples

### Example 1: Get Customer (Simple Request)

```
┌─────────────────────────────────────────────────────────────────────────┐
│  GET http://localhost:8080/api/customers/1                              │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  1. Client ──▶ Gateway Service (8080)                                   │
│                    │                                                     │
│                    │ Route: /api/customers/** → CUSTOMER-SERVICE        │
│                    ▼                                                     │
│  2. Gateway looks up CUSTOMER-SERVICE in Eureka                         │
│                    │                                                     │
│                    │ Found: localhost:8081                              │
│                    ▼                                                     │
│  3. Gateway forwards request to Customer Service                        │
│     GET http://localhost:8081/api/customers/1                           │
│                    │                                                     │
│                    ▼                                                     │
│  4. Customer Service queries customer_schema.customers table            │
│                    │                                                     │
│                    ▼                                                     │
│  5. Response flows back: Customer Service → Gateway → Client            │
│                                                                          │
│  Response:                                                              │
│  {                                                                       │
│    "id": 1,                                                             │
│    "name": "John Doe",                                                  │
│    "email": "john@example.com",                                         │
│    "phone": "123-456-7890",                                             │
│    "address": "123 Main St"                                             │
│  }                                                                       │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

### Example 2: Create Bill (Inter-Service Communication)

```
┌─────────────────────────────────────────────────────────────────────────┐
│  POST http://localhost:8080/api/bills                                   │
│  Body: { "customerId": 1, "productId": 1, "quantity": 2 }              │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  1. Client ──▶ Gateway Service (8080)                                   │
│                    │                                                     │
│                    │ Route: /api/bills/** → BILLING-SERVICE             │
│                    ▼                                                     │
│  2. Gateway forwards to Billing Service                                 │
│                    │                                                     │
│                    ▼                                                     │
│  3. Billing Service receives request                                    │
│     ┌─────────────────────────────────────────────────────────────┐    │
│     │  @PostMapping                                               │    │
│     │  public ResponseEntity<Bill> createBill(@RequestBody Bill)  │    │
│     │  {                                                          │    │
│     │      // Need to validate customer exists                    │    │
│     │      // Need to get product price                           │    │
│     │  }                                                          │    │
│     └─────────────────────────────────────────────────────────────┘    │
│                    │                                                     │
│                    ▼                                                     │
│  4. Billing Service uses Feign to call Inventory Service               │
│     productServiceClient.getProductById(1)                              │
│                    │                                                     │
│                    │  ┌─────────────────────────────────────┐          │
│                    │  │ Feign → Eureka: Where is           │          │
│                    │  │         INVENTORY-SERVICE?          │          │
│                    │  │                                     │          │
│                    │  │ Eureka → Feign: localhost:8082     │          │
│                    │  │                                     │          │
│                    │  │ Feign → GET localhost:8082/api/    │          │
│                    │  │               products/1            │          │
│                    │  └─────────────────────────────────────┘          │
│                    │                                                     │
│                    │  Returns: { "price": 999.99, ... }                │
│                    ▼                                                     │
│  5. Billing Service uses Feign to call Customer Service                │
│     customerServiceClient.getCustomerById(1)                            │
│                    │                                                     │
│                    │  (Similar process as step 4)                       │
│                    ▼                                                     │
│  6. Billing Service calculates total: 999.99 × 2 = 1999.98             │
│                    │                                                     │
│                    ▼                                                     │
│  7. Billing Service saves bill to billing_schema.bills                 │
│                    │                                                     │
│                    ▼                                                     │
│  8. Response flows back: Billing → Gateway → Client                    │
│                                                                          │
│  Response:                                                              │
│  {                                                                       │
│    "id": 1,                                                             │
│    "customerId": 1,                                                     │
│    "productId": 1,                                                      │
│    "quantity": 2,                                                       │
│    "totalAmount": 1999.98,                                              │
│    "createdAt": "2025-11-29T10:30:00"                                   │
│  }                                                                       │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

### Example 3: Get Bill with Details (Aggregating Data)

```
┌─────────────────────────────────────────────────────────────────────────┐
│  GET http://localhost:8080/api/bills/1                                  │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  This endpoint is special - it returns the bill PLUS customer           │
│  and product details from other services!                               │
│                                                                          │
│  1. Request reaches Billing Service                                     │
│                    │                                                     │
│  2. Billing Service queries its database for the bill                   │
│                    │                                                     │
│  3. Billing Service calls Customer Service via Feign                    │
│                    │                                                     │
│  4. Billing Service calls Inventory Service via Feign                   │
│                    │                                                     │
│  5. Billing Service combines all data                                   │
│                    │                                                     │
│                    ▼                                                     │
│  Response:                                                              │
│  {                                                                       │
│    "bill": {                                                            │
│      "id": 1,                                                           │
│      "customerId": 1,                                                   │
│      "productId": 1,                                                    │
│      "quantity": 2,                                                     │
│      "totalAmount": 1999.98                                             │
│    },                                                                    │
│    "customer": {                        ← From Customer Service         │
│      "id": 1,                                                           │
│      "name": "John Doe",                                                │
│      "email": "john@example.com"                                        │
│    },                                                                    │
│    "product": {                         ← From Inventory Service        │
│      "id": 1,                                                           │
│      "name": "Laptop",                                                  │
│      "price": 999.99                                                    │
│    }                                                                     │
│  }                                                                       │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 12. Key Concepts Summary

### 🎯 Service Discovery (Eureka)

- **What:** A registry that keeps track of all running services
- **Why:** Services can find each other without hardcoding URLs
- **How:** Services register on startup, send heartbeats, query for other services

### 🔧 Configuration Server

- **What:** Centralized configuration management
- **Why:** Single source of truth, no need to restart services for config changes
- **How:** Services fetch their config from Config Server on startup

### 🚪 API Gateway

- **What:** Single entry point for all client requests
- **Why:** Simplifies client code, centralizes cross-cutting concerns
- **How:** Routes requests based on URL patterns to appropriate services

### 📞 Inter-Service Communication (OpenFeign)

- **What:** Declarative HTTP client for service-to-service calls
- **Why:** Makes calling other services as simple as calling local methods
- **How:** Define interfaces with annotations, Spring creates the implementation

### 🗄️ Database Strategy

- **What:** Each service has its own data storage (schema isolation)
- **Why:** Services are independent, can't accidentally access other services' data
- **How:** Separate PostgreSQL schemas for each service

---

## 🚀 Quick Reference: Service Startup Order

```
1. PostgreSQL Database    ─────▶  Must be first (other services need it)
         │
         ▼
2. Eureka Discovery       ─────▶  Must be second (other services register here)
         │
         ▼
3. Config Service         ─────▶  Provides configuration to other services
         │
         ▼
4. Customer Service  ─┐
   Inventory Service ─┼───────▶  Business services (can start in parallel)
   Billing Service   ─┘
         │
         ▼
5. Gateway Service        ─────▶  Routes traffic to all services
```

---

## 📚 Additional Resources

- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)
- [Netflix Eureka Wiki](https://github.com/Netflix/eureka/wiki)
- [Spring Cloud OpenFeign](https://spring.io/projects/spring-cloud-openfeign)
- [Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway)
- [Microservices.io Patterns](https://microservices.io/patterns/)

---

## 🎓 Key Takeaways

1. **Microservices = Small, Independent Services** that communicate over the network
2. **Service Discovery** eliminates hardcoded URLs between services
3. **API Gateway** provides a single entry point for clients
4. **OpenFeign** makes inter-service HTTP calls feel like local method calls
5. **Config Server** centralizes configuration management
6. **Each service owns its data** - no direct database sharing

---

_Happy Learning! 🚀_
