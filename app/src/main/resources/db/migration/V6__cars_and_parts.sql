CREATE TABLE work.w_car (
  id BIGSERIAL PRIMARY KEY,
  legacy_id VARCHAR(255),
  accounting_number BIGINT NOT NULL,
  registration_number VARCHAR(32) NOT NULL DEFAULT '',
  vehicle_name VARCHAR(255) NOT NULL,
  vehicle_name_latin VARCHAR(255) NOT NULL DEFAULT '',
  vin VARCHAR(32) NOT NULL DEFAULT '',
  insured_person VARCHAR(500) NOT NULL DEFAULT '',
  claim_number VARCHAR(120) NOT NULL DEFAULT '',
  insurer_id BIGINT REFERENCES work.w_insurer(id) ON DELETE SET NULL,
  contractor_id BIGINT REFERENCES work.w_contractor(id) ON DELETE SET NULL,
  accepted_at DATE,
  started_at DATE,
  appointment_date DATE,
  shift_id BIGINT REFERENCES work.w_shift(id) ON DELETE SET NULL,
  comment TEXT NOT NULL DEFAULT '',
  document_folder_url TEXT NOT NULL DEFAULT '',
  delivered BOOLEAN NOT NULL DEFAULT FALSE,
  delivered_at DATE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  version BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT ck_w_car_accounting_number_positive
    CHECK (accounting_number > 0),
  CONSTRAINT ck_w_car_vehicle_name_not_blank
    CHECK (BTRIM(vehicle_name) <> ''),
  CONSTRAINT ck_w_car_delivery_date
    CHECK (
      (delivered = TRUE AND delivered_at IS NOT NULL)
      OR (delivered = FALSE AND delivered_at IS NULL)
    )
);

CREATE UNIQUE INDEX ux_w_car_accounting_number
  ON work.w_car(accounting_number);
CREATE UNIQUE INDEX ux_w_car_legacy_id
  ON work.w_car(legacy_id)
  WHERE legacy_id IS NOT NULL;
CREATE INDEX idx_w_car_registration_number
  ON work.w_car(registration_number);
CREATE INDEX idx_w_car_vin
  ON work.w_car(vin);
CREATE INDEX idx_w_car_claim_number
  ON work.w_car(claim_number);
CREATE INDEX idx_w_car_delivered
  ON work.w_car(delivered);
CREATE INDEX idx_w_car_appointment_date
  ON work.w_car(appointment_date);
CREATE INDEX idx_w_car_insurer
  ON work.w_car(insurer_id);
CREATE INDEX idx_w_car_contractor
  ON work.w_car(contractor_id);
CREATE INDEX idx_w_car_shift
  ON work.w_car(shift_id);

CREATE TABLE work.w_part (
  id BIGSERIAL PRIMARY KEY,
  legacy_id VARCHAR(255),
  car_id BIGINT NOT NULL REFERENCES work.w_car(id) ON DELETE CASCADE,
  name VARCHAR(500) NOT NULL,
  article VARCHAR(255) NOT NULL DEFAULT '',
  supplier_id BIGINT REFERENCES work.w_supplier(id) ON DELETE SET NULL,
  expected_date DATE,
  received BOOLEAN NOT NULL DEFAULT FALSE,
  received_at DATE,
  sort_order INTEGER NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  version BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT ck_w_part_name_not_blank CHECK (BTRIM(name) <> ''),
  CONSTRAINT ck_w_part_received_date
    CHECK (
      (received = TRUE AND received_at IS NOT NULL)
      OR (received = FALSE AND received_at IS NULL)
    )
);

CREATE UNIQUE INDEX ux_w_part_legacy_id
  ON work.w_part(legacy_id)
  WHERE legacy_id IS NOT NULL;
CREATE INDEX idx_w_part_car
  ON work.w_part(car_id, sort_order);
CREATE INDEX idx_w_part_supplier
  ON work.w_part(supplier_id);
CREATE INDEX idx_w_part_expected_date_pending
  ON work.w_part(expected_date)
  WHERE received = FALSE;
CREATE INDEX idx_w_part_received
  ON work.w_part(received);

COMMENT ON TABLE work.w_car IS 'Реестр автомобилей кузовного сервиса';
COMMENT ON COLUMN work.w_car.legacy_id IS 'Исходный Firestore document id для повторяемого импорта';
COMMENT ON COLUMN work.w_car.accounting_number IS 'Уникальный внутренний номер автомобиля';
COMMENT ON COLUMN work.w_car.registration_number IS 'Нормализованный государственный номер';
COMMENT ON COLUMN work.w_car.version IS 'Версия optimistic locking';

COMMENT ON TABLE work.w_part IS 'Запчасти автомобиля';
COMMENT ON COLUMN work.w_part.legacy_id IS 'Исходный идентификатор запчасти для повторяемого импорта';
COMMENT ON COLUMN work.w_part.expected_date IS 'Ожидаемая дата поступления';
COMMENT ON COLUMN work.w_part.received IS 'Признак фактического поступления';
COMMENT ON COLUMN work.w_part.version IS 'Версия optimistic locking';
