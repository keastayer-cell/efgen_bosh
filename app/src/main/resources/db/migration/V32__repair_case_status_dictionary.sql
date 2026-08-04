CREATE TABLE work.w_repair_case_status (
    id INTEGER PRIMARY KEY,
    code VARCHAR(32) NOT NULL UNIQUE,
    label VARCHAR(120) NOT NULL,
    sort_order INTEGER NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO work.w_repair_case_status (id, code, label, sort_order) VALUES
    (1, 'CREATED', 'Создан', 1),
    (2, 'WAITING_PARTS', 'Ждём детали', 2),
    (3, 'PARTS_RECEIVED', 'Детали поступили', 3),
    (4, 'SCHEDULED', 'Запись на ремонт', 4),
    (5, 'IN_REPAIR', 'Ремонт', 5),
    (6, 'READY', 'Готов к выдаче', 6),
    (7, 'DELIVERED', 'Выдан', 7),
    (8, 'CLOSED', 'Закрыт', 8);

ALTER TABLE work.w_repair_case
    ADD CONSTRAINT fk_w_repair_case_status_dictionary
    FOREIGN KEY (status) REFERENCES work.w_repair_case_status(code);
