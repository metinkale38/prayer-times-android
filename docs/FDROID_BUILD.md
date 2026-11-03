# Building for F-Droid

## Prerequisites

- JDK 17
- Android SDK with platform 37 and build-tools 36.0.0 or newer
- Git (for the `open-prayer-times` submodule)
- No local Maven repository is required

## Checkout

```bash
git clone --recurse-submodules https://github.com/metinkale38/prayer-times-android.git
cd prayer-times-android
```

If the repository was cloned without submodules:

```bash
git submodule update --init --recursive
```

The submodule is pinned to a fixed `open-prayer-times` commit. Do not replace it
with `origin/main` during a release build.

## Secrets

`app/src/main/res/values/secrets.xml` is intentionally not checked in. The
build creates it automatically when it is missing. For local release builds
that need the Play flavor, create it with real values:

```xml
<resources>
  <string name="GOOGLE_API_KEY">YOUR_API_KEY</string>
  <string name="IGMG_API_KEY"></string>
  <string name="LONDON_PRAYER_TIMES_API_KEY"></string>
  <string name="SENTRY_DSN">YOUR_SENTRY_DSN</string>
</resources>
```

The F-Droid flavor does not need a Google API key or a Sentry DSN. Empty values
are sufficient.

## Build commands

```bash
./gradlew :app:assembleFdroidRelease
```

Output:

```text
app/build/outputs/apk/fdroid/release/app-fdroid-release-unsigned.apk
```

For the Play flavor:

```bash
./gradlew :app:assemblePlayRelease
```

The release APKs are unsigned. F-Droid signs the F-Droid APK with its own key.

## Low-memory / CI builds

Some CI containers limit the whole cgroup to 8 GB. Building debug and release
variants for both flavors in one Gradle invocation can exceed that limit,
especially because Gradle and the Kotlin compiler run in separate JVMs by
default. Use a bounded heap, one worker, and the in-process Kotlin compiler:

```bash
./gradlew :app:assembleFdroidRelease \
  --no-daemon \
  --max-workers=1 \
  -Pkotlin.compiler.execution.strategy=in-process \
  -Dorg.gradle.jvmargs="-Xmx3g -XX:MaxMetaspaceSize=768m"
```

Build one flavor at a time. `--no-daemon` still starts a single-use Gradle
daemon, but it exits after the build. The in-process Kotlin setting prevents a
separate Kotlin daemon from surviving the build.

`ci_build.sh` follows this strategy and builds the F-Droid and Play flavors in
separate invocations.

## F-Droid build recipe

The draft metadata in `metadata/com.metinkale.prayer.yml` uses:

- `submodules: true`
- `gradle: [fdroidRelease]`
- a `prebuild` step that creates `secrets.xml` with empty values

F-Droid builds the release variant with its own signing key and does not use
the `play` flavor.

## Reproducibility

The build uses the Gradle wrapper (9.6.1), Android Gradle Plugin 9.3.1, Kotlin
2.4.10, and Java 17. The `open-prayer-times` submodule is pinned. Avoid
`mavenLocal()` and avoid resolving SNAPSHOT artifacts from a mutable local
repository.

A full `fdroidserver` build was not run in this environment. Run
`fdroid build com.metinkale.prayer` in an `fdroiddata` checkout before
submitting the metadata.
