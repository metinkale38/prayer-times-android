# How to build this project

## Requirements

- JDK 17
- Android SDK platform 37 and build-tools 36.0.0 or newer
- Git

## Checkout

```bash
git clone --recurse-submodules https://github.com/metinkale38/prayer-times-android.git
cd prayer-times-android
```

If the repository was cloned without submodules:

```bash
git submodule update --init --recursive
```

`open-prayer-times` is pinned as a git submodule. The `client`, `calctimes`, and
`hijri` modules are built directly from it. There is no `mavenLocal()` step and
no `publishToMavenLocal` step.

## secrets.xml

Create `app/src/main/res/values/secrets.xml` with the following content:

```xml
<resources>
    <string name="GOOGLE_API_KEY">YOUR_API_KEY</string>
    <string name="IGMG_API_KEY">YOUR_IGMG_KEY</string>
    <string name="LONDON_PRAYER_TIMES_API_KEY">YOUR_API_KEY</string>
    <string name="SENTRY_DSN">YOUR_SENTRY_DSN</string>
</resources>
```

The CI creates this file with empty values when it is missing. The F-Droid
flavor does not need a Google API key or a Sentry DSN; empty values are enough.

1. `GOOGLE_API_KEY` can be obtained through Google Cloud Platform. It is only
   used by the Play flavor.
2. `IGMG_API_KEY` cannot be officially obtained. Since 2023 Diyanet and IGMG
   have the same times, so it is not needed in practice.
3. `LONDON_PRAYER_TIMES_API_KEY` can be obtained from
   https://www.londonprayertimes.com/api/.
4. `SENTRY_DSN` is only used by the Play flavor.

## Build

```bash
# F-Droid flavor
./gradlew :app:assembleFdroidRelease

# Play flavor
./gradlew :app:assemblePlayRelease
```

For low-memory environments, build one flavor at a time:

```bash
./gradlew :app:assembleFdroidRelease \
  --no-daemon \
  --max-workers=1 \
  -Pkotlin.compiler.execution.strategy=in-process \
  -Dorg.gradle.jvmargs="-Xmx3g -XX:MaxMetaspaceSize=768m"
```

See `docs/FDROID_BUILD.md` for F-Droid-specific instructions and
`docs/FDROID_AUDIT.md` for the readiness audit.

## Notes

If you find more steps that have to be done, please create an issue or a pull
request.
