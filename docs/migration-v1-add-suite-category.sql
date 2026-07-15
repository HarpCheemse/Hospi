-- =============================================================================
-- Migration: Add 'SUITE' to room_types category CHECK constraint
-- =============================================================================
-- The Java enum RoomCategory already supports SUITE, but the DB CHECK
-- constraint was missing it. This aligns the constraint with the enum.
-- =============================================================================

ALTER TABLE room_types
    DROP CONSTRAINT IF EXISTS room_types_category_check;

ALTER TABLE room_types
    ADD CONSTRAINT room_types_category_check
        CHECK (category IN ('SINGLE', 'DOUBLE', 'FAMILY', 'SUITE'));
