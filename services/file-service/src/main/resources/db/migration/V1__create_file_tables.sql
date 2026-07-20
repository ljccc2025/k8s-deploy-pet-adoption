CREATE SCHEMA IF NOT EXISTS file_schema;

CREATE TABLE file_schema.files (
  id UUID PRIMARY KEY,
  original_name VARCHAR(255) NOT NULL,
  content_type VARCHAR(100) NOT NULL,
  storage_path VARCHAR(500) NOT NULL,
  created_at TIMESTAMP NOT NULL
);
