#!/bin/bash
set -euo pipefail

OPEN_PRAYER_TIMES_REPO="https://github.com/metinkale38/open-prayer-times.git"
OPEN_PRAYER_TIMES_REF="${OPEN_PRAYER_TIMES_REF:-0378851c10cc0a188908d93ea2dbb75fec6e9546}"

# Build one flavor at a time. The Kotlin compiler runs in-process and the Gradle
# heap is bounded so the release builds do not exhaust small CI containers.
GRADLE_ARGS=(
  --no-daemon
  --max-workers=1
  -Pkotlin.compiler.execution.strategy=in-process
  -Dorg.gradle.jvmargs="-Xmx3g -XX:MaxMetaspaceSize=768m"
)

# CI checks this repository out at the pinned commit before running this script.
# For local builds, clone it on demand.
if [ ! -f "open-prayer-times/settings.gradle.kts" ]; then
  echo "Cloning open-prayer-times at ${OPEN_PRAYER_TIMES_REF}"
  git clone "$OPEN_PRAYER_TIMES_REPO" open-prayer-times
  git -C open-prayer-times checkout --detach "$OPEN_PRAYER_TIMES_REF"
fi

echo "Publish open-prayer-times to Maven local"
(cd open-prayer-times && bash gradlew publishToMavenLocal "${GRADLE_ARGS[@]}")

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

FLAVORS=("$@")
if [ ${#FLAVORS[@]} -eq 0 ]; then
  FLAVORS=(fdroid play)
fi

for flavor in "${FLAVORS[@]}"; do
  case "${flavor,,}" in
    fdroid)
      task=assembleFdroid
      label=F-Droid
      ;;
    play)
      task=assemblePlay
      label=Play
      ;;
    *)
      echo "Unknown flavor: ${flavor}" >&2
      exit 1
      ;;
  esac

  echo "Build ${label} flavor"
  bash ./gradlew "$task" "${GRADLE_ARGS[@]}"
done
