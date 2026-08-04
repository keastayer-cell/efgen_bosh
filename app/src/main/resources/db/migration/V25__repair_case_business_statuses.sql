ALTER TABLE work.w_repair_case DROP CONSTRAINT ck_w_repair_case_status;
UPDATE work.w_repair_case SET status = 'CREATED'
WHERE status NOT IN ('CREATED', 'WAITING_PARTS', 'PARTS_RECEIVED', 'SCHEDULED', 'IN_REPAIR', 'READY', 'DELIVERED', 'CLOSED');
ALTER TABLE work.w_repair_case ADD CONSTRAINT ck_w_repair_case_status CHECK (status IN ('CREATED', 'WAITING_PARTS', 'PARTS_RECEIVED', 'SCHEDULED', 'IN_REPAIR', 'READY', 'DELIVERED', 'CLOSED'));
