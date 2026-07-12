# VortexNet Distributed Commerce System

A comprehensive RESTful API for a distributed e-commerce platform, demonstrating professional-grade software architecture, design patterns, and microservices principles.

## 🎯 Project Overview

VortexNet is a fully functional e-commerce system backend that implements:
- Clean REST API design with proper HTTP semantics
- Three-layer architecture for maintainable code
- Dependency injection and IoC container patterns
- Database persistence with JPA/Hibernate
- Scalable application structure for distributed systems
- Best practices in Java enterprise development

## 🏗️ Architecture

### Three-Layer Architecture

```
┌─────────────────────────────────────┐
│      REST API Layer                 │
│    (HTTP Requests & Responses)      │
│    - Controllers                    │
│    - Route Mapping                  │
│    - Request Validation             │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│    Business Logic Layer             │
│    (Core Application Logic)         │
│    - Services                       │
│    - Business Rules                 │
│    - Data Processing                │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│    Data Access Layer                │
│    (Data Persistence)               │
│    - Repository Pattern             │
│    - Database Operations            │
│    - ORM Mapping                    │
└─────────────────────────────────────┘
```

## 🚀 Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Framework | Spring Boot | 3.5.15 |
| Language | Java | 17+ |
| Build Tool | Maven | 3.8+ |
| Database | MySQL | 8.0.46 |
| ORM | JPA/Hibernate | - |
| Server | Embedded Tomcat | - |
| API Style | RESTful | - |

## 📡 API Endpoints

### Assignment 1: Basic REST APIs

```
GET    /hello                    Simple greeting endpoint
GET    /greeting?name=xxx        Greeting with query parameter
GET    /user/{username}          Retrieve user information
POST   /api/users                Create new user
GET    /api/message              Retrieve message content
POST   /api/messages             Create new message
```

### Assignment 2: TODO Management (CRUD)

```
GET    /api/todos                Retrieve all TODO items
GET    /api/todos/{id}           Retrieve specific TODO by ID
POST   /api/todos                Create new TODO
PUT    /api/todos/{id}           Update existing TODO
DELETE /api/todos/{id}           Delete TODO
```

### Assignment 3: Product Management (CRUD) with Database

```
GET    /api/products             Retrieve all products
GET    /api/products/{id}        Retrieve specific product by ID
POST   /api/products             Create new product
PUT    /api/products/{id}        Update existing product
DELETE /api/products/{id}        Delete product
GET    /api/products/search?keyword=xxx   Search products by name
```

## 📊 Project Structure

```
distributed-commerce-system/
├── README.md                          # Project documentation
├── LICENSE                            # MIT License
├── .gitignore                         # Git configuration
├── CONTRIBUTING.md                    # Contribution guidelines
│
└── backend/
    ├── pom.xml                        # Maven configuration
    │
    └── src/main/
        ├── java/com/example/demo/
        │   ├── DemoApplication.java   # Spring Boot entry point
        │   │
        │   ├── controller/            # HTTP Request Handlers
        │   │   ├── HelloController.java      (Assignment 1)
        │   │   ├── TodoController.java      (Assignment 2)
        │   │   └── ProductController.java   (Assignment 3)
        │   │
        │   ├── service/               # Business Logic Layer
        │   │   ├── TodoService.java         (Assignment 2)
        │   │   └── ProductService.java      (Assignment 3)
        │   │
        │   ├── model/                 # Data Models
        │   │   └── Todo.java                (Assignment 2)
        │   │
        │   ├── entity/                # JPA Entities
        │   │   └── Product.java             (Assignment 3)
        │   │
        │   ├── repository/            # Data Access Layer
        │   │   └── ProductRepository.java   (Assignment 3)
        │   │
        │   └── dto/                   # Data Transfer Objects
        │       ├── Message.java             (Assignment 1)
        │       └── User.java               (Assignment 1)
        │
        └── resources/
            └── application.properties  # Database configuration
```

## 🔑 Key Features

### Assignment 1: REST API Fundamentals ✅
- 6 REST API endpoints
- GET requests with various parameter types
- POST requests with JSON body
- Response serialization to JSON

### Assignment 2: CRUD API with Service Layer ✅
- Complete CRUD operations (Create, Read, Update, Delete)
- Three-layer architecture implementation
- Dependency Injection with @Autowired
- Stream API for data queries
- AtomicLong for thread-safe ID generation
- In-memory data storage with ArrayList

### Assignment 3: Repository and Database ✅
- MySQL database integration
- JPA/Hibernate ORM implementation
- Repository pattern for data access
- Entity mapping with annotations
- Automatic table creation and schema management
- Data persistence across application restarts
- Complete CRUD with database operations

## 🛠️ Getting Started

### Prerequisites

```
- Java 17 or higher
- Maven 3.8 or higher
- MySQL 8.0 or higher
- Git for version control
- Postman or similar tool for API testing
- IntelliJ IDEA (recommended)
```

### Installation

1. **Clone the repository**
```bash
git clone https://github.com/AlexQuinn-Analytics/distributed-commerce-system.git
cd distributed-commerce-system
```

2. **Navigate to backend**
```bash
cd backend
```

3. **Configure MySQL**
- Ensure MySQL is running on localhost:3306
- Create database: `ecommerce_db`
- Update database credentials in `application.properties` if needed

4. **Run the application**
```bash
mvn spring-boot:run
```

5. **Verify startup**
```
The application will start on http://localhost:8080
You should see: "Started DemoApplication in X.XXX seconds"
```

## 🧪 API Testing with Postman

### Sample Requests

**Create a Product**
```
Method: POST
URL: http://localhost:8080/api/products
Headers: Content-Type: application/json
Body:
{
    "name": "Laptop",
    "price": 999.99,
    "description": "High-performance gaming laptop",
    "stock": 10
}

Expected Response: 201 Created
```

**Get All Products**
```
Method: GET
URL: http://localhost:8080/api/products

Expected Response: 200 OK
Body: [list of all products]
```

**Get Product by ID**
```
Method: GET
URL: http://localhost:8080/api/products/1

Expected Response: 200 OK
Body: {product details}
```

**Update Product**
```
Method: PUT
URL: http://localhost:8080/api/products/1
Headers: Content-Type: application/json
Body:
{
    "name": "Gaming Laptop Pro",
    "price": 1299.99,
    "description": "Ultimate gaming laptop",
    "stock": 5
}

Expected Response: 200 OK
```

**Delete Product**
```
Method: DELETE
URL: http://localhost:8080/api/products/1

Expected Response: 204 No Content
```

## 💡 Core Concepts Implemented

### Three-Layer Architecture
- **Controller Layer**: Handles HTTP requests and responses
- **Service Layer**: Contains business logic and validation
- **Repository Layer**: Manages data persistence with database

### Dependency Injection
- Uses `@Autowired` for loose coupling
- Spring IoC Container manages bean lifecycle
- Easy to test and maintain

### REST API Design
- Proper HTTP methods (GET, POST, PUT, DELETE)
- Meaningful URL paths
- Appropriate HTTP status codes
- JSON request/response format

### ORM with JPA/Hibernate
- `@Entity` annotation for database mapping
- `@Table` for table configuration
- `@Column` for column constraints
- `@Id` and `@GeneratedValue` for primary keys
- Automatic SQL generation and execution

### Design Patterns
- **MVC Pattern**: Separation of concerns
- **Dependency Injection**: Loose coupling
- **Repository Pattern**: Data access abstraction
- **Service Layer Pattern**: Business logic encapsulation

## 📈 Learning Outcomes

✅ Spring Boot fundamentals and configuration
✅ REST API design and implementation
✅ Three-layer architecture patterns
✅ Dependency injection and IoC containers
✅ JPA/Hibernate ORM and database mapping
✅ MySQL database integration
✅ Java 17+ features (Stream API, Lambda, Optional)
✅ Git and GitHub workflow
✅ Postman API testing
✅ Professional code organization

## 🚀 Future Enhancements

- Authentication with JWT tokens
- Input validation and error handling
- Unit and integration testing
- API documentation with Swagger/OpenAPI
- Pagination and filtering for list endpoints
- Database transaction management
- Caching with Redis
- Docker containerization
- Deployment to cloud platforms (AWS/Azure)

## 🤝 Contributing

Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines on how to contribute to this project.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👤 Author

**Alex Quinn**
- GitHub: [@AlexQuinn-Analytics](https://github.com/AlexQuinn-Analytics)
- Project: VortexNet Distributed Commerce System

## 📊 Project Statistics

- **Total API Endpoints**: 16 (6 from Assignment 1, 5 from Assignment 2, 5 from Assignment 3)
- **Code Files**: 10+ Java files
- **Architecture Layers**: 3 (Controller, Service, Repository)
- **Database Tables**: 2 (products, todos - in-memory)
- **Design Patterns**: 4+ implemented patterns
- **Status**: Active Development

---

**Last Updated**: July 12, 2026  
**Current Status**: Assignment 3 Complete ✅  
**Maintenance**: Active  

Made with ❤️ by Alex Quinn
