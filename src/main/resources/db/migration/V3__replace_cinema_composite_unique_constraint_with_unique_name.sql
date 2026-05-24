ALTER TABLE cinemas
DROP INDEX uk_cinemas_name_address_city_deleted;

ALTER TABLE cinemas
ADD COLUMN normalized_name VARCHAR(150)
    GENERATED ALWAYS AS (LOWER(TRIM(name))) STORED;

ALTER TABLE cinemas
ADD CONSTRAINT uk_cinemas_normalized_name
UNIQUE (normalized_name);