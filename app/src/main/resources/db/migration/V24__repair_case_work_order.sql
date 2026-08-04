ALTER TABLE work.w_work_order ADD COLUMN repair_case_id BIGINT;
ALTER TABLE work.w_work_order ADD CONSTRAINT fk_w_work_order_repair_case
  FOREIGN KEY (repair_case_id) REFERENCES work.w_repair_case(id) ON DELETE CASCADE;
CREATE UNIQUE INDEX ux_w_work_order_repair_case ON work.w_work_order(repair_case_id)
  WHERE repair_case_id IS NOT NULL;
