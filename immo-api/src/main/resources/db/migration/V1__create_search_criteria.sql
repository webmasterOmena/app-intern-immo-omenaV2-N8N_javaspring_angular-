CREATE TABLE search_criteria (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    city VARCHAR(120) NOT NULL,
    postal_code VARCHAR(10),
    radius_km INTEGER NOT NULL CHECK (radius_km >= 0),
    max_price NUMERIC(15, 2),
    min_surface NUMERIC(10, 2),
    property_type VARCHAR(40),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_search_criteria_active ON search_criteria(active);
CREATE INDEX idx_search_criteria_city ON search_criteria(city);
