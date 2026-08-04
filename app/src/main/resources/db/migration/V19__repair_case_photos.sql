ALTER TABLE work.w_car_photo ADD COLUMN repair_case_id BIGINT REFERENCES work.w_repair_case(id) ON DELETE CASCADE;
UPDATE work.w_car_photo p SET repair_case_id = c.id FROM work.w_repair_case c WHERE c.car_id = p.car_id AND c.case_number = '1' AND p.repair_case_id IS NULL;
CREATE INDEX idx_w_car_photo_repair_case ON work.w_car_photo(repair_case_id, created_at DESC);
