ALTER TABLE work.w_repair_case ADD COLUMN repair_type VARCHAR(16) NOT NULL DEFAULT 'INSURANCE';
ALTER TABLE work.w_repair_case ADD CONSTRAINT ck_w_repair_case_type CHECK (repair_type IN ('INSURANCE','REPAIR'));
