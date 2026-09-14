-- Legacy databases split received return quantities into good and damaged
-- buckets. The current domain model stores the quantity accepted back into
-- stock as qty_received, which corresponds to the legacy good quantity.
ALTER TABLE inventory_return_items
    ADD COLUMN IF NOT EXISTS qty_received INTEGER;

UPDATE inventory_return_items
SET qty_received = COALESCE(qty_received_good, 0)
WHERE qty_received IS NULL;

ALTER TABLE inventory_return_items
    ALTER COLUMN qty_received SET DEFAULT 0,
    ALTER COLUMN qty_received SET NOT NULL;
