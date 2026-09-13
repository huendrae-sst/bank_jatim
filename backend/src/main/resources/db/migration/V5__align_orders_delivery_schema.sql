ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS delivery_method VARCHAR(50) NOT NULL DEFAULT 'COURIER',
    ADD COLUMN IF NOT EXISTS pickup_pic_nip VARCHAR(50),
    ADD COLUMN IF NOT EXISTS pickup_pic_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS pickup_pic_position VARCHAR(255),
    ADD COLUMN IF NOT EXISTS pickup_notes TEXT;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'orders'
          AND column_name = 'is_pickup_kp'
    ) THEN
        UPDATE orders
        SET delivery_method = 'PICKUP_KP'
        WHERE is_pickup_kp = TRUE
          AND delivery_method = 'COURIER';
    END IF;
END $$;
