ALTER TABLE adoption_schema.adoption_applications
  ADD COLUMN active_pet_id UUID;

UPDATE adoption_schema.adoption_applications
SET active_pet_id = pet_id
WHERE status IN ('SUBMITTED', 'APPROVED');

CREATE UNIQUE INDEX ux_adoption_applications_active_pet_id
  ON adoption_schema.adoption_applications (active_pet_id);

CREATE TABLE adoption_schema.adoption_outbox_events (
  id UUID PRIMARY KEY,
  event_type VARCHAR(128) NOT NULL,
  application_id UUID NOT NULL,
  pet_id UUID NOT NULL,
  user_id UUID NOT NULL,
  pet_status_update VARCHAR(32),
  occurred_at TIMESTAMP NOT NULL,
  processed_at TIMESTAMP,
  error_message TEXT
);

CREATE INDEX idx_adoption_outbox_events_processed_at
  ON adoption_schema.adoption_outbox_events (processed_at);

CREATE INDEX idx_adoption_outbox_events_occurred_at
  ON adoption_schema.adoption_outbox_events (occurred_at);
