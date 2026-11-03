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
| License | Apache-2.0 (`LICENSE`) |
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

- `open-prayer-times` is now a pinned git submodule instead of a local Maven
  dependency. The `client`, `calctimes`, and `hijri` modules are included as
  Gradle projects.
- `mavenLocal()` was removed from `settings.gradle`.
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
- CI builds both flavors sequentially with bounded memory.

## FOSS dependency status

The `fdroidReleaseRuntimeClasspath` contains only libraries under permissive
licenses:

- Apache-2.0: AndroidX, Jetpack Compose, Material Components, Kotlin,
  kotlinx, Ktor, ION/AndroidAsync, AboutLibraries, Gson, Guava
  `listenablefuture`, `error_prone_annotations`, JSpecify, Relinker.
- BSD-2-Clause: Flexmark.
- MIT: SLF4J API, android-gif-drawable.
- Unknown: the three `open-prayer-times` modules. Their repository does not
  declare a license; see the blocker below.

The `fdroidReleaseRuntimeClasspath` does **not** contain
`com.google.android.gms:*`, Firebase, Sentry, or other proprietary libraries.

See `FDROID_DEPENDENCIES.md` for details and the upstream license question.

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

## Blockers before submission

1. **`open-prayer-times` has no top-level LICENSE file.** The main app is
   Apache-2.0, but the library repository does not declare a license. F-Droid
   needs a clear license for every included source repository. This must be
   fixed upstream, or the library must be relicensed/removed. This is a hard
   blocker.
2. **`open-prayer-times/calctimes/NOTICE.txt` says the module is a Kotlin port
   of praytimes.org.** The original PrayTimes code was GPL-3.0 (v1), then
   LGPL-3.0 (v2), and is MIT since v3. The port does not state which version it
   is based on. This needs a legal/license review and an explicit license and
   attribution in `open-prayer-times` before F-Droid submission.
3. **No screenshots or feature graphic are stored in the repository.** Fastlane
   metadata can only include the existing 512x512 icon. Screenshots must be
   added by the maintainer.

## Reproducible build notes

- Gradle wrapper: 9.6.1
- Android Gradle Plugin: 9.3.1
- Kotlin: 2.4.10
- Java: 17
- The submodule commit is pinned in `.gitmodules`/the gitlink.
- Release APKs are unsigned by the project; F-Droid signs them with its own key.
- A full `fdroidserver` reproducibility check was not possible in this
  environment because `fdroidserver` and Docker/Podman are not installed.
  The Gradle build itself was verified for `fdroidDebug`, `fdroidRelease`,
  `playDebug`, and `playRelease`.

## Verification performed

- `:app:assembleFdroidDebug`
- `:app:assembleFdroidRelease`
- `:app:assemblePlayDebug`
- `:app:assemblePlayRelease`
- `:app:testFdroidDebugUnitTest`
- Dependency graph inspection for `fdroidReleaseRuntimeClasspath`
- Merged F-Droid manifest inspection (no GMS, Firebase, Sentry, or AD_ID)

`lintFdroidRelease` could not complete because of an Android lint tooling
failure in this environment: the `BidirectionalTextDetector` crashes on JDK 17
(`List.removeLast()` is a newer JDK API), and the build-script analyzer crashes
with `findFirCompiledSymbol` when using Kotlin 2.4.10. These are lint/AGP
toolchain issues, not app findings. The app source compiles and the unit tests
pass. Run lint with a supported JDK (for example JDK 21) before submission.

The local container has an 8 GB cgroup memory limit. Release builds were run
sequentially with a bounded Gradle heap and the Kotlin compiler in-process; see
`FDROID_BUILD.md`.
