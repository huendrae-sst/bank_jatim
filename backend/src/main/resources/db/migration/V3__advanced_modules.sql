-- =============================================================================
-- Flyway Migration V3: Emboss Integration, Reverse Inventory, GL & Stock Opname
-- Target DB: PostgreSQL 15+
-- =============================================================================

-- 1. Emboss Integration (ATM/Debit Card Personalization from Core Banking)
CREATE TABLE IF NOT EXISTS emboss_files (
    id BIGSERIAL PRIMARY KEY,
    filename VARCHAR(255) NOT NULL,
    file_path TEXT,
    total_records INTEGER NOT NULL DEFAULT 0,
    valid_records INTEGER NOT NULL DEFAULT 0,
    rejected_records INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'UPLOADED', 
    -- UPLOADED, PARSED, VALIDATED, ORDERS_GENERATED, FAILED
    error_message TEXT,
    uploaded_by_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS emboss_records (
    id BIGSERIAL PRIMARY KEY,
    emboss_file_id BIGINT NOT NULL REFERENCES emboss_files(id) ON DELETE CASCADE,
    account_number VARCHAR(50) NOT NULL,
    customer_name VARCHAR(255) NOT NULL,
    card_number_masked VARCHAR(50) NOT NULL,
    card_type VARCHAR(50) NOT NULL, -- GPN, MASTERCARD, VISA
    branch_code VARCHAR(50) NOT NULL,
    item_id BIGINT REFERENCES items(id) ON DELETE SET NULL, -- Matched card blank item
    pin_envelope_item_id BIGINT REFERENCES items(id) ON DELETE SET NULL,
    order_id BIGINT REFERENCES orders(id) ON DELETE SET NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'VALID', -- VALID, REJECTED, PROCESSED
    rejection_reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_emboss_records_file ON emboss_records(emboss_file_id);
CREATE INDEX IF NOT EXISTS idx_emboss_records_status ON emboss_records(status);

-- 2. Production Orders (Card Embossing & Warkat Personalization)
CREATE TABLE IF NOT EXISTS production_orders (
    id BIGSERIAL PRIMARY KEY,
    production_number VARCHAR(100) NOT NULL UNIQUE,
    warehouse_id BIGINT NOT NULL REFERENCES warehouses(id) ON DELETE CASCADE,
    created_by_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    production_date DATE NOT NULL DEFAULT CURRENT_DATE,
    total_qty INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT', -- DRAFT, IN_PRODUCTION, COMPLETED, CANCELLED
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS production_order_items (
    id BIGSERIAL PRIMARY KEY,
    production_order_id BIGINT NOT NULL REFERENCES production_orders(id) ON DELETE CASCADE,
    item_id BIGINT NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    qty_planned INTEGER NOT NULL DEFAULT 0,
    qty_produced INTEGER NOT NULL DEFAULT 0,
    qty_damaged INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. Reverse Inventory: Inventory Returns
CREATE TABLE IF NOT EXISTS inventory_returns (
    id BIGSERIAL PRIMARY KEY,
    return_number VARCHAR(100) NOT NULL UNIQUE,
    organization_id BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    destination_warehouse_id BIGINT NOT NULL REFERENCES warehouses(id) ON DELETE CASCADE,
    created_by_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    approved_by_user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    reason TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT', 
    -- DRAFT, SUBMITTED, APPROVED, SHIPPED, RECEIVED, COMPLETED, REJECTED
    shipped_at TIMESTAMP WITH TIME ZONE,
    received_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS inventory_return_items (
    id BIGSERIAL PRIMARY KEY,
    inventory_return_id BIGINT NOT NULL REFERENCES inventory_returns(id) ON DELETE CASCADE,
    item_id BIGINT NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    qty_returned INTEGER NOT NULL DEFAULT 1,
    qty_received INTEGER NOT NULL DEFAULT 0,
    condition VARCHAR(50) NOT NULL DEFAULT 'GOOD', -- GOOD, DAMAGED, OBSOLETE
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 4. Reverse Inventory: Stock Destructions (Pemusnahan Barang Kadaluwarsa/Rusak)
CREATE TABLE IF NOT EXISTS stock_destructions (
    id BIGSERIAL PRIMARY KEY,
    destruction_number VARCHAR(100) NOT NULL UNIQUE,
    warehouse_id BIGINT NOT NULL REFERENCES warehouses(id) ON DELETE CASCADE,
    created_by_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    approved_by_user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    reason TEXT NOT NULL,
    berita_acara_number VARCHAR(100),
    berita_acara_url TEXT,
    witness_1_name VARCHAR(255),
    witness_2_name VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT', -- DRAFT, WAITING_APPROVAL, APPROVED, EXECUTED, REJECTED
    executed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS stock_destruction_items (
    id BIGSERIAL PRIMARY KEY,
    stock_destruction_id BIGINT NOT NULL REFERENCES stock_destructions(id) ON DELETE CASCADE,
    item_id BIGINT NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    qty_destroyed INTEGER NOT NULL DEFAULT 1,
    estimated_value NUMERIC(15, 2) NOT NULL DEFAULT 0,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 5. Stock Adjustments
CREATE TABLE IF NOT EXISTS stock_adjustments (
    id BIGSERIAL PRIMARY KEY,
    adjustment_number VARCHAR(100) NOT NULL UNIQUE,
    warehouse_id BIGINT NOT NULL REFERENCES warehouses(id) ON DELETE CASCADE,
    item_id BIGINT NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    adjustment_type VARCHAR(50) NOT NULL, -- INCREASE, DECREASE, DAMAGE_HOLD
    qty_change INTEGER NOT NULL DEFAULT 0,
    reason TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED
    requested_by_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    approved_by_user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    approved_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 6. Stock Opnames
CREATE TABLE IF NOT EXISTS stock_opnames (
    id BIGSERIAL PRIMARY KEY,
    opname_number VARCHAR(100) NOT NULL UNIQUE,
    warehouse_id BIGINT NOT NULL REFERENCES warehouses(id) ON DELETE CASCADE,
    conducted_by_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    verified_by_user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    opname_date DATE NOT NULL DEFAULT CURRENT_DATE,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT', -- DRAFT, IN_PROGRESS, COMPLETED, CANCELLED
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS stock_opname_items (
    id BIGSERIAL PRIMARY KEY,
    stock_opname_id BIGINT NOT NULL REFERENCES stock_opnames(id) ON DELETE CASCADE,
    item_id BIGINT NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    qty_system INTEGER NOT NULL DEFAULT 0,
    qty_physical INTEGER NOT NULL DEFAULT 0,
    qty_difference INTEGER NOT NULL DEFAULT 0,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 7. Accounting Master & General Ledger (GL)
CREATE TABLE IF NOT EXISTS chart_of_accounts (
    id BIGSERIAL PRIMARY KEY,
    account_code VARCHAR(50) NOT NULL UNIQUE,
    account_name VARCHAR(255) NOT NULL,
    account_type VARCHAR(50) NOT NULL, -- ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cost_centers (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS general_ledger_entries (
    id BIGSERIAL PRIMARY KEY,
    transaction_date DATE NOT NULL DEFAULT CURRENT_DATE,
    reference_number VARCHAR(100) NOT NULL,
    account_code VARCHAR(50) NOT NULL,
    cost_center_code VARCHAR(50),
    organization_id BIGINT REFERENCES organizations(id) ON DELETE SET NULL,
    debit_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    credit_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    description TEXT,
    created_by_user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_gl_ref ON general_ledger_entries(reference_number);
CREATE INDEX IF NOT EXISTS idx_gl_account ON general_ledger_entries(account_code);

-- 8. Expedition Mappings
CREATE TABLE IF NOT EXISTS expedition_mappings (
    id BIGSERIAL PRIMARY KEY,
    destination_organization_id BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    courier_id BIGINT NOT NULL REFERENCES couriers(id) ON DELETE CASCADE,
    service_type VARCHAR(50) NOT NULL DEFAULT 'REGULER',
    estimated_lead_days INTEGER NOT NULL DEFAULT 2,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_expedition_dest UNIQUE (destination_organization_id)
);
