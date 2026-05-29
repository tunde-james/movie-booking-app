CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    version INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,

    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(15) NOT NULL,
    role VARCHAR(50) NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT uk_users_username
        UNIQUE (username),

    CONSTRAINT uk_users_email
        UNIQUE (email)
);

CREATE TABLE bookings (
    id BIGINT NOT NULL AUTO_INCREMENT,
    version INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,

    user_id BIGINT NOT NULL,
    show_id BIGINT NOT NULL,
    number_of_seats INT NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    booking_status VARCHAR(50) NOT NULL,
    booking_time DATETIME(6) NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_bookings_user
        FOREIGN KEY (user_id) REFERENCES users (id),

    CONSTRAINT fk_bookings_show
        FOREIGN KEY (show_id) REFERENCES shows (id)
);

CREATE INDEX idx_bookings_show_status_deleted
    ON bookings (show_id, booking_status, deleted);

CREATE INDEX idx_bookings_user_status_deleted
    ON bookings (user_id, booking_status, deleted);
