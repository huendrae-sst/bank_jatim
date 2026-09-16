# Data-Driven Role Navigation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Render navigation from database data and deny direct browser URLs that are not assigned to the authenticated user's role.

**Architecture:** Spring Boot exposes one authenticated `/navigation` projection containing every active menu, its database-backed route rules, and an `allowed` flag computed from the current principal. Vue keeps that projection in a session-scoped Pinia store; pure policy functions drive both the generated layout and an async router guard so display and access use the same decisions.

**Tech Stack:** Java 21, Spring Boot 4.1.1, Spring Security, Spring Data JPA, Flyway/PostgreSQL, JUnit 5, Mockito, Vue 3, Pinia, Vue Router 4, Node test runner.

## Global Constraints

- Do not hardcode business roles, menu codes, paths, titles, icons, or modules in Vue layout or router access logic.
- The authenticated backend principal is the only source of the current role; `/navigation` accepts no role parameter.
- Only active menus are returned.
- `SUPER_ADMIN` gets every active menu through existing `RoleMenuService` behavior.
- Route matching is segment-safe and the longest database rule wins.
- A protected route without a matching active rule is denied.
- Public/system routes are limited to authentication pages and access denied.
- Navigation load failure fails closed.
- Existing menu and role-menu assignments remain intact.
- No frontend runtime dependency is added.

## File Structure

### Backend

- Create `backend/src/main/resources/db/migration/V18__create_menu_route_rules.sql`: schema and current-route coverage.
- Create `backend/src/main/java/com/bankjatim/jims/domain/MenuRouteRule.java` and `RouteMatchType.java`: route rule model.
- Create `backend/src/main/java/com/bankjatim/jims/repository/MenuRouteRuleRepository.java`: ordered rule lookup.
- Modify `backend/src/main/java/com/bankjatim/jims/repository/MenuRepository.java`: active menu lookup.
- Create `backend/src/main/java/com/bankjatim/jims/dto/NavigationRouteRuleResponse.java` and `NavigationItemResponse.java`: response contract.
- Create `backend/src/main/java/com/bankjatim/jims/service/NavigationService.java`: current-user projection.
- Create `backend/src/main/java/com/bankjatim/jims/controller/NavigationController.java`: authenticated endpoint.
- Create focused service and controller tests.

### Frontend

- Create `frontend/src/services/navigationApi.js`: endpoint client.
- Create `frontend/src/utils/navigationPolicy.js`: path matching, resolution, grouping, and home selection.
- Create `frontend/src/stores/navigationCore.js` and `navigation.js`: session state and Pinia adapter.
- Create `frontend/src/router/navigationGuard.js`: async authorization guard.
- Modify `frontend/src/router/index.js` and `frontend/src/views/auth/LoginView.vue`: dynamic entry and direct-URL protection.
- Create `frontend/src/views/errors/AccessDeniedView.vue`.
- Modify `frontend/src/components/AppLayout.vue`: data-driven sidebar/topbar.
- Create API, policy, core, and guard tests.

---

### Task 1: Persist Route Rules and Build the Navigation Projection

**Files:**
- Create: `backend/src/main/java/com/bankjatim/jims/domain/RouteMatchType.java`
- Create: `backend/src/main/java/com/bankjatim/jims/domain/MenuRouteRule.java`
- Create: `backend/src/main/java/com/bankjatim/jims/repository/MenuRouteRuleRepository.java`
- Modify: `backend/src/main/java/com/bankjatim/jims/repository/MenuRepository.java`
- Create: `backend/src/main/java/com/bankjatim/jims/dto/NavigationRouteRuleResponse.java`
- Create: `backend/src/main/java/com/bankjatim/jims/dto/NavigationItemResponse.java`
- Create: `backend/src/main/java/com/bankjatim/jims/service/NavigationService.java`
- Test: `backend/src/test/java/com/bankjatim/jims/service/NavigationServiceTest.java`

**Interfaces:**
- Consumes: `RoleMenuService.getMenuCodesForRole(String)` and `UserPrincipal.getRole()`.
- Produces: `NavigationService.getNavigation(UserPrincipal): List<NavigationItemResponse>`.
- Produces: `MenuRouteRuleRepository.findAllByMenu_IdInOrderByMenu_SortOrderAscSortOrderAsc(Collection<Long>)`.
- Produces records `NavigationItemResponse(String code, String title, String module, String parentCode, String path, String icon, Integer order, boolean allowed, List<NavigationRouteRuleResponse> routeRules)` and `NavigationRouteRuleResponse(String path, RouteMatchType matchType, Integer order)`.

- [ ] **Step 1: Write failing service tests**

Create three tests using real domain values and mocked repository boundaries:

```java
@Test
void marksOnlyAssignedActiveMenusAsAllowedAndGroupsRulesByOwner() {
    Menu orders = menu(10L, "ORD_LIST", "/orders", 10);
    Menu approvals = menu(11L, "ORD_APPROVAL", "/orders/approvals", 11);
    when(menuRepository.findAllByStatusIgnoreCaseOrderBySortOrderAsc("AKTIF"))
            .thenReturn(List.of(orders, approvals));
    when(roleMenuService.getMenuCodesForRole("REQUESTER_CABANG"))
            .thenReturn(List.of("ORD_LIST"));
    when(ruleRepository.findAllByMenu_IdInOrderByMenu_SortOrderAscSortOrderAsc(List.of(10L, 11L)))
            .thenReturn(List.of(
                    rule(orders, "/orders", RouteMatchType.PREFIX, 1),
                    rule(approvals, "/orders/approvals", RouteMatchType.EXACT, 1)
            ));

    List<NavigationItemResponse> result =
            service.getNavigation(principal("REQUESTER_CABANG"));

    assertThat(result).extracting(
            NavigationItemResponse::code,
            NavigationItemResponse::allowed
    ).containsExactly(
            tuple("ORD_LIST", true),
            tuple("ORD_APPROVAL", false)
    );
    assertThat(result.getFirst().routeRules())
            .extracting(NavigationRouteRuleResponse::path)
            .containsExactly("/orders");
}

@Test
void superAdminReceivesEveryActiveMenuAsAllowed() {
    Menu dashboard = menu(1L, "DASH_EXEC", "/dashboard", 1);
    Menu orders = menu(10L, "ORD_LIST", "/orders", 10);
    when(menuRepository.findAllByStatusIgnoreCaseOrderBySortOrderAsc("AKTIF"))
            .thenReturn(List.of(dashboard, orders));
    when(roleMenuService.getMenuCodesForRole("SUPER_ADMIN"))
            .thenReturn(List.of("DASH_EXEC", "ORD_LIST"));
    when(ruleRepository.findAllByMenu_IdInOrderByMenu_SortOrderAscSortOrderAsc(List.of(1L, 10L)))
            .thenReturn(List.of());

    assertThat(service.getNavigation(principal("SUPER_ADMIN")))
            .allMatch(NavigationItemResponse::allowed);
}

@Test
void returnsEmptyNavigationWithoutLoadingPermissionsWhenNoActiveMenuExists() {
    when(menuRepository.findAllByStatusIgnoreCaseOrderBySortOrderAsc("AKTIF"))
            .thenReturn(List.of());

    assertThat(service.getNavigation(principal("USER_ADMIN"))).isEmpty();
    verifyNoInteractions(ruleRepository, roleMenuService);
}
```

Helpers must assign IDs and construct a complete principal through `UserPrincipal.create`. These tests catch “all menus allowed,” “inactive query replaced with all-menu query,” and “rules attached to the wrong menu.”

- [ ] **Step 2: Run tests and verify RED**

Run:

```bash
cd backend && ./mvnw -Dtest=NavigationServiceTest test
```

Expected: compilation fails because navigation types and repository methods do not exist.

- [ ] **Step 3: Implement the minimum model, repository, DTOs, and service**

`RouteMatchType` contains only `EXACT` and `PREFIX`. Map `MenuRouteRule` to `menu_route_rules` with generated `Long id`, eager `@ManyToOne Menu menu`, `String path`, enumerated match type, and `Integer sortOrder`. Add to `MenuRepository`:

```java
List<Menu> findAllByStatusIgnoreCaseOrderBySortOrderAsc(String status);
```

Implement response-record `from` factories. Implement the service core exactly as:

```java
List<Menu> menus = menuRepository
        .findAllByStatusIgnoreCaseOrderBySortOrderAsc("AKTIF");
if (menus.isEmpty()) return List.of();

Set<String> allowedCodes = new HashSet<>(
        roleMenuService.getMenuCodesForRole(principal.getRole()));
List<MenuRouteRule> rules = ruleRepository
        .findAllByMenu_IdInOrderByMenu_SortOrderAscSortOrderAsc(
                menus.stream().map(Menu::getId).toList());

Map<Long, List<NavigationRouteRuleResponse>> rulesByMenu = rules.stream()
        .collect(Collectors.groupingBy(
                rule -> rule.getMenu().getId(),
                LinkedHashMap::new,
                Collectors.mapping(
                        NavigationRouteRuleResponse::from,
                        Collectors.toList())
        ));

return menus.stream()
        .map(menu -> NavigationItemResponse.from(
                menu,
                allowedCodes.contains(menu.getCode()),
                rulesByMenu.getOrDefault(menu.getId(), List.of())))
        .toList();
```

- [ ] **Step 4: Run focused tests and verify GREEN**

```bash
cd backend && ./mvnw -Dtest=NavigationServiceTest test
```

Expected: all three tests pass without warnings.

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/com/bankjatim/jims/domain/RouteMatchType.java backend/src/main/java/com/bankjatim/jims/domain/MenuRouteRule.java backend/src/main/java/com/bankjatim/jims/repository/MenuRouteRuleRepository.java backend/src/main/java/com/bankjatim/jims/repository/MenuRepository.java backend/src/main/java/com/bankjatim/jims/dto/NavigationRouteRuleResponse.java backend/src/main/java/com/bankjatim/jims/dto/NavigationItemResponse.java backend/src/main/java/com/bankjatim/jims/service/NavigationService.java backend/src/test/java/com/bankjatim/jims/service/NavigationServiceTest.java
git commit -m "feat: project role-aware navigation data"
```

### Task 2: Add Route Data and the Authenticated Endpoint

**Files:**
- Create: `backend/src/main/resources/db/migration/V18__create_menu_route_rules.sql`
- Create: `backend/src/main/java/com/bankjatim/jims/controller/NavigationController.java`
- Test: `backend/src/test/java/com/bankjatim/jims/controller/NavigationControllerTest.java`

**Interfaces:**
- Consumes: `NavigationService.getNavigation(UserPrincipal)`.
- Produces: `GET /navigation -> ApiResponse<List<NavigationItemResponse>>`.
- Produces database route rules used by the browser guard.

- [ ] **Step 1: Write the failing controller test**

```java
@Test
void navigationUsesAuthenticatedPrincipalAndReturnsProjection() {
    NavigationService service = mock(NavigationService.class);
    UserPrincipal principal = UserPrincipal.create(
            7L, "Ayu", "ayu@bankjatim.co.id", "NIP-7", "secret",
            "REQUESTER_CABANG", 2L, 3L, BigDecimal.ZERO, true);
    NavigationItemResponse item = new NavigationItemResponse(
            "ORD_LIST", "Daftar Order Cabang", "Permintaan & Order",
            null, "/orders", "bi-cart3", 10, true,
            List.of(new NavigationRouteRuleResponse(
                    "/orders", RouteMatchType.PREFIX, 1)));
    when(service.getNavigation(principal)).thenReturn(List.of(item));

    ResponseEntity<ApiResponse<List<NavigationItemResponse>>> response =
            new NavigationController(service).getNavigation(principal);

    verify(service).getNavigation(principal);
    assertThat(response.getBody().getData()).containsExactly(item);
}

@Test
void navigationRejectsMissingAuthenticatedPrincipal() {
    assertThatThrownBy(() ->
            new NavigationController(mock(NavigationService.class))
                    .getNavigation(null)
    ).isInstanceOf(AuthenticationCredentialsNotFoundException.class);
}
```

- [ ] **Step 2: Run it and verify RED**

```bash
cd backend && ./mvnw -Dtest=NavigationControllerTest test
```

Expected: compilation fails because `NavigationController` is absent.

- [ ] **Step 3: Implement the endpoint**

```java
@RestController
@RequestMapping("/navigation")
@RequiredArgsConstructor
public class NavigationController {
    private final NavigationService navigationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NavigationItemResponse>>> getNavigation(
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            throw new AuthenticationCredentialsNotFoundException(
                    "Authenticated principal is required");
        }
        return ResponseEntity.ok(
                ApiResponse.ok(navigationService.getNavigation(principal)));
    }
}
```

Do not add a role query parameter or path variable.

- [ ] **Step 4: Create the schema and complete route data migration**

Create `V18__create_menu_route_rules.sql` with:

```sql
CREATE TABLE menu_route_rules (
    id BIGSERIAL PRIMARY KEY,
    menu_id BIGINT NOT NULL REFERENCES menus(id) ON DELETE CASCADE,
    path VARCHAR(255) NOT NULL,
    match_type VARCHAR(20) NOT NULL
        CHECK (match_type IN ('EXACT', 'PREFIX')),
    sort_order INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT uk_menu_route_rule UNIQUE (menu_id, path, match_type)
);

CREATE INDEX idx_menu_route_rules_path ON menu_route_rules(path);
CREATE INDEX idx_menu_route_rules_menu ON menu_route_rules(menu_id);

INSERT INTO menus (
    code, title, module, parent_code, path, icon,
    sort_order, status, description
) VALUES
    ('MST_ROLE_MENUS', 'Mapping Role & Menu', 'Master Data', NULL,
     '/master/role-menus', 'bi-list-check', 79, 'AKTIF',
     'Pengaturan hak akses menu untuk setiap peran'),
    ('MST_DATA', 'Data Master', 'Master Data', NULL,
     '/master/data', 'bi-database', 80, 'AKTIF',
     'Ringkasan data referensi sistem'),
    ('REP_ESS', 'Laporan Executive Support', 'Laporan & Rekapitulasi', NULL,
     '/reports/ess', 'bi-file-earmark-bar-graph', 115, 'AKTIF',
     'Ringkasan laporan executive support system')
ON CONFLICT (code) DO NOTHING;

INSERT INTO role_menu_permissions (role_id, menu_id)
SELECT r.id, m.id
FROM roles r
JOIN menus m ON m.code IN ('MST_ROLE_MENUS', 'MST_DATA')
WHERE r.code IN ('SUPER_ADMIN', 'USER_ADMIN')
ON CONFLICT DO NOTHING;

INSERT INTO role_menu_permissions (role_id, menu_id)
SELECT r.id, m.id
FROM roles r
JOIN menus m ON m.code = 'REP_ESS'
WHERE r.code IN ('SUPER_ADMIN', 'MANAGEMENT', 'AUDITOR')
ON CONFLICT DO NOTHING;

INSERT INTO menu_route_rules (menu_id, path, match_type, sort_order)
SELECT id, path, 'PREFIX', 1
FROM menus
ON CONFLICT DO NOTHING;

INSERT INTO menu_route_rules (menu_id, path, match_type, sort_order)
SELECT m.id, rule.path, rule.match_type, rule.sort_order
FROM (VALUES
    ('WH_PICKING', '/warehouse/picking-packing', 'PREFIX', 2),
    ('WH_SHIPMENT', '/distribution/manifest', 'PREFIX', 2),
    ('WH_SHIPMENT', '/distribution/label', 'PREFIX', 3),
    ('INV_BALANCES', '/inventory/stock-balances', 'PREFIX', 2),
    ('INV_MUTATION', '/inventory/ledger', 'PREFIX', 2),
    ('INV_MUTATION', '/inventory/stock-card', 'PREFIX', 3),
    ('INV_EWS', '/inventory/ews', 'PREFIX', 2),
    ('INV_SWITCHING', '/inventory/switching-stocks', 'PREFIX', 2),
    ('MST_VENDOR', '/master/vendors-couriers', 'PREFIX', 2),
    ('MST_MENUS', '/menus', 'EXACT', 2),
    ('MST_ROLE_MENUS', '/role-menus', 'EXACT', 2)
) AS rule(menu_code, path, match_type, sort_order)
JOIN menus m ON m.code = rule.menu_code
ON CONFLICT DO NOTHING;
```

Canonical prefix rules cover detail/create/print children. Extra rows cover registered routes whose segment is not beneath the canonical menu path.

- [ ] **Step 5: Run focused and full backend tests**

```bash
cd backend && ./mvnw -Dtest=NavigationServiceTest,NavigationControllerTest test
cd backend && ./mvnw test
```

Expected: focused tests and the full backend suite pass.

- [ ] **Step 6: Commit**

```bash
git add backend/src/main/resources/db/migration/V18__create_menu_route_rules.sql backend/src/main/java/com/bankjatim/jims/controller/NavigationController.java backend/src/test/java/com/bankjatim/jims/controller/NavigationControllerTest.java
git commit -m "feat: expose authenticated navigation rules"
```

### Task 3: Implement the Pure Frontend Navigation Policy

**Files:**
- Create: `frontend/src/utils/navigationPolicy.js`
- Test: `frontend/test/navigation-policy.test.js`

**Interfaces:**
- Produces `normalizeNavigationPath(path)`, `routeRuleMatches(rule, path)`, `resolveNavigationItem(items, path)`, `buildNavigationGroups(items)`, and `firstAccessiblePath(items)`.

- [ ] **Step 1: Write failing policy tests**

```js
test('prefix matching respects path segment boundaries', () => {
  assert.equal(routeRuleMatches(
    { path: '/orders', matchType: 'PREFIX' },
    '/orders/42'
  ), true);
  assert.equal(routeRuleMatches(
    { path: '/orders', matchType: 'PREFIX' },
    '/orders-archive'
  ), false);
});

test('the most specific rule owns a route even when denied', () => {
  const items = [
    item('ORD_LIST', true, 10, [
      { path: '/orders', matchType: 'PREFIX', order: 1 }
    ]),
    item('ORD_APPROVAL', false, 11, [
      { path: '/orders/approvals', matchType: 'PREFIX', order: 1 }
    ])
  ];
  const result = resolveNavigationItem(items, '/orders/approvals');
  assert.equal(result.code, 'ORD_APPROVAL');
  assert.equal(result.allowed, false);
});

test('exact rules do not grant child paths', () => {
  assert.equal(routeRuleMatches(
    { path: '/menus', matchType: 'EXACT' },
    '/menus/42'
  ), false);
});

test('groups filter denied items, honor order, and attach children', () => {
  const groups = buildNavigationGroups([
    nav('MASTER', 'Administration', '/master', true, 10, null),
    nav('USERS', 'Administration', '/master/users', true, 11, 'MASTER'),
    nav('ROLES', 'Administration', '/master/roles', false, 12, 'MASTER'),
    nav('ORD', 'Order', '/orders', true, 2, null)
  ]);
  assert.deepEqual(
    groups.map((group) => group.module),
    ['Order', 'Administration']
  );
  assert.deepEqual(
    groups[1].items[0].children.map((child) => child.code),
    ['USERS']
  );
});

test('first path is data-driven and an empty assignment returns null', () => {
  assert.equal(firstAccessiblePath([
    nav('DENIED', 'Admin', '/denied', false, 1, null),
    nav('ALLOWED', 'Operations', '/allowed', true, 2, null)
  ]), '/allowed');
  assert.equal(firstAccessiblePath([]), null);
});
```

These tests catch naive `startsWith`, first-match rather than longest-match, denied-item leakage, unstable ordering, and flattened parent data.

- [ ] **Step 2: Run tests and verify RED**

```bash
cd frontend && node --test test/navigation-policy.test.js
```

Expected: module-not-found failure for `navigationPolicy.js`.

- [ ] **Step 3: Implement normalization, matching, and resolution**

```js
export function normalizeNavigationPath(value) {
  const path = String(value || '/').split(/[?#]/, 1)[0] || '/';
  if (path === '/') return '/';
  return '/' + path.replace(/^\/+|\/+$/g, '');
}

export function routeRuleMatches(rule, targetPath) {
  const rulePath = normalizeNavigationPath(rule?.path);
  const target = normalizeNavigationPath(targetPath);
  if (rule?.matchType === 'EXACT') return target === rulePath;
  if (rule?.matchType === 'PREFIX') {
    return target === rulePath || target.startsWith(rulePath + '/');
  }
  return false;
}

export function resolveNavigationItem(items, targetPath) {
  const matches = items.flatMap((item) =>
    (item.routeRules || [])
      .filter((rule) => routeRuleMatches(rule, targetPath))
      .map((rule) => ({
        item,
        rule,
        length: normalizeNavigationPath(rule.path).length
      }))
  );
  matches.sort((left, right) =>
    right.length - left.length ||
    Number(right.rule.matchType === 'EXACT') -
      Number(left.rule.matchType === 'EXACT') ||
    (left.rule.order || 0) - (right.rule.order || 0)
  );
  return matches[0]?.item || null;
}
```

`buildNavigationGroups` filters `allowed === true`, sorts numerically by `order`, attaches visible children to a visible `parentCode`, and groups roots by `module`. Promote an allowed orphan to a module root rather than hiding a valid assignment. Set group order to its lowest root order. `firstAccessiblePath` returns the first sorted allowed nonblank path or `null`.

```js
const byOrder = (left, right) =>
  (Number(left.order) || 0) - (Number(right.order) || 0);

export function buildNavigationGroups(items) {
  const visible = items
    .filter((item) => item.allowed === true)
    .map((item) => ({ ...item, children: [] }))
    .sort(byOrder);
  const byCode = new Map(visible.map((item) => [item.code, item]));
  const roots = [];

  for (const item of visible) {
    const parent = item.parentCode
      ? byCode.get(item.parentCode)
      : null;
    if (parent) parent.children.push(item);
    else roots.push(item);
  }

  const groups = new Map();
  for (const item of roots) {
    const module = item.module || 'Lainnya';
    if (!groups.has(module)) {
      groups.set(module, {
        key: module,
        module,
        icon: item.icon,
        order: Number(item.order) || 0,
        items: []
      });
    }
    groups.get(module).items.push(item);
  }

  return [...groups.values()].sort(byOrder);
}

export function firstAccessiblePath(items) {
  return [...items]
    .filter((item) => item.allowed === true && item.path)
    .sort(byOrder)[0]?.path || null;
}
```

- [ ] **Step 4: Run tests and verify GREEN**

```bash
cd frontend && node --test test/navigation-policy.test.js
```

Expected: all policy tests pass.

- [ ] **Step 5: Commit**

```bash
git add frontend/src/utils/navigationPolicy.js frontend/test/navigation-policy.test.js
git commit -m "feat: add data-driven navigation policy"
```

### Task 4: Add Navigation API and Session-Scoped State

**Files:**
- Create: `frontend/src/services/navigationApi.js`
- Create: `frontend/src/stores/navigationCore.js`
- Create: `frontend/src/stores/navigation.js`
- Test: `frontend/test/navigation-api.test.js`
- Test: `frontend/test/navigation-core.test.js`

**Interfaces:**
- Produces `createNavigationApi(client).getNavigation()`.
- Produces `createNavigationState()`, `ensureNavigationLoaded(state, loader, userKey)`, and `clearNavigationState(state)`.
- Produces Pinia getters `accessibleItems`, `groups`, `firstAccessiblePath`, and `resolveRoute(path)`, plus actions `ensureLoaded(userKey)` and `clear()`.

- [ ] **Step 1: Write failing API and state tests**

```js
test('navigation API reads authenticated projection', async () => {
  const calls = [];
  const navigationApi = createNavigationApi({
    get: async (url) => {
      calls.push(url);
      return { success: true, data: [{ code: 'ORD_LIST' }] };
    }
  });
  const response = await navigationApi.getNavigation();
  assert.deepEqual(calls, ['/navigation']);
  assert.equal(response.data[0].code, 'ORD_LIST');
});

test('state loads once per user and reloads for a new identity', async () => {
  const state = createNavigationState();
  let calls = 0;
  const loader = async () => {
    calls += 1;
    return [{ code: 'MENU_' + calls }];
  };
  await ensureNavigationLoaded(state, loader, '7:REQUESTER_CABANG');
  await ensureNavigationLoaded(state, loader, '7:REQUESTER_CABANG');
  await ensureNavigationLoaded(state, loader, '8:AUDITOR');
  assert.equal(calls, 2);
  assert.equal(state.items[0].code, 'MENU_2');
});

test('load failure clears old permission and clear resets session state', async () => {
  const state = createNavigationState();
  state.items = [{ code: 'OLD', allowed: true }];
  await assert.rejects(() => ensureNavigationLoaded(
    state,
    async () => { throw new Error('offline'); },
    '7:USER'
  ));
  assert.deepEqual(state.items, []);
  assert.equal(state.loaded, false);
  assert.equal(state.error, 'offline');
  clearNavigationState(state);
  assert.equal(state.loadedForUser, null);
  assert.equal(state.error, null);
});
```

- [ ] **Step 2: Run tests and verify RED**

```bash
cd frontend && node --test test/navigation-api.test.js test/navigation-core.test.js
```

Expected: module-not-found failures for the API and core modules.

- [ ] **Step 3: Implement API, core, and Pinia adapter**

```js
export function createNavigationApi(client) {
  return {
    getNavigation: () => client.get('/navigation')
  };
}

export function createNavigationState() {
  return {
    items: [],
    loading: false,
    loaded: false,
    loadedForUser: null,
    error: null
  };
}
```

`ensureNavigationLoaded` returns cached items only when `loaded` and the user key matches. Otherwise it clears items, awaits the loader, rejects non-array results with `Format respons navigasi tidak valid.`, and caches only successful data. On failure it keeps `items=[]`, `loaded=false`, and the error message. `clearNavigationState` restores the exact initial fields.

```js
export async function ensureNavigationLoaded(state, loader, userKey) {
  if (state.loaded && state.loadedForUser === userKey) {
    return state.items;
  }
  state.items = [];
  state.loading = true;
  state.loaded = false;
  state.loadedForUser = null;
  state.error = null;
  try {
    const items = await loader();
    if (!Array.isArray(items)) {
      throw new Error('Format respons navigasi tidak valid.');
    }
    state.items = items;
    state.loaded = true;
    state.loadedForUser = userKey;
    return items;
  } catch (error) {
    state.items = [];
    state.error = error?.message || 'Gagal memuat navigasi.';
    throw error;
  } finally {
    state.loading = false;
  }
}

export function clearNavigationState(state) {
  Object.assign(state, createNavigationState());
}
```

`navigation.js` uses `defineStore('navigation', ...)`, the shared API client, core functions, and Task 3 policy functions. Its loader is:

```js
ensureNavigationLoaded(
  this,
  async () => {
    const response = await navigationApi.getNavigation();
    return response?.data ?? response;
  },
  userKey
)
```

All getters call pure policy functions instead of duplicating access logic.

- [ ] **Step 4: Run focused tests and verify GREEN**

```bash
cd frontend && node --test test/navigation-api.test.js test/navigation-core.test.js test/navigation-policy.test.js
```

Expected: all focused tests pass.

- [ ] **Step 5: Commit**

```bash
git add frontend/src/services/navigationApi.js frontend/src/stores/navigationCore.js frontend/src/stores/navigation.js frontend/test/navigation-api.test.js frontend/test/navigation-core.test.js
git commit -m "feat: load navigation for active session"
```

### Task 5: Enforce Direct URL Access in the Router

**Files:**
- Create: `frontend/src/router/navigationGuard.js`
- Modify: `frontend/src/router/index.js`
- Modify: `frontend/src/views/auth/LoginView.vue`
- Test: `frontend/test/navigation-guard.test.js`

**Interfaces:**
- Consumes store `ensureLoaded`, `resolveRoute`, and `firstAccessiblePath`.
- Produces `createNavigationGuard({ getAuthStore, getNavigationStore })`, an async return-style Vue Router guard.
- Uses `meta.public` for unauthenticated pages and `meta.system` for access denied.

- [ ] **Step 1: Write failing guard tests**

```js
test('unauthenticated users go to login before navigation loads', async () => {
  let loads = 0;
  const guard = createNavigationGuard({
    getAuthStore: () => ({ isAuthenticated: false }),
    getNavigationStore: () => ({
      ensureLoaded: async () => { loads += 1; }
    })
  });
  assert.equal(await guard(route('/orders')), '/login');
  assert.equal(loads, 0);
});

test('authorized direct URLs load current-user navigation and pass', async () => {
  const navigation = navigationStore({
    code: 'ORD_LIST',
    allowed: true
  });
  const guard = createNavigationGuard(dependencies(navigation));
  assert.equal(await guard(route('/orders/42')), true);
  assert.deepEqual(
    navigation.loadedKeys,
    ['7:REQUESTER_CABANG']
  );
});

test('a specific denied owner blocks a URL', async () => {
  const navigation = navigationStore({
    code: 'ORD_APPROVAL',
    allowed: false
  });
  const result = await createNavigationGuard(dependencies(navigation))(
    route('/orders/approvals')
  );
  assert.deepEqual(result, {
    name: 'access-denied',
    query: {
      from: '/orders/approvals',
      reason: 'forbidden'
    }
  });
});

test('system page bypasses matching and unmatched route fails closed', async () => {
  const navigation = navigationStore(null);
  const guard = createNavigationGuard(dependencies(navigation));
  assert.equal(await guard(route(
    '/access-denied',
    { system: true }
  )), true);
  assert.deepEqual(await guard(route('/unregistered')), {
    name: 'access-denied',
    query: {
      from: '/unregistered',
      reason: 'unmatched'
    }
  });
});

test('load failure fails closed', async () => {
  const navigation = navigationStore(null);
  navigation.ensureLoaded = async () => {
    throw new Error('offline');
  };
  assert.deepEqual(
    await createNavigationGuard(dependencies(navigation))(
      route('/orders')
    ),
    {
      name: 'access-denied',
      query: {
        from: '/orders',
        reason: 'unavailable'
      }
    }
  );
});

test('root selects data-driven home or empty state', async () => {
  const withHome = navigationStore(null, '/inventory/balances');
  assert.equal(
    await createNavigationGuard(dependencies(withHome))(route('/')),
    '/inventory/balances'
  );
  const empty = navigationStore(null, null);
  assert.deepEqual(
    await createNavigationGuard(dependencies(empty))(route('/')),
    {
      name: 'access-denied',
      query: { reason: 'empty' }
    }
  );
});
```

Helpers use literal auth user ID `7`, role `REQUESTER_CABANG`, and return-style results. They assert application decisions, not Vue Router internals.

- [ ] **Step 2: Run tests and verify RED**

```bash
cd frontend && node --test test/navigation-guard.test.js
```

Expected: module-not-found failure for `navigationGuard.js`.

- [ ] **Step 3: Implement and install the guard**

```js
export function createNavigationGuard({
  getAuthStore,
  getNavigationStore
}) {
  return async (to) => {
    if (to.meta.public) return true;

    const auth = getAuthStore();
    if (!auth.isAuthenticated) return '/login';
    if (to.meta.system) return true;

    const navigation = getNavigationStore();
    const identity = auth.user?.id ?? auth.user?.email;
    const userKey = String(identity) + ':' + auth.userRole;

    try {
      await navigation.ensureLoaded(userKey);
    } catch {
      return {
        name: 'access-denied',
        query: {
          from: to.fullPath,
          reason: 'unavailable'
        }
      };
    }

    if (to.path === '/') {
      return navigation.firstAccessiblePath || {
        name: 'access-denied',
        query: { reason: 'empty' }
      };
    }

    const owner = navigation.resolveRoute(to.path);
    if (!owner) {
      return {
        name: 'access-denied',
        query: {
          from: to.fullPath,
          reason: 'unmatched'
        }
      };
    }

    return owner.allowed === true
      ? true
      : {
          name: 'access-denied',
          query: {
            from: to.fullPath,
            reason: 'forbidden'
          }
        };
  };
}
```

In `router/index.js` remove the `/dashboard` root redirect, add `/access-denied` named `access-denied` with `meta: { system: true }`, replace the catch-all dashboard redirect with an access-denied redirect, and install the return-style guard with `useAuthStore()` and `useNavigationStore()` factories. Change successful login navigation from `/dashboard/operational` to `/`.

- [ ] **Step 4: Run focused and complete frontend tests**

```bash
cd frontend && node --test test/navigation-guard.test.js
cd frontend && npm test
```

Expected: guard tests and complete frontend suite pass.

- [ ] **Step 5: Commit**

```bash
git add frontend/src/router/navigationGuard.js frontend/src/router/index.js frontend/src/views/auth/LoginView.vue frontend/test/navigation-guard.test.js
git commit -m "feat: guard direct URLs with navigation data"
```

### Task 6: Render the Layout from Navigation Data

**Files:**
- Create: `frontend/src/views/errors/AccessDeniedView.vue`
- Modify: `frontend/src/components/AppLayout.vue`
- Test: `frontend/test/navigation-policy.test.js`

**Interfaces:**
- Consumes `navigationStore.groups`, `accessibleItems`, `resolveRoute(path)`, and `firstAccessiblePath`.
- Produces a layout without a hardcoded business menu catalog.
- Produces access-denied states for `forbidden`, `unmatched`, `unavailable`, and `empty`.

- [ ] **Step 1: Run the nested navigation test before layout work**

Run:

```bash
cd frontend && node --test test/navigation-policy.test.js
```

Expected: the parent-child, denied-item, and ordering tests from Task 3 pass. Mutate the child attachment condition locally to confirm the nested assertion fails, then restore it before editing the layout.

- [ ] **Step 2: Replace hardcoded layout navigation**

In `AppLayout.vue`:

- import and instantiate `useNavigationStore`;
- compute `navigationGroups` from `navigationStore.groups`;
- compute topbar shortcuts from `navigationStore.accessibleItems.slice(0, 3)`;
- change the logo route to `/`;
- replace the entire hardcoded business sidebar with nested `v-for` rendering;
- read every link path, title, icon, order, and module from response data;
- determine active ownership with `navigationStore.resolveRoute(route.path)?.code`;
- key `openMenus` by module string instead of fixed business keys;
- render “Tidak ada menu yang diberikan” after loading when groups are empty.

A group with one root item and no children renders a direct link. Other groups render a collapsible module header, roots, and children. The module header uses its first root item's icon because module icons are not separately persisted. Remove fixed computed flags such as `isOrdersActive`, `isWarehouseActive`, and every fixed `onMounted` expansion branch. Preserve theme, notification, scanner, profile, and content behavior.

Use this template shape for the replacement block:

```vue
<li v-for="group in navigationGroups" :key="group.key" class="nav-item">
  <router-link
    v-if="group.items.length === 1 && group.items[0].children.length === 0"
    :to="group.items[0].path"
    class="nav-link"
    :class="{ active: ownsCurrentRoute(group.items[0]) }"
  >
    <i :class="['nav-icon', 'bi', group.items[0].icon]"></i>
    <span class="nav-text">{{ group.items[0].title }}</span>
  </router-link>
  <template v-else>
    <a
      href="#"
      class="nav-link"
      :class="{ active: groupOwnsCurrentRoute(group) }"
      @click.prevent="toggleMenu(group.key)"
    >
      <i :class="['nav-icon', 'bi', group.icon]"></i>
      <span class="nav-text">{{ group.module }}</span>
      <i class="nav-arrow bi bi-chevron-right"
         :class="{ rotated: openMenus[group.key] }"></i>
    </a>
    <ul v-show="openMenus[group.key]" class="nav nav-treeview">
      <template v-for="item in group.items" :key="item.code">
        <li class="nav-item">
          <router-link
            :to="item.path"
            class="nav-link"
            :class="{ active: ownsCurrentRoute(item) }"
          >
            <i :class="['nav-icon', 'bi', item.icon]"></i>
            <span class="nav-text">{{ item.title }}</span>
          </router-link>
        </li>
        <li v-for="child in item.children"
            :key="child.code"
            class="nav-item ms-3">
          <router-link
            :to="child.path"
            class="nav-link"
            :class="{ active: ownsCurrentRoute(child) }"
          >
            <i :class="['nav-icon', 'bi', child.icon]"></i>
            <span class="nav-text">{{ child.title }}</span>
          </router-link>
        </li>
      </template>
    </ul>
  </template>
</li>
```

Use these script helpers:

```js
const navigationGroups = computed(() => navigationStore.groups);
const topbarShortcuts = computed(() =>
  navigationStore.accessibleItems.slice(0, 3)
);
const currentOwner = computed(() =>
  navigationStore.resolveRoute(route.path)
);
const ownsCurrentRoute = (item) =>
  currentOwner.value?.code === item.code;
const groupOwnsCurrentRoute = (group) =>
  group.items.some((item) =>
    ownsCurrentRoute(item) || item.children.some(ownsCurrentRoute)
  );
```

Update logout flow to call `navigationStore.clear()` immediately before `authStore.logout()`. Keep `auth.js` unchanged so the stores do not form a circular dependency.

- [ ] **Step 3: Create the access-denied view**

Use `useRoute()` and `useNavigationStore()`. Render exact messages:

- `unavailable`: “Navigasi tidak dapat dimuat. Silakan coba lagi.”
- `empty`: “Belum ada menu yang diberikan untuk akun ini.”
- `unmatched`: “Halaman ini belum terdaftar pada konfigurasi menu.”
- otherwise: “Anda tidak memiliki akses ke halaman ini.”

Show “Kembali ke menu utama” only when `firstAccessiblePath` is non-null. Never retry the denied URL automatically.

- [ ] **Step 4: Run frontend tests and production build**

```bash
cd frontend && npm test
cd frontend && npm run build
```

Expected: all Node tests pass and Vite production build has no unresolved import or template error.

- [ ] **Step 5: Run backend regression tests and inspect diff**

```bash
cd backend && ./mvnw test
git diff --check
git status --short
```

Expected: backend tests pass, `git diff --check` is empty, and only intended navigation files plus pre-existing `graphify-out` changes remain.

- [ ] **Step 6: Commit**

```bash
git add frontend/src/views/errors/AccessDeniedView.vue frontend/src/components/AppLayout.vue
git commit -m "feat: render navigation from role menu data"
```

- [ ] **Step 7: Perform final acceptance checks**

After Flyway applies V18:

1. Log in as `SUPER_ADMIN`; every active menu is visible and active URLs open.
2. Log in as a restricted role; only assigned menus appear.
3. Paste an admin-only URL into the restricted session; the target component never renders and access denied appears.
4. Disable an assigned menu, log in again, and confirm both sidebar and URL access remove it.
5. Clear assignments for a non-super-admin role, log in again, and confirm the empty state has no redirect loop.
6. Restore status/assignment data changed only for verification.

Record commands and outcomes in the final handoff. Do not commit temporary verification data.
