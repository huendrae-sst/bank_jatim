-- =============================================================================
-- Flyway Migration V29: Dedicated 'Percetakan & Produksi' Menu Module & Permissions
-- =============================================================================

-- 1. Update existing ORD_PRODUCTION menu to dedicated 'Percetakan & Produksi' module
UPDATE menus
SET module = 'Percetakan & Produksi',
    title = 'SPK Produksi & Cetak',
    icon = 'bi-printer',
    sort_order = 25
WHERE code = 'ORD_PRODUCTION';

-- 2. Insert PROD_CONSOL menu for branch order consolidation
INSERT INTO menus (code, title, module, parent_code, path, icon, sort_order, status, description)
VALUES (
    'PROD_CONSOL',
    'Konsolidasi Order Cabang',
    'Percetakan & Produksi',
    NULL,
    '/production/consolidation',
    'bi-layers',
    26,
    'AKTIF',
    'Konsolidasi permintaan kartu emboss dari berbagai cabang ke dalam SPK produksi'
) ON CONFLICT (code) DO UPDATE
SET module = EXCLUDED.module,
    title = EXCLUDED.title,
    path = EXCLUDED.path,
    icon = EXCLUDED.icon,
    sort_order = EXCLUDED.sort_order,
    status = EXCLUDED.status,
    description = EXCLUDED.description;

-- 3. Assign role_menu_permissions for ORD_PRODUCTION and PROD_CONSOL
INSERT INTO role_menu_permissions (role_id, menu_id)
SELECT r.id, m.id
FROM roles r
CROSS JOIN menus m
WHERE m.code IN ('ORD_PRODUCTION', 'PROD_CONSOL')
  AND r.code IN ('SUPER_ADMIN', 'WAREHOUSE_OFFICER', 'INVENTORY_OFFICER', 'ORDER_APPROVER', 'MANAGEMENT')
ON CONFLICT DO NOTHING;

-- 4. Route matching rules (PREFIX matching for detail pages and subroutes)
INSERT INTO menu_route_rules (menu_id, path, match_type, sort_order)
SELECT m.id, '/production', 'PREFIX', 1
FROM menus m WHERE m.code = 'ORD_PRODUCTION'
ON CONFLICT DO NOTHING;

INSERT INTO menu_route_rules (menu_id, path, match_type, sort_order)
SELECT m.id, '/production/consolidation', 'PREFIX', 1
FROM menus m WHERE m.code = 'PROD_CONSOL'
ON CONFLICT DO NOTHING;
