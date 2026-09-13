-- =============================================================================
-- Flyway Migration V8: Align Emboss Records Schema
-- =============================================================================

DO $$
BEGIN
    -- 1. customer_name
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' AND table_name = 'emboss_records' AND column_name = 'name'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' AND table_name = 'emboss_records' AND column_name = 'customer_name'
    ) THEN
        ALTER TABLE emboss_records RENAME COLUMN name TO customer_name;
    ELSIF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' AND table_name = 'emboss_records' AND column_name = 'cardholder_name'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' AND table_name = 'emboss_records' AND column_name = 'customer_name'
    ) THEN
        ALTER TABLE emboss_records RENAME COLUMN cardholder_name TO customer_name;
    ELSIF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' AND table_name = 'emboss_records' AND column_name = 'client_name'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' AND table_name = 'emboss_records' AND column_name = 'customer_name'
    ) THEN
        ALTER TABLE emboss_records RENAME COLUMN client_name TO customer_name;
    ELSIF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' AND table_name = 'emboss_records' AND column_name = 'customer_name'
    ) THEN
        ALTER TABLE emboss_records ADD COLUMN customer_name VARCHAR(255) NOT NULL DEFAULT '-';
    END IF;

    -- 2. account_number
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' AND table_name = 'emboss_records' AND column_name = 'account_no'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' AND table_name = 'emboss_records' AND column_name = 'account_number'
    ) THEN
        ALTER TABLE emboss_records RENAME COLUMN account_no TO account_number;
    ELSIF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' AND table_name = 'emboss_records' AND column_name = 'account_number'
    ) THEN
        ALTER TABLE emboss_records ADD COLUMN account_number VARCHAR(50) NOT NULL DEFAULT '-';
    END IF;

    -- 3. card_number_masked
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' AND table_name = 'emboss_records' AND column_name = 'card_number'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' AND table_name = 'emboss_records' AND column_name = 'card_number_masked'
    ) THEN
        ALTER TABLE emboss_records RENAME COLUMN card_number TO card_number_masked;
    ELSIF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' AND table_name = 'emboss_records' AND column_name = 'card_number_masked'
    ) THEN
        ALTER TABLE emboss_records ADD COLUMN card_number_masked VARCHAR(50) NOT NULL DEFAULT '-';
    END IF;

    -- 4. branch_code
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' AND table_name = 'emboss_records' AND column_name = 'branch'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' AND table_name = 'emboss_records' AND column_name = 'branch_code'
    ) THEN
        ALTER TABLE emboss_records RENAME COLUMN branch TO branch_code;
    ELSIF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' AND table_name = 'emboss_records' AND column_name = 'branch_code'
    ) THEN
        ALTER TABLE emboss_records ADD COLUMN branch_code VARCHAR(50) NOT NULL DEFAULT '-';
    END IF;

    -- 5. card_type
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' AND table_name = 'emboss_records' AND column_name = 'card_type'
    ) THEN
        ALTER TABLE emboss_records ADD COLUMN card_type VARCHAR(50) NOT NULL DEFAULT 'GPN';
    END IF;

    -- 6. item_id, pin_envelope_item_id, order_id, status, rejection_reason
    ALTER TABLE emboss_records
        ADD COLUMN IF NOT EXISTS item_id BIGINT REFERENCES items(id) ON DELETE SET NULL,
        ADD COLUMN IF NOT EXISTS pin_envelope_item_id BIGINT REFERENCES items(id) ON DELETE SET NULL,
        ADD COLUMN IF NOT EXISTS order_id BIGINT REFERENCES orders(id) ON DELETE SET NULL,
        ADD COLUMN IF NOT EXISTS status VARCHAR(50) NOT NULL DEFAULT 'VALID',
        ADD COLUMN IF NOT EXISTS rejection_reason TEXT;
END $$;

CREATE INDEX IF NOT EXISTS idx_emboss_records_file ON emboss_records(emboss_file_id);
CREATE INDEX IF NOT EXISTS idx_emboss_records_status ON emboss_records(status);
