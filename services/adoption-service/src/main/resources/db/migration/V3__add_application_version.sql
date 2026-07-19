ALTER TABLE adoption_schema.adoption_applications
  ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
