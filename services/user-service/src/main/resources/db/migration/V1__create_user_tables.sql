CREATE SCHEMA IF NOT EXISTS user_schema;

CREATE TABLE user_schema.user_profiles (
  user_id UUID PRIMARY KEY,
  display_name VARCHAR(100) NOT NULL,
  phone VARCHAR(30) NOT NULL,
  city VARCHAR(100) NOT NULL,
  housing VARCHAR(255) NOT NULL,
  updated_at TIMESTAMP NOT NULL
);
