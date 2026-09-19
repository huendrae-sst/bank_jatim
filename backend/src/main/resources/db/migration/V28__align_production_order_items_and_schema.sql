-- =============================================================================
-- Flyway Migration V28: Fully Align Production & BOM Schema for Hibernate Validation
-- =============================================================================

-- 1. Ensure all columns in production_orders
ALTER TABLE production_orders
    ADD COLUMN IF NOT EXISTS production_number VARCHAR(100),
    ADD COLUMN IF NOT EXISTS warehouse_id BIGINT REFERENCES warehouses(id) ON DELETE CASCADE,
    ADD COLUMN IF NOT EXISTS created_by_user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    ADD COLUMN IF NOT EXISTS production_date DATE DEFAULT CURRENT_DATE,
    ADD COLUMN IF NOT EXISTS total_qty INTEGER DEFAULT 0,
    ADD COLUMN IF NOT EXISTS total_produced INTEGER DEFAULT 0,
    ADD COLUMN IF NOT EXISTS total_damaged INTEGER DEFAULT 0,
    ADD COLUMN IF NOT EXISTS status VARCHAR(50) DEFAULT 'DRAFT',
    ADD COLUMN IF NOT EXISTS material_issue_status VARCHAR(50) DEFAULT 'PENDING',
    ADD COLUMN IF NOT EXISTS approved_by_user_id BIGINT REFERENCES users(id),
    ADD COLUMN IF NOT EXISTS approved_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS material_issued_by_user_id BIGINT REFERENCES users(id),
    ADD COLUMN IF NOT EXISTS material_issued_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS emboss_file_id BIGINT REFERENCES emboss_files(id),
    ADD COLUMN IF NOT EXISTS completed_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS notes TEXT,
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP;

-- 2. Ensure all columns in production_order_items (including qty_damaged)
ALTER TABLE production_order_items
    ADD COLUMN IF NOT EXISTS production_order_id BIGINT REFERENCES production_orders(id) ON DELETE CASCADE,
    ADD COLUMN IF NOT EXISTS item_id BIGINT REFERENCES items(id) ON DELETE CASCADE,
    ADD COLUMN IF NOT EXISTS qty_planned INTEGER DEFAULT 0,
    ADD COLUMN IF NOT EXISTS qty_produced INTEGER DEFAULT 0,
    ADD COLUMN IF NOT EXISTS qty_damaged INTEGER DEFAULT 0,
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP;

-- 3. Ensure all columns in item_bom
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

ALTER TABLE item_bom
    ADD COLUMN IF NOT EXISTS finished_item_id BIGINT REFERENCES items(id) ON DELETE CASCADE,
    ADD COLUMN IF NOT EXISTS material_item_id BIGINT REFERENCES items(id) ON DELETE CASCADE,
    ADD COLUMN IF NOT EXISTS qty_per_unit INTEGER DEFAULT 1,
    ADD COLUMN IF NOT EXISTS notes TEXT,
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP;

-- 4. Ensure all columns in production_order_fulfillments
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
    ADD COLUMN IF NOT EXISTS production_order_id BIGINT REFERENCES production_orders(id) ON DELETE CASCADE,
    ADD COLUMN IF NOT EXISTS order_id BIGINT REFERENCES orders(id) ON DELETE CASCADE,
    ADD COLUMN IF NOT EXISTS qty_fulfilled INTEGER DEFAULT 0,
    ADD COLUMN IF NOT EXISTS fulfilled_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP;

-- 5. Ensure all columns in emboss_records
ALTER TABLE emboss_records
    ADD COLUMN IF NOT EXISTS production_order_id BIGINT REFERENCES production_orders(id),
    ADD COLUMN IF NOT EXISTS production_status VARCHAR(50) DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS produced_item_id BIGINT REFERENCES items(id);

CREATE INDEX IF NOT EXISTS idx_emboss_records_production ON emboss_records(production_order_id);
CREATE INDEX IF NOT EXISTS idx_emboss_records_prod_status ON emboss_records(production_status);
