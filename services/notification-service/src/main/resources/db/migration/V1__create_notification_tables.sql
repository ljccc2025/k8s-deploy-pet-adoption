CREATE SCHEMA IF NOT EXISTS notification_schema;

CREATE TABLE notification_schema.notifications (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL,
  application_id UUID NOT NULL,
  event_type VARCHAR(100) NOT NULL,
  message VARCHAR(500) NOT NULL,
  read_at TIMESTAMP WITH TIME ZONE,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX ux_notifications_event_user
  ON notification_schema.notifications (event_type, application_id, user_id);

CREATE INDEX ix_notifications_user_created
  ON notification_schema.notifications (user_id, created_at DESC);
