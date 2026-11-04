# How to build this project

## Requirements

- JDK 17
- Android SDK platform 37 and build-tools 36.0.0 or newer
- Git

## Checkout

```bash
git clone https://github.com/metinkale38/prayer-times-android.git
cd prayer-times-android
```

`open-prayer-times` is not a git submodule. Check it out next to the app at the
pinned commit and publish it to the local Maven repository:

```bash
git clone https://github.com/metinkale38/open-prayer-times.git open-prayer-times
git -C open-prayer-times checkout --detach f44a7ed8fbf68ef9416eb4c487df8ea940adfed3
(cd open-prayer-times && bash ./gradlew publishToMavenLocal)
```

The app resolves `dev.metinkale:open-prayer-times-client:1.0-SNAPSHOT`,
`dev.metinkale:calctimes:1.0-SNAPSHOT`, and `dev.metinkale:hijri:1.0-SNAPSHOT`
from `mavenLocal()`. `ci_build.sh` performs the clone and publish step
automatically when `open-prayer-times` is missing.

## secrets.xml

Create `app/src/main/res/values/secrets.xml` with the following content:

```xml
<resources>
    <string name="GOOGLE_API_KEY">YOUR_API_KEY</string>
    <string name="SENTRY_DSN">YOUR_SENTRY_DSN</string>
</resources>
```

The CI creates this file with empty values when it is missing. The F-Droid
flavor does not need a Google API key or a Sentry DSN; empty values are enough.

1. `GOOGLE_API_KEY` can be obtained through Google Cloud Platform. It is only
   used by the Play flavor.
2. `SENTRY_DSN` is only used by the Play flavor.

## Build

```bash
# F-Droid flavor
bash ./gradlew :app:assembleFdroidRelease

# Play flavor
bash ./gradlew :app:assemblePlayRelease
```

For low-memory environments, build one flavor at a time:

```bash
bash ./gradlew :app:assembleFdroidRelease \
  --no-daemon \
  --max-workers=1 \
  -Pkotlin.compiler.execution.strategy=in-process \
  -Dorg.gradle.jvmargs="-Xmx3g -XX:MaxMetaspaceSize=768m"
```

## Notes

If you find more steps that have to be done, please create an issue or a pull
request.
