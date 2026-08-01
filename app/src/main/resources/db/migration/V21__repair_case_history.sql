CREATE TABLE work.w_repair_case_history (
  id BIGSERIAL PRIMARY KEY,
  repair_case_id BIGINT NOT NULL REFERENCES work.w_repair_case(id) ON DELETE CASCADE,
  previous_status VARCHAR(32),
  new_status VARCHAR(32) NOT NULL,
  comment TEXT NOT NULL DEFAULT '',
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  created_by BIGINT REFERENCES work.w_user_login(id) ON DELETE SET NULL
);
CREATE INDEX idx_w_repair_case_history_case ON work.w_repair_case_history(repair_case_id, created_at DESC);
INSERT INTO work.w_repair_case_history(repair_case_id, previous_status, new_status, comment, created_at, created_by)
SELECT id, NULL, status, 'История перенесена при создании журнала', created_at, created_by FROM work.w_repair_case;
