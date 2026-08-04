UPDATE work.w_repair_case
SET status = 'DELIVERED'
WHERE status = 'CLOSED';

UPDATE work.w_repair_case_status
SET active = FALSE
WHERE code = 'CLOSED';
