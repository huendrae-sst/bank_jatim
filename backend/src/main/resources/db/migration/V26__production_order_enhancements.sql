-- =============================================================================
-- Flyway Migration V26: Production Order Enhancements & Bill of Materials (BOM)
-- =============================================================================

-- 1. Bill of Materials (BOM) for finished goods vs raw blank cards
CREATE TABLE IF NOT EXISTS item_bom (
    id               BIGSERIAL PRIMARY KEY,
    finished_item_id BIGINT NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    material_item_id BIGINT NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    qty_per_unit     INTEGER NOT NULL DEFAULT 1,
    notes            TEXT,
    created_at       TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(finished_item_id, material_item_id)
);

-- 2. Enhance production_orders with approval and material issue workflows
ALTER TABLE production_orders
    ADD COLUMN IF NOT EXISTS approved_by_user_id        BIGINT REFERENCES users(id),
    ADD COLUMN IF NOT EXISTS approved_at                TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS material_issued_by_user_id BIGINT REFERENCES users(id),
    ADD COLUMN IF NOT EXISTS material_issued_at         TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS material_issue_status      VARCHAR(50) DEFAULT 'PENDING',
    ADD COLUMN IF NOT EXISTS emboss_file_id             BIGINT REFERENCES emboss_files(id),
    ADD COLUMN IF NOT EXISTS completed_at               TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS total_produced             INTEGER DEFAULT 0,
    ADD COLUMN IF NOT EXISTS total_damaged              INTEGER DEFAULT 0;

-- 3. Enhance emboss_records for individual customer card traceability
ALTER TABLE emboss_records
    ADD COLUMN IF NOT EXISTS production_order_id BIGINT REFERENCES production_orders(id),
    ADD COLUMN IF NOT EXISTS production_status   VARCHAR(50) DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS produced_item_id    BIGINT REFERENCES items(id);

CREATE INDEX IF NOT EXISTS idx_emboss_records_production ON emboss_records(production_order_id);
CREATE INDEX IF NOT EXISTS idx_emboss_records_prod_status ON emboss_records(production_status);

-- 4. Production Order Fulfillments (Link SPK to multiple branch orders)
CREATE TABLE IF NOT EXISTS production_order_fulfillments (
    id                  BIGSERIAL PRIMARY KEY,
    production_order_id BIGINT NOT NULL REFERENCES production_orders(id) ON DELETE CASCADE,
    order_id            BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    qty_fulfilled       INTEGER NOT NULL DEFAULT 0,
    fulfilled_at        TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_at          TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(production_order_id, order_id)
);

ALTER TABLE production_order_fulfillments
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP;

-- 5. Seed finished good embossed card items & BOM mapping
DO $$
DECLARE
    v_cat_id BIGINT;
    v_blank_gpn_id BIGINT;
    v_blank_mc_id BIGINT;
    v_emb_gpn_id BIGINT;
    v_emb_mc_id BIGINT;
BEGIN
    SELECT id INTO v_cat_id FROM categories WHERE name ILIKE '%kartu%' OR name ILIKE '%card%' LIMIT 1;
    IF v_cat_id IS NULL THEN
        SELECT id INTO v_cat_id FROM categories LIMIT 1;
    END IF;

    -- Blank cards IDs
    SELECT id INTO v_blank_gpn_id FROM items WHERE sku IN ('ATM-INST-001', 'SKU-BLANK-GPN') LIMIT 1;
    SELECT id INTO v_blank_mc_id FROM items WHERE sku IN ('ATM-NAME-001', 'SKU-BLANK-MC') LIMIT 1;

    -- Insert finished good: GPN Emboss
    IF NOT EXISTS (SELECT 1 FROM items WHERE sku = 'ATM-EMB-GPN-001') THEN
        INSERT INTO items (
            category_id, sku, barcode, name, uom, specification,
            estimated_unit_price, is_active, min_stock, max_stock, safety_stock, reorder_point, lead_time_days,
            created_at, updated_at
        ) VALUES (
            v_cat_id, 'ATM-EMB-GPN-001', 'BRC-ATM-EMB-GPN-001',
            'Kartu Debit GPN Emboss Nasabah', 'PCS',
            'Kartu chip GPN yang sudah dipersonalisasi emboss nama nasabah (Finished Good)',
            35000.00, true, 10, 5000, 50, 100, 3,
            CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
        ) RETURNING id INTO v_emb_gpn_id;
    ELSE
        SELECT id INTO v_emb_gpn_id FROM items WHERE sku = 'ATM-EMB-GPN-001';
    END IF;

    -- Insert finished good: Mastercard Emboss
    IF NOT EXISTS (SELECT 1 FROM items WHERE sku = 'ATM-EMB-MC-001') THEN
        INSERT INTO items (
            category_id, sku, barcode, name, uom, specification,
            estimated_unit_price, is_active, min_stock, max_stock, safety_stock, reorder_point, lead_time_days,
            created_at, updated_at
        ) VALUES (
            v_cat_id, 'ATM-EMB-MC-001', 'BRC-ATM-EMB-MC-001',
            'Kartu Debit Mastercard Emboss Nasabah', 'PCS',
            'Kartu chip Mastercard yang sudah dipersonalisasi emboss nama nasabah (Finished Good)',
            45000.00, true, 10, 5000, 50, 100, 3,
            CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
        ) RETURNING id INTO v_emb_mc_id;
    ELSE
        SELECT id INTO v_emb_mc_id FROM items WHERE sku = 'ATM-EMB-MC-001';
    END IF;

    -- BOM Mapping: GPN
    IF v_emb_gpn_id IS NOT NULL AND v_blank_gpn_id IS NOT NULL THEN
        INSERT INTO item_bom (finished_item_id, material_item_id, qty_per_unit, notes, created_at, updated_at)
        VALUES (v_emb_gpn_id, v_blank_gpn_id, 1, 'Blank card GPN -> Kartu Debit GPN Emboss Nasabah', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        ON CONFLICT (finished_item_id, material_item_id) DO NOTHING;
    END IF;

    -- BOM Mapping: Mastercard
    IF v_emb_mc_id IS NOT NULL AND v_blank_mc_id IS NOT NULL THEN
        INSERT INTO item_bom (finished_item_id, material_item_id, qty_per_unit, notes, created_at, updated_at)
        VALUES (v_emb_mc_id, v_blank_mc_id, 1, 'Blank card Mastercard -> Kartu Debit Mastercard Emboss Nasabah', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        ON CONFLICT (finished_item_id, material_item_id) DO NOTHING;
    END IF;
END $$;
