CREATE TABLE shows (
    id BIGINT NOT NULL AUTO_INCREMENT,
    version INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,

    movie_id BIGINT NOT NULL,
    auditorium_id BIGINT NOT NULL,
    start_time DATETIME(6) NOT NULL,
    start_time_offset_seconds INT NOT NULL,
    end_time DATETIME(6) NOT NULL,
    end_time_offset_seconds INT NOT NULL,
    total_capacity INT NOT NULL,
    available_capacity INT NOT NULL,
    price_per_ticket DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_shows_movie
        FOREIGN KEY (movie_id) REFERENCES movies (id),

    CONSTRAINT fk_shows_auditorium
        FOREIGN KEY (auditorium_id) REFERENCES auditoriums (id)
);

CREATE INDEX idx_shows_auditorium_status_time_deleted
    ON shows (auditorium_id, status, start_time, end_time, deleted);

CREATE INDEX idx_shows_movie_status_start_time_deleted
    ON shows (movie_id, status, start_time, deleted);