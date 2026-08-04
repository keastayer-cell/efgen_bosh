CREATE TABLE work.w_counterparty (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(500) NOT NULL,
  inn VARCHAR(20) NOT NULL DEFAULT '',
  address TEXT NOT NULL DEFAULT '',
  phone VARCHAR(40) NOT NULL DEFAULT '',
  note TEXT NOT NULL DEFAULT '',
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  version BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT ck_w_counterparty_name CHECK (BTRIM(name) <> '')
);

CREATE INDEX idx_w_counterparty_name ON work.w_counterparty(LOWER(name));
CREATE INDEX idx_w_counterparty_inn ON work.w_counterparty(inn);
