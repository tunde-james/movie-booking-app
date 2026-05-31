ALTER TABLE bookings
    MODIFY user_id BIGINT NULL;

ALTER TABLE bookings
    ADD COLUMN first_name VARCHAR(100) NOT NULL AFTER user_id,
    ADD COLUMN last_name VARCHAR(100) NOT NULL AFTER first_name,
    ADD COLUMN email VARCHAR(150) NOT NULL AFTER last_name,
    ADD COLUMN phone_number VARCHAR(20) NULL AFTER email;

CREATE INDEX idx_bookings_email_status_deleted
    ON bookings (email, status, deleted);
