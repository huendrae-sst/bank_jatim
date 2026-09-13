# Database Source of Truth Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make PostgreSQL-backed API responses the only runtime source of business data and remove all frontend seed datasets.

**Architecture:** Repair backend schema compatibility through an additive Flyway migration, then enforce a frontend boundary where views start empty and only assign parsed API responses. A Node source-policy test prevents seed modules, seed imports, and known automatic fallback patterns from returning.

**Tech Stack:** Spring Boot 3.3, Java 17, Flyway, PostgreSQL 15+, Vue 3, Axios, Vite, Node built-in test runner.

## Global Constraints

- Preserve all existing database rows.
- Preserve unrelated uncommitted work already present in the worktree.
- API failure must produce empty/error state, never sample records.
- API responses containing an empty list are valid and must remain empty.
- Static UI option definitions such as status labels may remain; business record fixtures may not.

---

### Task 1: Add Regression Tests for the Data-Source Policy

**Files:**
- Create: `frontend/test/data-source-policy.test.js`
- Create: `frontend/test/response-parser.test.js`
- Modify: `frontend/package.json`

**Interfaces:**
- Consumes: Vue source files under `frontend/src/views` and `extractList(res)` from `frontend/src/utils/responseParser.js`.
- Produces: `npm test`, which rejects runtime seed modules/imports and validates supported API response shapes.

- [ ] **Step 1: Write the failing source-policy test**

Use `node:test` to recursively scan Vue/JavaScript application sources. Assert that `src/stores/masterDataSeeds.js` and `src/stores/transactionSeeds.js` do not exist, no view imports them, and no API error branch assigns `DEFAULT_*` records.

- [ ] **Step 2: Write response parser tests**

Cover direct arrays, interceptor-unwrapped `ApiResponse<List>`, interceptor-unwrapped `ApiResponse<PageResponse>`, raw Axios envelopes, and valid empty arrays. Invalid shapes must return `null`.

- [ ] **Step 3: Run `npm test` and verify RED**

Expected failure: both seed modules still exist and API-backed views still import them.

- [ ] **Step 4: Keep the tests unchanged for the implementation tasks**

The production change that makes the policy test pass is deletion of the two seed modules plus removal of their consumers.

---

### Task 2: Align the Deployed Database Schema

**Files:**
- Create: `backend/src/main/resources/db/migration/V13__align_general_ledger_amount_columns.sql`

**Interfaces:**
- Consumes: legacy `general_ledger_entries.debit` and `.credit` columns when present.
- Produces: `general_ledger_entries.debit_amount` and `.credit_amount`, matching `GeneralLedgerEntry`.

- [ ] **Step 1: Capture the current failing startup check**

Run the backend against the configured database and retain the observed Hibernate validation failure for missing `credit_amount`.

- [ ] **Step 2: Add a conditional, value-preserving migration**

Use a PostgreSQL `DO` block. Rename `debit` only when `debit` exists and `debit_amount` does not; apply the same rule to `credit`. Do not drop either column.

- [ ] **Step 3: Start the backend with schema validation enabled**

Flyway applies V13 automatically. If validation exposes another legacy mismatch, inspect both entity and database schema and add only the minimum value-preserving alignment required.

- [ ] **Step 4: Verify backend health**

Run `curl http://localhost:8080/api/actuator/health` and require an HTTP-successful response.

---

### Task 3: Remove Seed Data From API-Backed Transaction Views

**Files:**
- Modify API-backed views under `frontend/src/views/orders`, `procurement`, `warehouse`, `distribution`, `receiving`, `finance`, `emboss`, `returns`, `destructions`, and `inventory` identified by the source-policy test.

**Interfaces:**
- Consumes: existing backend endpoints and `extractList`.
- Produces: empty initial collections populated only from backend responses.

- [ ] **Step 1: Remove transaction seed imports and initializers**

Replace `ref(DEFAULT_*.map(...))`, seed-derived counters, and seed-derived selector defaults with empty arrays, zero values, or `null`.

- [ ] **Step 2: Remove automatic fallback assignments**

In every loader catch branch, clear the affected collection, set a readable error message where supported, and retain the existing authentication redirect behavior.

- [ ] **Step 3: Preserve legitimate empty responses**

Use `extractList`; assign `[]` when the parsed response is an empty array. Treat only `null` as an invalid envelope.

- [ ] **Step 4: Run `npm test`**

Expected: remaining failures identify master/report/dashboard seed consumers for Task 4.

---

### Task 4: Remove Seed Data From Master, Dashboard, ESS, and Report Views

**Files:**
- Modify affected views under `frontend/src/views/master`, `dashboard`, `ess`, `reports`, `audit`, and `notifications`.
- Delete: `frontend/src/stores/masterDataSeeds.js`
- Delete: `frontend/src/stores/transactionSeeds.js`

**Interfaces:**
- Consumes: master, inventory, dashboard, settlement, audit, and notification endpoints.
- Produces: database-derived tables and aggregates with neutral zero/empty states.

- [ ] **Step 1: Remove master seed imports and initializers**

Selectors remain empty if their endpoint fails. Disable dependent actions naturally through missing required selections.

- [ ] **Step 2: Remove dashboard and report fallback calculations**

Metrics and report rows are derived only from successful API payloads. Failed requests produce zero/empty state and an error indication.

- [ ] **Step 3: Remove remaining business-record sample arrays**

For views without an endpoint, replace inline record fixtures with empty collections. Keep only presentation metadata such as status choices, report definitions, and category labels.

- [ ] **Step 4: Delete both seed modules**

Delete them only after all imports are removed.

- [ ] **Step 5: Run `npm test` and verify GREEN**

Expected: policy and parser tests pass.

---

### Task 5: Audit and Live Verification

**Files:**
- Modify: `docs/mock-data-production-audit.md`

**Interfaces:**
- Consumes: routed frontend views, backend controller endpoints, and the configured database.
- Produces: a current coverage report and reproducible verification evidence.

- [ ] **Step 1: Update the production audit**

Classify every routed data page as API-backed, partially API-backed, or awaiting an endpoint. Record that no category uses runtime seed data.

- [ ] **Step 2: Run frontend verification**

Run `npm test` and `npm run build` from `frontend`.

- [ ] **Step 3: Run backend verification**

Run `mvn test` from `backend`, then start the application with the configured environment and verify health.

- [ ] **Step 4: Compare `/orders` with PostgreSQL**

Run a read-only `SELECT COUNT(*) FROM orders`, request `/orders?page=0&size=1000`, and compare database count, API pagination total, and returned content count. The expected configured-database result at plan creation is `2`.

- [ ] **Step 5: Verify failure behavior**

With the backend unavailable, confirm `/orders` shows no sample rows and exposes an error/empty state.

- [ ] **Step 6: Review the final diff**

Check that unrelated worktree changes remain intact and that no debug instrumentation or temporary fixture remains.
