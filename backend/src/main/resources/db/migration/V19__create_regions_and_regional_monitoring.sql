-- =============================================================================
-- Flyway Migration V19: Apply Regions and Regional Monitoring Schema
-- Target DB: PostgreSQL 15+
-- =============================================================================

-- 1. Create regions table
CREATE TABLE IF NOT EXISTS regions (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_regions_code ON regions(code);
CREATE INDEX IF NOT EXISTS idx_regions_active ON regions(is_active);

-- 2. Add region_id to organizations
ALTER TABLE organizations
    ADD COLUMN IF NOT EXISTS region_id BIGINT REFERENCES regions(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_organizations_region_id ON organizations(region_id);

-- 3. Add region_id to users
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS region_id BIGINT REFERENCES regions(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_users_region_id ON users(region_id);

-- 4. Insert REGIONAL_MONITOR Role
INSERT INTO roles (code, name, description, system_role)
VALUES (
    'REGIONAL_MONITOR',
    'Pengawas Wilayah',
    'Akses pemantauan dan laporan persediaan serta order tingkat wilayah',
    TRUE
)
ON CONFLICT (code) DO NOTHING;

-- 5. Insert role menu permissions for REGIONAL_MONITOR
INSERT INTO role_menu_permissions (role_id, menu_id)
SELECT r.id, m.id
FROM roles r
CROSS JOIN menus m
WHERE r.code = 'REGIONAL_MONITOR'
  AND m.code IN (
      'DASH_EXEC',
      'DASH_OPS',
      'ORD_LIST',
      'WH_SHIPMENT',
      'INV_BALANCES',
      'INV_MUTATION',
      'INV_EWS',
      'INV_FORECAST',
      'ESS_VALUATION',
      'ESS_SAVINGS',
      'ESS_ITO',
      'ESS_HEATMAP',
      'ESS_SLA',
      'REP_VALUATION',
      'REP_DIST'
  )
ON CONFLICT DO NOTHING;

-- 6. Seed Default Master Regions (Bank Jatim Standard)
INSERT INTO regions (code, name, description, is_active)
VALUES
    ('WIL_01', 'Wilayah I (Surabaya, Sidoarjo, Gresik)', 'Wilayah operasional koordinator Surabaya Raya', TRUE),
    ('WIL_02', 'Wilayah II (Malang, Pasuruan, Probolinggo)', 'Wilayah operasional koordinator Malang dan sekitarnya', TRUE),
    ('WIL_03', 'Wilayah III (Madiun, Kediri, Blitar)', 'Wilayah operasional koordinator Mataraman dan sekitarnya', TRUE),
    ('WIL_04', 'Wilayah IV (Jember, Banyuwangi, Bondowoso)', 'Wilayah operasional koordinator Tapal Kuda', TRUE)
ON CONFLICT (code) DO NOTHING;
