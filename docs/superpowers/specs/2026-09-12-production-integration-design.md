# Production Integration Design

## Goal

Integrate the Vue frontend, Spring Boot backend, and PostgreSQL production database so the application runs against real backend data and no longer falls back to mock authentication data.

## Architecture

The backend remains the source of truth for all data and authentication. It reads database configuration from production environment variables and supports the provided `DATABASE_URL` format (`postgresql://user:password@host:port/database`) by translating it into Spring datasource properties before Spring Boot creates the datasource.

The frontend uses `VITE_API_BASE_URL` for production API routing and keeps the existing Vite `/api` proxy for local development. Authentication failures are surfaced to the user instead of creating a mock JWT session.

## Components

- Backend database URL adapter: maps `DATABASE_URL` to `spring.datasource.url`, `spring.datasource.username`, and `spring.datasource.password`.
- Backend CORS configuration: reads allowed frontend origins from environment with local defaults.
- Backend configuration: keeps explicit `SPRING_DATASOURCE_*` variables as the highest-priority override.
- Frontend API client: reads `VITE_API_BASE_URL`, defaulting to `/api`.
- Frontend auth store: removes fallback mock login and quick-switch mock session behavior.
- Production environment examples: documents the backend and frontend variables required for deployment.

## Data Flow

The browser calls the configured API base URL. The Spring Boot API authenticates users against PostgreSQL-backed user records and returns JWTs. The frontend stores only JWT/session data returned by the backend. Flyway migrations remain enabled so the production database schema can be migrated on backend startup.

## Error Handling

If the backend cannot connect to PostgreSQL, the backend should fail startup or return real API errors. If login fails or the API is unavailable, the frontend returns the backend error message or a connection error instead of silently logging in with mock data.

## Testing

Backend unit tests cover `DATABASE_URL` translation, explicit Spring datasource override behavior, and missing-variable behavior. Frontend verification uses `npm run build` because the project currently has no frontend test runner.

## Constraints

- Do not hard-code the production database password in committed application defaults.
- Support `DATABASE_URL="postgresql://admin:aninza@192.168.18.67:5432/bank_jatim"`.
- Remove mock authentication fallback from production source code.
- Preserve existing local development defaults unless overridden by environment variables.
