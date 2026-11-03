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

# checkout open-prayer-times and run publishToMavenLocal
if [ -d "open-prayer-times" ]; then
  echo "open-prayer-times exists, fetch latest"
  cd open-prayer-times
  git fetch origin
  git reset --hard origin/main
else
  echo "open-prayer-times does not exist, checkout"
  git clone https://github.com/metinkale38/open-prayer-times
  cd open-prayer-times
fi
chmod +x gradlew
append_gradle_property gradle.properties org.gradle.daemon false
./gradlew publishToMavenLocal --no-daemon
cd ..

# create secrets.xml
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

# remove com.google.gms build plugin (not needed for the fdroid flavor)
sed -i '/com\.google\.gms/s|^|//|' app/build.gradle build.gradle

# build project
echo "Build project"
append_gradle_property gradle.properties org.gradle.daemon false
append_gradle_property gradle.properties org.gradle.jvmargs "-Xmx2g -XX:MaxMetaspaceSize=512m"
chmod +x gradlew
./gradlew assembleFdroid --no-daemon
