# F-Droid Readiness Audit

Status: preparation only. No submission to F-Droid has been made.

## Application

| Item | Value |
| --- | --- |
| Application ID | `com.metinkale.prayer` |
| Name | Prayer Times |
| Version name | `3.9.0` (`3.9.0 Play Store` for the Play flavor) |
| Version code | `257` |
| Minimum SDK | 26 |
| Target / compile SDK | 37 |
| License | App Apache-2.0; bundled `calctimes` LGPL-3.0; F-Droid metadata `LGPL-3.0-only` |
| Source | https://github.com/metinkale38/prayer-times-android |
| Issue tracker | https://github.com/metinkale38/prayer-times-android/issues |
| Translations | https://crowdin.com/project/prayer-times-android |

## Flavors

The project has two product flavors:

- `fdroid`: FOSS-only build for F-Droid. It does not depend on Google Play
  Services, Firebase, Sentry, or any other proprietary runtime library.
- `play`: Play Store build. It keeps the existing proprietary dependencies
  (Google Play Services Maps/Location and Sentry crash reporting).

See `FDROID_FLAVORS.md` for the complete difference list.

## Build changes made for F-Droid

- `open-prayer-times` is no longer a git submodule. CI and `ci_build.sh` clone
  it at a pinned commit, run `publishToMavenLocal`, and the app consumes the
  `dev.metinkale:*:1.0-SNAPSHOT` artifacts from `mavenLocal()`.
- `settings.gradle` includes only `:app` and resolves Maven local before
  `google()` and `mavenCentral()`.
- `jitpack.io` was removed from the repository list. No dependency required it.
- The `com.google.gms.google-services` Gradle plugin was removed. The app does
  not use Firebase; the plugin only required an unnecessary
  `google-services.json`.
- `io.sentry:sentry-android` is now `playImplementation`.
- `CrashReporter` is flavor-specific:
  - `app/src/play/java/com/metinkale/prayer/CrashReporter.kt` uses Sentry.
  - `app/src/fdroid/java/com/metinkale/prayer/CrashReporter.kt` is a no-op.
- Sentry manifest metadata moved from the main manifest to the Play manifest.
- The Google Advertising ID removal declaration moved to the Play manifest,
  where Google Play Services is actually present.
- CI builds each flavor in a separate matrix job with bounded memory, so an
  F-Droid failure cannot hide a Play failure.

## FOSS dependency status

The `fdroidReleaseRuntimeClasspath` contains only libraries under permissive
licenses:

- Apache-2.0: AndroidX, Jetpack Compose, Material Components, Kotlin,
  kotlinx, Ktor, ION/AndroidAsync, AboutLibraries, Gson, Guava
  `listenablefuture`, `error_prone_annotations`, JSpecify, Relinker.
- BSD-2-Clause: Flexmark.
- MIT: SLF4J API, android-gif-drawable.
- Apache-2.0: `open-prayer-times` root, `client`, `hijri`, `diyanet-client`, and
  `server` modules.
- LGPL-3.0: `open-prayer-times/calctimes`, a Kotlin port of PrayTimes.js 2.3
  (LGPL-3.0) with the upstream NOTICE and full LGPL/GPL texts.

The `fdroidReleaseRuntimeClasspath` does **not** contain
`com.google.android.gms:*`, Firebase, Sentry, or other proprietary libraries.

See `FDROID_DEPENDENCIES.md` for details.

## Anti-features

### TetheredNet (likely)

The app uses a developer-hosted service at `https://opt.mk38.de` for city
search and for prayer times from providers other than the offline calculation
providers. The server source is part of `open-prayer-times`, but the server
URL is compiled into the library and is not user-configurable. The app also
downloads sounds and hadith databases from
`https://metinkale38.github.io/prayer-times-android`.

F-Droid may therefore classify the app as `TetheredNet`. This does not block
inclusion, but it must be declared in the F-Droid metadata. Making the server
URL configurable would remove the anti-feature, but it would require changes in
`open-prayer-times`, which is outside this repository.

No other anti-features were found: there are no ads, no tracking SDKs, no
non-free assets, and no proprietary dependencies in the `fdroid` flavor.

## Network access

| Endpoint | Purpose | Flavor |
| --- | --- | --- |
| `https://opt.mk38.de` | City search and non-calculated prayer times | both |
| `https://metinkale38.github.io/prayer-times-android` | Sound catalog, sound files, hadith databases | both |
| Sentry DSN (configured at build time) | Crash reports | Play only |

Location data is used locally and is sent to the city-search service when the
user enables automatic location. See `FDROID_PRIVACY.md`.

## Permissions

The app requests location, exact alarm, boot, notification, foreground service,
wake lock, network, vibration, keyguard, notification-policy, and shortcut
permissions. All are tied to user-visible features. There is no request for
the advertising ID in the F-Droid flavor.

## Remaining submission items

1. **Declare the `TetheredNet` anti-feature** in the F-Droid metadata.
2. **Confirm buildserver toolchain support** for compile/target SDK 37, AGP
   9.3.1, Gradle 9.6.1, Kotlin 2.4.10, and JDK 17.
3. **Feature graphic is optional.** Eight Play Store phone screenshots are
   included in `fastlane/metadata/android/en-US/images/phoneScreenshots/`.

## Reproducible build notes

- Gradle wrapper: 9.6.1
- Android Gradle Plugin: 9.3.1
- Kotlin: 2.4.10
- Java: 17
- The `open-prayer-times` commit is pinned in `ci_build.sh`, the CI workflow,
  and the F-Droid metadata prebuild step.
- Release APKs are unsigned by the project; F-Droid signs them with its own key.
- A full `fdroidserver` reproducibility check was not possible in this
  environment because `fdroidserver` and Docker/Podman are not installed.
  The Gradle build itself was verified for `fdroidDebug`, `fdroidRelease`,
  `playDebug`, and `playRelease`.
- Run lint with a supported JDK (for example JDK 21) before submission. Android
  lint crashes on JDK 17 with Kotlin 2.4.10 in `BidirectionalTextDetector` and
  `findFirCompiledSymbol`; these are tooling issues, not app findings.
