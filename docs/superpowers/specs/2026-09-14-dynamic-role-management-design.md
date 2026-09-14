# Dynamic Role Management Design

**Date:** 2026-09-14

## Goal

Provide persistent backend support for `/master/roles` and `/master/role-menus` so administrators can create, update, and safely delete custom roles, and assign menus to roles without frontend seed data or comma-separated role lists.

## Scope

This change covers:

- A normalized `roles` table.
- A normalized `role_menu_permissions` join table.
- Migration of the existing 18 `UserRole` values and the current `menus.roles` assignments.
- A foreign-key relationship from `users.role` to `roles.code`.
- Backend CRUD APIs for roles and read/write APIs for role-menu assignments.
- Database-only data flows for the `/master/roles` and `/master/role-menus` frontend pages.
- Validation, conflict handling, and automated regression tests.

This change does not replace the existing endpoint authorization rules with a fully dynamic permission engine. Role-menu assignments control menu availability only. Existing backend operations guarded by static role codes retain their current authorization behavior.

## Data Model

### `roles`

The `roles` table contains:

- `id`: generated primary key.
- `code`: unique, immutable role identifier using uppercase letters, digits, and underscores.
- `name`: administrator-facing display name.
- `description`: optional explanation of the role.
- `system_role`: identifies built-in roles referenced by backend authorization rules.
- `created_at` and `updated_at`: audit timestamps.

The existing 18 `UserRole` enum values are inserted as system roles by an idempotent Flyway migration. This is reference-data migration, not an application startup seed. Runtime code must not recreate deleted custom data or insert demo roles.

System roles may have their names and descriptions updated, but their codes cannot be changed and their records cannot be deleted. Custom-role codes are also immutable after creation so user and authorization references remain stable.

### `role_menu_permissions`

The join table contains:

- `role_id`: foreign key to `roles.id`.
- `menu_id`: foreign key to `menus.id`.
- A composite primary key or equivalent unique constraint on `(role_id, menu_id)`.

Deleting a menu cascades only its join-table assignments. Deleting an eligible custom role cascades its menu assignments. No role deletion may cascade to users.

### User relationship

The existing `users.role` column remains the stored role code to avoid destructive rewriting of user data. It gains a foreign key to `roles.code`, and the Java user model resolves the persisted role rather than depending on the `UserRole` enum as the source of truth.

Security authorities remain string-based in the form `ROLE_<code>`. This preserves compatibility with existing `@PreAuthorize` expressions for built-in roles while allowing a custom role to authenticate and receive its assigned menus.

### Legacy menu-role data

The current comma-separated `menus.roles` values are split and copied into `role_menu_permissions` during migration. The migration validates role codes against `roles`, avoids duplicate pairs, and preserves every valid assignment. After backend and frontend code use the join table, the obsolete `menus.roles` column is removed.

## Backend API

All endpoints use the application's existing API response envelope and authentication conventions.

### Role CRUD

- `GET /api/master/roles` returns role objects ordered consistently by name and code.
- `POST /api/master/roles` creates a custom role.
- `PUT /api/master/roles/{code}` updates the role name and description. The code and `system_role` status are not client-editable.
- `DELETE /api/master/roles/{code}` deletes an unused custom role.

Role codes are trimmed, normalized to uppercase, and must match `^[A-Z][A-Z0-9_]*$`. Duplicate codes produce a validation/conflict response. Names are required after trimming.

Deletion produces HTTP `409 Conflict` when the role is assigned to one or more users. Deletion of a system role is rejected. The response message must distinguish these cases so the frontend can explain the corrective action.

### Role-menu assignments

- `GET /api/master/roles/{code}/menus` returns the menu identifiers assigned to one role.
- `PUT /api/master/roles/{code}/menus` replaces that role's complete menu assignment in one transaction.
- `GET /api/master/role-menus` returns all persisted role-menu pairs for administration and audit use.

Replacement validates that the role and every submitted menu exist before changing any rows. Duplicate submitted menu IDs are collapsed. An empty list intentionally removes every menu assignment for the role. Validation failure leaves the previous assignments unchanged.

Menu APIs stop reading and writing comma-separated role values. Where compatibility requires a menu response to expose role codes, those codes are derived from `role_menu_permissions` rather than `menus.roles`.

## Frontend Behavior

### `/master/roles`

The page loads role objects exclusively from `GET /api/master/roles`. It supports:

- Creating a custom role with code, name, and optional description.
- Editing the name and description of any role.
- Deleting an unused custom role after confirmation.
- Clearly identifying system roles and disabling their delete action.
- Displaying backend validation and conflict messages, including a role-still-in-use message.

After every successful mutation, the store reloads roles from the backend. Failed requests never restore local defaults or sample records.

### `/master/role-menus`

The page loads roles and menus from backend APIs. Selecting a role loads its persisted menu identifiers. Saving sends the complete selected identifier list to the replacement endpoint, then reloads the saved assignment from the backend.

Loading, empty, and error states remain distinct. A failed role or menu request leaves the affected collection empty and exposes a retryable error; it does not use fallback data.

## Error Handling and Transactions

- Missing roles or menus return HTTP `404`.
- Invalid role payloads or menu identifiers return HTTP `400`.
- Duplicate role codes, deletion of protected system roles, and deletion of roles assigned to users return HTTP `409`.
- Role creation, update, deletion, and assignment replacement each run in a service-level transaction.
- Role deletion checks user references inside the transaction before removing the role.
- Assignment replacement validates all references before deleting existing pairs.

## Migration and Compatibility

The migration order is:

1. Create `roles` and insert the 18 existing role codes as system reference data.
2. Add the user-role foreign key after confirming every current `users.role` value exists in `roles`.
3. Create `role_menu_permissions`.
4. Convert valid values from `menus.roles` into normalized pairs.
5. Switch application reads and writes to the normalized model.
6. Remove `menus.roles` in the final schema migration included with the application change.

The migration must be safe for existing production records and repeatable through Flyway's normal versioned-migration guarantees. It must not overwrite role names or assignments created after deployment.

## Testing

Backend coverage includes:

- Migration of all built-in role codes and legacy comma-separated menu assignments.
- Creation and update of a custom role.
- Validation and duplicate-code rejection.
- Protection against deleting a system role.
- HTTP `409` when deleting a role assigned to a user.
- Successful deletion of an unused custom role and cleanup of its menu assignments.
- Transactional replacement, clearing, deduplication, and validation of role-menu assignments.
- Authentication authority generation from the persisted role code.

Frontend coverage includes:

- Parsing object-based role responses.
- Role-store CRUD requests and backend refresh after successful changes.
- Role-menu load and save requests using menu identifiers.
- Rendering protected delete state and backend conflict messages.
- Source-policy checks proving these pages do not import runtime seeds or restore sample data.

The complete backend test suite, frontend test suite, and frontend production build must pass. A live verification will compare role and role-menu API results with direct database counts and assignments.

## Success Criteria

- `roles` and `role_menu_permissions` exist as persistent normalized tables.
- Existing users and menu assignments survive migration without semantic data loss.
- `/master/roles` supports backend-backed create, update, and safe deletion of custom roles.
- `/master/role-menus` reads and saves only database assignments.
- A role assigned to any user cannot be deleted.
- Built-in roles needed by static backend authorization cannot be deleted or renamed by code.
- No frontend or backend runtime seed supplies role or role-menu data.
- Existing static backend operation authorization continues to work for built-in role codes.
