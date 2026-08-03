-- Страховой случай теперь проходит ровно пять бизнес-статусов.
UPDATE work.w_repair_case
SET status = 'SCHEDULED'
WHERE status IN ('IN_REPAIR', 'READY');

UPDATE work.w_repair_case
SET status = 'DELIVERED'
WHERE status = 'CLOSED';

UPDATE work.w_repair_case_status
SET active = FALSE
WHERE code IN ('IN_REPAIR', 'READY', 'CLOSED');

UPDATE work.w_repair_case_status
SET sort_order = sort_order + 100;

UPDATE work.w_repair_case_status
SET label = 'Ждем детали', sort_order = 2, active = TRUE
WHERE code = 'WAITING_PARTS';

UPDATE work.w_repair_case_status
SET label = 'Машина выдана', sort_order = 5, active = TRUE
WHERE code = 'DELIVERED';

ALTER TABLE work.w_repair_case DROP CONSTRAINT IF EXISTS ck_w_repair_case_status;
ALTER TABLE work.w_repair_case ADD CONSTRAINT ck_w_repair_case_status
    CHECK (status IN ('CREATED', 'WAITING_PARTS', 'PARTS_RECEIVED', 'SCHEDULED', 'DELIVERED'));
