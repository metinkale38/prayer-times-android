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
git -C open-prayer-times checkout --detach 0378851c10cc0a188908d93ea2dbb75fec6e9546
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
  <string name="IGMG_API_KEY"></string>
  <string name="LONDON_PRAYER_TIMES_API_KEY"></string>
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

## F-Droid build recipe

The draft metadata in `metadata/com.metinkale.prayer.yml` uses:

- `gradle: [fdroidRelease]`
- a `prebuild` step that clones `open-prayer-times` at the pinned commit, runs
  `publishToMavenLocal`, and creates `secrets.xml` with empty values

F-Droid builds the release variant with its own signing key and does not use
the `play` flavor.

## Reproducibility

The build uses the Gradle wrapper (9.6.1), Android Gradle Plugin 9.3.1, Kotlin
2.4.10, and Java 17. The `open-prayer-times` checkout is pinned and published
locally before the app build. The `1.0-SNAPSHOT` version is resolved only from
that local publish; no remote snapshot repository is used.

A full `fdroidserver` build was not run in this environment. Run
`fdroid build com.metinkale.prayer` in an `fdroiddata` checkout before
submitting the metadata.
