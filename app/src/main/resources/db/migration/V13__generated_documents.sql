CREATE TABLE work.w_generated_document (
  id BIGSERIAL PRIMARY KEY,
  work_order_id BIGINT NOT NULL REFERENCES work.w_work_order(id) ON DELETE CASCADE,
  document_type VARCHAR(32) NOT NULL,
  document_number VARCHAR(64) NOT NULL DEFAULT '',
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_w_generated_document_order ON work.w_generated_document(work_order_id, created_at DESC);
