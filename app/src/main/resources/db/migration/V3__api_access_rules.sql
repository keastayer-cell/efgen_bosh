CREATE TABLE work.w_api_access_rule (
  id BIGSERIAL PRIMARY KEY,
  role_id BIGINT NOT NULL REFERENCES work.w_role(id) ON DELETE CASCADE,
  url_pattern VARCHAR(255) NOT NULL,
  http_method VARCHAR(16) NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX ux_w_api_access_rule_active
  ON work.w_api_access_rule(role_id, url_pattern, http_method)
  WHERE active = TRUE;

INSERT INTO work.w_api_access_rule(role_id, url_pattern, http_method, active)
SELECT role.id, '/api/**', '*', TRUE
FROM work.w_role role
WHERE role.code = 'ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO work.w_api_access_rule(role_id, url_pattern, http_method, active)
SELECT role.id, '/api/**', methods.http_method, TRUE
FROM work.w_role role
CROSS JOIN (
  VALUES ('GET'), ('POST'), ('PUT'), ('PATCH'), ('DELETE')
) AS methods(http_method)
WHERE role.code = 'OPERATOR'
ON CONFLICT DO NOTHING;

INSERT INTO work.w_api_access_rule(role_id, url_pattern, http_method, active)
SELECT role.id, '/api/**', 'GET', TRUE
FROM work.w_role role
WHERE role.code = 'VIEWER'
ON CONFLICT DO NOTHING;

COMMENT ON TABLE work.w_api_access_rule IS 'Матрица доступа к API по роли, URL и HTTP-методу';
COMMENT ON COLUMN work.w_api_access_rule.id IS 'Идентификатор правила доступа к API';
COMMENT ON COLUMN work.w_api_access_rule.role_id IS 'Идентификатор роли';
COMMENT ON COLUMN work.w_api_access_rule.url_pattern IS 'URL или шаблон URL (Ant style)';
COMMENT ON COLUMN work.w_api_access_rule.http_method IS 'HTTP-метод (GET/POST/PUT/PATCH/DELETE или *)';
COMMENT ON COLUMN work.w_api_access_rule.active IS 'Признак активности правила';
COMMENT ON COLUMN work.w_api_access_rule.created_at IS 'Дата создания правила';
