ALTER TABLE cinemas
ADD CONSTRAINT uk_cinemas_name_address_city_deleted
UNIQUE (name, address, city, deleted);