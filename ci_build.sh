#!/bin/bash

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
  chmod +x gradlew
fi
echo "org.gradle.daemon=false" >> gradle.properties
./gradlew publishToMavenLocal --no-daemon
cd ..

# create secrets.xml
if [ -d "/features/base/src/main/res/secrets.xml" ]; then
  echo "secrets.xml already exists, skip"
else
  echo 'Generate secrets.xml'
  echo '<resources>' > features/base/src/main/res/values/secrets.xml
  echo '  <string name="GOOGLE_API_KEY">YOUR_API_KEY</string>' >> features/base/src/main/res/values/secrets.xml
  echo '  <string name="IGMG_API_KEY">YOUR_IGMG_KEY</string>' >> features/base/src/main/res/values/secrets.xml
  echo '  <string name="LONDON_PRAYER_TIMES_API_KEY">YOUR_API_KEY</string>' >> features/base/src/main/res/values/secrets.xml
  echo '</resources>' >> features/base/src/main/res/values/secrets.xml
fi

# remove com.google.gms build plugin
sed -e '/com.google.gms/ s/^\/*/\/\//' -i app/build.gradle
sed -e '/com.google.gms/ s/^\/*/\/\//' -i build.gradle
sed -e '/crashlytics/ s/^\/*/\/\//' -i app/build.gradle
sed -e '/crashlytics/ s/^\/*/\/\//' -i build.gradle

# build project
echo "Build project"
echo "org.gradle.daemon=false" >> gradle.properties
chmod +x gradlew
./gradlew assembleFdroid --no-daemon
