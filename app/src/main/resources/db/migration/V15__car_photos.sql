CREATE TABLE work.w_car_photo (
  id BIGSERIAL PRIMARY KEY,
  car_id BIGINT NOT NULL REFERENCES work.w_car(id) ON DELETE CASCADE,
  file_name VARCHAR(255) NOT NULL DEFAULT '',
  mime_type VARCHAR(100) NOT NULL DEFAULT 'image/jpeg',
  data_url TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_w_car_photo_car ON work.w_car_photo(car_id, created_at DESC);
