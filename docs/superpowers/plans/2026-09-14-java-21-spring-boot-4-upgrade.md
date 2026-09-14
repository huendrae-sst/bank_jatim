# Java 21 and Spring Boot 4.1.1 Upgrade Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Upgrade the JIMS backend to Java 21 and Spring Boot 4.1.1 without changing its REST contracts, Flyway history, or application data.

**Architecture:** Use Spring Boot 3.5.16 as a temporary compatibility checkpoint, then migrate the final build to Boot 4.1.1 and its modular starters. Preserve the existing layered controllers/services/repositories and adapt only platform integration points: bootstrap environment processing, Jackson 3, Redis serialization, tests, and runtime documentation.

**Tech Stack:** Java 21, Spring Boot 4.1.1, Spring Framework 7, Spring Security 7, Spring Data JPA/Redis 4, Hibernate ORM 7, Jackson 3, Flyway, PostgreSQL, Maven, JUnit 6.

## Global Constraints

- Final runtime and compiler release must be Java 21.
- Final Spring Boot parent must be exactly 4.1.1 and must not use milestone or snapshot repositories.
- Use Spring Boot 3.5.16 only as an intermediate verification checkpoint.
- Preserve `/api`, all controller mappings, DTO shapes, JWT behavior, and authorization outcomes.
- Do not edit, reorder, delete, or add Flyway migrations for this platform-only upgrade.
- Do not seed, truncate, or manually mutate application data.
- Final remote verification must load `backend/.env` with exported variables and must not fall back to localhost PostgreSQL.
- Preserve unrelated workspace changes, including `erd.drawio`, `.$erd.drawio.bkp`, and Graphify files.

---

### Task 1: Establish Java 21 and Spring Boot 3.5 checkpoint

**Files:**
- Create: `backend/src/test/java/com/bankjatim/jims/PlatformUpgradeContractTest.java`
- Modify: `backend/pom.xml`

**Interfaces:**
- Consumes: the existing Maven project and all existing backend tests.
- Produces: a Java 21 build that passes on Spring Boot 3.5.16 before any Boot 4 API migration.

- [ ] **Step 1: Stop the currently running Java 17 backend**

Run:

```bash
lsof -nP -iTCP:8080 -sTCP:LISTEN
kill 44807
```

Expected: port 8080 is released. If the PID differs, resolve the exact Java process from `lsof` and stop only that process.

- [ ] **Step 2: Write the failing Java runtime contract test**

Create:

```java
package com.bankjatim.jims;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformUpgradeContractTest {

    @Test
    void requiresJava21OrNewer() {
        assertThat(Runtime.version().feature()).isGreaterThanOrEqualTo(21);
    }
}
```

- [ ] **Step 3: Run the contract on the current Java 17 runtime and verify RED**

Run:

```bash
cd backend
env JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home mvn -Dtest=PlatformUpgradeContractTest test
```

Expected: FAIL because the runtime feature is 17, not at least 21.

- [ ] **Step 4: Install or locate Java 21**

First check `/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home`. If absent, install the Homebrew formula:

```bash
brew install openjdk@21
```

Verify:

```bash
env JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home /opt/homebrew/opt/openjdk@21/bin/java -version
```

Expected: Java reports major version 21.

- [ ] **Step 5: Upgrade the checkpoint build configuration**

Update the parent and compiler property in `backend/pom.xml`:

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.16</version>
    <relativePath/>
</parent>
```

```xml
<java.version>21</java.version>
```

- [ ] **Step 6: Verify GREEN on the Boot 3.5 checkpoint**

Run:

```bash
cd backend
env JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home mvn clean test
```

Expected: the Java contract and complete existing backend suite pass on Java 21 and Spring Boot 3.5.16. The existing Redis serializer may be reported as deprecated; Task 3 replaces it with the Spring Data Redis 4 Jackson 3 serializer.

- [ ] **Step 7: Commit the checkpoint**

```bash
git add backend/pom.xml backend/src/test/java/com/bankjatim/jims/PlatformUpgradeContractTest.java
git commit -m "build: establish Java 21 Boot 3.5 checkpoint"
```

---

### Task 2: Define Boot 4 bootstrap and version contracts

**Files:**
- Modify: `backend/src/test/java/com/bankjatim/jims/PlatformUpgradeContractTest.java`
- Modify: `backend/src/test/java/com/bankjatim/jims/config/DatabaseUrlEnvironmentPostProcessorTest.java`

**Interfaces:**
- Consumes: the passing Java 21/Spring Boot 3.5.16 checkpoint.
- Produces: failing tests that describe the exact Boot 4.1.1 version and new `EnvironmentPostProcessor` registration contract.

- [ ] **Step 1: Add the Spring Boot version contract**

Add to `PlatformUpgradeContractTest`:

```java
import org.springframework.boot.SpringBootVersion;

@Test
void usesSpringBoot411() {
    assertThat(SpringBootVersion.getVersion()).isEqualTo("4.1.1");
}
```

- [ ] **Step 2: Add the Boot 4 processor-registration contract**

Add to `DatabaseUrlEnvironmentPostProcessorTest`:

```java
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.Properties;

@Test
void registersProcessorUsingBoot4EnvironmentPostProcessorKey() throws IOException {
    Properties factories = PropertiesLoaderUtils.loadAllProperties("META-INF/spring.factories");

    assertThat(factories.getProperty("org.springframework.boot.EnvironmentPostProcessor"))
            .contains(DatabaseUrlEnvironmentPostProcessor.class.getName());
}
```

- [ ] **Step 3: Run both tests and verify RED**

Run:

```bash
cd backend
env JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home mvn -Dtest=PlatformUpgradeContractTest,DatabaseUrlEnvironmentPostProcessorTest test
```

Expected: version assertion reports 3.5.16 instead of 4.1.1, and the new Boot 4 factories key is absent.

- [ ] **Step 4: Commit only the failing contracts**

```bash
git add backend/src/test/java/com/bankjatim/jims/PlatformUpgradeContractTest.java backend/src/test/java/com/bankjatim/jims/config/DatabaseUrlEnvironmentPostProcessorTest.java
git commit -m "test: define Spring Boot 4 platform contracts"
```

---

### Task 3: Migrate the backend to Spring Boot 4.1.1

**Files:**
- Modify: `backend/pom.xml`
- Modify: `backend/src/main/java/com/bankjatim/jims/config/DatabaseUrlEnvironmentPostProcessor.java`
- Modify: `backend/src/main/java/com/bankjatim/jims/config/RedisConfig.java`
- Modify: `backend/src/main/resources/META-INF/spring.factories`
- Create: `backend/src/test/java/com/bankjatim/jims/config/RedisConfigTest.java`
- Modify: `backend/src/test/java/com/bankjatim/jims/controller/AuditNotificationControllerTest.java`
- Modify: `backend/src/test/java/com/bankjatim/jims/controller/EmbossControllerTest.java`
- Modify: `backend/src/test/java/com/bankjatim/jims/controller/InventoryControllerTest.java`
- Modify: `backend/src/test/java/com/bankjatim/jims/controller/MasterDataControllerTest.java`
- Modify: `backend/src/test/java/com/bankjatim/jims/controller/SwitchingStockControllerTest.java`

**Interfaces:**
- Consumes: the failing Boot 4 version and bootstrap contracts from Task 2.
- Produces: a modular Boot 4.1.1 build using Jackson 3 while preserving existing API JSON and Redis template behavior.

- [ ] **Step 1: Replace the parent, web, Flyway, OpenAPI, and test dependencies**

In `backend/pom.xml`, set:

```xml
<version>4.1.1</version>
```

for `spring-boot-starter-parent`, set:

```xml
<springdoc.version>3.1.1</springdoc.version>
```

replace `spring-boot-starter-web` with:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc</artifactId>
</dependency>
```

replace the direct `flyway-core` dependency with:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-flyway</artifactId>
</dependency>
```

retain `flyway-database-postgresql`, retain `spring-boot-starter-test`, and add:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc-test</artifactId>
    <scope>test</scope>
</dependency>
```

- [ ] **Step 2: Migrate the environment post-processor package**

In `DatabaseUrlEnvironmentPostProcessor.java`, replace:

```java
import org.springframework.boot.env.EnvironmentPostProcessor;
```

with:

```java
import org.springframework.boot.EnvironmentPostProcessor;
```

In `META-INF/spring.factories`, use exactly:

```properties
org.springframework.boot.EnvironmentPostProcessor=\
com.bankjatim.jims.config.DatabaseUrlEnvironmentPostProcessor
```

- [ ] **Step 3: Write and verify the failing Redis serializer test**

Create:

```java
package com.bankjatim.jims.config;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class RedisConfigTest {

    @Test
    void usesJackson3ForValuesAndHashValues() {
        RedisTemplate<String, Object> template = new RedisConfig()
                .redisTemplate(mock(RedisConnectionFactory.class));

        assertThat(template.getValueSerializer())
                .isInstanceOf(GenericJacksonJsonRedisSerializer.class);
        assertThat(template.getHashValueSerializer())
                .isInstanceOf(GenericJacksonJsonRedisSerializer.class);
    }
}
```

Run:

```bash
cd backend
env JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home mvn -Dtest=RedisConfigTest test
```

Expected: FAIL because `RedisConfig` still installs `GenericJackson2JsonRedisSerializer`.

- [ ] **Step 4: Migrate Redis serialization to Jackson 3 and verify GREEN**

In `RedisConfig.java`, replace `GenericJackson2JsonRedisSerializer` with `GenericJacksonJsonRedisSerializer` for both value and hash-value serializers:

```java
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;

GenericJacksonJsonRedisSerializer jsonSerializer = new GenericJacksonJsonRedisSerializer();
template.setValueSerializer(jsonSerializer);
template.setHashValueSerializer(jsonSerializer);
```

Keep string serialization for keys and hash keys unchanged. This application does not rely on persisted Redis cache as the database source of truth, so no Jackson 2 compatibility module is added.

Run:

```bash
cd backend
env JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home mvn -Dtest=RedisConfigTest test
```

Expected: PASS.

- [ ] **Step 5: Migrate test JSON parsing imports to Jackson 3**

In the five controller tests listed above, replace:

```java
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
```

with:

```java
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
```

Keep `com.fasterxml.jackson.annotation.*` imports in production DTOs and entities because Jackson 3 intentionally retains that annotation package.

- [ ] **Step 6: Run the focused platform tests and verify GREEN**

Run:

```bash
cd backend
env JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home mvn -Dtest=PlatformUpgradeContractTest,DatabaseUrlEnvironmentPostProcessorTest test
```

Expected: both test classes pass on Spring Boot 4.1.1.

- [ ] **Step 7: Run controller and Redis configuration tests**

Run:

```bash
cd backend
env JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home mvn -Dtest=RedisConfigTest,AuditNotificationControllerTest,EmbossControllerTest,InventoryControllerTest,MasterDataControllerTest,SwitchingStockControllerTest test
```

Expected: the Redis serializer contract and all existing API JSON assertions pass with Jackson 3.

- [ ] **Step 8: Commit the Boot 4 migration**

```bash
git add backend/pom.xml backend/src/main/java/com/bankjatim/jims/config/DatabaseUrlEnvironmentPostProcessor.java backend/src/main/java/com/bankjatim/jims/config/RedisConfig.java backend/src/main/resources/META-INF/spring.factories backend/src/test/java/com/bankjatim/jims/config/RedisConfigTest.java backend/src/test/java/com/bankjatim/jims/controller/AuditNotificationControllerTest.java backend/src/test/java/com/bankjatim/jims/controller/EmbossControllerTest.java backend/src/test/java/com/bankjatim/jims/controller/InventoryControllerTest.java backend/src/test/java/com/bankjatim/jims/controller/MasterDataControllerTest.java backend/src/test/java/com/bankjatim/jims/controller/SwitchingStockControllerTest.java
git commit -m "build: upgrade backend to Spring Boot 4.1.1"
```

---

### Task 4: Verify behavior and update runtime documentation

**Files:**
- Modify: `README.md`

**Interfaces:**
- Consumes: the completed Java 21/Spring Boot 4.1.1 backend.
- Produces: a packaged backend, unchanged API behavior, and developer commands that select Java 21.

- [ ] **Step 1: Run the complete backend suite from a clean target directory**

Run:

```bash
cd backend
env JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home mvn clean test
```

Expected: every backend test passes with no compilation errors and no test failures.

- [ ] **Step 2: Package the production artifact**

Run:

```bash
cd backend
env JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home mvn clean package
```

Expected: `backend/target/jims-backend-1.0.0-SNAPSHOT.jar` is created.

- [ ] **Step 3: Verify the packaged bytecode target**

Run:

```bash
javap -verbose backend/target/classes/com/bankjatim/jims/JimsApplication.class
```

Expected: `major version: 65`, which represents Java 21 bytecode.

- [ ] **Step 4: Update README platform and startup commands**

Change the backend stack description to `Spring Boot 4.1.1 (Java 21 LTS)`. Replace every Homebrew Java 17 path with:

```bash
export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
```

Do not change frontend, database, Redis, RabbitMQ, or MinIO instructions.

- [ ] **Step 5: Verify documentation no longer advertises Java 17 or Boot 3.3**

Run:

```bash
rg -n "Java 17|openjdk@17|Spring Boot 3\.3" README.md backend/pom.xml
```

Expected: no matches.

- [ ] **Step 6: Commit documentation**

```bash
git add README.md
git commit -m "docs: document Java 21 backend runtime"
```

---

### Task 5: Verify startup against the remote `.env` database

**Files:**
- No source files should change.

**Interfaces:**
- Consumes: the packaged Boot 4.1.1 backend and `backend/.env`.
- Produces: evidence that Boot 4 starts against the configured remote PostgreSQL database without changing application data.

- [ ] **Step 1: Capture read-only database counts before startup**

From `backend`, export `.env` and query only counts and migration status:

```bash
set -a
source .env
set +a
psql "$DATABASE_URL" -X -A -F '|' -c "SELECT 'orders', COUNT(*)::text FROM orders UNION ALL SELECT 'roles', COUNT(*)::text FROM roles UNION ALL SELECT 'role_menu_permissions', COUNT(*)::text FROM role_menu_permissions;"
```

Expected: the query connects to the host encoded in `DATABASE_URL`; credentials are not printed.

- [ ] **Step 2: Confirm port 8080 is free and start Boot 4 using exported `.env`**

Run from `backend`:

```bash
set -a
source .env
set +a
env JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home mvn spring-boot:run
```

Expected: logs identify Spring Boot 4.1.1, Java 21, and the remote PostgreSQL host from `.env`; Flyway validates schema version 17 and Tomcat starts on port 8080 with context path `/api`.

- [ ] **Step 3: Verify health and protected endpoints**

Run from another terminal:

```bash
curl --max-time 10 -sS -w '\nHTTP_STATUS=%{http_code}\n' http://localhost:8080/api/actuator/health
curl --max-time 10 -sS -o /dev/null -w 'orders_status=%{http_code}\n' http://localhost:8080/api/orders
curl --max-time 10 -sS -o /dev/null -w 'roles_status=%{http_code}\n' http://localhost:8080/api/master/roles
```

Expected: actuator responds; protected endpoints return 401 without a JWT and remain reachable. A DOWN actuator caused solely by an unavailable optional Redis service does not invalidate confirmed PostgreSQL connectivity, but must be reported.

- [ ] **Step 4: Confirm database counts remain unchanged**

Repeat the read-only query from Step 1 and compare the three counts.

Expected: `orders`, `roles`, and `role_menu_permissions` counts are identical before and after startup, and no Flyway migration beyond version 17 was introduced.

- [ ] **Step 5: Inspect the final diff and workspace state**

Run:

```bash
git diff --check
git status --short
git log -5 --oneline
```

Expected: upgrade files are committed; unrelated pre-existing workspace changes remain preserved.
