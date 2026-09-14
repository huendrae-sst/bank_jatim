# Backend `.env` Auto-Load Design

## Goal

Allow developers to start the backend from the `backend` directory using only:

```bash
mvn spring-boot:run
```

The application must automatically read `backend/.env` and use its remote `DATABASE_URL` instead of falling back to PostgreSQL on localhost.

## Design

Add an optional Spring Config import to `backend/src/main/resources/application.yml`:

```yaml
spring:
  config:
    import: optional:file:.env[.properties]
```

The relative path is resolved from the backend process working directory. The supported invocation therefore remains `cd backend` followed by `mvn spring-boot:run`.

The import is optional so packaged deployments without a local `.env` file can continue to start from operating-system or container environment variables. Standard Spring property-source precedence remains intact: environment variables supplied by the deployment override values imported from `.env`.

The existing `DatabaseUrlEnvironmentPostProcessor` remains responsible for converting a PostgreSQL URL such as `postgresql://user:password@host:5432/database` into Spring datasource properties.

## Error Handling

- A missing `.env` file does not fail startup.
- An invalid `DATABASE_URL` continues to fail with the existing validation error.
- If neither `.env` nor deployment environment variables define a database, the existing localhost development fallback remains unchanged.

## Verification

Add a regression test that starts Spring's environment preparation with a temporary `.env`-style properties file and verifies that the registered environment post-processor maps its `DATABASE_URL` to the expected remote JDBC URL, username, and password.

Run the complete backend test suite and package the application on Java 21. Finally, start the backend from `backend` using only `mvn spring-boot:run`, verify that Flyway connects to the remote host configured in `backend/.env`, and stop the process after the health checks.

No Flyway migration, seed, or application data is changed by this work.
