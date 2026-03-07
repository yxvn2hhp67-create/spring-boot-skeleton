# spring-boot-skeleton

Minimal Spring Boot skeleton project with web, Thymeleaf, JPA, and H2.

## Requirements

- Java 21
- Maven 3.6+

## Running the application

**Maven:**

```bash
mvn spring-boot:run
```

The app runs at [http://localhost:8080](http://localhost:8080).

## Features

- **Web** — Spring MVC with Thymeleaf templates
- **JPA** — Spring Data JPA with Hibernate
- **H2** — In-memory database; H2 console at [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
- **DevTools** — Auto-restart and live reload during development

## Project structure

- `src/main/java/com/example/starter/` — Application and controllers
- `src/main/resources/templates/` — Thymeleaf HTML templates
- `src/main/resources/application.yml` — Server and datasource configuration
