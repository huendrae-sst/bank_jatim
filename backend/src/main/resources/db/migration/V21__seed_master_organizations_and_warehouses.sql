-- =============================================================================
-- Flyway Migration V21: Seed Master Organizations, Warehouses, and Default Admin
-- Target DB: PostgreSQL 15+
-- =============================================================================

-- 1. Insert Kantor Pusat
INSERT INTO organizations (code, name, type, parent_id, region_id, address, city, phone, cost_center_code, is_active)
VALUES (
    'KP_SBY',
    'Kantor Pusat Surabaya',
    'HEAD_OFFICE',
    NULL,
    NULL,
    'Jl. Basuki Rahmat No. 98-104, Surabaya',
    'Surabaya',
    '(031) 5310090',
    'CC_KP_001',
    TRUE
)
ON CONFLICT (code) DO NOTHING;

-- 2. Insert Main Branches linked to Regions (WIL_01 to WIL_04)
-- Wilayah I: Surabaya, Sidoarjo, Gresik
INSERT INTO organizations (code, name, type, parent_id, region_id, address, city, phone, cost_center_code, is_active)
SELECT 'KC_SBY', 'Kantor Cabang Utama Surabaya', 'MAIN_BRANCH', kp.id, r.id, 'Jl. Basuki Rahmat No. 106, Surabaya', 'Surabaya', '(031) 5310091', 'CC_KC_SBY', TRUE
FROM organizations kp, regions r
WHERE kp.code = 'KP_SBY' AND r.code = 'WIL_01'
ON CONFLICT (code) DO NOTHING;

INSERT INTO organizations (code, name, type, parent_id, region_id, address, city, phone, cost_center_code, is_active)
SELECT 'KC_SDA', 'Kantor Cabang Sidoarjo', 'MAIN_BRANCH', kp.id, r.id, 'Jl. Ahmad Yani No. 12, Sidoarjo', 'Sidoarjo', '(031) 8941010', 'CC_KC_SDA', TRUE
FROM organizations kp, regions r
WHERE kp.code = 'KP_SBY' AND r.code = 'WIL_01'
ON CONFLICT (code) DO NOTHING;

INSERT INTO organizations (code, name, type, parent_id, region_id, address, city, phone, cost_center_code, is_active)
SELECT 'KC_GSK', 'Kantor Cabang Gresik', 'MAIN_BRANCH', kp.id, r.id, 'Jl. Dr. Soetomo No. 34, Gresik', 'Gresik', '(031) 3981220', 'CC_KC_GSK', TRUE
FROM organizations kp, regions r
WHERE kp.code = 'KP_SBY' AND r.code = 'WIL_01'
ON CONFLICT (code) DO NOTHING;

-- Wilayah II: Malang, Pasuruan, Probolinggo
INSERT INTO organizations (code, name, type, parent_id, region_id, address, city, phone, cost_center_code, is_active)
SELECT 'KC_MLG', 'Kantor Cabang Malang', 'MAIN_BRANCH', kp.id, r.id, 'Jl. Jaksa Agung Suprapto No. 25, Malang', 'Malang', '(0341) 364100', 'CC_KC_MLG', TRUE
FROM organizations kp, regions r
WHERE kp.code = 'KP_SBY' AND r.code = 'WIL_02'
ON CONFLICT (code) DO NOTHING;

INSERT INTO organizations (code, name, type, parent_id, region_id, address, city, phone, cost_center_code, is_active)
SELECT 'KC_PAS', 'Kantor Cabang Pasuruan', 'MAIN_BRANCH', kp.id, r.id, 'Jl. Pahlawan No. 18, Pasuruan', 'Pasuruan', '(0343) 424100', 'CC_KC_PAS', TRUE
FROM organizations kp, regions r
WHERE kp.code = 'KP_SBY' AND r.code = 'WIL_02'
ON CONFLICT (code) DO NOTHING;

INSERT INTO organizations (code, name, type, parent_id, region_id, address, city, phone, cost_center_code, is_active)
SELECT 'KC_PROB', 'Kantor Cabang Probolinggo', 'MAIN_BRANCH', kp.id, r.id, 'Jl. Suroyo No. 45, Probolinggo', 'Probolinggo', '(0335) 421500', 'CC_KC_PROB', TRUE
FROM organizations kp, regions r
WHERE kp.code = 'KP_SBY' AND r.code = 'WIL_02'
ON CONFLICT (code) DO NOTHING;

-- Wilayah III: Madiun, Kediri, Blitar
INSERT INTO organizations (code, name, type, parent_id, region_id, address, city, phone, cost_center_code, is_active)
SELECT 'KC_MDN', 'Kantor Cabang Madiun', 'MAIN_BRANCH', kp.id, r.id, 'Jl. Pahlawan No. 56, Madiun', 'Madiun', '(0351) 462100', 'CC_KC_MDN', TRUE
FROM organizations kp, regions r
WHERE kp.code = 'KP_SBY' AND r.code = 'WIL_03'
ON CONFLICT (code) DO NOTHING;

INSERT INTO organizations (code, name, type, parent_id, region_id, address, city, phone, cost_center_code, is_active)
SELECT 'KC_KDR', 'Kantor Cabang Kediri', 'MAIN_BRANCH', kp.id, r.id, 'Jl. Brawijaya No. 10, Kediri', 'Kediri', '(0354) 682100', 'CC_KC_KDR', TRUE
FROM organizations kp, regions r
WHERE kp.code = 'KP_SBY' AND r.code = 'WIL_03'
ON CONFLICT (code) DO NOTHING;

INSERT INTO organizations (code, name, type, parent_id, region_id, address, city, phone, cost_center_code, is_active)
SELECT 'KC_BLT', 'Kantor Cabang Blitar', 'MAIN_BRANCH', kp.id, r.id, 'Jl. Panglima Sudirman No. 8, Blitar', 'Blitar', '(0342) 801100', 'CC_KC_BLT', TRUE
FROM organizations kp, regions r
WHERE kp.code = 'KP_SBY' AND r.code = 'WIL_03'
ON CONFLICT (code) DO NOTHING;

-- Wilayah IV: Jember, Banyuwangi
INSERT INTO organizations (code, name, type, parent_id, region_id, address, city, phone, cost_center_code, is_active)
SELECT 'KC_JBR', 'Kantor Cabang Jember', 'MAIN_BRANCH', kp.id, r.id, 'Jl. Gajah Mada No. 100, Jember', 'Jember', '(0331) 487100', 'CC_KC_JBR', TRUE
FROM organizations kp, regions r
WHERE kp.code = 'KP_SBY' AND r.code = 'WIL_04'
ON CONFLICT (code) DO NOTHING;

INSERT INTO organizations (code, name, type, parent_id, region_id, address, city, phone, cost_center_code, is_active)
SELECT 'KC_BWX', 'Kantor Cabang Banyuwangi', 'MAIN_BRANCH', kp.id, r.id, 'Jl. Dr. Wahidin No. 2, Banyuwangi', 'Banyuwangi', '(0333) 424500', 'CC_KC_BWX', TRUE
FROM organizations kp, regions r
WHERE kp.code = 'KP_SBY' AND r.code = 'WIL_04'
ON CONFLICT (code) DO NOTHING;

-- 3. Insert Sub-Branches (Capem) linked to parent Main Branches
INSERT INTO organizations (code, name, type, parent_id, region_id, address, city, phone, cost_center_code, is_active)
SELECT 'KCP_DARMO', 'KCP Darmo Surabaya', 'SUB_BRANCH', kc.id, NULL, 'Jl. Raya Darmo No. 40, Surabaya', 'Surabaya', '(031) 5671100', 'CC_KCP_DRM', TRUE
FROM organizations kc WHERE kc.code = 'KC_SBY'
ON CONFLICT (code) DO NOTHING;

INSERT INTO organizations (code, name, type, parent_id, region_id, address, city, phone, cost_center_code, is_active)
SELECT 'KCP_RUNGKUT', 'KCP Rungkut Surabaya', 'SUB_BRANCH', kc.id, NULL, 'Jl. Rungkut Industri No. 15, Surabaya', 'Surabaya', '(031) 8432200', 'CC_KCP_RKT', TRUE
FROM organizations kc WHERE kc.code = 'KC_SBY'
ON CONFLICT (code) DO NOTHING;

INSERT INTO organizations (code, name, type, parent_id, region_id, address, city, phone, cost_center_code, is_active)
SELECT 'KCP_WARU', 'KCP Waru Sidoarjo', 'SUB_BRANCH', kc.id, NULL, 'Jl. Letjend Sutoyo No. 88, Waru', 'Sidoarjo', '(031) 8531100', 'CC_KCP_WRU', TRUE
FROM organizations kc WHERE kc.code = 'KC_SDA'
ON CONFLICT (code) DO NOTHING;

INSERT INTO organizations (code, name, type, parent_id, region_id, address, city, phone, cost_center_code, is_active)
SELECT 'KCP_SAWOJAJAR', 'KCP Sawojajar Malang', 'SUB_BRANCH', kc.id, NULL, 'Jl. Danau Toba No. 12, Malang', 'Malang', '(0341) 712300', 'CC_KCP_SWJ', TRUE
FROM organizations kc WHERE kc.code = 'KC_MLG'
ON CONFLICT (code) DO NOTHING;

-- 4. Insert Warehouses (Gudang Logistik)
INSERT INTO warehouses (organization_id, code, name, type, address, is_active)
SELECT o.id, 'WH_KP_LOG', 'Gudang Logistik Pusat Surabaya', 'CENTRAL_LOGISTICS', o.address, TRUE
FROM organizations o WHERE o.code = 'KP_SBY'
ON CONFLICT (code) DO NOTHING;

INSERT INTO warehouses (organization_id, code, name, type, address, is_active)
SELECT o.id, 'WH_KC_SBY', 'Gudang Logistik KC Surabaya', 'BRANCH_STORAGE', o.address, TRUE
FROM organizations o WHERE o.code = 'KC_SBY'
ON CONFLICT (code) DO NOTHING;

INSERT INTO warehouses (organization_id, code, name, type, address, is_active)
SELECT o.id, 'WH_KC_SDA', 'Gudang Logistik KC Sidoarjo', 'BRANCH_STORAGE', o.address, TRUE
FROM organizations o WHERE o.code = 'KC_SDA'
ON CONFLICT (code) DO NOTHING;

INSERT INTO warehouses (organization_id, code, name, type, address, is_active)
SELECT o.id, 'WH_KC_GSK', 'Gudang Logistik KC Gresik', 'BRANCH_STORAGE', o.address, TRUE
FROM organizations o WHERE o.code = 'KC_GSK'
ON CONFLICT (code) DO NOTHING;

INSERT INTO warehouses (organization_id, code, name, type, address, is_active)
SELECT o.id, 'WH_KC_MLG', 'Gudang Logistik KC Malang', 'BRANCH_STORAGE', o.address, TRUE
FROM organizations o WHERE o.code = 'KC_MLG'
ON CONFLICT (code) DO NOTHING;

INSERT INTO warehouses (organization_id, code, name, type, address, is_active)
SELECT o.id, 'WH_KC_PAS', 'Gudang Logistik KC Pasuruan', 'BRANCH_STORAGE', o.address, TRUE
FROM organizations o WHERE o.code = 'KC_PAS'
ON CONFLICT (code) DO NOTHING;

INSERT INTO warehouses (organization_id, code, name, type, address, is_active)
SELECT o.id, 'WH_KC_PROB', 'Gudang Logistik KC Probolinggo', 'BRANCH_STORAGE', o.address, TRUE
FROM organizations o WHERE o.code = 'KC_PROB'
ON CONFLICT (code) DO NOTHING;

INSERT INTO warehouses (organization_id, code, name, type, address, is_active)
SELECT o.id, 'WH_KC_MDN', 'Gudang Logistik KC Madiun', 'BRANCH_STORAGE', o.address, TRUE
FROM organizations o WHERE o.code = 'KC_MDN'
ON CONFLICT (code) DO NOTHING;

INSERT INTO warehouses (organization_id, code, name, type, address, is_active)
SELECT o.id, 'WH_KC_KDR', 'Gudang Logistik KC Kediri', 'BRANCH_STORAGE', o.address, TRUE
FROM organizations o WHERE o.code = 'KC_KDR'
ON CONFLICT (code) DO NOTHING;

INSERT INTO warehouses (organization_id, code, name, type, address, is_active)
SELECT o.id, 'WH_KC_BLT', 'Gudang Logistik KC Blitar', 'BRANCH_STORAGE', o.address, TRUE
FROM organizations o WHERE o.code = 'KC_BLT'
ON CONFLICT (code) DO NOTHING;

INSERT INTO warehouses (organization_id, code, name, type, address, is_active)
SELECT o.id, 'WH_KC_JBR', 'Gudang Logistik KC Jember', 'BRANCH_STORAGE', o.address, TRUE
FROM organizations o WHERE o.code = 'KC_JBR'
ON CONFLICT (code) DO NOTHING;

INSERT INTO warehouses (organization_id, code, name, type, address, is_active)
SELECT o.id, 'WH_KC_BWX', 'Gudang Logistik KC Banyuwangi', 'BRANCH_STORAGE', o.address, TRUE
FROM organizations o WHERE o.code = 'KC_BWX'
ON CONFLICT (code) DO NOTHING;

-- 5. Seed Default Admin & Super Admin Users if not already present
-- Password hash for 'password': $2a$10$cBE53blTb09sI5i/ggr5LuVVzypIHvRkVVGOkvXtiNmt93Bp7VI2G
INSERT INTO users (name, email, nip, password, role, organization_id, warehouse_id, approval_limit, is_active)
SELECT 
    'Super Administrator JIMS',
    'admin@bankjatim.co.id',
    '198501012010011001',
    '$2a$10$cBE53blTb09sI5i/ggr5LuVVzypIHvRkVVGOkvXtiNmt93Bp7VI2G',
    'SUPER_ADMIN',
    o.id,
    w.id,
    1000000000.00,
    TRUE
FROM organizations o, warehouses w
WHERE o.code = 'KP_SBY' AND w.code = 'WH_KP_LOG'
ON CONFLICT (email) DO UPDATE SET
    organization_id = EXCLUDED.organization_id,
    warehouse_id = EXCLUDED.warehouse_id;
