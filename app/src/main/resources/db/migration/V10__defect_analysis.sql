CREATE TABLE work.w_defect_analysis (
  id BIGSERIAL PRIMARY KEY,
  car_id BIGINT NOT NULL UNIQUE REFERENCES work.w_car(id),
  status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
  findings TEXT NOT NULL DEFAULT '',
  recommendations TEXT NOT NULL DEFAULT '',
  photos_json TEXT NOT NULL DEFAULT '[]',
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  version BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE work.w_defect_analysis IS 'Ручная дефектовка автомобиля и фотографии осмотра';
