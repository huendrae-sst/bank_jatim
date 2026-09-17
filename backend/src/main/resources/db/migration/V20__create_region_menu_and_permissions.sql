-- =============================================================================
-- Flyway Migration V20: Master Wilayah Navigation Menu and Role Permissions
-- Target DB: PostgreSQL 15+
-- =============================================================================

-- 1. Insert Master Wilayah Menu (menus schema without legacy roles column)
INSERT INTO menus (code, title, module, parent_code, path, icon, sort_order, status, description)
VALUES (
    'MST_REGION',
    'Master Wilayah',
    'Master Data',
    NULL,
    '/master/regions',
    'bi-geo-alt',
    69,
    'AKTIF',
    'Master data wilayah koordinator dan pemetaan kantor cabang terafiliasi'
)
ON CONFLICT (code) DO UPDATE SET
    title = EXCLUDED.title,
    path = EXCLUDED.path,
    icon = EXCLUDED.icon,
    sort_order = EXCLUDED.sort_order,
    status = EXCLUDED.status,
    description = EXCLUDED.description;

-- 2. Populate role_menu_permissions for MST_REGION
INSERT INTO role_menu_permissions (role_id, menu_id)
SELECT r.id, m.id
FROM roles r
CROSS JOIN menus m
WHERE m.code = 'MST_REGION'
  AND r.code IN ('SUPER_ADMIN', 'USER_ADMIN', 'MASTER_MAKER', 'MASTER_APPROVER', 'REGIONAL_MONITOR')
ON CONFLICT DO NOTHING;

-- 3. Populate menu_route_rules for MST_REGION
INSERT INTO menu_route_rules (menu_id, path, match_type, sort_order)
SELECT m.id, m.path, 'PREFIX', 1
FROM menus m
WHERE m.code = 'MST_REGION'
ON CONFLICT DO NOTHING;

