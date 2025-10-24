CREATE TABLE IF NOT EXISTS projector_state (
  component TEXT PRIMARY KEY,
  last_recorded_at TIMESTAMPTZ NOT NULL DEFAULT 'epoch',
  last_id UUID
);
INSERT INTO projector_state(component, last_recorded_at, last_id)
VALUES ('hourly_delta', 'epoch', NULL)
ON CONFLICT (component) DO NOTHING;
