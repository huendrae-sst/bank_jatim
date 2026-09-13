-- =============================================================================
-- Flyway Migration V6: Align Emboss Schema (Add missing columns if not exists)
-- =============================================================================

ALTER TABLE emboss_files
    ADD COLUMN IF NOT EXISTS file_path TEXT,
    ADD COLUMN IF NOT EXISTS total_records INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS valid_records INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS rejected_records INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS status VARCHAR(50) NOT NULL DEFAULT 'UPLOADED',
    ADD COLUMN IF NOT EXISTS error_message TEXT;

ALTER TABLE emboss_records
    ADD COLUMN IF NOT EXISTS card_number_masked VARCHAR(50),
    ADD COLUMN IF NOT EXISTS card_type VARCHAR(50),
    ADD COLUMN IF NOT EXISTS branch_code VARCHAR(50),
    ADD COLUMN IF NOT EXISTS item_id BIGINT REFERENCES items(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS pin_envelope_item_id BIGINT REFERENCES items(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS order_id BIGINT REFERENCES orders(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS status VARCHAR(50) NOT NULL DEFAULT 'VALID',
    ADD COLUMN IF NOT EXISTS rejection_reason TEXT;

CREATE INDEX IF NOT EXISTS idx_emboss_records_file ON emboss_records(emboss_file_id);
CREATE INDEX IF NOT EXISTS idx_emboss_records_status ON emboss_records(status);
