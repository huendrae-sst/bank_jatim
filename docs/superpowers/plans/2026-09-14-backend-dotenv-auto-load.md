# Backend `.env` Auto-Load Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make `mvn spring-boot:run`, when executed from `backend`, automatically load `backend/.env` and connect to its configured remote database.

**Architecture:** Use Spring Boot Config Data's optional extension hint to load the extensionless `.env` file as Java properties. Preserve the existing environment post-processor for `DATABASE_URL` conversion and preserve deployment environment variables as higher-precedence overrides.

**Tech Stack:** Java 21, Spring Boot 4.1.1 Config Data, Maven, JUnit 6, AssertJ.

## Global Constraints

- The supported working directory is `backend`.
- A missing `.env` file must not fail application startup.
- Operating-system and container environment variables must override `.env` values.
- Do not edit Flyway migrations, seed data, or application data.
- Runtime verification must prove the datasource host is not localhost.

---

### Task 1: Import and verify `backend/.env`

**Files:**
- Modify: `backend/src/test/java/com/bankjatim/jims/config/DatabaseUrlEnvironmentPostProcessorTest.java`
- Modify: `backend/src/main/resources/application.yml`
- Modify: `README.md`

**Interfaces:**
- Consumes: Spring Boot's `spring.config.import` property and the existing `DatabaseUrlEnvironmentPostProcessor`.
- Produces: an optional `file:.env[.properties]` import available during environment preparation.

- [ ] **Step 1: Write the failing configuration contract**

Add a test that loads the first YAML document and checks the exact optional import:

```java
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.List;

@Test
void applicationConfigurationImportsOptionalDotenv() throws IOException {
    List<PropertySource<?>> sources = new YamlPropertySourceLoader()
            .load("application", new ClassPathResource("application.yml"));

    assertThat(sources)
            .extracting(source -> source.getProperty("spring.config.import"))
            .contains("optional:file:.env[.properties]");
}
```

- [ ] **Step 2: Run the focused test and verify RED**

Run:

```bash
cd backend
env JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home mvn -Dtest=DatabaseUrlEnvironmentPostProcessorTest test
```

Expected: FAIL because `spring.config.import` is absent.

- [ ] **Step 3: Add the optional Config Data import**

Add beneath the existing top-level `spring` mapping in `application.yml`:

```yaml
spring:
  config:
    import: optional:file:.env[.properties]
```

- [ ] **Step 4: Run the focused test and verify GREEN**

Run the command from Step 2.

Expected: every `DatabaseUrlEnvironmentPostProcessorTest` test passes.

- [ ] **Step 5: Document the one-command startup behavior**

Add a note below the backend startup command in `README.md` stating that running from `backend` automatically loads `backend/.env`, while deployment environment variables remain valid overrides.

- [ ] **Step 6: Run the clean backend build**

Run:

```bash
cd backend
env JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home mvn clean package
```

Expected: all tests pass and the executable JAR is rebuilt.

- [ ] **Step 7: Verify one-command remote startup**

From `backend`, ensure `DATABASE_URL` and `SPRING_DATASOURCE_URL` are unset in the shell, then run:

```bash
mvn spring-boot:run
```

Expected: logs show Java 21, Spring Boot 4.1.1, Flyway connecting to the remote PostgreSQL host configured in `.env`, schema version 17, and Tomcat listening on port 8080. Stop the process after checking liveness and readiness.

- [ ] **Step 8: Verify application data remains unchanged**

Use the database URL from `.env` only for a read-only count query before and after startup:

```sql
SELECT 'orders', COUNT(*)::text FROM orders
UNION ALL SELECT 'roles', COUNT(*)::text FROM roles
UNION ALL SELECT 'role_menu_permissions', COUNT(*)::text FROM role_menu_permissions;
```

Expected: all three counts are identical before and after startup.

- [ ] **Step 9: Commit the implementation**

```bash
git add backend/src/test/java/com/bankjatim/jims/config/DatabaseUrlEnvironmentPostProcessorTest.java backend/src/main/resources/application.yml README.md
git commit -m "fix: load backend dotenv during local startup"
```
