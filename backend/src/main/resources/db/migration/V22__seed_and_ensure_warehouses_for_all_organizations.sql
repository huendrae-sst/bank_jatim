-- =============================================================================
-- Flyway Migration V22: Ensure Warehouses For All Organizations
-- Target DB: PostgreSQL 15+
-- =============================================================================

-- 1. Ensure central warehouse WH_KP_LOG exists
INSERT INTO warehouses (organization_id, code, name, type, address, is_active)
SELECT 
    o.id,
    'WH_KP_LOG',
    'Gudang Logistik Pusat Surabaya',
    'CENTRAL_LOGISTICS',
    COALESCE(o.address, 'Jl. Basuki Rahmat No. 98-104, Surabaya'),
    TRUE
FROM organizations o
WHERE o.type = 'HEAD_OFFICE' OR o.code IN ('KP_SBY', 'KP-001')
ORDER BY (CASE WHEN o.code = 'KP_SBY' THEN 1 WHEN o.type = 'HEAD_OFFICE' THEN 2 ELSE 3 END)
LIMIT 1
ON CONFLICT (code) DO NOTHING;

-- 2. Ensure every organization has an active warehouse
INSERT INTO warehouses (organization_id, code, name, type, address, is_active)
SELECT 
    o.id,
    'WH_' || UPPER(REGEXP_REPLACE(o.code, '[^a-zA-Z0-9]', '_', 'g')),
    'Gudang Logistik ' || o.name,
    CASE 
        WHEN o.type = 'HEAD_OFFICE' THEN 'CENTRAL_LOGISTICS'
        ELSE 'BRANCH_STORAGE'
    END,
    COALESCE(o.address, 'Surabaya, Jawa Timur'),
    TRUE
FROM organizations o
WHERE NOT EXISTS (
    SELECT 1 FROM warehouses w WHERE w.organization_id = o.id
)
ON CONFLICT (code) DO UPDATE SET
    organization_id = EXCLUDED.organization_id,
    name = EXCLUDED.name,
    is_active = TRUE;
