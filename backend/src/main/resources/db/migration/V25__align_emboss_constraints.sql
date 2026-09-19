-- =============================================================================
-- Flyway Migration V25: Drop NOT NULL constraints on legacy emboss columns
-- =============================================================================

DO $$
BEGIN
    -- 1. emboss_files: file_hash
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' AND table_name = 'emboss_files' AND column_name = 'file_hash'
    ) THEN
        ALTER TABLE emboss_files ALTER COLUMN file_hash DROP NOT NULL;
        ALTER TABLE emboss_files ALTER COLUMN file_hash SET DEFAULT NULL;
    END IF;

    -- 2. emboss_records: external_reference_id
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' AND table_name = 'emboss_records' AND column_name = 'external_reference_id'
    ) THEN
        ALTER TABLE emboss_records ALTER COLUMN external_reference_id DROP NOT NULL;
        ALTER TABLE emboss_records ALTER COLUMN external_reference_id SET DEFAULT NULL;
    END IF;

    -- 3. emboss_records: product_code
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' AND table_name = 'emboss_records' AND column_name = 'product_code'
    ) THEN
        ALTER TABLE emboss_records ALTER COLUMN product_code DROP NOT NULL;
        ALTER TABLE emboss_records ALTER COLUMN product_code SET DEFAULT NULL;
    END IF;
END $$;
