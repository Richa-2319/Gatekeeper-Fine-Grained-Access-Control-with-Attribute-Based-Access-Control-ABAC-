# 🛡️ Gatekeeper ABAC System

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-green.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7-red.svg)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-Compose-blue.svg)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A **fine-grained access control system** implementing **Attribute-Based Access Control (ABAC)** for enterprise-grade authorization. Unlike traditional role-based systems, Gatekeeper dynamically evaluates access requests based on user attributes, resource context, environmental factors, and custom policies.

## 🚀 Features

### 🔐 **Advanced Authorization**
- **Attribute-Based Access Control (ABAC)** - Dynamic policy evaluation
- **Real-time Policy Updates** - Kafka-powered policy synchronization
- **Multi-factor Decision Making** - User, resource, action, context, and time-based rules
- **Custom Policy Engine** - Support for complex business rules
- **Open Policy Agent (OPA)** integration for advanced Rego policies

### ⚡ **High Performance**
- **Sub-millisecond Policy Lookups** - Redis caching layer
- **Intelligent Caching** - Decision and policy caching with TTL
- **Async Operations** - Non-blocking audit logging and notifications
- **Horizontal Scaling** - Stateless architecture with shared cache

### 🔍 **Enterprise Features**
- **Comprehensive Auditing** - Every access decision logged
- **JWT Authentication** - Secure token-based authentication
- **RESTful APIs** - Clean API design for easy integration
- **Health Monitoring** - Spring Actuator endpoints
- **Docker Ready** - Complete containerization support

### 🛠️ **Developer Experience**
- **Hot Policy Reloading** - No downtime for policy updates
- **Extensive Logging** - Debug-friendly logging at all levels
- **API Documentation** - Ready-to-use API endpoints
- **Test Coverage** - Comprehensive test suite

## 🏗️ Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Client App    │───▶│  Gatekeeper API  │───▶│ Policy Engine   │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                              │                          │
                              ▼                          ▼
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│  Audit Service  │◀───│      Redis       │───▶│      OPA        │
└─────────────────┘    │     Cache        │    │  (Optional)     │
         │              └──────────────────┘    └─────────────────┘
         ▼                        │                       │
┌─────────────────┐              │               ┌─────────────────┐
│   PostgreSQL    │◀─────────────┼──────────────▶│     Kafka       │
│   (Audit Logs,  │              │               │ (Policy Updates)│
│   Users, Policies)│            │               └─────────────────┘
└─────────────────┘              │
                                 ▼
                       ┌──────────────────┐
                       │  Policy Updates  │
                       │   Broadcasting   │
                       └──────────────────┘
```

## 🛠️ Tech Stack

| Component | Technology | Purpose |
|-----------|------------|---------|
| **Backend** | Spring Boot 3.2.0 | Main application framework |
| **Database** | PostgreSQL 15 | User data, policies, audit logs |
| **Cache** | Redis 7 | High-speed policy and decision caching |
| **Messaging** | Apache Kafka | Real-time policy update distribution |
| **Policy Engine** | Custom + OPA | Rule evaluation and decision making |
| **Authentication** | JWT | Secure token-based auth |
| **Containerization** | Docker Compose | Development and deployment |
| **Monitoring** | Spring Actuator | Health checks and metrics |

## 🚀 Quick Start

### Prerequisites

- **Java 17+**
- **Maven 3.8+**
- **Docker & Docker Compose**
- **Git**

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/gatekeeper-abac.git
cd gatekeeper-abac
```

### 2. Start Infrastructure Services

```bash
# Start PostgreSQL, Redis, Kafka, and OPA
docker-compose up -d

# Verify services are running
docker-compose ps
```

### 3. Build and Run the Application

```bash
# Compile the project
mvn clean compile

# Start the application
mvn spring-boot:run
```

### 4. Verify Installation

```bash
# Health check
curl http://localhost:8080/actuator/health

# Expected response:
# {"status":"UP","components":{"db":{"status":"UP"},"redis":{"status":"UP"}}}
```

## 📝 API Usage

### Authentication

#### Login
```bash
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "password"
}

# Response:
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "username": "admin",
  "role": "admin",
  "expiresIn": 86400000
}
```

#### Register User
```bash
POST /api/auth/register
Content-Type: application/json

{
  "username": "john.doe",
  "password": "securepassword",
  "email": "john@example.com",
  "role": "user",
  "department": "engineering",
  "location": "office"
}
```

### Authorization

#### Check Access
```bash
POST /api/auth/authorize
Authorization: Bearer <your-jwt-token>
Content-Type: application/json

{
  "resource": "user-data",
  "action": "read",
  "context": {
    "department": "engineering",
    "sensitivity": "low",
    "requestTime": "09:30"
  }
}

# Response:
{
  "allowed": true,
  "decision": "PERMIT",
  "reason": "Access granted by applicable policies",
  "appliedPolicies": ["business-hours-access", "department-data-access"],
  "evaluatedAt": "2024-01-15T09:30:00",
  "evaluationTimeMs": 15
}
```

### Policy Management

#### Create Policy
```bash
POST /api/policies
Authorization: Bearer <admin-jwt-token>
Content-Type: application/json

{
  "name": "sensitive-data-policy",
  "regoRule": "business_hours_access AND office_location_access",
  "description": "Sensitive data requires office access during business hours",
  "resource": "sensitive-*",
  "action": "*",
  "active": true,
  "priority": 90
}
```

#### List All Policies
```bash
GET /api/policies
Authorization: Bearer <your-jwt-token>
```

#### Update Policy
```bash
PUT /api/policies/{id}
Authorization: Bearer <admin-jwt-token>
Content-Type: application/json

{
  "name": "updated-policy-name",
  "regoRule": "new rule logic",
  "active": true,
  "priority": 95
}
```

## 🔧 Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/gatekeeper` | Database connection |
| `SPRING_REDIS_HOST` | `localhost` | Redis server host |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka cluster |
| `GATEKEEPER_JWT_SECRET` | `myVerySecretKey...` | JWT signing secret |
| `GATEKEEPER_OPA_URL` | `http://localhost:8181` | OPA server URL |
| `GATEKEEPER_OPA_ENABLED` | `true` | Enable/disable OPA integration |

### Application Profiles

```yaml
# application-dev.yml
spring:
  profiles:
    active: dev
logging:
  level:
    com.gatekeeper: DEBUG

# application-prod.yml  
spring:
  profiles:
    active: prod
logging:
  level:
    com.gatekeeper: INFO
gatekeeper:
  audit:
    enabled: true
```

## 🔐 Policy Examples

### Time-Based Access
```yaml
name: "business-hours-only"
regoRule: "business_hours_access"
description: "Allow access only during business hours (9 AM - 5 PM)"
resource: "*"
action: "*"
priority: 50
```

### Department-Based Access
```yaml
name: "department-data-access"
regoRule: "department_access"
description: "Users can only access their department's data"
resource: "department-*"
action: "read"
priority: 60
```

### Location-Based Access
```yaml
name: "sensitive-office-only"
regoRule: "office_location_access"
description: "Sensitive resources require office location"
resource: "sensitive-*"
action: "*"
priority: 80
```

### Admin Override
```yaml
name: "admin-full-access"
regoRule: "input.user.role == \"admin\""
description: "Administrators have unrestricted access"
resource: "*"
action: "*"
priority: 100
```

## 🧪 Testing

### Run Unit Tests
```bash
mvn test
```

### Integration Testing
```bash
# Start infrastructure
docker-compose up -d

# Run integration tests
mvn verify
```

### Manual API Testing
```bash
# Test script
chmod +x scripts/test-api.sh
./scripts/test-api.sh
```

## 📊 Monitoring & Observability

### Health Endpoints
```bash
# Application health
GET /actuator/health

# Detailed health info
GET /actuator/health/detailed

# Application metrics
GET /actuator/metrics
```

### Audit Logs
```sql
-- View recent access decisions
SELECT user_id, resource, action, decision, timestamp 
FROM audit_logs 
ORDER BY timestamp DESC 
LIMIT 10;

-- Failed access attempts
SELECT user_id, resource, reason, timestamp
FROM audit_logs 
WHERE decision = 'DENY'
ORDER BY timestamp DESC;
```

### Performance Metrics
```bash
# Redis cache statistics
docker exec gatekeeper-redis redis-cli info stats

# Database connections
docker exec gatekeeper-postgres pg_stat_activity
```

## 🚀 Deployment

### Docker Deployment
```bash
# Build application image
docker build -t gatekeeper-abac:latest .

# Deploy with Docker Compose
docker-compose -f docker-compose-prod.yml up -d
```

### Production Configuration
```yaml
# docker-compose-prod.yml
services:
  gatekeeper-app:
    image: gatekeeper-abac:latest
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - GATEKEEPER_JWT_SECRET=your-production-secret-key
      - SPRING_DATASOURCE_PASSWORD=secure-db-password
    deploy:
      replicas: 3
      restart_policy:
        condition: on-failure
```

### Scaling Guidelines

#### Horizontal Scaling
- **Application**: Stateless design allows multiple instances
- **Database**: Use read replicas for policy lookups
- **Cache**: Redis cluster for high availability
- **Message Queue**: Kafka partitioning for load distribution

#### Performance Tuning
```yaml
# Redis optimization
spring:
  redis:
    lettuce:
      pool:
        max-active: 20
        max-idle: 10

# Database optimization  
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
```

## 🔍 Troubleshooting

### Common Issues

#### Service Connection Issues
```bash
# Check service health
docker-compose ps
docker-compose logs <service-name>

# Test connections
telnet localhost 5432  # PostgreSQL
telnet localhost 6379  # Redis  
telnet localhost 9092  # Kafka
```

#### Policy Evaluation Issues
```bash
# Enable debug logging
logging.level.com.gatekeeper.util.PolicyEvaluator: DEBUG

# Check OPA service
curl http://localhost:8181/health
```

#### Cache Issues
```bash
# Clear Redis cache
docker exec gatekeeper-redis redis-cli FLUSHALL

# Monitor cache usage
docker exec gatekeeper-redis redis-cli MONITOR
```

### Performance Issues

#### Slow Authorization Requests
1. Check Redis connectivity
2. Review policy complexity
3. Monitor database query performance
4. Verify JVM heap settings

#### High Memory Usage
1. Tune Redis memory limits
2. Optimize policy caching TTL
3. Review audit log retention

## 🤝 Contributing

1. **Fork the repository**
2. **Create a feature branch**: `git checkout -b feature/amazing-feature`
3. **Commit changes**: `git commit -m 'Add amazing feature'`
4. **Push to branch**: `git push origin feature/amazing-feature`
5. **Open a Pull Request**

### Development Setup
```bash
# Install development dependencies
mvn clean compile

# Run in development mode
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Run tests with coverage
mvn clean test jacoco:report
```

## 📄 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

- **Documentation**: [Wiki Pages](https://github.com/your-username/gatekeeper-abac/wiki)
- **Issues**: [GitHub Issues](https://github.com/your-username/gatekeeper-abac/issues)
- **Discussions**: [GitHub Discussions](https://github.com/your-username/gatekeeper-abac/discussions)
- **Email**: support@gatekeeper-abac.com

## 🙏 Acknowledgments

- [Spring Boot](https://spring.io/projects/spring-boot) - Application framework
- [Open Policy Agent](https://www.openpolicyagent.org/) - Policy engine
- [Redis](https://redis.io/) - Caching layer
- [Apache Kafka](https://kafka.apache.org/) - Messaging platform
- [PostgreSQL](https://www.postgresql.org/) - Database system

---



For more information, visit our [documentation(https://github.com/Richa-2319/Gatekeeper-Fine-Grained-Access-Control-with-Attribute-Based-Access-Control-ABAC-/wiki) or check out the [API reference](https://github.com/your-username/gatekeeper-abac/blob/main/docs/API.md).
