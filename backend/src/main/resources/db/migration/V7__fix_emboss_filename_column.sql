-- =============================================================================
-- Flyway Migration V7: Ensure filename column exists in emboss_files
-- =============================================================================

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' AND table_name = 'emboss_files' AND column_name = 'file_name'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' AND table_name = 'emboss_files' AND column_name = 'filename'
    ) THEN
        ALTER TABLE emboss_files RENAME COLUMN file_name TO filename;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' AND table_name = 'emboss_files' AND column_name = 'filename'
    ) THEN
        ALTER TABLE emboss_files ADD COLUMN filename VARCHAR(255) NOT NULL DEFAULT 'untitled.csv';
    END IF;
END $$;
