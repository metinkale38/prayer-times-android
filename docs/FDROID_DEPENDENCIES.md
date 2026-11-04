# Dependency and License Audit

This file records the dependency review for the `fdroid` flavor. It is not a
legal opinion. Confirm the upstream license files before an F-Droid submission.

## How the graph was checked

```bash
./gradlew :app:dependencies --configuration fdroidReleaseRuntimeClasspath
./gradlew :app:dependencies --configuration playReleaseRuntimeClasspath
```

The `fdroidReleaseRuntimeClasspath` contains no `com.google.android.gms:*`,
Firebase, Sentry, or other known proprietary runtime libraries.

## F-Droid flavor

### Permissive / FOSS libraries

| License | Libraries |
| --- | --- |
| Apache-2.0 | AndroidX (`androidx.*`), Jetpack Compose and Compose Multiplatform runtime, Material Components, Kotlin stdlib, kotlinx coroutines/datetime/io/serialization, Ktor client, ION and AndroidAsync, AboutLibraries, Gson, Guava `listenablefuture`, `error_prone_annotations`, JSpecify, Relinker |
| BSD-2-Clause | Flexmark and its `flexmark-util-*` modules |
| MIT | SLF4J API, android-gif-drawable |

### First-party source

| Module | License |
| --- | --- |
| `open-prayer-times/client` | Apache-2.0 |
| `open-prayer-times/calctimes` | LGPL-3.0, with `NOTICE.txt` attributing PrayTimes.js 2.3 (LGPL-3.0) |
| `open-prayer-times/hijri` | Apache-2.0 |
| `open-prayer-times/diyanet-client` | Apache-2.0 |
| `open-prayer-times/server` | Apache-2.0 |
| `prayer-times-android` app | Apache-2.0 (`LICENSE`) |

The overall F-Droid license is `LGPL-3.0-only` because the app combines
Apache-2.0 and LGPL-3.0 source. The license files and POM metadata are added by
`open-prayer-times` PR #3; the build pins the license commit
`2891167b2896c17e98145b7e8258ed3c7a213eeb`.

### Removed or avoided dependencies

| Dependency | Status |
| --- | --- |
| `com.google.gms:google-services` Gradle plugin | Removed. The app does not use Firebase and does not need `google-services.json`. |
| `com.google.android.gms:play-services-gcm` | Removed. The app did not use it. |
| `org.jetbrains.kotlin:kotlin-reflect` | Removed. The app did not use reflection directly. |
| `androidx.legacy:legacy-preference-v14` | Replaced with `androidx.preference:preference:1.2.1`. |
| `androidx.documentfile:documentfile` | Removed as a direct dependency; a transitive 1.0.0 copy remains. |
| `androidx.compose.ui:ui-tooling` | Removed from debug builds; no Compose previews are used. |
| `io.ktor:ktor-client-logging` | Removed. The app does not install Ktor logging. |
| `https://jitpack.io` | Removed. No dependency required it. |

`mavenLocal()` is required for the locally published `open-prayer-times`
`1.0-SNAPSHOT` artifacts. It is populated by the pinned checkout before the app
build; no remote snapshot repository is used.

## Play flavor only

These are deliberately kept out of the `fdroid` flavor:

| Dependency | License | Purpose |
| --- | --- | --- |
| `com.google.android.gms:play-services-maps` | Proprietary | Google Maps in the Qibla map screen |
| `com.google.android.gms:play-services-location` | Proprietary | Fused location in the Play map screen |
| `io.sentry:sentry-android` | MIT | Crash reporting |

The `play` flavor still uses the Google Maps API key from `secrets.xml`. The
`fdroid` flavor uses the non-Google map placeholder in
`app/src/fdroid/java/com/metinkale/prayer/compass/FragMap.java`.

## Notes for F-Droid reviewers

- The app does not use Firebase.
- The app does not use Google Play Services in the `fdroid` flavor.
- Sentry is not present in the `fdroid` flavor.
- The `fdroid` release APK is built from `fdroidRelease` and is unsigned.
- `calctimes` is LGPL-3.0; the full LGPL-3.0 and GPL-3.0 texts and the
  PrayTimes.js attribution are included in the `open-prayer-times` source.
