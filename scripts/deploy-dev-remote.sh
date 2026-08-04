#!/usr/bin/env bash
set -Eeuo pipefail

RELEASE_DIR="${1:-}"
RELEASE_ID="${RELEASE_ID:-$(date -u +%Y%m%dT%H%M%SZ)}"
APP_JAR_PATH="${APP_JAR_PATH:-/opt/efgen-bosh/test/app.jar}"
WEB_ROOT="${WEB_ROOT:-/var/www/efgen-bosh/current}"
RELEASES_ROOT="${RELEASES_ROOT:-/opt/efgen-bosh/releases}"
APP_SERVICE="${APP_SERVICE:-efgen-bosh-app@test.service}"
LOCAL_BASE_URL="${LOCAL_BASE_URL:-http://127.0.0.1:8088}"

(( EUID == 0 )) || { echo "Remote deploy must run as root." >&2; exit 1; }
[[ -n "$RELEASE_DIR" && -d "$RELEASE_DIR" ]] || { echo "Usage: $0 <release-directory>" >&2; exit 2; }
[[ -f "$RELEASE_DIR/app.jar" && -f "$RELEASE_DIR/web-dist.tgz" ]] || {
  echo "Release must contain app.jar and web-dist.tgz." >&2
  exit 1
}

exec 9>/var/lock/efgen-bosh-deploy.lock
flock -n 9 || { echo "Another deployment is already running." >&2; exit 1; }

release_state="$RELEASES_ROOT/$RELEASE_ID"
previous_state="$release_state/previous"
mkdir -p "$previous_state" "$WEB_ROOT"
deployment_started=false

rollback() {
  local exit_code=$?
  trap - ERR
  [[ "$deployment_started" == true ]] || exit "$exit_code"
  echo "Deployment failed; restoring previous runtime artifacts." >&2
  [[ -f "$previous_state/app.jar" ]] && install -o efgen-bosh -g efgen-bosh -m 0640 "$previous_state/app.jar" "$APP_JAR_PATH"
  if [[ -f "$previous_state/web-dist.tgz" ]]; then
    rm -rf "${WEB_ROOT:?}/"*
    tar -xzf "$previous_state/web-dist.tgz" -C "$WEB_ROOT"
  fi
  systemctl restart "$APP_SERVICE" || true
  echo "Runtime rollback completed. Database migrations are not rolled back automatically." >&2
  exit "$exit_code"
}
trap rollback ERR

[[ ! -f "$APP_JAR_PATH" ]] || cp -p "$APP_JAR_PATH" "$previous_state/app.jar"
tar -C "$WEB_ROOT" -czf "$previous_state/web-dist.tgz" .
printf '%s\n' "release=$RELEASE_ID" "deployed_at=$(date -u +%Y-%m-%dT%H:%M:%SZ)" > "$release_state/manifest"

# Mark the runtime mutation before stopping the service so failures during any
# replacement step trigger rollback as well.
deployment_started=true
# The service is stopped before replacing the JAR, preventing partial reads.
systemctl stop "$APP_SERVICE"
install -o efgen-bosh -g efgen-bosh -m 0640 "$RELEASE_DIR/app.jar" "$APP_JAR_PATH"

web_staging="${WEB_ROOT}.next-${RELEASE_ID}"
rm -rf "$web_staging"
mkdir -p "$web_staging"
tar -xzf "$RELEASE_DIR/web-dist.tgz" -C "$web_staging"
rm -rf "${WEB_ROOT:?}/"*
cp -a "$web_staging/." "$WEB_ROOT/"
rm -rf "$web_staging"

nginx -t
systemctl start "$APP_SERVICE"
bash "$RELEASE_DIR/smoke-check.sh" --base-url "$LOCAL_BASE_URL" --retries 30
deployment_started=false
echo "Dev deployment $RELEASE_ID completed."
