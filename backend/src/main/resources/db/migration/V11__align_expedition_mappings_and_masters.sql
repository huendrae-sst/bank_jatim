-- =============================================================================
-- Flyway Migration V11: Align Expedition Mappings and Shipping Destination Schema
-- =============================================================================

-- 1. Expedition Mappings
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' AND table_name = 'expedition_mappings' AND column_name = 'organization_id'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' AND table_name = 'expedition_mappings' AND column_name = 'destination_organization_id'
    ) THEN
        ALTER TABLE expedition_mappings RENAME COLUMN organization_id TO destination_organization_id;
    ELSIF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' AND table_name = 'expedition_mappings' AND column_name = 'destination_org_id'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' AND table_name = 'expedition_mappings' AND column_name = 'destination_organization_id'
    ) THEN
        ALTER TABLE expedition_mappings RENAME COLUMN destination_org_id TO destination_organization_id;
    ELSIF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' AND table_name = 'expedition_mappings' AND column_name = 'branch_id'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' AND table_name = 'expedition_mappings' AND column_name = 'destination_organization_id'
    ) THEN
        ALTER TABLE expedition_mappings RENAME COLUMN branch_id TO destination_organization_id;
    ELSIF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' AND table_name = 'expedition_mappings' AND column_name = 'destination_organization_id'
    ) THEN
        ALTER TABLE expedition_mappings ADD COLUMN destination_organization_id BIGINT REFERENCES organizations(id) ON DELETE CASCADE;
    END IF;

    -- Ensure courier_id, service_type, estimated_lead_days exist on expedition_mappings
    ALTER TABLE IF EXISTS expedition_mappings
        ADD COLUMN IF NOT EXISTS courier_id BIGINT REFERENCES couriers(id) ON DELETE CASCADE,
        ADD COLUMN IF NOT EXISTS service_type VARCHAR(50) NOT NULL DEFAULT 'REGULER',
        ADD COLUMN IF NOT EXISTS estimated_lead_days INTEGER NOT NULL DEFAULT 2;
END $$;

-- 2. Shipments
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' AND table_name = 'shipments' AND column_name = 'organization_id'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' AND table_name = 'shipments' AND column_name = 'destination_organization_id'
    ) THEN
        ALTER TABLE shipments RENAME COLUMN organization_id TO destination_organization_id;
    ELSIF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' AND table_name = 'shipments' AND column_name = 'destination_organization_id'
    ) THEN
        ALTER TABLE shipments ADD COLUMN destination_organization_id BIGINT REFERENCES organizations(id) ON DELETE CASCADE;
    END IF;
END $$;

-- 3. Switching Stocks
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' AND table_name = 'switching_stocks' AND column_name = 'source_org_id'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' AND table_name = 'switching_stocks' AND column_name = 'source_organization_id'
    ) THEN
        ALTER TABLE switching_stocks RENAME COLUMN source_org_id TO source_organization_id;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' AND table_name = 'switching_stocks' AND column_name = 'destination_org_id'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' AND table_name = 'switching_stocks' AND column_name = 'destination_organization_id'
    ) THEN
        ALTER TABLE switching_stocks RENAME COLUMN destination_org_id TO destination_organization_id;
    END IF;
END $$;
