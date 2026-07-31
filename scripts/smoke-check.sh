#!/usr/bin/env bash

set -euo pipefail

APP_BASE_URL="${APP_BASE_URL:-http://127.0.0.1:8080}"
WEB_BASE_URL="${WEB_BASE_URL:-http://127.0.0.1:5173}"

curl -fsS "$APP_BASE_URL/api/health" >/dev/null
curl -fsS "$WEB_BASE_URL/" >/dev/null

echo "Backend and web smoke checks passed."
