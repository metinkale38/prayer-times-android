#!/bin/bash
set -euo pipefail

append_gradle_property() {
  local file="$1"
  local key="$2"
  local value="$3"

  touch "$file"
  if [ -s "$file" ] && [ -n "$(tail -c 1 "$file")" ]; then
    echo >> "$file"
  fi
  if ! grep -qF "${key}=" "$file"; then
    echo "${key}=${value}" >> "$file"
  fi
}

# The open-prayer-times sources are pinned as a git submodule.
if [ ! -f "open-prayer-times/settings.gradle.kts" ]; then
  echo "Initializing open-prayer-times submodule"
  git submodule update --init --recursive
fi

# create secrets.xml with empty values; release secrets are injected by the release workflow
SECRETS_FILE="app/src/main/res/values/secrets.xml"
if [ -f "$SECRETS_FILE" ]; then
  echo "secrets.xml already exists, skip"
else
  echo "Generate secrets.xml"
  mkdir -p "$(dirname "$SECRETS_FILE")"
  cat > "$SECRETS_FILE" <<'EOF'
<resources>
  <string name="GOOGLE_API_KEY"></string>
  <string name="IGMG_API_KEY"></string>
  <string name="LONDON_PRAYER_TIMES_API_KEY"></string>
  <string name="SENTRY_DSN"></string>
</resources>
EOF
fi

# Build one flavor at a time. The Kotlin compiler runs in-process and the Gradle
# heap is bounded so the two release builds do not exhaust small CI containers.
GRADLE_ARGS=(
  --no-daemon
  --max-workers=1
  -Pkotlin.compiler.execution.strategy=in-process
  -Dorg.gradle.jvmargs="-Xmx3g -XX:MaxMetaspaceSize=768m"
)

append_gradle_property gradle.properties org.gradle.daemon false
chmod +x gradlew

echo "Build F-Droid flavor"
./gradlew assembleFdroid "${GRADLE_ARGS[@]}"

echo "Build Play flavor"
./gradlew assemblePlay "${GRADLE_ARGS[@]}"
