# Java 21 and Spring Boot 4.1.1 Upgrade Design

## Objective

Upgrade the JIMS backend from Java 17 and Spring Boot 3.3.3 to Java 21 and Spring Boot 4.1.1 while preserving the existing REST API contracts, authentication behavior, Flyway history, and application data.

## Scope

The upgrade covers only the backend build and compatibility work required by the new platform baseline. The Vue frontend, endpoint URLs, response DTOs, PostgreSQL schema contents, and business workflows must remain unchanged.

The final supported runtime is:

- Java 21
- Spring Boot 4.1.1
- Spring Framework 7 as managed by Spring Boot
- Jakarta EE 11-compatible servlet and persistence APIs
- Maven build using `--release 21`

## Migration Strategy

The migration uses two verification checkpoints:

1. Raise Java to 21 and temporarily validate the application on the latest available Spring Boot 3.5.x maintenance release. This surfaces Java and late-Boot-3 deprecations separately from Boot 4 breaking changes.
2. Move to Spring Boot 4.1.1, update incompatible integrations, and retain only the final Boot 4 configuration in version control.

Spring Boot 4's classic compatibility starters may be used temporarily for diagnosis, but the committed result must use the modular Boot 4 starters directly. In particular, MVC applications should use `spring-boot-starter-webmvc` rather than the deprecated `spring-boot-starter-web` name.

## Required Compatibility Changes

### Build and dependencies

- Change `java.version` from 17 to 21.
- Change `spring-boot-starter-parent` from 3.3.3 to 4.1.1.
- Replace the MVC starter with `spring-boot-starter-webmvc` if required by Boot 4.1.1 dependency management.
- Upgrade `springdoc-openapi` from 2.5.0 to a stable 3.x release compatible with Spring Boot 4.
- Prefer versions managed by Spring Boot for PostgreSQL, Flyway, Redis, AMQP, Micrometer, testing, Jackson, Hibernate, and validation.
- Retain explicit versions for JJWT, MinIO, Apache POI, OpenPDF, OpenCSV, and Lombok only when they are not managed or require a newer compatible release.

### Bootstrap integration

`DatabaseUrlEnvironmentPostProcessor` currently imports the Boot 3 `org.springframework.boot.env.EnvironmentPostProcessor` type and registers it under that key in `META-INF/spring.factories`. Boot 4 moves the interface to `org.springframework.boot.EnvironmentPostProcessor`. Both the Java import and registration key must be updated while preserving parsing of `DATABASE_URL` and the separate username/password overrides.

### Web, security, and persistence

- Preserve the `/api` context path and every existing controller mapping.
- Preserve stateless JWT authentication and existing authorization outcomes.
- Compile and test against Spring Security 7 without weakening endpoint protection.
- Validate all entities and repositories against Jakarta Persistence 3.2 and Hibernate ORM 7.
- Preserve Flyway as the only schema migration mechanism; Hibernate must not create or mutate production schema.

### Tests

Boot 4 introduces JUnit 6 and changes portions of Spring's test integration. Existing controller and service tests must be adapted only where required. Tests must continue to assert the same API behavior, role rules, and database URL parsing.

## Database Safety

No migration file will be edited, reordered, deleted, or have its checksum changed. No new migration is expected because this is a runtime upgrade, not a domain-model change.

Automated tests run against the isolated test configuration. A final startup verification may connect to the PostgreSQL database defined in `backend/.env`, which currently points to the remote database rather than localhost. That verification may run Flyway validation, but it must not seed, truncate, or manually alter application tables.

## Verification

The upgrade is complete when all of the following pass:

- Maven resolves the final dependency graph without milestone or snapshot repositories.
- The complete backend test suite passes on Java 21.
- The application packages successfully with Java release 21 bytecode.
- Flyway validates all existing migrations without checksum or ordering errors.
- The application starts on port 8080 using the database configuration exported from `backend/.env`.
- PostgreSQL connectivity succeeds and the existing `orders`, `roles`, and `role_menu_permissions` data remains intact.
- `/api/actuator/health`, `/api/orders`, and `/api/master/roles` are reachable with their expected authentication behavior.
- No frontend source or API contract change is required.

## Rollback

The upgrade will be isolated in normal Git commits. If Boot 4 compatibility cannot be completed safely, the code can return to the previous Java 17/Spring Boot 3.3.3 commit without database rollback because the upgrade does not introduce schema migrations or destructive data operations.

## Out of Scope

- Frontend framework upgrades
- API redesigns
- Database schema redesigns
- Changes to role semantics or menu authorization
- Redis, RabbitMQ, MinIO, or PostgreSQL infrastructure upgrades
- Deployment or production rollout
