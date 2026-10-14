-- PROPOSED ONLY: review and approve before applying to Neon production.
-- Additive schema support for customer order history and POS status updates.
-- This file does not run automatically and must not be applied without explicit approval.

BEGIN;

ALTER TABLE public.restaurant_orders
  ADD COLUMN IF NOT EXISTS order_status text NOT NULL DEFAULT 'Received';

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM pg_constraint
    WHERE conname = 'restaurant_orders_order_status_check'
      AND conrelid = 'public.restaurant_orders'::regclass
  ) THEN
    ALTER TABLE public.restaurant_orders
      ADD CONSTRAINT restaurant_orders_order_status_check
      CHECK (order_status IN ('Received', 'Preparing', 'Ready', 'Completed', 'Cancelled'));
  END IF;
END
$$;

ALTER TABLE public.restaurant_orders
  ADD COLUMN IF NOT EXISTS customer_access_token_hash text;

CREATE UNIQUE INDEX IF NOT EXISTS restaurant_orders_customer_access_token_hash_uidx
  ON public.restaurant_orders (customer_access_token_hash)
  WHERE customer_access_token_hash IS NOT NULL;

CREATE INDEX IF NOT EXISTS restaurant_orders_status_created_at_idx
  ON public.restaurant_orders (order_status, created_at DESC);

-- Both existing sequences start at 1001 and no orders exist. Use one canonical
-- sequence for new order numbers; do not reset either sequence after orders exist.
ALTER TABLE public.restaurant_orders
  ALTER COLUMN order_number SET DEFAULT nextval('public.restaurant_order_number_seq'::regclass);

COMMIT;
