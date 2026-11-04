# Building for F-Droid

## Prerequisites

- JDK 17
- Android SDK with platform 37 and build-tools 36.0.0 or newer
- Git
- A local Maven repository populated from the pinned `open-prayer-times` checkout

## Checkout

```bash
git clone https://github.com/metinkale38/prayer-times-android.git
cd prayer-times-android
```

`open-prayer-times` is not a git submodule. Clone it next to the app at the
pinned commit and publish it to Maven local:

```bash
git clone https://github.com/metinkale38/open-prayer-times.git open-prayer-times
git -C open-prayer-times checkout --detach f44a7ed8fbf68ef9416eb4c487df8ea940adfed3
(cd open-prayer-times && bash ./gradlew publishToMavenLocal)
```

The app consumes the resulting `dev.metinkale:*:1.0-SNAPSHOT` artifacts from
`mavenLocal()`. Do not replace the pinned checkout with `origin/main` during a
release build. `ci_build.sh` performs the clone and publish step automatically
when `open-prayer-times` is missing.

## Secrets

`app/src/main/res/values/secrets.xml` is intentionally not checked in. The
build creates it automatically when it is missing. For local release builds
that need the Play flavor, create it with real values:

```xml
<resources>
  <string name="GOOGLE_API_KEY">YOUR_API_KEY</string>
  <string name="SENTRY_DSN">YOUR_SENTRY_DSN</string>
</resources>
```

The F-Droid flavor does not need a Google API key or a Sentry DSN. Empty values
are sufficient.

## Build commands

```bash
bash ./gradlew :app:assembleFdroidRelease
```

Output:

```text
app/build/outputs/apk/fdroid/release/app-fdroid-release-unsigned.apk
```

For the Play flavor:

```bash
bash ./gradlew :app:assemblePlayRelease
```

The release APKs are unsigned. F-Droid signs the F-Droid APK with its own key.

## Low-memory / CI builds

Some CI containers limit the whole cgroup to 8 GB. Building debug and release
variants for both flavors in one Gradle invocation can exceed that limit,
especially because Gradle and the Kotlin compiler run in separate JVMs by
default. Use a bounded heap, one worker, and the in-process Kotlin compiler:

```bash
bash ./gradlew :app:assembleFdroidRelease \
  --no-daemon \
  --max-workers=1 \
  -Pkotlin.compiler.execution.strategy=in-process \
  -Dorg.gradle.jvmargs="-Xmx3g -XX:MaxMetaspaceSize=768m"
```

Build one flavor at a time. `--no-daemon` still starts a single-use Gradle
daemon, but it exits after the build. The in-process Kotlin setting prevents a
separate Kotlin daemon from surviving the build.

`ci_build.sh` follows this strategy and builds the F-Droid and Play flavors in
separate invocations. Pass `fdroid` or `play` to build a single flavor; with no
argument it builds both.

## fdroiddata entry

The submission metadata lives in the personal fdroiddata fork on branch
`fdroid-prayer-times`:

```text
metadata/com.metinkale.prayer.yml
srclibs/open-prayer-times.yml
```

The app entry uses:

- `gradle: [fdroid]`
- `srclibs: open-prayer-times@f44a7ed8fbf68ef9416eb4c487df8ea940adfed3`
- a `build` step that publishes the srclib to Maven local before Gradle builds
  the app
- a `prebuild` step that creates `secrets.xml` with empty values

The app commit referenced by the entry is
`dfa93eb1a5da015334a3388afc6af5925db7b381`. Update it to the merge commit after
this branch is merged.

## Docker test environment

`/workspace/fdroidserver` contains a Docker Compose environment for the actual
test build:

- Debian with OpenJDK 17, Android SDK 37/build-tools 36.0.0, and fdroidserver
- SSH access on port 2234 as user `fdroid`
- persistent Gradle, Maven, Android, and F-Droid caches
- no Docker socket and no privileged container

The environment is prepared but has not been started. The practical
`fdroid build com.metinkale.prayer` test is intentionally deferred until the
user starts it and provides SSH access.

## Reproducibility

The build uses the Gradle wrapper (9.6.1), Android Gradle Plugin 9.3.1, Kotlin
2.4.10, and Java 17. The `open-prayer-times` checkout is pinned and published
locally before the app build. The `1.0-SNAPSHOT` version is resolved only from
that local publish; no remote snapshot repository is used.

`fdroid lint -f com.metinkale.prayer` passes. The official `fdroid build` and
reproducibility comparison remain to be run in the Docker/SSH environment or
on the official F-Droid buildserver.
