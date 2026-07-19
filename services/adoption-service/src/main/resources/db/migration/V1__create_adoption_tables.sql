CREATE SCHEMA IF NOT EXISTS adoption_schema;

CREATE TABLE adoption_schema.adoption_applications (
  id UUID PRIMARY KEY,
  pet_id UUID NOT NULL,
  user_id UUID NOT NULL,
  reason TEXT NOT NULL,
  experience TEXT NOT NULL,
  status VARCHAR(32) NOT NULL,
  reviewer_id UUID,
  review_comment TEXT,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_adoption_applications_user_id ON adoption_schema.adoption_applications (user_id);
CREATE INDEX idx_adoption_applications_pet_id ON adoption_schema.adoption_applications (pet_id);
CREATE INDEX idx_adoption_applications_status ON adoption_schema.adoption_applications (status);
CREATE INDEX idx_adoption_applications_created_at ON adoption_schema.adoption_applications (created_at);
