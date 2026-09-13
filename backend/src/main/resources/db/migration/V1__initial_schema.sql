-- =============================================================================
-- Flyway Migration V1: Core Master Data, User Scoping, and Stock Balance Engine
-- Target DB: PostgreSQL 15+
-- =============================================================================

-- 1. Organizations (Kantor Pusat, Cabang Utama, Sub Branch, Gudang)
CREATE TABLE organizations (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL DEFAULT 'SUB_BRANCH', -- HEAD_OFFICE, MAIN_BRANCH, SUB_BRANCH, WAREHOUSE
    parent_id BIGINT REFERENCES organizations(id) ON DELETE SET NULL,
    address TEXT,
    city VARCHAR(100) DEFAULT 'Surabaya',
    phone VARCHAR(50),
    cost_center_code VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_organizations_code ON organizations(code);
CREATE INDEX idx_organizations_type ON organizations(type);

-- 2. Warehouses (Gudang Logistik Pusat, Tempat Penyimpanan Cabang)
CREATE TABLE warehouses (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL DEFAULT 'BRANCH_STORAGE', -- CENTRAL_LOGISTICS, BRANCH_STORAGE
    address TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_warehouses_org ON warehouses(organization_id);

-- 3. Users Table (18 Roles with Organization and Warehouse Scoping)
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT REFERENCES organizations(id) ON DELETE SET NULL,
    warehouse_id BIGINT REFERENCES warehouses(id) ON DELETE SET NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    nip VARCHAR(50) UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'REQUESTER_CABANG',
    approval_limit NUMERIC(15, 2) NOT NULL DEFAULT 0,
    phone VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_nip ON users(nip);
CREATE INDEX idx_users_role ON users(role);

-- 4. Item Categories
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 5. Master Items
CREATE TABLE items (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    sku VARCHAR(100) NOT NULL UNIQUE,
    barcode VARCHAR(100) UNIQUE,
    name VARCHAR(255) NOT NULL,
    uom VARCHAR(50) NOT NULL DEFAULT 'PCS', -- RIM, BOX, UNIT, PCS, PACK, SET
    specification TEXT,
    min_stock INTEGER NOT NULL DEFAULT 10,
    max_stock INTEGER NOT NULL DEFAULT 500,
    safety_stock INTEGER NOT NULL DEFAULT 20,
    reorder_point INTEGER NOT NULL DEFAULT 30,
    lead_time_days INTEGER NOT NULL DEFAULT 5,
    estimated_unit_price NUMERIC(15, 2) NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_items_sku ON items(sku);
CREATE INDEX idx_items_category ON items(category_id);

-- 6. Item UOM Conversions
CREATE TABLE item_conversions (
    id BIGSERIAL PRIMARY KEY,
    item_id BIGINT NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    from_uom VARCHAR(50) NOT NULL,
    to_uom VARCHAR(50) NOT NULL,
    multiplier NUMERIC(12, 4) NOT NULL DEFAULT 1.0000,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 7. Vendors & Couriers
CREATE TABLE vendors (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(50),
    address TEXT,
    sla_days INTEGER NOT NULL DEFAULT 7,
    payment_terms VARCHAR(100) DEFAULT 'TOP 30 Hari',
    rating NUMERIC(3, 2) DEFAULT 5.00,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE couriers (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    service_types JSONB, -- ["REGULER", "EXPRESS", "CARGO", "INTERNAL"]
    sla_days INTEGER NOT NULL DEFAULT 2,
    phone VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 8. Budgets (Pagu Anggaran per Unit & Cost Center)
CREATE TABLE budgets (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    cost_center_code VARCHAR(50) NOT NULL,
    year INTEGER NOT NULL,
    allocated_amount NUMERIC(15, 2) NOT NULL DEFAULT 0, -- Pagu
    committed_amount NUMERIC(15, 2) NOT NULL DEFAULT 0, -- Komitmen
    realized_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,  -- Realisasi
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_budget_org_year_cost UNIQUE (organization_id, year, cost_center_code)
);

CREATE INDEX idx_budgets_org_year ON budgets(organization_id, year);

-- 9. Stock Balances (Single Source of Truth per Warehouse & Item)
CREATE TABLE stock_balances (
    id BIGSERIAL PRIMARY KEY,
    warehouse_id BIGINT NOT NULL REFERENCES warehouses(id) ON DELETE CASCADE,
    item_id BIGINT NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    on_hand INTEGER NOT NULL DEFAULT 0,
    reserved INTEGER NOT NULL DEFAULT 0,
    allocated INTEGER NOT NULL DEFAULT 0,
    in_transit INTEGER NOT NULL DEFAULT 0,
    hold INTEGER NOT NULL DEFAULT 0,
    damaged INTEGER NOT NULL DEFAULT 0,
    min_stock_override INTEGER,
    max_stock_override INTEGER,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_stock_warehouse_item UNIQUE (warehouse_id, item_id)
);

CREATE INDEX idx_stock_balances_lookup ON stock_balances(warehouse_id, item_id);

-- 10. Immutable Stock Ledgers (Audit Trail of All Quantities & Values)
CREATE TABLE stock_ledgers (
    id BIGSERIAL PRIMARY KEY,
    warehouse_id BIGINT NOT NULL REFERENCES warehouses(id) ON DELETE CASCADE,
    item_id BIGINT NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    transaction_type VARCHAR(100) NOT NULL, 
    -- PROCUREMENT_RECEIPT, GOODS_ISSUE, TRANSFER_OUT, TRANSFER_IN, SWITCHING_STOCK,
    -- GOODS_RECEIPT_UNIT, STOCK_ADJUSTMENT, STOCK_OPNAME, DAMAGED_HOLD, RETURN, REVERSAL
    reference_number VARCHAR(100),
    qty_in INTEGER NOT NULL DEFAULT 0,
    qty_out INTEGER NOT NULL DEFAULT 0,
    balance_after INTEGER NOT NULL DEFAULT 0,
    unit_cost NUMERIC(15, 2) NOT NULL DEFAULT 0,
    total_value NUMERIC(15, 2) NOT NULL DEFAULT 0,
    notes TEXT,
    created_by_user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_stock_ledgers_wh_item ON stock_ledgers(warehouse_id, item_id);
CREATE INDEX idx_stock_ledgers_ref ON stock_ledgers(reference_number);
