CREATE TABLE IF NOT EXISTS txn (
  id UUID PRIMARY KEY,
  occurred_at TIMESTAMPTZ NOT NULL,
  recorded_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  amount NUMERIC(38, 8) NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_txn_occurred_at ON txn (occurred_at);
CREATE INDEX IF NOT EXISTS idx_txn_recorded_id ON txn (recorded_at, id);
