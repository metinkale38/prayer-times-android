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

No anti-features are declared.

The city-search and non-calculated prayer-time service defaults to
`https://opt.mk38.de`, but the URL is user-configurable in the app settings.
`open-prayer-times` PR #4 added the runtime configuration API and the app
exposes it as "Server URL". The optional sound and hadith downloads still use
`https://metinkale38.github.io/prayer-times-android`; they are not required for
the core prayer-time functionality.

There are no ads, tracking SDKs, non-free assets, or proprietary dependencies
in the `fdroid` flavor.

## Network access

| Endpoint | Purpose | Flavor |
| --- | --- | --- |
| Configurable server URL, default `https://opt.mk38.de` | City search and non-calculated prayer times | both |
| `https://metinkale38.github.io/prayer-times-android` | Optional sound catalog, sound files, hadith databases | both |
| Sentry DSN (configured at build time) | Crash reports | Play only |

Location data is used locally and is sent to the city-search service when the
user enables automatic location. See `FDROID_PRIVACY.md`.

## Permissions

The app requests location, exact alarm, boot, notification, foreground service,
wake lock, network, vibration, keyguard, notification-policy, and shortcut
permissions. All are tied to user-visible features. There is no request for
the advertising ID in the F-Droid flavor.

## Docker test build

The fdroiddata recipe was tested in the prepared Docker/SSH environment on
2026-11-04:

- `fdroid lint -f com.metinkale.prayer` passed.
- `fdroid build --verbose com.metinkale.prayer` succeeded from app commit
  `dfa93eb1a5da015334a3388afc6af5925db7b381`.
- APK: `app-fdroid-release-unsigned.apk`, `6.5 MB`, SHA-256
  `86babcf7707f32b8a95cf29dba6594a2875b01ae40520996183ed64d402f9ca7`.
- Package `com.metinkale.prayer`, versionCode `257`, versionName `3.9.0`,
  minSdk `26`, target/compileSdk `37`.
- No GMS, Firebase, Sentry, or advertising-ID permission was found. No
  proprietary packages are defined in the DEX.
- A class-name string reference to
  `com.google.android.gms.security.ProviderInstaller` remains in the DEX, but
  no GMS classes are defined and no GMS dependency is on the runtime classpath.
- The APK is unsigned as expected; F-Droid signs it.

This is a Docker test build, not the official F-Droid buildserver VM.

## Remaining submission items

1. **Confirm buildserver toolchain support** for compile/target SDK 37, AGP
   9.3.1, Gradle 9.6.1, Kotlin 2.4.10, and JDK 17.
2. **Run official reproducibility checks** and add `AllowedAPKSigningKeys`
   after the first successful F-Droid build.
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
- A Docker `fdroid build` succeeded; official F-Droid reproducibility and
  signing-key verification are still pending.
- Run lint with a supported JDK (for example JDK 21) before submission. Android
  lint crashes on JDK 17 with Kotlin 2.4.10 in `BidirectionalTextDetector` and
  `findFirCompiledSymbol`; these are tooling issues, not app findings.
