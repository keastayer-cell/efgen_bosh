CREATE SCHEMA IF NOT EXISTS work;

GRANT USAGE ON SCHEMA work TO efgen_bosh;
GRANT CREATE ON SCHEMA work TO efgen_bosh;

ALTER TABLE public.app_user SET SCHEMA work;
ALTER TABLE work.app_user RENAME TO w_user_login;
ALTER TABLE work.w_user_login RENAME COLUMN display_name TO name;
ALTER TABLE work.w_user_login RENAME COLUMN password_change_required TO must_change_password;
ALTER TABLE work.w_user_login
  ADD COLUMN token_version INTEGER NOT NULL DEFAULT 0;
ALTER TABLE work.w_user_login
  ADD COLUMN password_changed_at TIMESTAMPTZ;

UPDATE work.w_user_login
SET password_changed_at = COALESCE(password_changed_at, created_at)
WHERE password_changed_at IS NULL;

ALTER INDEX work.app_user_email_lower_unique
  RENAME TO idx_w_user_login_email_lower;

ALTER TABLE public.user_role RENAME TO legacy_user_role;

ALTER TABLE public.audit_log SET SCHEMA work;
ALTER TABLE work.audit_log RENAME TO w_audit_log;
ALTER INDEX work.audit_log_created_at_idx
  RENAME TO idx_w_audit_log_created_at;
ALTER INDEX work.audit_log_entity_idx
  RENAME TO idx_w_audit_log_entity;

CREATE TABLE work.w_role (
  id BIGSERIAL PRIMARY KEY,
  code VARCHAR(50) NOT NULL UNIQUE,
  name_ru VARCHAR(120) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE work.w_user_role (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES work.w_user_login(id) ON DELETE CASCADE,
  role_id BIGINT NOT NULL REFERENCES work.w_role(id) ON DELETE CASCADE,
  granted_by_user_id BIGINT REFERENCES work.w_user_login(id),
  granted_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE work.w_auth_audit_log (
  id BIGSERIAL PRIMARY KEY,
  actor_user_id BIGINT REFERENCES work.w_user_login(id),
  target_user_id BIGINT REFERENCES work.w_user_login(id),
  action_code VARCHAR(80) NOT NULL,
  payload_json JSONB NOT NULL DEFAULT '{}'::JSONB,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX ux_w_user_role_active
  ON work.w_user_role(user_id, role_id)
  WHERE active = TRUE;

INSERT INTO work.w_role(code, name_ru)
VALUES
  ('ADMIN', 'Администратор'),
  ('OPERATOR', 'Оператор'),
  ('VIEWER', 'Наблюдатель')
ON CONFLICT (code) DO NOTHING;

INSERT INTO work.w_user_role(user_id, role_id, granted_by_user_id, active)
SELECT legacy.user_id, role.id, NULL, TRUE
FROM public.legacy_user_role legacy
JOIN work.w_role role ON role.code = legacy.role
ON CONFLICT DO NOTHING;

DROP TABLE public.legacy_user_role;

COMMENT ON TABLE work.w_user_login IS 'Пользователи входа в систему';
COMMENT ON COLUMN work.w_user_login.id IS 'Идентификатор пользователя';
COMMENT ON COLUMN work.w_user_login.email IS 'Email для входа';
COMMENT ON COLUMN work.w_user_login.name IS 'Имя пользователя';
COMMENT ON COLUMN work.w_user_login.password_hash IS 'Хэш пароля';
COMMENT ON COLUMN work.w_user_login.must_change_password IS 'Признак обязательной смены пароля';
COMMENT ON COLUMN work.w_user_login.token_version IS 'Версия токена для принудительного завершения сессий';
COMMENT ON COLUMN work.w_user_login.password_changed_at IS 'Дата последней смены пароля';
COMMENT ON COLUMN work.w_user_login.created_at IS 'Дата и время создания пользователя';

COMMENT ON TABLE work.w_role IS 'Справочник ролей доступа';
COMMENT ON COLUMN work.w_role.id IS 'Идентификатор роли';
COMMENT ON COLUMN work.w_role.code IS 'Код роли';
COMMENT ON COLUMN work.w_role.name_ru IS 'Название роли на русском';
COMMENT ON COLUMN work.w_role.created_at IS 'Дата создания роли';

COMMENT ON TABLE work.w_user_role IS 'Назначенные роли пользователей';
COMMENT ON COLUMN work.w_user_role.id IS 'Идентификатор назначения роли';
COMMENT ON COLUMN work.w_user_role.user_id IS 'Пользователь, которому назначили роль';
COMMENT ON COLUMN work.w_user_role.role_id IS 'Назначенная роль';
COMMENT ON COLUMN work.w_user_role.granted_by_user_id IS 'Кто назначил роль';
COMMENT ON COLUMN work.w_user_role.granted_at IS 'Дата назначения роли';
COMMENT ON COLUMN work.w_user_role.active IS 'Признак активного назначения';

COMMENT ON TABLE work.w_auth_audit_log IS 'Журнал действий по правам доступа';
COMMENT ON COLUMN work.w_auth_audit_log.actor_user_id IS 'Кто выполнил действие';
COMMENT ON COLUMN work.w_auth_audit_log.target_user_id IS 'Для какого пользователя выполнено действие';
COMMENT ON COLUMN work.w_auth_audit_log.action_code IS 'Код действия';
COMMENT ON COLUMN work.w_auth_audit_log.payload_json IS 'Детали действия в JSON';
COMMENT ON COLUMN work.w_auth_audit_log.created_at IS 'Дата и время действия';
