CREATE TABLE IF NOT EXISTS hourly_delta (
  hour_end TIMESTAMPTZ PRIMARY KEY,
  delta NUMERIC(38, 8) NOT NULL
);
ALTER TABLE hourly_delta
  ADD CONSTRAINT ck_hour_truncated
  CHECK (hour_end = date_trunc('hour', hour_end));
