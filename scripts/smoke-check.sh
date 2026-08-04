#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-}"
RETRIES="${SMOKE_RETRIES:-30}"
DELAY_SECONDS="${SMOKE_DELAY_SECONDS:-2}"

while (( $# > 0 )); do
  case "$1" in
    --base-url) BASE_URL="${2:-}"; shift 2 ;;
    --retries) RETRIES="${2:-}"; shift 2 ;;
    *) echo "Unknown argument: $1" >&2; exit 2 ;;
  esac
done

[[ -n "$BASE_URL" ]] || { echo "--base-url is required." >&2; exit 2; }
[[ "$RETRIES" =~ ^[1-9][0-9]*$ ]] || { echo "--retries must be positive." >&2; exit 2; }
BASE_URL="${BASE_URL%/}"

wait_for_up() {
  local name="$1" url="$2" require_json="$3" response
  for attempt in $(seq 1 "$RETRIES"); do
    if response="$(curl --connect-timeout 3 --max-time 10 -fsS "$url" 2>/dev/null)"; then
      if [[ "$require_json" != true || "$response" == *'"status":"UP"'* || "$response" == *'"status": "UP"'* ]]; then
        echo "OK: $name"
        return 0
      fi
    fi
    echo "Waiting for $name ($attempt/$RETRIES)"
    sleep "$DELAY_SECONDS"
  done
  echo "FAILED: $name ($url)" >&2
  return 1
}

wait_for_up "backend health" "$BASE_URL/api/health" true
wait_for_up "frontend home" "$BASE_URL/" false
echo "Post-deploy smoke-check passed."
