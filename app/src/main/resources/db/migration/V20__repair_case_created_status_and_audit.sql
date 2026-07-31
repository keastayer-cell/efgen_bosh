ALTER TABLE work.w_repair_case ADD COLUMN created_by BIGINT REFERENCES work.w_user_login(id) ON DELETE SET NULL;
ALTER TABLE work.w_repair_case ADD COLUMN updated_by BIGINT REFERENCES work.w_user_login(id) ON DELETE SET NULL;
ALTER TABLE work.w_repair_case DROP CONSTRAINT ck_w_repair_case_status;
ALTER TABLE work.w_repair_case ADD CONSTRAINT ck_w_repair_case_status CHECK (status IN ('CREATED', 'OPEN', 'IN_REPAIR', 'READY', 'CLOSED'));
UPDATE work.w_repair_case SET status = 'CREATED' WHERE status = 'OPEN';
