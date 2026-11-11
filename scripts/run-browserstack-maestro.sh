#!/usr/bin/env bash
set -euo pipefail

BROWSERSTACK_USERNAME="${BROWSERSTACK_USERNAME:-${BROWSERSTACK_USER:-}}"
BROWSERSTACK_ACCESS_KEY="${BROWSERSTACK_ACCESS_KEY:-${BROWSERSTACK_TOKEN:-}}"
: "${BROWSERSTACK_USERNAME:?Set BROWSERSTACK_USERNAME (or BROWSERSTACK_USER)}"
: "${BROWSERSTACK_ACCESS_KEY:?Set BROWSERSTACK_ACCESS_KEY (or BROWSERSTACK_TOKEN)}"

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
APP_APK="${APP_APK:-$ROOT/app/build/outputs/apk/play/debug/app-play-debug.apk}"
PROJECT="${MAESTRO_PROJECT:-prayer-times-android}"
DEVICES_CSV="${MAESTRO_DEVICES:-Google Pixel 8-14.0,Samsung Galaxy S23-13.0,Google Pixel 6-12.0}"

[[ -f "$APP_APK" ]] || { echo "Missing APK: $APP_APK" >&2; exit 1; }

TMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TMP_DIR"' EXIT
TEST_ZIP="$TMP_DIR/maestro-tests.zip"
mkdir -p "$TMP_DIR/maestro"
cp -R "$ROOT/.maestro/." "$TMP_DIR/maestro/"
(cd "$TMP_DIR" && zip -qr "$TEST_ZIP" maestro)

auth=(-u "$BROWSERSTACK_USERNAME:$BROWSERSTACK_ACCESS_KEY")
api="https://api-cloud.browserstack.com/app-automate"

echo "Uploading app..."
app_url="$(curl -fsS "${auth[@]}" -X POST "$api/maestro/v2/app" -F "file=@$APP_APK" | jq -er '.app_url')"
echo "App: $app_url"

echo "Uploading Maestro test suite..."
test_suite_url="$(curl -fsS "${auth[@]}" -X POST "$api/maestro/v2/test-suite" -F "file=@$TEST_ZIP" | jq -er '.test_suite_url')"
echo "Test suite: $test_suite_url"

devices_json="$(printf '%s' "$DEVICES_CSV" | jq -R 'split(",")')"
build_payload="$(jq -n \
  --arg app "$app_url" \
  --arg suite "$test_suite_url" \
  --arg project "$PROJECT" \
  --argjson devices "$devices_json" \
  '{app:$app, testSuite:$suite, devices:$devices, project:$project, deviceLogs:true, execute:["smoke.yaml"]}')"

echo "Starting Maestro build..."
build_response="$(curl -fsS "${auth[@]}" -X POST "$api/maestro/v2/android/build" \
  -H 'Content-Type: application/json' -d "$build_payload")"
build_id="$(jq -er '.build_id' <<<"$build_response")"
echo "Build: $build_id"
echo "Dashboard: https://app-automate.browserstack.com/dashboard/v2/builds/$build_id"

status=""
for _ in $(seq 1 120); do
  build="$(curl -fsS "${auth[@]}" "$api/maestro/v2/builds/$build_id")"
  status="$(jq -r '.status // empty' <<<"$build")"
  echo "status: ${status:-unknown}"
  case "$status" in
    passed|completed|failed|error|skipped) break ;;
  esac
  sleep 10
done

echo "$build" | jq .
case "$status" in
  passed|completed) exit 0 ;;
  *) exit 1 ;;
esac
