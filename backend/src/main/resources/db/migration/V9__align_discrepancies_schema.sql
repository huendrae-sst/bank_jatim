-- =============================================================================
-- Flyway Migration V9: Align Discrepancies Schema (Add missing columns if not exists)
-- =============================================================================

ALTER TABLE discrepancies
    ADD COLUMN IF NOT EXISTS order_item_id BIGINT REFERENCES order_items(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS discrepancy_type VARCHAR(50) NOT NULL DEFAULT 'DAMAGED',
    ADD COLUMN IF NOT EXISTS qty_expected INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS qty_actual INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS qty_damaged INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS resolution_status VARCHAR(50) NOT NULL DEFAULT 'REPORTED',
    ADD COLUMN IF NOT EXISTS berita_acara_number VARCHAR(100),
    ADD COLUMN IF NOT EXISTS berita_acara_url TEXT,
    ADD COLUMN IF NOT EXISTS resolution_notes TEXT;

CREATE INDEX IF NOT EXISTS idx_discrepancies_receiving ON discrepancies(receiving_id);
CREATE INDEX IF NOT EXISTS idx_discrepancies_status ON discrepancies(resolution_status);
