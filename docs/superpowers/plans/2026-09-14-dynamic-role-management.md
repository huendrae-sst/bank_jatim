# Dynamic Role Management Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace enum/CSV-backed role data with persistent dynamic roles and normalized role-menu assignments, including safe role CRUD and database-only Vue pages.

**Architecture:** A `Role` aggregate supplies immutable string role codes to users and Spring Security, while an explicit `RoleMenuPermission` join entity owns normalized menu assignments. `RoleService` owns role lifecycle rules, `RoleMenuService` owns assignment replacement/query operations, and a focused `RoleController` exposes the APIs without further expanding `MasterDataController`.

**Tech Stack:** Java 17, Spring Boot 3.3.3, Spring Data JPA, Spring Security, Flyway/PostgreSQL, JUnit 5/Mockito, Vue 3, Pinia, Axios, Node test runner.

## Global Constraints

- PostgreSQL is the only runtime source of role and role-menu data; do not add frontend fallbacks, local-storage caches, startup seeders, or demo records.
- The 18 existing role codes are migrated as protected system reference data, not recreated by runtime application code.
- Role-menu assignments control menu availability only; existing static backend operation authorization remains unchanged.
- Role codes are normalized to uppercase, match `^[A-Z][A-Z0-9_]*$`, and are immutable after creation.
- A system role cannot be deleted; a custom role assigned to any user returns HTTP `409 Conflict` on deletion.
- Existing production users and valid `menus.roles` assignments must survive migration without semantic data loss.
- Preserve unrelated workspace changes and use Java 17 for every Maven command.

---

## File Structure

### Backend files to create

- `backend/src/main/resources/db/migration/V16__create_roles_and_role_menu_permissions.sql` — creates and backfills normalized role storage while retaining the legacy CSV column temporarily.
- `backend/src/main/resources/db/migration/V17__drop_legacy_menu_roles.sql` — removes `menus.roles` after application mappings no longer depend on it.
- `backend/src/main/java/com/bankjatim/jims/domain/Role.java` — persistent role aggregate.
- `backend/src/main/java/com/bankjatim/jims/domain/RoleMenuPermission.java` — explicit normalized join entity.
- `backend/src/main/java/com/bankjatim/jims/domain/RoleMenuPermissionId.java` — composite join-table key.
- `backend/src/main/java/com/bankjatim/jims/repository/RoleRepository.java` — role lookup and ordered listing.
- `backend/src/main/java/com/bankjatim/jims/repository/RoleMenuPermissionRepository.java` — assignment lookup and bulk deletion.
- `backend/src/main/java/com/bankjatim/jims/dto/RoleCreateRequest.java` — validated create payload.
- `backend/src/main/java/com/bankjatim/jims/dto/RoleUpdateRequest.java` — validated mutable fields.
- `backend/src/main/java/com/bankjatim/jims/dto/RoleResponse.java` — stable frontend role contract.
- `backend/src/main/java/com/bankjatim/jims/dto/RoleMenuResponse.java` — flattened role/menu pair contract.
- `backend/src/main/java/com/bankjatim/jims/common/ConflictException.java` — maps domain conflicts to HTTP 409.
- `backend/src/main/java/com/bankjatim/jims/service/RoleService.java` — role lifecycle and deletion safeguards.
- `backend/src/main/java/com/bankjatim/jims/service/RoleMenuService.java` — transactional assignment queries/replacements.
- `backend/src/main/java/com/bankjatim/jims/controller/RoleController.java` — `/master/roles` and `/master/role-menus` endpoints.
- `backend/src/test/java/com/bankjatim/jims/migration/RoleMigrationContractTest.java` — guards the migration contract.
- `backend/src/test/java/com/bankjatim/jims/service/RoleServiceTest.java` — role CRUD behavior.
- `backend/src/test/java/com/bankjatim/jims/service/RoleMenuServiceTest.java` — normalized assignment behavior.
- `backend/src/test/java/com/bankjatim/jims/controller/RoleControllerTest.java` — response/status contract.
- `backend/src/test/java/com/bankjatim/jims/security/UserPrincipalTest.java` — dynamic string authority regression.

### Frontend files to create

- `frontend/src/services/roleApi.js` — small injectable API adapter for role CRUD.
- `frontend/src/services/roleMenuApi.js` — small injectable API adapter for assignment reads/writes.
- `frontend/src/utils/roleMapper.js` — strict object-role normalization.
- `frontend/test/role-api.test.js` — endpoint/payload contract tests with fake clients.
- `frontend/test/role-mapper.test.js` — response normalization tests.

### Existing files to modify

- User/security DTOs and services: replace `UserRole` enum values with persisted `Role` references and string response/authority codes.
- Menu service/entity/controller: replace CSV reads/writes with `RoleMenuService`.
- Existing backend tests: construct `Role` objects or string authorities instead of enum constants.
- `frontend/src/stores/role.js`, `frontend/src/stores/menu.js`: use backend adapters and refresh persisted state after mutations.
- `frontend/src/views/master/RolesView.vue`, `RoleMenusView.vue`: render persistent fields and conflict/protected states.
- `frontend/test/data-source-policy.test.js`: enforce the new database-only role contracts.

---

### Task 1: Add normalized role schema and persistent role aggregate

**Files:**
- Create: `backend/src/main/resources/db/migration/V16__create_roles_and_role_menu_permissions.sql`
- Create: `backend/src/main/java/com/bankjatim/jims/domain/Role.java`
- Create: `backend/src/main/java/com/bankjatim/jims/repository/RoleRepository.java`
- Create: `backend/src/test/java/com/bankjatim/jims/migration/RoleMigrationContractTest.java`

**Interfaces:**
- Produces: `RoleRepository.findByCodeIgnoreCase(String)`, `findAllByOrderByNameAscCodeAsc()`, and `existsByCodeIgnoreCase(String)`.
- Produces: database tables `roles` and `role_menu_permissions`; keeps `menus.roles` until Task 4.

- [ ] **Step 1: Write the failing migration contract test**

Read `V16__create_roles_and_role_menu_permissions.sql` as a classpath resource and assert that it contains all values from this exact built-in list:

```java
List<String> builtIns = List.of(
    "SUPER_ADMIN", "USER_ADMIN", "MASTER_MAKER", "MASTER_APPROVER",
    "BUDGET_OFFICER", "PROCUREMENT_OFFICER", "PROCUREMENT_APPROVER",
    "INVENTORY_OFFICER", "WAREHOUSE_OFFICER", "REQUESTER_CABANG",
    "ORDER_APPROVER", "SWITCHING_APPROVER", "DISTRIBUTION_OFFICER",
    "RECEIVING_OFFICER", "FINANCE_OFFICER", "FINANCE_APPROVER",
    "AUDITOR", "MANAGEMENT"
);
```

Also assert the SQL contains the `(role_id, menu_id)` primary key, `regexp_split_to_table`, a foreign key from `users(role)` to `roles(code)`, and does **not** drop `menus.roles` yet.

- [ ] **Step 2: Run the focused test and verify it fails because V16 is absent**

Run: `cd backend && env JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home mvn -Dtest=RoleMigrationContractTest test`

Expected: FAIL because the migration resource cannot be found.

- [ ] **Step 3: Implement V16 with data-preserving SQL**

Create `roles` with `id BIGSERIAL`, unique `code VARCHAR(50)`, required `name`, optional `description`, required `system_role BOOLEAN DEFAULT FALSE`, and timestamps. Insert all 18 codes with readable names using `ON CONFLICT (code) DO NOTHING`.

Create `role_menu_permissions` with:

```sql
role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
menu_id BIGINT NOT NULL REFERENCES menus(id) ON DELETE CASCADE,
PRIMARY KEY (role_id, menu_id)
```

Backfill assignments with `CROSS JOIN LATERAL regexp_split_to_table(m.roles, ',')`, `TRIM`, joins to `roles.code`, and `ON CONFLICT DO NOTHING`. Add `fk_users_role_code` only after built-in roles exist. Do not update existing user role values and do not drop the CSV column.

- [ ] **Step 4: Add the `Role` entity and repository**

`Role extends BaseEntity` and maps `code`, `name`, `description`, and `systemRole`; code length is 50 and unique. Do not expose a setter or service operation that changes a saved code.

```java
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCase(String code);
    List<Role> findAllByOrderByNameAscCodeAsc();
}
```

- [ ] **Step 5: Run the focused test and compile the backend**

Run: `cd backend && env JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home mvn -Dtest=RoleMigrationContractTest test`

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add backend/src/main/resources/db/migration/V16__create_roles_and_role_menu_permissions.sql backend/src/main/java/com/bankjatim/jims/domain/Role.java backend/src/main/java/com/bankjatim/jims/repository/RoleRepository.java backend/src/test/java/com/bankjatim/jims/migration/RoleMigrationContractTest.java
git commit -m "feat: persist role definitions"
```

---

### Task 2: Resolve users and Spring Security authorities from persisted role codes

**Files:**
- Modify: `backend/src/main/java/com/bankjatim/jims/domain/User.java`
- Modify: `backend/src/main/java/com/bankjatim/jims/dto/UserRequest.java`
- Modify: `backend/src/main/java/com/bankjatim/jims/dto/UserResponse.java`
- Modify: `backend/src/main/java/com/bankjatim/jims/dto/LoginResponse.java`
- Modify: `backend/src/main/java/com/bankjatim/jims/repository/UserRepository.java`
- Modify: `backend/src/main/java/com/bankjatim/jims/service/UserService.java`
- Modify: `backend/src/main/java/com/bankjatim/jims/service/AuthService.java`
- Modify: `backend/src/main/java/com/bankjatim/jims/service/NotificationService.java`
- Modify: `backend/src/main/java/com/bankjatim/jims/dto/AuditLogResponse.java`
- Modify: `backend/src/main/java/com/bankjatim/jims/security/UserPrincipal.java`
- Modify: `backend/src/main/java/com/bankjatim/jims/security/CustomUserDetailsService.java`
- Modify: `backend/src/main/java/com/bankjatim/jims/security/JwtTokenProvider.java`
- Delete: `backend/src/main/java/com/bankjatim/jims/security/UserRole.java`
- Modify: existing backend controller/service tests that import `UserRole`
- Create: `backend/src/test/java/com/bankjatim/jims/security/UserPrincipalTest.java`

**Interfaces:**
- Consumes: `RoleRepository.findByCodeIgnoreCase(String)` from Task 1.
- Produces: `User.getRole(): Role`, JSON `role: String`, and `UserPrincipal.getRole(): String`.

- [ ] **Step 1: Write failing tests for dynamic role resolution and authority generation**

Update `UserServiceTest` so requests use `request.setRole("CUSTOM_AUDITOR")`, the fake `RoleRepository` resolves a `Role` with that code, and the saved user references that entity. Add `UserPrincipalTest` asserting:

```java
UserPrincipal principal = UserPrincipal.create(
    1L, "Custom User", "custom@example.test", null, "hash",
    "CUSTOM_AUDITOR", null, null, BigDecimal.ZERO, true
);
assertThat(principal.getAuthorities())
    .extracting(GrantedAuthority::getAuthority)
    .containsExactly("ROLE_CUSTOM_AUDITOR");
```

- [ ] **Step 2: Run the tests and verify enum-based compilation/tests fail**

Run: `cd backend && env JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home mvn -Dtest=UserServiceTest,UserPrincipalTest test`

Expected: FAIL because requests, users, and principals still require `UserRole`.

- [ ] **Step 3: Change the user domain and API boundary**

Map `User.role` as an eager required relationship using the existing column:

```java
@ManyToOne(fetch = FetchType.EAGER, optional = false)
@JoinColumn(name = "role", referencedColumnName = "code", nullable = false)
private Role role;
```

Change `UserRequest.role`, `UserResponse.role`, and `LoginResponse.role` to `String`. `UserResponse.from` returns `user.getRole().getCode()`.

Inject `RoleRepository` into `UserService`; both create and update call one helper that trims/uppercases the submitted code and throws `ResourceNotFoundException("Role tidak ditemukan: " + code)` if absent. Update repository fetch queries to fetch `u.role` with organization and warehouse.

- [ ] **Step 4: Change authentication internals to string codes**

`UserPrincipal.role` becomes `String`, authority creation remains `new SimpleGrantedAuthority("ROLE_" + role)`, and JWT writes the string directly. `CustomUserDetailsService` passes `user.getRole().getCode()`. Notification lookup and audit/login responses use the code string.

Delete `UserRole.java`, then replace enum constants in `AuditNotificationControllerTest`, `EmbossControllerTest`, `SwitchingStockControllerTest`, `MasterDataControllerTest`, and `UserServiceTest` with either role entities for users or strings for principals.

- [ ] **Step 5: Run all security/user-related tests**

Run: `cd backend && env JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home mvn -Dtest=UserServiceTest,UserPrincipalTest,AuditNotificationControllerTest,EmbossControllerTest,SwitchingStockControllerTest,MasterDataControllerTest test`

Expected: PASS, including serialization of `role` as `"USER_ADMIN"`.

- [ ] **Step 6: Commit**

```bash
git add backend/src/main/java backend/src/test/java
git commit -m "refactor: resolve user roles from database"
```

---

### Task 3: Implement safe dynamic role CRUD APIs

**Files:**
- Create: `backend/src/main/java/com/bankjatim/jims/common/ConflictException.java`
- Modify: `backend/src/main/java/com/bankjatim/jims/common/GlobalExceptionHandler.java`
- Create: `backend/src/main/java/com/bankjatim/jims/dto/RoleCreateRequest.java`
- Create: `backend/src/main/java/com/bankjatim/jims/dto/RoleUpdateRequest.java`
- Create: `backend/src/main/java/com/bankjatim/jims/dto/RoleResponse.java`
- Create: `backend/src/main/java/com/bankjatim/jims/service/RoleService.java`
- Create: `backend/src/main/java/com/bankjatim/jims/controller/RoleController.java`
- Modify: `backend/src/main/java/com/bankjatim/jims/controller/MasterDataController.java`
- Modify: `backend/src/main/java/com/bankjatim/jims/repository/UserRepository.java`
- Create: `backend/src/test/java/com/bankjatim/jims/service/RoleServiceTest.java`
- Create: `backend/src/test/java/com/bankjatim/jims/controller/RoleControllerTest.java`

**Interfaces:**
- Produces: `RoleService.getRoles()`, `createRole(RoleCreateRequest)`, `updateRole(String, RoleUpdateRequest)`, `deleteRole(String)`.
- Produces: GET/POST `/master/roles`, PUT/DELETE `/master/roles/{code}`.

- [ ] **Step 1: Write failing `RoleServiceTest` cases**

Use Mockito repositories and cover: ordered database listing, normalization of ` custom_auditor ` to `CUSTOM_AUDITOR`, invalid pattern rejection, duplicate rejection, name/description update without code mutation, system-role delete conflict, assigned custom-role delete conflict, and unused custom-role deletion.

The deletion check must use:

```java
long countByRole_Code(String roleCode);
```

- [ ] **Step 2: Run the service test and verify missing classes fail**

Run: `cd backend && env JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home mvn -Dtest=RoleServiceTest test`

Expected: FAIL because the service and DTO contracts do not exist.

- [ ] **Step 3: Implement validation, conflict mapping, and role lifecycle**

`RoleCreateRequest` has `@NotBlank code`, `@Pattern(regexp = "^[A-Za-z][A-Za-z0-9_]*$")`, `@NotBlank name`, and optional description. `RoleUpdateRequest` exposes only name and description. `RoleResponse` returns `id`, `code`, `name`, `description`, `systemRole`, `createdAt`, and `updatedAt`.

Add `ConflictException` and map it in `GlobalExceptionHandler` to `HttpStatus.CONFLICT` with `ApiResponse.error(ex.getMessage())`. In `RoleService`, normalize code once, set `systemRole=false` for every API-created role, and keep deletion checks inside `@Transactional`.

- [ ] **Step 4: Write and run failing controller contract tests**

Mock `RoleService` and assert GET returns object roles, POST returns 201, PUT uses the immutable path code, and DELETE returns a success envelope. Separately call `GlobalExceptionHandler.handleConflict` and assert status 409 and the original Indonesian conflict message.

- [ ] **Step 5: Add `RoleController` and remove the enum-backed endpoint**

Create `RoleController` at `@RequestMapping("/master")`. Remove only `getRoles()` and its `UserRole`/`Arrays` imports from `MasterDataController`; other master endpoints remain in place.

- [ ] **Step 6: Run focused and existing controller tests**

Run: `cd backend && env JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home mvn -Dtest=RoleServiceTest,RoleControllerTest,MasterDataControllerTest test`

Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add backend/src/main/java backend/src/test/java
git commit -m "feat: add dynamic role CRUD API"
```

---

### Task 4: Replace CSV menu authorization with normalized assignments

**Files:**
- Create: `backend/src/main/java/com/bankjatim/jims/domain/RoleMenuPermissionId.java`
- Create: `backend/src/main/java/com/bankjatim/jims/domain/RoleMenuPermission.java`
- Create: `backend/src/main/java/com/bankjatim/jims/repository/RoleMenuPermissionRepository.java`
- Create: `backend/src/main/java/com/bankjatim/jims/dto/RoleMenuResponse.java`
- Create: `backend/src/main/java/com/bankjatim/jims/service/RoleMenuService.java`
- Modify: `backend/src/main/java/com/bankjatim/jims/domain/Menu.java`
- Modify: `backend/src/main/java/com/bankjatim/jims/service/MenuService.java`
- Modify: `backend/src/main/java/com/bankjatim/jims/controller/MasterDataController.java`
- Modify: `backend/src/main/java/com/bankjatim/jims/controller/RoleController.java`
- Create: `backend/src/main/resources/db/migration/V17__drop_legacy_menu_roles.sql`
- Create: `backend/src/test/java/com/bankjatim/jims/service/RoleMenuServiceTest.java`
- Modify: `backend/src/test/java/com/bankjatim/jims/controller/MasterDataControllerTest.java`
- Modify: `backend/src/test/java/com/bankjatim/jims/controller/RoleControllerTest.java`

**Interfaces:**
- Produces: `getMenuCodesForRole`, `replaceRoleMenus`, `getAllMappings`, `replaceMenuRoles`, and batched `getRoleCodesByMenuId` behavior.
- Produces: GET/PUT `/master/roles/{code}/menus` and GET `/master/role-menus`.

- [ ] **Step 1: Write failing assignment service tests**

Cover these exact cases with Mockito:

- Unknown role throws `ResourceNotFoundException` before deletion.
- Any unknown submitted menu code throws `BadRequestException` and `deleteAllByRole_Id` is never called.
- Duplicate menu codes create one permission per menu.
- Empty input deletes prior assignments and saves none.
- Valid replacement deletes old pairs then saves the complete new set in one service call.
- `SUPER_ADMIN` resolves/retains all menu codes.
- All-pairs output is flattened as `(roleCode, menuCode)` records.

- [ ] **Step 2: Run the focused test and verify it fails**

Run: `cd backend && env JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home mvn -Dtest=RoleMenuServiceTest test`

Expected: FAIL because join entity/repository/service are absent.

- [ ] **Step 3: Implement the explicit join model and repository**

`RoleMenuPermissionId` is `@Embeddable`, `Serializable`, and contains `Long roleId` and `Long menuId` with value-based equality. `RoleMenuPermission` uses `@EmbeddedId`, `@MapsId("roleId")`, and `@MapsId("menuId")` to reference `Role` and `Menu`.

Repository methods include:

```java
List<RoleMenuPermission> findAllByRole_CodeOrderByMenu_SortOrderAsc(String roleCode);
List<RoleMenuPermission> findAllByOrderByRole_CodeAscMenu_SortOrderAsc();
List<RoleMenuPermission> findAllByMenu_IdIn(Collection<Long> menuIds);
void deleteAllByRole_Id(Long roleId);
void deleteAllByMenu_Id(Long menuId);
```

- [ ] **Step 4: Implement transactional `RoleMenuService`**

Normalize role/menu codes, resolve the complete submitted menu set before deleting anything, and use a `LinkedHashSet` to deduplicate while preserving deterministic order. `replaceMenuRoles` provides the equivalent validated replacement used by menu create/edit. Enforce the existing `SUPER_ADMIN` invariant by assigning it to every menu and rejecting/remedying attempts to remove it.

- [ ] **Step 5: Switch menu code away from CSV and expose endpoints**

Remove `roles`, `getRoleList`, and `setRoleList` from `Menu`. `MenuService` injects `RoleMenuService`; list/create/update responses obtain role codes from the join service, and menu deletion clears assignments before deleting the menu. The existing `PUT /master/menus/{id}/roles` delegates to normalized replacement.

Move role-oriented GET/PUT methods from `MasterDataController` to `RoleController`, and add:

```java
@GetMapping("/role-menus")
ResponseEntity<ApiResponse<List<RoleMenuResponse>>> getRoleMenuMappings()
```

- [ ] **Step 6: Add V17 and update backend tests**

V17 contains only a guarded PostgreSQL drop:

```sql
ALTER TABLE menus DROP COLUMN IF EXISTS roles;
```

Update controller test fixtures so roles are supplied by the mocked `RoleMenuService`, never by `Menu.roles`.

- [ ] **Step 7: Run role/menu tests and the full backend suite**

Run: `cd backend && env JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home mvn test`

Expected: all tests PASS and no source reference remains to `getRoleList`, `setRoleList`, or `UserRole`.

- [ ] **Step 8: Commit**

```bash
git add backend/src/main backend/src/test
git commit -m "feat: normalize role menu permissions"
```

---

### Task 5: Connect Pinia stores to role and assignment APIs

**Files:**
- Create: `frontend/src/services/roleApi.js`
- Create: `frontend/src/services/roleMenuApi.js`
- Create: `frontend/src/utils/roleMapper.js`
- Modify: `frontend/src/stores/role.js`
- Modify: `frontend/src/stores/menu.js`
- Create: `frontend/test/role-api.test.js`
- Create: `frontend/test/role-mapper.test.js`
- Modify: `frontend/test/data-source-policy.test.js`

**Interfaces:**
- Consumes: role and role-menu HTTP APIs from Tasks 3–4.
- Produces: strict role objects, CRUD actions that reload database state, and assignment load/save actions.

- [ ] **Step 1: Write failing pure frontend tests**

`role-mapper.test.js` asserts that an object response maps `systemRole` to a boolean and that strings/null/missing code throw `Format role dari backend tidak valid.` rather than generating display fallback records.

`role-api.test.js` supplies fake clients that record calls and asserts:

```text
GET    /master/roles
POST   /master/roles                 {code,name,description}
PUT    /master/roles/CUSTOM_AUDITOR  {name,description}
DELETE /master/roles/CUSTOM_AUDITOR
GET    /master/roles/CUSTOM_AUDITOR/menus
PUT    /master/roles/CUSTOM_AUDITOR/menus  ["MST_USERS"]
GET    /master/role-menus
```

- [ ] **Step 2: Run frontend tests and verify modules are missing**

Run: `cd frontend && npm test`

Expected: FAIL because API adapters and strict mapper do not exist.

- [ ] **Step 3: Implement injectable API adapters and mapper**

Each adapter exports `createRoleApi(client)`/`createRoleMenuApi(client)` for tests and a default instance using `api`. Use `extractList` only for list responses and throw on invalid envelopes. Preserve backend `message` values when rejecting requests.

- [ ] **Step 4: Replace role-store stubs with real mutations**

`fetchRoles` accepts object records only. `addRole`, `updateRole(code, payload)`, and `deleteRole(code)` call the adapter and then `await this.fetchRoles()`; they do not optimistically invent records. Remove category/status getters that are no longer part of the API.

- [ ] **Step 5: Add persisted assignment state to `menu.js`**

Add `roleMenuMappings`, `fetchRoleMenuMappings()`, and `fetchRoleMenus(roleCode)`. `updateRoleMenus` PUTs the complete code list, then GETs that role and all mappings again. It may update `menu.roles` only from these backend responses, never from the submitted request itself.

- [ ] **Step 6: Strengthen the source-policy test**

Fail if `role.js` contains the old “Backend belum menyediakan endpoint” stubs, if it accepts `typeof item === 'string'`, or if `menu.js` treats submitted menu codes as the saved result without re-fetching.

- [ ] **Step 7: Run tests and build**

Run: `cd frontend && npm test && npm run build`

Expected: all Node tests PASS and Vite production build succeeds.

- [ ] **Step 8: Commit**

```bash
git add frontend/src/services frontend/src/utils/roleMapper.js frontend/src/stores frontend/test
git commit -m "feat: connect dynamic role stores"
```

---

### Task 6: Update role administration pages for the persistent contract

**Files:**
- Modify: `frontend/src/views/master/RolesView.vue`
- Modify: `frontend/src/views/master/RoleMenusView.vue`
- Verify: `frontend/src/views/master/UsersView.vue`
- Verify: `frontend/src/views/master/MenusView.vue`

**Interfaces:**
- Consumes: `RoleResponse` objects and assignment actions from Task 5.
- Produces: safe role CRUD UI and explicit backend-loaded role-menu selection state.

- [ ] **Step 1: Update `/master/roles` fields and controls**

Remove category/status filters, form inputs, computed counts, and badges because those fields are not persisted. Replace them with a “Sistem”/“Kustom” type badge based on `systemRole`. Submit only `{code,name,description}` on create and `{name,description}` on update. Always call update/delete by immutable `r.code`, not database ID.

Disable the delete button for `systemRole`, with title `Role sistem tidak dapat dihapus`. Keep the user-count precheck for convenience, but treat backend HTTP 409 as authoritative and show its `message` in the page alert.

- [ ] **Step 2: Make `/master/role-menus` load selected assignments explicitly**

Remove category filtering. On mount fetch roles, menus, and all role-menu mappings. Make `selectRole` async and call `menuStore.fetchRoleMenus(roleCode)`; populate both selected sets from that returned database list. Disable controls while a selection or save is loading, and surface errors without retaining another role's selection.

Derive per-role counts from `roleMenuMappings`, not `menu.roles`. After save, set the initial set only from the re-fetched result returned by the store.

- [ ] **Step 3: Verify dependent user/menu forms**

Confirm `UsersView.vue` continues to submit a role code string and displays every custom role from `roleStore.roleCodes`. Confirm `MenusView.vue` sends role code lists accepted by normalized menu endpoints. Make only contract-required corrections; do not redesign unrelated UI.

- [ ] **Step 4: Run frontend checks**

Run: `cd frontend && npm test && npm run build`

Expected: tests and build PASS; searches find no role `category`/`status` dependence in the two changed role pages.

- [ ] **Step 5: Commit**

```bash
git add frontend/src/views/master/RolesView.vue frontend/src/views/master/RoleMenusView.vue frontend/src/views/master/UsersView.vue frontend/src/views/master/MenusView.vue
git commit -m "feat: enable dynamic role administration"
```

---

### Task 7: Verify migration and live database/API consistency

**Files:**
- Modify only if verification exposes a scoped defect in files from Tasks 1–6.

**Interfaces:**
- Consumes: complete backend/frontend implementation.
- Produces: evidence that API results match PostgreSQL and legacy assignments survived.

- [ ] **Step 1: Run static legacy-reference checks**

Run:

```bash
rg -n "UserRole|getRoleList|setRoleList|menus\\.roles|Backend belum menyediakan endpoint" backend/src frontend/src
```

Expected: no obsolete runtime references. Migration SQL references to legacy `m.roles` are allowed only in V16.

- [ ] **Step 2: Run complete automated verification**

Run:

```bash
cd backend && env JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home mvn test
cd frontend && npm test && npm run build
```

Expected: every backend test, frontend test, and production build passes.

- [ ] **Step 3: Apply Flyway migrations through normal backend startup**

Ensure port 8080 is free, then start from `backend` with the configured `.env` and Java 17. Do not run a seed command. Confirm Flyway applies V16 and V17 and Hibernate schema validation succeeds.

- [ ] **Step 4: Verify database/API parity**

Using the configured database and an authenticated API token, compare:

- `SELECT COUNT(*) FROM roles` with `GET /api/master/roles` length.
- `SELECT COUNT(*) FROM role_menu_permissions` with `GET /api/master/role-menus` length.
- One custom role's database menu codes with `GET /api/master/roles/{code}/menus`.

Also create a temporary custom role through POST, update it, assign one menu, verify all three API reads, and delete it. The final delete must leave no role or permission row. Do not alter built-in or user-assigned roles.

- [ ] **Step 5: Verify guarded deletion behavior**

Attempt to delete `SUPER_ADMIN` and assert HTTP 409. Select an existing role assigned to a user, attempt deletion, and assert HTTP 409 without changing its row or assignments.

- [ ] **Step 6: Review final diff and commit any verification-only fixes**

Run `git diff --check`, inspect `git status --short`, and ensure no environment file, token, generated build output, or unrelated file is staged. If fixes were necessary, commit only those scoped files with `fix: complete dynamic role verification`.
