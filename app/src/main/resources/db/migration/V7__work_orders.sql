CREATE TABLE work.w_work_order (
  id BIGSERIAL PRIMARY KEY,
  car_id BIGINT NOT NULL UNIQUE REFERENCES work.w_car(id) ON DELETE CASCADE,
  status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
  document_date DATE NOT NULL DEFAULT CURRENT_DATE,
  customer VARCHAR(500) NOT NULL DEFAULT '',
  claim_number VARCHAR(120) NOT NULL DEFAULT '',
  vehicle_name VARCHAR(255) NOT NULL DEFAULT '',
  registration_number VARCHAR(32) NOT NULL DEFAULT '',
  vin VARCHAR(32) NOT NULL DEFAULT '',
  created_by BIGINT REFERENCES work.w_user_login(id) ON DELETE SET NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  version BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT ck_w_work_order_status CHECK (status IN ('DRAFT', 'READY', 'CLOSED'))
);

CREATE TABLE work.w_work_order_line (
  id BIGSERIAL PRIMARY KEY,
  work_order_id BIGINT NOT NULL REFERENCES work.w_work_order(id) ON DELETE CASCADE,
  category_name_snapshot VARCHAR(255) NOT NULL DEFAULT '',
  name_snapshot VARCHAR(500) NOT NULL,
  unit VARCHAR(40) NOT NULL DEFAULT 'шт.',
  quantity NUMERIC(12, 3) NOT NULL DEFAULT 1,
  price NUMERIC(14, 2) NOT NULL DEFAULT 0,
  sort_order INTEGER NOT NULL DEFAULT 0,
  CONSTRAINT ck_w_work_order_line_name CHECK (BTRIM(name_snapshot) <> ''),
  CONSTRAINT ck_w_work_order_line_quantity CHECK (quantity > 0),
  CONSTRAINT ck_w_work_order_line_price CHECK (price >= 0)
);

CREATE INDEX idx_w_work_order_line_order
  ON work.w_work_order_line(work_order_id, sort_order, id);

COMMENT ON TABLE work.w_work_order IS 'Черновик заказ-наряда автомобиля';
COMMENT ON TABLE work.w_work_order_line IS 'Снимок строк работ заказ-наряда';
