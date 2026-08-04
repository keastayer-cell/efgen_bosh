CREATE TABLE work.w_repair_case (
  id BIGSERIAL PRIMARY KEY,
  car_id BIGINT NOT NULL REFERENCES work.w_car(id) ON DELETE CASCADE,
  case_number VARCHAR(64) NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'OPEN',
  insured_person VARCHAR(500) NOT NULL DEFAULT '',
  claim_number VARCHAR(120) NOT NULL DEFAULT '',
  insurer_id BIGINT REFERENCES work.w_insurer(id) ON DELETE SET NULL,
  contractor_id BIGINT REFERENCES work.w_contractor(id) ON DELETE SET NULL,
  shift_id BIGINT REFERENCES work.w_shift(id) ON DELETE SET NULL,
  accepted_at DATE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  version BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT ck_w_repair_case_status CHECK (status IN ('OPEN', 'IN_REPAIR', 'READY', 'CLOSED'))
);
CREATE UNIQUE INDEX ux_w_repair_case_number ON work.w_repair_case(car_id, case_number);
CREATE INDEX idx_w_repair_case_car ON work.w_repair_case(car_id, created_at DESC);
INSERT INTO work.w_repair_case(car_id, case_number, status, insured_person, claim_number, insurer_id, contractor_id, shift_id, accepted_at)
SELECT id, '1', CASE WHEN delivered THEN 'CLOSED' ELSE 'OPEN' END, insured_person, claim_number, insurer_id, contractor_id, shift_id, accepted_at
FROM work.w_car;
