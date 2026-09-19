-- =============================================================================
-- Flyway Migration V24: Align Emboss Files Schema
-- Ensure file_id is nullable or has a valid default to prevent NOT NULL constraint violations
-- =============================================================================

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' AND table_name = 'emboss_files' AND column_name = 'file_id'
    ) THEN
        ALTER TABLE emboss_files ALTER COLUMN file_id DROP NOT NULL;
        ALTER TABLE emboss_files ALTER COLUMN file_id SET DEFAULT NULL;
    ELSE
        ALTER TABLE emboss_files ADD COLUMN IF NOT EXISTS file_id VARCHAR(50);
    END IF;
END $$;
