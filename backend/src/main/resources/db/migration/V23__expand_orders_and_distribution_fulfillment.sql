-- =============================================================================
-- Flyway Migration V23: Expand Orders, Distribution Fulfillment & Routine Drops
-- Target DB: PostgreSQL 15+
-- =============================================================================

-- 1. Tambah kolom pada tabel orders
ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS order_type VARCHAR(50) NOT NULL DEFAULT 'INTERNAL_REQUEST',
    ADD COLUMN IF NOT EXISTS fulfillment_status VARCHAR(50) NOT NULL DEFAULT 'UNFULFILLED',
    ADD COLUMN IF NOT EXISTS purchase_request_id BIGINT REFERENCES purchase_requests(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS emboss_file_id BIGINT REFERENCES emboss_files(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS batch_manifest_number VARCHAR(100),
    ADD COLUMN IF NOT EXISTS routine_period VARCHAR(100);

CREATE INDEX IF NOT EXISTS idx_orders_type ON orders(order_type);
CREATE INDEX IF NOT EXISTS idx_orders_fulfillment ON orders(fulfillment_status);
CREATE INDEX IF NOT EXISTS idx_orders_pr ON orders(purchase_request_id);
CREATE INDEX IF NOT EXISTS idx_orders_emboss ON orders(emboss_file_id);

-- 2. Tambah kolom status pemenuhan pada purchase_requests & kuantitas pada purchase_request_items
ALTER TABLE purchase_requests
    ADD COLUMN IF NOT EXISTS fulfillment_status VARCHAR(50) NOT NULL DEFAULT 'UNFULFILLED';

ALTER TABLE purchase_request_items
    ADD COLUMN IF NOT EXISTS qty_fulfilled INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS qty_received INTEGER NOT NULL DEFAULT 0;

-- 3. Tambah kolom status pemenuhan pada emboss_files
ALTER TABLE emboss_files
    ADD COLUMN IF NOT EXISTS fulfillment_status VARCHAR(50) NOT NULL DEFAULT 'UNFULFILLED';

-- 4. Tambah kolom pada shipments untuk distribusi terpadu
ALTER TABLE shipments
    ADD COLUMN IF NOT EXISTS distribution_type VARCHAR(50) NOT NULL DEFAULT 'ORDER_REQUEST',
    ADD COLUMN IF NOT EXISTS batch_manifest_number VARCHAR(100);

CREATE INDEX IF NOT EXISTS idx_shipments_dist_type ON shipments(distribution_type);
CREATE INDEX IF NOT EXISTS idx_shipments_batch_manifest ON shipments(batch_manifest_number);

-- 5. Tabel shipment_items untuk audit kuantitas fisik per baris barang yang dikirim & diterima
CREATE TABLE IF NOT EXISTS shipment_items (
    id BIGSERIAL PRIMARY KEY,
    shipment_id BIGINT NOT NULL REFERENCES shipments(id) ON DELETE CASCADE,
    order_item_id BIGINT REFERENCES order_items(id) ON DELETE SET NULL,
    purchase_request_item_id BIGINT REFERENCES purchase_request_items(id) ON DELETE SET NULL,
    item_id BIGINT NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    qty_shipped INTEGER NOT NULL DEFAULT 1 CHECK (qty_shipped > 0),
    qty_received INTEGER NOT NULL DEFAULT 0,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_shipment_items_shipment ON shipment_items(shipment_id);
CREATE INDEX IF NOT EXISTS idx_shipment_items_order_item ON shipment_items(order_item_id);
CREATE INDEX IF NOT EXISTS idx_shipment_items_pr_item ON shipment_items(purchase_request_item_id);

-- 6. Registrasi menu baru pada tabel menus
INSERT INTO menus (code, title, module, parent_code, path, icon, sort_order, status, description)
VALUES
    ('ORD_EMBOSS_ORDER', 'Order Emboss Kartu', 'Permintaan & Order', NULL, '/orders/emboss', 'bi-credit-card-2-front', 15, 'AKTIF', 'Pengajuan dan monitoring order cetak kartu ATM dengan file data nasabah'),
    ('WH_ROUTINE', 'Distribusi Rutin', 'Gudang & Distribusi', NULL, '/distribution/routine', 'bi-arrow-repeat', 23, 'AKTIF', 'Inisiasi dan monitoring distribusi kuota rutin/stok awal dari pusat ke cabang')
ON CONFLICT (code) DO UPDATE SET
    title = EXCLUDED.title,
    module = EXCLUDED.module,
    path = EXCLUDED.path,
    icon = EXCLUDED.icon,
    sort_order = EXCLUDED.sort_order,
    status = EXCLUDED.status,
    description = EXCLUDED.description;

-- 7. Hak akses role_menu_permissions untuk menu baru
INSERT INTO role_menu_permissions (role_id, menu_id)
SELECT r.id, m.id
FROM roles r
CROSS JOIN menus m
WHERE m.code = 'ORD_EMBOSS_ORDER'
  AND r.code IN ('SUPER_ADMIN', 'REQUESTER_CABANG', 'ORDER_APPROVER', 'WAREHOUSE_OFFICER', 'MANAGEMENT')
ON CONFLICT DO NOTHING;

INSERT INTO role_menu_permissions (role_id, menu_id)
SELECT r.id, m.id
FROM roles r
CROSS JOIN menus m
WHERE m.code = 'WH_ROUTINE'
  AND r.code IN ('SUPER_ADMIN', 'DISTRIBUTION_OFFICER', 'WAREHOUSE_OFFICER', 'MANAGEMENT')
ON CONFLICT DO NOTHING;

-- 8. Aturan rute navigasi menu_route_rules
INSERT INTO menu_route_rules (menu_id, path, match_type, sort_order)
SELECT m.id, m.path, 'PREFIX', 1
FROM menus m
WHERE m.code IN ('ORD_EMBOSS_ORDER', 'WH_ROUTINE')
ON CONFLICT DO NOTHING;
