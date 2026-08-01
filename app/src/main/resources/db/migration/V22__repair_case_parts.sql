ALTER TABLE work.w_part ADD COLUMN repair_case_id BIGINT REFERENCES work.w_repair_case(id) ON DELETE CASCADE;
UPDATE work.w_part p SET repair_case_id = c.id FROM work.w_repair_case c WHERE c.car_id = p.car_id AND c.case_number = '1' AND p.repair_case_id IS NULL;
CREATE INDEX idx_w_part_repair_case ON work.w_part(repair_case_id, sort_order, id);
