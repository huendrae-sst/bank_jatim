-- Map the legacy return origin/requester fields to the current domain model
-- while retaining the legacy columns for backward compatibility.
ALTER TABLE inventory_returns
    ADD COLUMN IF NOT EXISTS organization_id BIGINT,
    ADD COLUMN IF NOT EXISTS created_by_user_id BIGINT;

UPDATE inventory_returns r
SET organization_id = w.organization_id
FROM warehouses w
WHERE r.organization_id IS NULL
  AND w.id = r.origin_warehouse_id;

UPDATE inventory_returns
SET created_by_user_id = requested_by_user_id
WHERE created_by_user_id IS NULL;

ALTER TABLE inventory_returns
    ALTER COLUMN organization_id SET NOT NULL,
    ALTER COLUMN created_by_user_id SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_inventory_returns_organization'
    ) THEN
        ALTER TABLE inventory_returns
            ADD CONSTRAINT fk_inventory_returns_organization
            FOREIGN KEY (organization_id) REFERENCES organizations(id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_inventory_returns_created_by_user'
    ) THEN
        ALTER TABLE inventory_returns
            ADD CONSTRAINT fk_inventory_returns_created_by_user
            FOREIGN KEY (created_by_user_id) REFERENCES users(id);
    END IF;
END $$;
