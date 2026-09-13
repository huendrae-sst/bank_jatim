-- =============================================================================
-- Flyway Migration V2: Core Transactions, Workflows, Logistics & Settlements
-- Target DB: PostgreSQL 15+
-- =============================================================================

-- 1. Purchase Requests (PR) & Items
CREATE TABLE IF NOT EXISTS purchase_requests (
    id BIGSERIAL PRIMARY KEY,
    pr_number VARCHAR(100) NOT NULL UNIQUE,
    organization_id BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    created_by_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    approved_by_user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    procurement_method VARCHAR(50) NOT NULL DEFAULT 'DIRECT', -- DIRECT, TENDER, E_CATALOG
    purpose TEXT NOT NULL,
    estimated_total_cost NUMERIC(15, 2) NOT NULL DEFAULT 0,
    budget_status VARCHAR(50) NOT NULL DEFAULT 'VALIDATED', -- VALIDATED, INSUFFICIENT
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT', 
    -- DRAFT, SUBMITTED, BUDGET_VALIDATED, WAITING_APPROVAL, APPROVED, PARTIALLY_ORDERED, FULLY_ORDERED, REJECTED, CLOSED
    rejection_reason TEXT,
    submitted_at TIMESTAMP WITH TIME ZONE,
    approved_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_pr_org ON purchase_requests(organization_id);
CREATE INDEX IF NOT EXISTS idx_pr_status ON purchase_requests(status);

CREATE TABLE IF NOT EXISTS purchase_request_items (
    id BIGSERIAL PRIMARY KEY,
    purchase_request_id BIGINT NOT NULL REFERENCES purchase_requests(id) ON DELETE CASCADE,
    item_id BIGINT NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    qty_requested INTEGER NOT NULL DEFAULT 1,
    qty_approved INTEGER NOT NULL DEFAULT 0,
    qty_ordered INTEGER NOT NULL DEFAULT 0,
    estimated_unit_price NUMERIC(15, 2) NOT NULL DEFAULT 0,
    estimated_subtotal NUMERIC(15, 2) NOT NULL DEFAULT 0,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. Purchase Orders (PO) & Items (Supports Multi-PR Consolidation)
CREATE TABLE IF NOT EXISTS purchase_orders (
    id BIGSERIAL PRIMARY KEY,
    po_number VARCHAR(100) NOT NULL UNIQUE,
    vendor_id BIGINT NOT NULL REFERENCES vendors(id) ON DELETE CASCADE,
    warehouse_id BIGINT NOT NULL REFERENCES warehouses(id) ON DELETE CASCADE,
    created_by_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    approved_by_user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    order_date DATE NOT NULL DEFAULT CURRENT_DATE,
    expected_delivery_date DATE,
    subtotal NUMERIC(15, 2) NOT NULL DEFAULT 0,
    tax_amount NUMERIC(15, 2) NOT NULL DEFAULT 0, -- PPN 11%
    total_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT', 
    -- DRAFT, GENERATED_FROM_PR, ISSUED, VENDOR_PROCESS, IN_DELIVERY, PARTIAL_RECEIVED, RECEIVED, COMPLETED, CANCELLED
    rejection_reason TEXT,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_po_vendor ON purchase_orders(vendor_id);
CREATE INDEX IF NOT EXISTS idx_po_status ON purchase_orders(status);

CREATE TABLE IF NOT EXISTS purchase_order_items (
    id BIGSERIAL PRIMARY KEY,
    purchase_order_id BIGINT NOT NULL REFERENCES purchase_orders(id) ON DELETE CASCADE,
    purchase_request_item_id BIGINT REFERENCES purchase_request_items(id) ON DELETE SET NULL,
    item_id BIGINT NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    qty_ordered INTEGER NOT NULL DEFAULT 1,
    qty_received INTEGER NOT NULL DEFAULT 0,
    unit_price NUMERIC(15, 2) NOT NULL DEFAULT 0,
    subtotal NUMERIC(15, 2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. Goods Receipts from Vendor (GRN)
CREATE TABLE IF NOT EXISTS goods_receipts (
    id BIGSERIAL PRIMARY KEY,
    grn_number VARCHAR(100) NOT NULL UNIQUE,
    purchase_order_id BIGINT NOT NULL REFERENCES purchase_orders(id) ON DELETE CASCADE,
    warehouse_id BIGINT NOT NULL REFERENCES warehouses(id) ON DELETE CASCADE,
    received_by_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    vendor_delivery_note_number VARCHAR(100),
    receipt_date DATE NOT NULL DEFAULT CURRENT_DATE,
    status VARCHAR(50) NOT NULL DEFAULT 'RECEIVED', -- RECEIVED, PARTIAL, DISCREPANCY
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS goods_receipt_items (
    id BIGSERIAL PRIMARY KEY,
    goods_receipt_id BIGINT NOT NULL REFERENCES goods_receipts(id) ON DELETE CASCADE,
    purchase_order_item_id BIGINT NOT NULL REFERENCES purchase_order_items(id) ON DELETE CASCADE,
    item_id BIGINT NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    qty_received INTEGER NOT NULL DEFAULT 0,
    qty_accepted INTEGER NOT NULL DEFAULT 0,
    qty_rejected INTEGER NOT NULL DEFAULT 0,
    condition_notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 4. Branch Orders & Items
CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(100) NOT NULL UNIQUE,
    requesting_organization_id BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    requesting_warehouse_id BIGINT REFERENCES warehouses(id) ON DELETE SET NULL,
    created_by_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    approved_by_user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    priority VARCHAR(50) NOT NULL DEFAULT 'NORMAL', -- NORMAL, HIGH, URGENT
    required_date DATE,
    total_items INTEGER NOT NULL DEFAULT 0,
    total_estimated_value NUMERIC(15, 2) NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT', 
    -- DRAFT, SUBMITTED, WAITING_APPROVAL, APPROVED, ALLOCATED, PICKING, PACKING, READY_TO_SHIP, IN_TRANSIT, RECEIVED, COMPLETED, REJECTED, CANCELLED
    is_overbudget BOOLEAN NOT NULL DEFAULT FALSE,
    is_pickup_kp BOOLEAN NOT NULL DEFAULT FALSE,
    rejection_reason TEXT,
    notes TEXT,
    submitted_at TIMESTAMP WITH TIME ZONE,
    approved_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_orders_org ON orders(requesting_organization_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);

CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    item_id BIGINT NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    qty_requested INTEGER NOT NULL DEFAULT 1,
    qty_approved INTEGER NOT NULL DEFAULT 0,
    qty_allocated INTEGER NOT NULL DEFAULT 0,
    qty_picked INTEGER NOT NULL DEFAULT 0,
    qty_packed INTEGER NOT NULL DEFAULT 0,
    qty_shipped INTEGER NOT NULL DEFAULT 0,
    qty_received INTEGER NOT NULL DEFAULT 0,
    unit_price_ref NUMERIC(15, 2) NOT NULL DEFAULT 0,
    subtotal_ref NUMERIC(15, 2) NOT NULL DEFAULT 0,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 5. Switching Stocks (Inter-branch fulfillment when central is low)
CREATE TABLE IF NOT EXISTS switching_stocks (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT REFERENCES orders(id) ON DELETE SET NULL,
    source_organization_id BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    source_warehouse_id BIGINT NOT NULL REFERENCES warehouses(id) ON DELETE CASCADE,
    destination_organization_id BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    destination_warehouse_id BIGINT NOT NULL REFERENCES warehouses(id) ON DELETE CASCADE,
    proposed_by_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    approved_by_user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PROPOSED', 
    -- PROPOSED, WAITING_APPROVAL, APPROVED, RESERVED, DISPATCHED, TRANSFERRED, RECEIVED, COMPLETED, REJECTED, CANCELLED
    recommendation_reason TEXT,
    rejection_reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS switching_stock_items (
    id BIGSERIAL PRIMARY KEY,
    switching_stock_id BIGINT NOT NULL REFERENCES switching_stocks(id) ON DELETE CASCADE,
    item_id BIGINT NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    qty_requested INTEGER NOT NULL DEFAULT 1,
    qty_approved INTEGER NOT NULL DEFAULT 0,
    qty_transferred INTEGER NOT NULL DEFAULT 0,
    qty_received INTEGER NOT NULL DEFAULT 0,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 6. Order Allocations
CREATE TABLE IF NOT EXISTS order_allocations (
    id BIGSERIAL PRIMARY KEY,
    order_item_id BIGINT NOT NULL REFERENCES order_items(id) ON DELETE CASCADE,
    source_warehouse_id BIGINT NOT NULL REFERENCES warehouses(id) ON DELETE CASCADE,
    qty_allocated INTEGER NOT NULL DEFAULT 0,
    allocation_type VARCHAR(50) NOT NULL DEFAULT 'DIRECT_WAREHOUSE', -- DIRECT_WAREHOUSE, SWITCHING_STOCK
    switching_stock_id BIGINT REFERENCES switching_stocks(id) ON DELETE SET NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'RESERVED', -- RESERVED, PICKED, PACKED, SHIPPED, RECEIVED, CANCELLED
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 7. Warehouse Picking & Packing
CREATE TABLE IF NOT EXISTS warehouse_pickings (
    id BIGSERIAL PRIMARY KEY,
    picking_number VARCHAR(100) NOT NULL UNIQUE,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    warehouse_id BIGINT NOT NULL REFERENCES warehouses(id) ON DELETE CASCADE,
    picked_by_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL DEFAULT 'ASSIGNED', -- ASSIGNED, IN_PROGRESS, COMPLETED
    picked_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS warehouse_packings (
    id BIGSERIAL PRIMARY KEY,
    packing_number VARCHAR(100) NOT NULL UNIQUE,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    warehouse_id BIGINT NOT NULL REFERENCES warehouses(id) ON DELETE CASCADE,
    packed_by_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    koli_count INTEGER NOT NULL DEFAULT 1,
    total_weight_kg NUMERIC(8, 2) NOT NULL DEFAULT 1.00,
    dimensions_cm VARCHAR(50),
    status VARCHAR(50) NOT NULL DEFAULT 'PACKED', -- PACKED, VERIFIED
    packed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 8. Shipments / Manifests & Courier Tracking
CREATE TABLE IF NOT EXISTS shipments (
    id BIGSERIAL PRIMARY KEY,
    manifest_number VARCHAR(100) NOT NULL UNIQUE,
    order_id BIGINT REFERENCES orders(id) ON DELETE SET NULL,
    switching_stock_id BIGINT REFERENCES switching_stocks(id) ON DELETE SET NULL,
    origin_warehouse_id BIGINT NOT NULL REFERENCES warehouses(id) ON DELETE CASCADE,
    destination_organization_id BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    courier_id BIGINT REFERENCES couriers(id) ON DELETE SET NULL,
    service_type VARCHAR(50) NOT NULL DEFAULT 'REGULER',
    tracking_number VARCHAR(100), -- No Resi/AWB
    dispatched_by_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    koli_count INTEGER NOT NULL DEFAULT 1,
    total_weight_kg NUMERIC(8, 2) NOT NULL DEFAULT 1.00,
    shipping_cost NUMERIC(15, 2) NOT NULL DEFAULT 0,
    eta_date DATE,
    status VARCHAR(50) NOT NULL DEFAULT 'CREATED', 
    -- CREATED, READY_TO_SHIP, DISPATCHED, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, CLOSED, DELIVERY_FAILED, RETURNED, PARTIAL_RECEIVED
    dispatched_at TIMESTAMP WITH TIME ZONE,
    delivered_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_shipments_tracking ON shipments(tracking_number);

-- 9. Receivings & Discrepancies
CREATE TABLE IF NOT EXISTS receivings (
    id BIGSERIAL PRIMARY KEY,
    receiving_number VARCHAR(100) NOT NULL UNIQUE,
    shipment_id BIGINT NOT NULL REFERENCES shipments(id) ON DELETE CASCADE,
    order_id BIGINT REFERENCES orders(id) ON DELETE SET NULL,
    switching_stock_id BIGINT REFERENCES switching_stocks(id) ON DELETE SET NULL,
    organization_id BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    warehouse_id BIGINT NOT NULL REFERENCES warehouses(id) ON DELETE CASCADE,
    received_by_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    receipt_date DATE NOT NULL DEFAULT CURRENT_DATE,
    status VARCHAR(50) NOT NULL DEFAULT 'RECEIVED_FULL', -- RECEIVED_FULL, RECEIVED_PARTIAL, DISCREPANCY
    pod_signature TEXT,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS discrepancies (
    id BIGSERIAL PRIMARY KEY,
    receiving_id BIGINT NOT NULL REFERENCES receivings(id) ON DELETE CASCADE,
    order_item_id BIGINT REFERENCES order_items(id) ON DELETE SET NULL,
    item_id BIGINT NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    discrepancy_type VARCHAR(50) NOT NULL DEFAULT 'DAMAGED', -- MISSING, DAMAGED, WRONG_ITEM, EXCESS
    qty_expected INTEGER NOT NULL DEFAULT 0,
    qty_actual INTEGER NOT NULL DEFAULT 0,
    qty_damaged INTEGER NOT NULL DEFAULT 0,
    resolution_status VARCHAR(50) NOT NULL DEFAULT 'REPORTED', -- REPORTED, UNDER_REVIEW, RESOLVED, CLAIMED
    berita_acara_number VARCHAR(100),
    berita_acara_url TEXT,
    resolution_notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 10. Inter-Unit Financial Settlements
CREATE TABLE IF NOT EXISTS settlements (
    id BIGSERIAL PRIMARY KEY,
    settlement_number VARCHAR(100) NOT NULL UNIQUE,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    debit_organization_id BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    credit_organization_id BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    debit_cost_center VARCHAR(50) NOT NULL,
    credit_cost_center VARCHAR(50) NOT NULL,
    item_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    shipping_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    total_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT', 
    -- DRAFT, VALIDATED, WAITING_APPROVAL, APPROVED, POSTED, COMPLETED, REJECTED, REVERSED
    created_by_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    approved_by_user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    posted_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 11. Notifications
CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    target_role VARCHAR(50),
    target_organization_id BIGINT REFERENCES organizations(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL DEFAULT 'INFORMATION', -- ACTION_REQUIRED, ALERT, INFORMATION
    priority VARCHAR(50) NOT NULL DEFAULT 'INFO', -- INFO, WARNING, HIGH, CRITICAL
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    reference_transaction_type VARCHAR(50), -- PR, PO, ORDER, SWITCHING_STOCK, SHIPMENT, RECEIVING, SETTLEMENT
    reference_transaction_id BIGINT,
    action_url VARCHAR(255),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_notifications_user ON notifications(user_id, is_read);

-- 12. Audit Logs
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    action VARCHAR(50) NOT NULL, -- CREATE, UPDATE, DELETE, SUBMIT, APPROVE, REJECT, CONSOLIDATE, RECEIVE, POST, SWITCH
    auditable_type VARCHAR(100) NOT NULL,
    auditable_id BIGINT NOT NULL,
    organization_id BIGINT REFERENCES organizations(id) ON DELETE SET NULL,
    ip_address VARCHAR(50),
    user_agent TEXT,
    old_values JSONB,
    new_values JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_audit_logs_auditable ON audit_logs(auditable_type, auditable_id);
