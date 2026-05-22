CREATE TABLE movies (
    id BIGINT NOT NULL AUTO_INCREMENT,
    version INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,

    title VARCHAR(200) NOT NULL,
    description TEXT,
    genre VARCHAR(50) NOT NULL,
    duration_in_minutes INT NOT NULL,
    release_date DATE NOT NULL,
    language VARCHAR(50) NOT NULL,
    rating VARCHAR(50) NOT NULL,
    movie_status VARCHAR(50) NOT NULL,
    poster_url VARCHAR(500),

    PRIMARY KEY (id),
    CONSTRAINT uk_movies_title_release_date_language_deleted
        UNIQUE (title, release_date, language, deleted)
);

CREATE INDEX idx_movies_status_deleted
    ON movies (movie_status, deleted);

CREATE INDEX idx_movies_genre_language_status_deleted
    ON movies (genre, language, movie_status, deleted);

CREATE TABLE cinemas (
    id BIGINT NOT NULL AUTO_INCREMENT,
    version INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,

    name VARCHAR(150) NOT NULL,
    address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,

    PRIMARY KEY (id)
);

CREATE INDEX idx_cinemas_city_deleted
    ON cinemas (city, deleted);