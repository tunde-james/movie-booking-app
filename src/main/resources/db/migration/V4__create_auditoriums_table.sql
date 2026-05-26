CREATE TABLE auditoriums (
    id BIGINT NOT NULL AUTO_INCREMENT,
    version INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,

    cinema_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    capacity INT NOT NULL,

    normalized_name VARCHAR(100)
        GENERATED ALWAYS AS (LOWER(TRIM(name))) STORED,

    PRIMARY KEY (id),

    CONSTRAINT fk_auditoriums_cinema
        FOREIGN KEY (cinema_id) REFERENCES cinemas (id),

    CONSTRAINT uk_auditoriums_cinema_normalized_name
        UNIQUE (cinema_id, normalized_name)
);

CREATE INDEX idx_auditoriums_cinema_deleted
    ON auditoriums (cinema_id, deleted);