-- Align legacy accounting columns with the current GeneralLedgerEntry mapping.
-- Renaming preserves existing values and is skipped for databases already using
-- the current debit_amount / credit_amount names.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'general_ledger_entries'
          AND column_name = 'debit'
    ) AND NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'general_ledger_entries'
          AND column_name = 'debit_amount'
    ) THEN
        ALTER TABLE general_ledger_entries RENAME COLUMN debit TO debit_amount;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'general_ledger_entries'
          AND column_name = 'credit'
    ) AND NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'general_ledger_entries'
          AND column_name = 'credit_amount'
    ) THEN
        ALTER TABLE general_ledger_entries RENAME COLUMN credit TO credit_amount;
    END IF;
END $$;
