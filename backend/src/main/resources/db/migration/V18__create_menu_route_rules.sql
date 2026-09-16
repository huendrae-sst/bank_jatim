CREATE TABLE menu_route_rules (
    id BIGSERIAL PRIMARY KEY,
    menu_id BIGINT NOT NULL REFERENCES menus(id) ON DELETE CASCADE,
    path VARCHAR(255) NOT NULL,
    match_type VARCHAR(20) NOT NULL CHECK (match_type IN ('EXACT', 'PREFIX')),
    sort_order INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT uk_menu_route_rule UNIQUE (menu_id, path, match_type)
);

CREATE INDEX idx_menu_route_rules_path ON menu_route_rules(path);
CREATE INDEX idx_menu_route_rules_menu ON menu_route_rules(menu_id);

INSERT INTO menus (
    code,
    title,
    module,
    parent_code,
    path,
    icon,
    sort_order,
    status,
    description
)
VALUES
    (
        'MST_ROLE_MENUS',
        'Mapping Role & Menu',
        'Master Data',
        NULL,
        '/master/role-menus',
        'bi-list-check',
        79,
        'AKTIF',
        'Pengaturan hak akses menu untuk setiap peran'
    ),
    (
        'MST_DATA',
        'Data Master',
        'Master Data',
        NULL,
        '/master/data',
        'bi-database',
        80,
        'AKTIF',
        'Ringkasan data referensi sistem'
    ),
    (
        'REP_ESS',
        'Laporan Executive Support',
        'Laporan & Rekapitulasi',
        NULL,
        '/reports/ess',
        'bi-file-earmark-bar-graph',
        115,
        'AKTIF',
        'Ringkasan laporan executive support system'
    )
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
