CREATE TABLE work.w_insurer (
  id BIGSERIAL PRIMARY KEY,
  legacy_id VARCHAR(255),
  name VARCHAR(255) NOT NULL,
  legal_details TEXT NOT NULL DEFAULT '',
  active BOOLEAN NOT NULL DEFAULT TRUE,
  sort_order INTEGER NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  version BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT ck_w_insurer_name_not_blank CHECK (BTRIM(name) <> '')
);

CREATE UNIQUE INDEX ux_w_insurer_name_lower
  ON work.w_insurer(LOWER(name));
CREATE UNIQUE INDEX ux_w_insurer_legacy_id
  ON work.w_insurer(legacy_id)
  WHERE legacy_id IS NOT NULL;

CREATE TABLE work.w_supplier (
  id BIGSERIAL PRIMARY KEY,
  legacy_id VARCHAR(255),
  name VARCHAR(255) NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  sort_order INTEGER NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  version BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT ck_w_supplier_name_not_blank CHECK (BTRIM(name) <> '')
);

CREATE UNIQUE INDEX ux_w_supplier_name_lower
  ON work.w_supplier(LOWER(name));
CREATE UNIQUE INDEX ux_w_supplier_legacy_id
  ON work.w_supplier(legacy_id)
  WHERE legacy_id IS NOT NULL;

CREATE TABLE work.w_shift (
  id BIGSERIAL PRIMARY KEY,
  legacy_id VARCHAR(255),
  name VARCHAR(120) NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  sort_order INTEGER NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  version BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT ck_w_shift_name_not_blank CHECK (BTRIM(name) <> '')
);

CREATE UNIQUE INDEX ux_w_shift_name_lower
  ON work.w_shift(LOWER(name));
CREATE UNIQUE INDEX ux_w_shift_legacy_id
  ON work.w_shift(legacy_id)
  WHERE legacy_id IS NOT NULL;

CREATE TABLE work.w_vehicle_alias (
  id BIGSERIAL PRIMARY KEY,
  legacy_id VARCHAR(255),
  source_name VARCHAR(255) NOT NULL,
  normalized_latin_name VARCHAR(255) NOT NULL DEFAULT '',
  active BOOLEAN NOT NULL DEFAULT TRUE,
  sort_order INTEGER NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  version BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT ck_w_vehicle_alias_source_not_blank
    CHECK (BTRIM(source_name) <> '')
);

CREATE UNIQUE INDEX ux_w_vehicle_alias_source_lower
  ON work.w_vehicle_alias(LOWER(source_name));
CREATE UNIQUE INDEX ux_w_vehicle_alias_legacy_id
  ON work.w_vehicle_alias(legacy_id)
  WHERE legacy_id IS NOT NULL;

CREATE TABLE work.w_contractor (
  id BIGSERIAL PRIMARY KEY,
  legacy_id VARCHAR(255),
  code VARCHAR(80) NOT NULL,
  short_name VARCHAR(255) NOT NULL,
  full_name TEXT NOT NULL,
  signer_name VARCHAR(255) NOT NULL DEFAULT '',
  inn VARCHAR(20) NOT NULL DEFAULT '',
  ogrnip VARCHAR(20) NOT NULL DEFAULT '',
  address TEXT NOT NULL DEFAULT '',
  bank_name TEXT NOT NULL DEFAULT '',
  bank_inn VARCHAR(20) NOT NULL DEFAULT '',
  bank_kpp VARCHAR(20) NOT NULL DEFAULT '',
  bik VARCHAR(20) NOT NULL DEFAULT '',
  correspondent_account VARCHAR(34) NOT NULL DEFAULT '',
  settlement_account VARCHAR(34) NOT NULL DEFAULT '',
  stamp_storage_key VARCHAR(500),
  signature_storage_key VARCHAR(500),
  active BOOLEAN NOT NULL DEFAULT TRUE,
  sort_order INTEGER NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  version BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT ck_w_contractor_code_not_blank CHECK (BTRIM(code) <> ''),
  CONSTRAINT ck_w_contractor_short_name_not_blank
    CHECK (BTRIM(short_name) <> ''),
  CONSTRAINT ck_w_contractor_full_name_not_blank
    CHECK (BTRIM(full_name) <> '')
);

CREATE UNIQUE INDEX ux_w_contractor_code_lower
  ON work.w_contractor(LOWER(code));
CREATE UNIQUE INDEX ux_w_contractor_legacy_id
  ON work.w_contractor(legacy_id)
  WHERE legacy_id IS NOT NULL;

CREATE TABLE work.w_work_category (
  id BIGSERIAL PRIMARY KEY,
  legacy_id VARCHAR(255),
  name VARCHAR(255) NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  sort_order INTEGER NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  version BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT ck_w_work_category_name_not_blank CHECK (BTRIM(name) <> '')
);

CREATE UNIQUE INDEX ux_w_work_category_name_lower
  ON work.w_work_category(LOWER(name));
CREATE UNIQUE INDEX ux_w_work_category_legacy_id
  ON work.w_work_category(legacy_id)
  WHERE legacy_id IS NOT NULL;

CREATE TABLE work.w_work_catalog_item (
  id BIGSERIAL PRIMARY KEY,
  legacy_id VARCHAR(255),
  category_id BIGINT NOT NULL
    REFERENCES work.w_work_category(id) ON DELETE RESTRICT,
  code VARCHAR(120) NOT NULL,
  name VARCHAR(500) NOT NULL,
  default_unit VARCHAR(40) NOT NULL DEFAULT 'н/ч',
  active BOOLEAN NOT NULL DEFAULT TRUE,
  sort_order INTEGER NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  version BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT ck_w_work_catalog_item_code_not_blank CHECK (BTRIM(code) <> ''),
  CONSTRAINT ck_w_work_catalog_item_name_not_blank CHECK (BTRIM(name) <> '')
);

CREATE UNIQUE INDEX ux_w_work_catalog_item_code_lower
  ON work.w_work_catalog_item(LOWER(code));
CREATE INDEX idx_w_work_catalog_item_category
  ON work.w_work_catalog_item(category_id, sort_order);
CREATE UNIQUE INDEX ux_w_work_catalog_item_legacy_id
  ON work.w_work_catalog_item(legacy_id)
  WHERE legacy_id IS NOT NULL;

COMMENT ON TABLE work.w_insurer IS 'Редактируемый справочник страховых компаний';
COMMENT ON TABLE work.w_supplier IS 'Редактируемый справочник поставщиков запчастей';
COMMENT ON TABLE work.w_shift IS 'Редактируемый справочник смен и мастеров';
COMMENT ON TABLE work.w_vehicle_alias IS 'Варианты названий автомобилей и латинское написание';
COMMENT ON TABLE work.w_contractor IS 'Исполнители и защищенные реквизиты для документов';
COMMENT ON TABLE work.w_work_category IS 'Категории ремонтных работ';
COMMENT ON TABLE work.w_work_catalog_item IS 'Каталог ремонтных работ';
