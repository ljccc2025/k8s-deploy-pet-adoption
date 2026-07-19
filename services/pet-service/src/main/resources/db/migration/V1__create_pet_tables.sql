CREATE SCHEMA IF NOT EXISTS pet_schema;

CREATE TABLE pet_schema.pets (
  id UUID PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  type VARCHAR(50) NOT NULL,
  gender VARCHAR(20) NOT NULL,
  age_months INT NOT NULL,
  city VARCHAR(100) NOT NULL,
  health_status VARCHAR(255) NOT NULL,
  adoption_status VARCHAR(32) NOT NULL,
  image_url VARCHAR(500),
  description TEXT NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_pets_adoption_status ON pet_schema.pets (adoption_status);
CREATE INDEX idx_pets_type ON pet_schema.pets (type);
CREATE INDEX idx_pets_city ON pet_schema.pets (city);
