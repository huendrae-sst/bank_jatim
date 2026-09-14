# Database Source of Truth Design

**Date:** 2026-09-13

## Goal

Ensure every frontend page backed by an existing backend endpoint renders only data returned by the configured PostgreSQL database. An unavailable or invalid backend response must produce an explicit loading, empty, or error state and must never be replaced silently with local sample data.

## Scope

This change covers:

- The backend startup failure caused by differences between the deployed PostgreSQL schema and JPA entity mappings.
- All Vue pages that call an existing backend endpoint and also initialize from, or fall back to, `masterDataSeeds.js`, `transactionSeeds.js`, or inline sample arrays.
- Shared response parsing and regression checks needed to keep API-backed pages database-driven.

Pages for modules without a persistent backend endpoint remain outside the data-integration portion of this change. Their business collections remain empty until an endpoint exists; they must not present local sample records as database data.

## Source-of-Truth Rules

1. PostgreSQL is the source of truth for persistent business data.
2. An API-backed page starts with an empty collection or neutral metric state.
3. A successful API response, including an empty list, replaces the current page data exactly.
4. A failed API response clears stale data and shows an explicit error state with a retry path.
5. Local seed modules are not imported by API-backed views for runtime fallback.
6. Demo data, if needed later, must be enabled through an explicit application mode and must be visibly identified as demo data. This change does not introduce that mode.

## Backend Schema Alignment

The configured database currently contains two rows in `orders`, but the backend cannot start because Hibernate validation finds `debit` and `credit` in `general_ledger_entries` while `GeneralLedgerEntry` maps `debit_amount` and `credit_amount`.

A new additive Flyway migration will align the deployed schema with the current entity contract. The migration must preserve existing values and be safe when applied to both the legacy schema and a schema originally created from `V3__advanced_modules.sql`. PostgreSQL conditional SQL will rename legacy columns only when the target columns are absent. Any further validation mismatch discovered after this migration will be handled by the same evidence-driven, additive migration approach.

Production records must not be deleted, reseeded, or overwritten.

## Frontend Data Flow

API-backed views will use the existing Axios client and the shared `extractList` helper for supported response envelopes. Each loader follows one flow:

1. Set loading state and clear the previous error.
2. Request the backend endpoint.
3. Parse the response through the appropriate DTO mapper.
4. Assign the returned collection exactly, including `[]`.
5. On failure, assign `[]`, retain no seed data, and expose a readable error message.
6. End loading state in `finally`.

Reference lists such as organizations, items, warehouses, vendors, and couriers follow the same rule. If a reference request fails, its selector remains empty and dependent actions are disabled or display the error; sample options are not substituted.

Derived dashboards and reports may calculate aggregates in the browser, but their input must come exclusively from backend responses. A legitimate zero-row response must yield zero-valued metrics rather than seeded metrics.

## Page Classification

The audit will classify every routed view into one of three categories:

- **API-backed:** Existing endpoint is available; all runtime seed fallback is removed.
- **Partially API-backed:** Database-backed sections follow source-of-truth rules, while unsupported actions are disabled with an explicit message.
- **No persistent endpoint:** Business collections remain empty and the missing backend capability is documented.

The classification will be recorded in `docs/mock-data-production-audit.md` with endpoint coverage and remaining backend gaps.

## Error Handling

API failure must be distinguishable from an empty database result:

- Empty response: render the normal empty-state copy.
- Request or parsing failure: render an error alert and retry control where the page has a natural reload action.
- Authentication failure: continue using the global Axios interceptor to clear the invalid session and redirect to login.
- Partial multi-request failure: show only successfully loaded database data; do not fill failed portions with seeds.

Console warnings may supplement the UI but are not the only error signal for primary page data.

## Testing

The implementation will add automated checks before production changes:

- A frontend source-policy test that fails when an API-backed view imports known seed modules or assigns `DEFAULT_*` data after an API failure.
- Unit tests for `extractList` covering Axios-interceptor and Spring `ApiResponse`/`PageResponse` shapes, including empty lists.
- Backend migration verification against legacy `debit`/`credit` columns and current `debit_amount`/`credit_amount` columns where the test infrastructure permits.
- Existing backend tests and the frontend production build.
- A live verification against the configured database: database `COUNT(*)` and API pagination metadata/content count must agree for `/orders`.

## Safety and Compatibility

- Existing uncommitted work is preserved; edits are limited to files required by this design.
- Database changes are additive and value-preserving.
- No production row is inserted solely to make a page appear populated.
- Unsupported frontend mutations remain disabled until a persistent endpoint exists.
- The response parser remains backward-compatible with the response envelope shapes already used by the application.

## Success Criteria

- The backend starts successfully against the configured database with Hibernate schema validation enabled.
- `/orders` displays the two rows currently present in the `orders` table, subject only to explicit user filters.
- Stopping the backend makes `/orders` show an error/empty state, never four sample rows.
- Every routed API-backed page is free of automatic local seed fallback.
- The production audit accurately lists API-backed, partial, and non-persistent pages.
- Backend tests and frontend build pass.
