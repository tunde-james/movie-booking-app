ALTER TABLE bookings
    MODIFY user_id BIGINT NULL;

ALTER TABLE bookings
    ADD COLUMN first_name VARCHAR(100) NULL AFTER user_id,
    ADD COLUMN last_name VARCHAR(100) NULL AFTER first_name,
    ADD COLUMN email VARCHAR(150) NULL AFTER last_name,
    ADD COLUMN phone_number VARCHAR(20) NULL AFTER email,
    ADD COLUMN guest_access_token VARCHAR(64) NULL AFTER phone_number;

UPDATE bookings b
LEFT JOIN users u ON b.user_id = u.id
SET
    b.first_name = 'Guest',
    b.last_name = 'Customer',
    b.email = COALESCE(u.email, 'legacy-booking@example.com')
WHERE b.first_name IS NULL
   OR b.last_name IS NULL
   OR b.email IS NULL;

UPDATE bookings
SET guest_access_token = LOWER(REPLACE(UUID(), '-', ''))
WHERE guest_access_token IS NULL;

ALTER TABLE bookings
    MODIFY first_name VARCHAR(100) NOT NULL,
    MODIFY last_name VARCHAR(100) NOT NULL,
    MODIFY email VARCHAR(150) NOT NULL,
    MODIFY guest_access_token VARCHAR(64) NOT NULL;

CREATE INDEX idx_bookings_email_status_deleted
    ON bookings (email, status, deleted);