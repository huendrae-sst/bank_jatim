-- =============================================================================
-- Flyway Migration V27: Add created_at and updated_at to production_order_fulfillments
-- =============================================================================

ALTER TABLE production_order_fulfillments
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP;
