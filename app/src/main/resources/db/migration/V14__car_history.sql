CREATE TABLE work.w_car_history (
  id BIGSERIAL PRIMARY KEY,
  car_id BIGINT NOT NULL REFERENCES work.w_car(id) ON DELETE CASCADE,
  event_type VARCHAR(64) NOT NULL,
  details TEXT NOT NULL DEFAULT '',
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_w_car_history_car ON work.w_car_history(car_id, created_at DESC);
