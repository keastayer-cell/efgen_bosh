CREATE TABLE work.w_work_order_part_line (
  id BIGSERIAL PRIMARY KEY,
  work_order_id BIGINT NOT NULL REFERENCES work.w_work_order(id) ON DELETE CASCADE,
  part_id BIGINT REFERENCES work.w_part(id) ON DELETE SET NULL,
  name_snapshot VARCHAR(500) NOT NULL,
  article_snapshot VARCHAR(255) NOT NULL DEFAULT '',
  quantity NUMERIC(12, 3) NOT NULL DEFAULT 1,
  price NUMERIC(14, 2) NOT NULL DEFAULT 0,
  sort_order INTEGER NOT NULL DEFAULT 0,
  CONSTRAINT ck_w_work_order_part_line_name CHECK (BTRIM(name_snapshot) <> ''),
  CONSTRAINT ck_w_work_order_part_line_quantity CHECK (quantity > 0),
  CONSTRAINT ck_w_work_order_part_line_price CHECK (price >= 0)
);

CREATE INDEX idx_w_work_order_part_line_order
  ON work.w_work_order_part_line(work_order_id, sort_order, id);
