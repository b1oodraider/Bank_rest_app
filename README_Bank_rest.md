# Bank Card Management System

A comprehensive REST API for managing bank cards, users, and transfers built with Spring Boot 3 and Java 21.

## 🚀 Features

### Authentication & Authorization
- JWT-based authentication
- Role-based access control (ADMIN, USER)
- Secure password hashing with BCrypt

### Card Management
- Create, view, block, activate, and delete cards
- Card number encryption and masking
- Balance tracking
- Expiry date validation
- Status management (ACTIVE, BLOCKED, EXPIRED)

### Transfer System
- Transfer money between own cards
- Transaction validation and logging
- Insufficient funds protection

### User Management
- User registration and management
- Role assignment
- Secure authentication

## 🛠 Technology Stack

- **Java 21** - Latest LTS version
- **Spring Boot 3.5.3** - Modern Spring framework
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Data access layer
- **PostgreSQL** - Primary database
- **Liquibase** - Database migration tool
- **JWT** - Token-based authentication
- **Docker** - Containerization
- **Swagger/OpenAPI** - API documentation

## 📋 Requirements

- Java 21 or higher
- Maven 3.6+
- Docker and Docker Compose
- PostgreSQL 15+

## 🚀 Quick Start

### Using Docker (Recommended)

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd Bank_REST
   ```

2. **Start the application**
   ```bash
   docker-compose up -d
   ```

3. **Access the application**
   - API: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - Database Admin: http://localhost:8081 (Adminer)

### Manual Setup

1. **Start PostgreSQL database**
   ```bash
   docker-compose up db -d
   ```

2. **Run database migrations**
   ```bash
   mvn liquibase:update
   ```

3. **Start the application**
   ```bash
   mvn spring-boot:run
   ```

## 🔐 Default Users

The application comes with pre-configured users:

| Username | Password | Role |
|----------|----------|------|
| admin    | admin123 | ADMIN |
| user     | user123  | USER |

## 📚 API Documentation

### Authentication
- `POST /api/auth/login` - Authenticate and get JWT token

### Cards (User Operations)
- `GET /api/cards` - Get user's cards with pagination
- `POST /api/cards/{id}/block` - Request card blocking

### Cards (Admin Operations)
- `POST /api/cards` - Create new card
- `POST /api/cards/{id}/activate` - Activate card
- `DELETE /api/cards/{id}` - Delete card

### Transfers
- `POST /api/transfers` - Transfer between own cards

### Users (Admin Only)
- `POST /api/users` - Create new user

## 🗄 Database Schema

The application uses PostgreSQL with the following main tables:

- **users** - User accounts and authentication
- **user_roles** - User role assignments
- **cards** - Bank card information
- **transfers** - Transfer transaction history

## 🔧 Configuration

Key configuration properties in `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/bankdb
    username: postgres
    password: postgres

app:
  jwt:
    secret: YourSuperSecretKeyForJwtSigningMustBeLongEnough12345
    expiration-ms: 86400000
  encryption:
    secret: 1234567890123456
```

## 🧪 Testing

Run tests with:
```bash
mvn test
```

The project includes:
- Unit tests for services
- Integration tests for controllers
- Test coverage for critical business logic

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/example/bankrest/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST controllers
│   │   ├── dto/            # Data Transfer Objects
│   │   ├── entity/         # JPA entities
│   │   ├── exception/      # Custom exceptions
│   │   ├── repository/     # Data access layer
│   │   ├── security/       # Security components
│   │   └── service/        # Business logic
│   └── resources/
│       ├── db/             # Database migrations
│       └── application.yml # Configuration
└── test/                   # Test classes
```

## 🔒 Security Features

- **JWT Authentication** - Secure token-based authentication
- **Role-based Authorization** - Fine-grained access control
- **Password Encryption** - BCrypt hashing for passwords
- **Card Number Encryption** - AES encryption for sensitive data
- **Input Validation** - Comprehensive request validation
- **SQL Injection Protection** - JPA/Hibernate protection

## 🚀 Deployment

### Production Deployment

1. **Build the application**
   ```bash
   mvn clean package -DskipTests
   ```

2. **Deploy with Docker**
   ```bash
   docker build -t bank-rest-api .
   docker run -p 8080:8080 bank-rest-api
   ```

3. **Environment Variables**
   - Set `SPRING_DATASOURCE_URL` for production database
   - Configure `app.jwt.secret` with a strong secret
   - Set `app.encryption.secret` for card encryption

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License.

## 🆘 Support

For support and questions:
- Create an issue in the repository
- Contact: support@bankrest.com
