# Data-Driven Role Navigation Design

## Goal

Make sidebar visibility and direct URL access follow the role-menu assignments stored in the database. Navigation labels, grouping, icons, ordering, links, and route access rules must not be duplicated as hardcoded frontend menu configuration.

## Current State

- `AppLayout.vue` contains a hardcoded sidebar tree.
- The database already stores menus and role-menu assignments.
- `RoleMenuService` can resolve menu codes for a role.
- The frontend menu store can fetch menus and role assignments, but the layout does not use them.
- The router guard checks authentication only.
- Several browser routes are children, aliases, detail pages, or print pages whose URL differs from the canonical menu path.

## Scope

This change will:

- render the sidebar and top navigation links from backend navigation data;
- derive the current user's permissions from the authenticated JWT identity;
- prevent direct browser navigation to routes outside the user's assigned menus;
- support child, detail, create, print, alias, and approval routes through database-backed route rules;
- redirect the application root to the first accessible menu;
- add an access-denied page and fail closed when no valid navigation rule exists;
- test backend permission resolution and frontend route matching.

This change will not attempt a complete authorization audit of every REST endpoint. Existing `@PreAuthorize` rules remain in force. Browser route protection is an additional navigation boundary, not a replacement for backend API authorization.

## Data Model

Add a `menu_route_rules` table linked to `menus`:

| Field | Purpose |
| --- | --- |
| `id` | Primary key |
| `menu_id` | Owning menu |
| `path` | Normalized browser path used for matching |
| `match_type` | `EXACT` or `PREFIX` |
| `sort_order` | Stable ordering when rules have equal specificity |

Every active menu receives at least one rule for its canonical `menus.path`. Additional rules cover URLs that do not sit beneath the canonical path, such as standalone print or manifest pages.

Prefix rules match only complete path segments. For example, `/orders` can match `/orders/123`, but must not match `/orders-archive`. When several rules match, the rule with the longest path wins; `EXACT` wins over `PREFIX` when path lengths are equal. This ensures `/orders/approvals` is governed by its specific menu instead of the broader `/orders` menu.

The migration will seed rules for every registered protected frontend route. A route with no matching active database rule is denied by default. Public and system routes such as login and access denied remain router-level concerns rather than business menus.

## Backend Navigation API

Add an authenticated read-only endpoint:

`GET /navigation`

The endpoint obtains the current role from `SecurityContext`/`UserPrincipal`; it does not accept a role code from the client. It returns all active navigation entries so the client can distinguish an unauthorized specific route from an authorized broader prefix.

Each entry contains:

```json
{
  "code": "ORD_LIST",
  "title": "Daftar Order Cabang",
  "module": "Permintaan & Order",
  "parentCode": null,
  "path": "/orders",
  "icon": "bi-cart3",
  "order": 10,
  "allowed": true,
  "routeRules": [
    { "path": "/orders", "matchType": "PREFIX", "order": 1 }
  ]
}
```

For ordinary roles, `allowed` comes from `role_menu_permissions`. For `SUPER_ADMIN`, every active entry is allowed. Inactive menus and their route rules are excluded.

The response is ordered by menu order and then route-rule order. Permission calculation is performed in the backend so the frontend never trusts a client-supplied role or reconstructs role policy itself.

## Frontend Navigation State

Introduce a dedicated navigation store, or narrow the existing menu store to expose the same interface:

- `items`: all active navigation entries returned by `/navigation`;
- `accessibleItems`: entries where `allowed` is true;
- `groups`: accessible entries grouped by `module`, ordered entirely from response data;
- `loadedForUser`: identity key preventing stale permissions from surviving logout or a user change;
- `loading`, `loaded`, and `error` states;
- `loadNavigation()`, `clearNavigation()`, `resolveRoute(path)`, and `firstAccessiblePath()`.

The session is loaded once per authenticated user and reused by the layout and router guard. Logout clears navigation state. A subsequent login always reloads it.

Pure functions will handle path normalization, rule matching, longest-match selection, grouping, and first-route selection. Keeping these separate from Pinia makes the access decisions directly testable without mounting Vue.

## Sidebar and Top Navigation

`AppLayout.vue` will remove the hardcoded list of business menu links.

- Items are grouped by the backend `module` field.
- Item text, icon, link, and order come from navigation data.
- `parentCode` builds explicit menu nesting when present.
- Root entries without an explicit parent are grouped under their module.
- A module with one accessible root entry may render as a direct link; multiple entries render as a collapsible group.
- Empty modules are not shown.
- Active and expanded states are computed from the current route and the same route-rule matcher used by the guard.
- The logo links to `/`, which resolves dynamically to the first accessible menu.
- Existing hardcoded topbar shortcuts are replaced with data-driven shortcuts from the first accessible navigation entries, or removed if no entries exist.

No role names, menu codes, business paths, titles, icons, or module names are embedded in the layout.

## Router Access Flow

The global router guard follows this sequence:

1. Allow explicitly public system routes.
2. Redirect unauthenticated users to login.
3. Load navigation for the authenticated user if it is not current.
4. For `/`, redirect to `firstAccessiblePath()`.
5. Match the destination against all active route rules.
6. Select the most specific matching rule.
7. Allow navigation only when the owning menu has `allowed: true`.
8. Redirect unmatched or unauthorized routes to the access-denied page.

Redirect targets include the originally requested path for display only, not for automatic re-entry. The access-denied route is explicitly exempt from business permission matching to avoid loops.

After login, the app navigates to `/` rather than a hardcoded dashboard. Users with no assigned active menu land on the access-denied page with a clear “no menu assigned” state.

## Error Handling

- `401`: clear auth and navigation state, then redirect to login.
- `403` or an unmatched protected route: show the access-denied page.
- Navigation API/network failure: deny protected navigation and show an unavailable message rather than exposing all menus.
- Empty assignment: show no business links and provide a clear message on the access-denied page.
- Inactive menu: excluded by the backend and therefore unavailable in both sidebar and router.

## Testing Strategy

### Backend

- an ordinary role receives active menus with correct `allowed` values;
- `SUPER_ADMIN` receives every active menu as allowed;
- inactive menus are absent;
- the endpoint uses the authenticated principal rather than a request role parameter;
- route rules are ordered and serialized correctly;
- an unknown or unauthenticated identity is rejected.

### Frontend

Pure policy tests will verify:

- grouping and ordering use response data;
- entries marked inaccessible do not appear in visible navigation;
- exact and prefix matching respect path-segment boundaries;
- the longest matching rule wins;
- a specific denied rule overrides a broader allowed prefix;
- detail, create, print, and alias fixtures resolve to the expected menu;
- unmatched routes fail closed;
- root navigation selects the first accessible path;
- an empty assignment produces no destination.

Router integration tests will verify unauthenticated redirects, successful authorized navigation, direct-URL denial, navigation-load failure, and loop-free access-denied behavior.

## Migration and Compatibility

The database migration creates `menu_route_rules`, seeds coverage for the current route catalog, and leaves existing `menus` and `role_menu_permissions` assignments intact. The old `menus.roles` compatibility column is not used for runtime permission decisions.

The navigation API and frontend switch will ship together. Existing menu administration continues to manage menu metadata and role assignments; route-rule administration is initially migration-managed so invalid rules cannot be entered through the UI. A future administration screen may expose route rules with validation, but that is outside this change.

## Acceptance Criteria

- A user sees only active menus assigned to their role.
- Changing role-menu assignments changes navigation after the next permission reload/login without a frontend code change.
- Directly entering a denied URL shows access denied and does not render the target page.
- Detail and print routes inherit access according to database route rules.
- `SUPER_ADMIN` sees and can open every active menu.
- A route missing from navigation data is denied by default.
- The sidebar and topbar contain no hardcoded business menu catalog or role matrix.
- Automated backend and frontend tests cover the access decisions.
