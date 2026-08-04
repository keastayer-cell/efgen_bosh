INSERT INTO work.w_repair_case
    (car_id, case_number, status, repair_type, insured_person, claim_number,
     insurer_id, contractor_id, shift_id, accepted_at)
SELECT c.id, '1',
       CASE
           WHEN c.delivered THEN 'DELIVERED'
           WHEN COUNT(p.id) = 0 THEN 'CREATED'
           WHEN BOOL_AND(p.received) THEN 'PARTS_RECEIVED'
           ELSE 'WAITING_PARTS'
       END,
       'INSURANCE', c.insured_person, c.claim_number,
       c.insurer_id, c.contractor_id, c.shift_id, c.accepted_at
FROM work.w_car c
LEFT JOIN work.w_part p ON p.car_id = c.id
WHERE NOT EXISTS (SELECT 1 FROM work.w_repair_case rc WHERE rc.car_id = c.id)
GROUP BY c.id, c.delivered, c.insured_person, c.claim_number,
         c.insurer_id, c.contractor_id, c.shift_id, c.accepted_at;

UPDATE work.w_part p
SET repair_case_id = rc.id
FROM work.w_repair_case rc
WHERE p.repair_case_id IS NULL
  AND rc.car_id = p.car_id
  AND rc.case_number = '1';
