CREATE TABLE IF NOT EXISTS work.w_refresh_token_session (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES work.w_user_login(id) ON DELETE CASCADE,
  token_hash VARCHAR(128) NOT NULL UNIQUE,
  token_version INTEGER NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  expires_at TIMESTAMPTZ NOT NULL,
  last_used_at TIMESTAMPTZ,
  revoked_at TIMESTAMPTZ,
  replaced_by_token_hash VARCHAR(128),
  user_agent VARCHAR(255),
  ip_address VARCHAR(64)
);

CREATE INDEX IF NOT EXISTS idx_w_refresh_token_session_user
  ON work.w_refresh_token_session(user_id);
CREATE INDEX IF NOT EXISTS idx_w_refresh_token_session_expires
  ON work.w_refresh_token_session(expires_at);

COMMENT ON TABLE work.w_refresh_token_session IS 'Refresh-токены для долгоживущих пользовательских сессий';
COMMENT ON COLUMN work.w_refresh_token_session.token_hash IS 'SHA-256 хеш refresh token, исходный токен в БД не хранится';
COMMENT ON COLUMN work.w_refresh_token_session.token_version IS 'Снимок версии токена пользователя на момент выдачи refresh token';
COMMENT ON COLUMN work.w_refresh_token_session.replaced_by_token_hash IS 'Хеш нового токена после ротации текущего';
