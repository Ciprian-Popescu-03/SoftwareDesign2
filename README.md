# Order Management Platform 🛒

Welcome to the **Order Management Platform**, a full-stack web application built with **Spring Boot** and **Angular**.  
Its purpose is to provide a secure, role-based environment where administrators and customers can manage users, products, and orders with ease.  
From placing orders and browsing products to managing accounts and resetting passwords — all through a clean, responsive interface.

---

# Description 📖

Upon entering the app, users are directed to a **Login Page** where they authenticate with their email and password.  
Based on their role, they are redirected to their dedicated dashboard:

- **Administrators** land on the **Person Management** view, where they have full control over users, products, and orders.
- **Customers** land on their **Customer Dashboard**, where they can browse products, place orders, and manage their own bookings.

All data is securely persisted in a **PostgreSQL** database, with passwords hashed via **BCrypt** and sessions protected by **JWT tokens**.

---

# Features 🪄

- **Role-Based Access Control**: Separate ADMIN and CUSTOMER roles enforced on both the backend (Spring Security) and frontend (Angular route guards and HTTP interceptors).
- **JWT Authentication**: Stateless authentication using signed JWT tokens, automatically attached to every outgoing request via an Angular interceptor.
- **Person Management (Admin)**: Full CRUD operations for user accounts — create, view, update, and delete persons.
- **Product Management**: Admins can create, update, and delete products; customers have read-only access to the product catalogue.
- **Order Management**: Customers can place orders, add products to existing orders, view their order history, and delete orders. Admins can view and delete any order.
- **Password Reset Flow**: A two-step email-based reset — users receive a 6-digit code and submit a new password to regain access.
- **Client-Side Filtering & Sorting**: Multi-criteria search and filtering on Person and Product tables using Angular's `MatTableDataSource` for instant, zero-latency results.
- **Secure Password Validation**: Custom `@StrongPassword` Bean Validation annotation enforces password complexity at the data layer.
- **Global Exception Handling**: A `GlobalExceptionHandler` returns standardised JSON error responses with meaningful HTTP status codes.
- **Comprehensive Test Suite**: Unit tests (JUnit 5 + Mockito) for all services; integration tests (MockMvc + H2) for all controllers, including JWT protection and role enforcement.
- **Responsive Design**: Clean, user-friendly interface built with Angular Material.
- **Database Integration**: All data managed and persisted securely using **PostgreSQL** via Hibernate/JPA.

---

# Tech Stack 🛠

### Back-End:
- Spring Boot (Java)
- Spring Security (JWT + BCrypt)
- RESTful APIs
- PostgreSQL + Hibernate/JPA
- JavaMailSender (Email Service)
- JUnit 5 + Mockito + MockMvc (Testing)

### Front-End:
- Angular (SPA)
- Angular Material (UI Components)
- HTTP Interceptors & Route Guards

### Tools:
- IntelliJ IDEA (Backend development)
- DBeaver (Database management)
- Postman (API testing)
- Maven (Dependency management)
- H2 (In-memory database for integration tests)

---

# Architecture 🏗

The application follows a **3-Tier Architecture** with a clear separation of concerns:

- **Presentation Tier (Angular SPA):** Handles UI rendering, user interactions, and HTTP communication. Route guards enforce frontend access control.
- **Logic Tier (Spring Boot):** Controllers receive HTTP requests, Services enforce business rules and validations, and a JWT filter authenticates every incoming request.
- **Data Tier (PostgreSQL + Hibernate/JPA):** Repositories extend `JpaRepository` for clean CRUD operations. Entity relationships (1:N Person-to-Orders, M:N Order-to-Products) are managed through JPA annotations.

---

# Installation ⚙️

## Prerequisites:
- Java 17 or newer
- Node.js & npm
- Angular CLI
- Maven
- PostgreSQL

## Back-End Setup:
```bash
# Clone the repository
git clone https://github.com/Ciprian-Popescu-03/SoftwareDesign2.git
cd SoftwareDesign2

# Configure your PostgreSQL connection in src/main/resources/application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/your_db
spring.datasource.username=your_username
spring.datasource.password=your_password

# Build and run
mvn spring-boot:run
```

## Front-End Setup:
```bash
cd frontend

# Install dependencies
npm install

# Start the development server
ng serve
```

The app will be available at `http://localhost:4200`.

---

# API Overview 📡

| Method | Endpoint | Access |
|--------|----------|--------|
| POST | `/person/login` | Public |
| POST | `/forgot-password/request` | Public |
| POST | `/forgot-password/reset` | Public |
| GET | `/person` | ADMIN |
| POST / PUT / DELETE | `/person` | ADMIN |
| GET | `/product` | Authenticated |
| POST / PUT / DELETE | `/product` | ADMIN |
| GET / POST / DELETE | `/order` | Authenticated (scoped by role) |

---

# Testing 🧪

```bash
# Run all tests
mvn test
```

The test suite includes:
- **Unit Tests:** All service classes covered with JUnit 5 + Mockito.
- **Integration Tests:** All controller endpoints tested with MockMvc + H2, including JWT protection, role enforcement, and full password reset flow.
