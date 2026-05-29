ALTER TABLE shows
    ADD CONSTRAINT chk_shows_time_window
        CHECK (end_time > start_time);

ALTER TABLE shows
    ADD CONSTRAINT chk_shows_total_capacity_positive
        CHECK (total_capacity > 0);

ALTER TABLE shows
    ADD CONSTRAINT chk_shows_available_capacity_range
        CHECK (available_capacity >= 0 AND available_capacity <= total_capacity);

ALTER TABLE shows
    ADD CONSTRAINT chk_shows_price_positive
        CHECK (price_per_ticket > 0);