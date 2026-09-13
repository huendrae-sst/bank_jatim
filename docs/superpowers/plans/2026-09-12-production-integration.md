# Production Integration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make Bank Jatim JIMS production-ready by wiring frontend, backend, and PostgreSQL through environment-driven configuration and removing mock authentication fallback.

**Architecture:** Add a small Spring Boot environment post-processor that maps `DATABASE_URL` to Spring datasource properties before auto-configuration. Keep the Vue API client env-driven and remove auth mock session creation so the frontend reflects backend/database availability truthfully.

**Tech Stack:** Vue 3, Vite, Pinia, Axios, Spring Boot 3.3, Java 17, PostgreSQL, Flyway, JUnit 5.

## Global Constraints

- Do not hard-code the production database password in committed application defaults.
- Support `DATABASE_URL="postgresql://admin:aninza@192.168.18.67:5432/bank_jatim"`.
- Remove mock authentication fallback from production source code.
- Preserve existing local development defaults unless overridden by environment variables.
- Explicit `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD` must take precedence over `DATABASE_URL`.

---

### Task 1: Backend DATABASE_URL Adapter

**Files:**
- Create: `backend/src/main/java/com/bankjatim/jims/config/DatabaseUrlEnvironmentPostProcessor.java`
- Create: `backend/src/main/resources/META-INF/spring.factories`
- Create: `backend/src/test/java/com/bankjatim/jims/config/DatabaseUrlEnvironmentPostProcessorTest.java`
- Modify: `backend/src/main/java/com/bankjatim/jims/config/SecurityConfig.java`

**Interfaces:**
- Consumes: environment variable `DATABASE_URL`.
- Produces: Spring properties `spring.datasource.url`, `spring.datasource.username`, and `spring.datasource.password`.
- Produces: environment-driven CORS origins through `app.cors.allowed-origins`.

- [ ] **Step 1: Write the failing test**

```java
@Test
void mapsDatabaseUrlToSpringDatasourceProperties() {
    MockEnvironment environment = new MockEnvironment()
            .withProperty("DATABASE_URL", "postgresql://admin:aninza@192.168.18.67:5432/bank_jatim");

    new DatabaseUrlEnvironmentPostProcessor().postProcessEnvironment(environment, new SpringApplication());

    assertThat(environment.getProperty("spring.datasource.url"))
            .isEqualTo("jdbc:postgresql://192.168.18.67:5432/bank_jatim");
    assertThat(environment.getProperty("spring.datasource.username")).isEqualTo("admin");
    assertThat(environment.getProperty("spring.datasource.password")).isEqualTo("aninza");
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd backend && mvn test -Dtest=DatabaseUrlEnvironmentPostProcessorTest`

Expected: FAIL because `DatabaseUrlEnvironmentPostProcessor` does not exist.

- [ ] **Step 3: Write minimal implementation**

Create `DatabaseUrlEnvironmentPostProcessor` implementing `EnvironmentPostProcessor`, parse the URI user info, host, port, path, and query, and add a first-priority property source only when explicit Spring datasource properties are not already present.

- [ ] **Step 4: Register post-processor**

Create `META-INF/spring.factories` with:

```properties
org.springframework.boot.env.EnvironmentPostProcessor=\
com.bankjatim.jims.config.DatabaseUrlEnvironmentPostProcessor
```

- [ ] **Step 5: Run test to verify it passes**

Run: `cd backend && mvn test -Dtest=DatabaseUrlEnvironmentPostProcessorTest`

Expected: PASS.

### Task 2: Production Frontend Auth Behavior

**Files:**
- Modify: `frontend/src/api/client.js`
- Modify: `frontend/src/stores/auth.js`
- Create: `frontend/.env.production.example`
- Create: `backend/.env.production.example`

**Interfaces:**
- Consumes: `VITE_API_BASE_URL`.
- Produces: Axios client base URL and real backend-only auth behavior.

- [ ] **Step 1: Update API client**

Use `import.meta.env.VITE_API_BASE_URL || '/api'` as the Axios `baseURL`.

- [ ] **Step 2: Remove auth fallback**

In `login`, return `{ success: false, message }` on API failure. Remove fallback mock user creation and remove the quick-switch action that creates mock JWT sessions.

- [ ] **Step 3: Add env examples**

Backend example contains `DATABASE_URL=postgresql://admin:aninza@192.168.18.67:5432/bank_jatim`. Frontend example contains `VITE_API_BASE_URL=/api`.

- [ ] **Step 4: Build frontend**

Run: `cd frontend && npm run build`

Expected: PASS.

### Task 3: Production Documentation

**Files:**
- Modify: `README.md`

**Interfaces:**
- Consumes: production environment variables.
- Produces: deploy/run instructions for backend and frontend.

- [ ] **Step 1: Document production env**

Add backend and frontend production env examples, including `DATABASE_URL`, Redis/RabbitMQ/MinIO variables, `JWT_SECRET`, and `VITE_API_BASE_URL`.

- [ ] **Step 2: Document verification**

Add commands for backend tests, frontend build, backend startup, health endpoint, and login validation.

- [ ] **Step 3: Final verification**

Run: `cd backend && mvn test` and `cd frontend && npm run build`.

Expected: both PASS.
