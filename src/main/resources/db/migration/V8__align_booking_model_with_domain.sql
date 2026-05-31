ALTER TABLE bookings
    RENAME COLUMN number_of_seats TO ticket_quantity;

ALTER TABLE bookings
    ADD COLUMN unit_price DECIMAL(10, 2) NOT NULL AFTER ticket_quantity;

ALTER TABLE bookings
    RENAME COLUMN booking_status TO status;

ALTER TABLE bookings
    ADD CONSTRAINT chk_bookings_ticket_quantity_positive
        CHECK (ticket_quantity > 0);

ALTER TABLE bookings
    ADD CONSTRAINT chk_bookings_unit_price_positive
        CHECK (unit_price > 0);

ALTER TABLE bookings
    ADD CONSTRAINT chk_bookings_total_price_positive
        CHECK (total_price > 0);