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
| Apache-2.0 | AndroidX (`androidx.*`), Jetpack Compose and Compose Multiplatform runtime, Material Components, Kotlin stdlib/reflect, kotlinx coroutines/datetime/io/serialization, Ktor client, ION and AndroidAsync, AboutLibraries, Gson, Guava `listenablefuture`, `error_prone_annotations`, JSpecify, Relinker |
| BSD-2-Clause | Flexmark and its `flexmark-util-*` modules |
| MIT | SLF4J API, android-gif-drawable |

### First-party source

| Module | License status |
| --- | --- |
| `open-prayer-times/client` | No top-level license file in the repository. Needs an explicit license. |
| `open-prayer-times/calctimes` | No top-level license file. `calctimes/NOTICE.txt` says it is a Kotlin port of praytimes.org; the applicable PrayTimes version/license is not stated. |
| `open-prayer-times/hijri` | No top-level license file in the repository. Needs an explicit license. |
| `prayer-times-android` app | Apache-2.0 (`LICENSE`) |

This is the main licensing risk for F-Droid. The app's Apache-2.0 license does
not automatically cover the submodule. The submodule repository must declare a
license that is compatible with Apache-2.0 and with the PrayTimes port.

### Removed or avoided dependencies

| Dependency | Status |
| --- | --- |
| `com.google.gms:google-services` Gradle plugin | Removed. The app does not use Firebase and does not need `google-services.json`. |
| `mavenLocal()` | Removed. All dependencies are resolved from `google()` and `mavenCentral()` or built from the pinned submodule. |
| `https://jitpack.io` | Removed. No dependency required it. |

## Play flavor only

These are deliberately kept out of the `fdroid` flavor:

| Dependency | License | Purpose |
| --- | --- | --- |
| `com.google.android.gms:play-services-maps` | Proprietary | Google Maps in the Qibla map screen |
| `com.google.android.gms:play-services-location` | Proprietary | Fused location in the Play map screen |
| `com.google.android.gms:play-services-gcm` | Proprietary | Currently unused; retained to avoid changing Play behavior in this preparation |
| `io.sentry:sentry-android` | MIT | Crash reporting |

The `play` flavor still uses the Google Maps API key from `secrets.xml`. The
`fdroid` flavor uses the non-Google map placeholder in
`app/src/fdroid/java/com/metinkale/prayer/compass/FragMap.java`.

## Notes for F-Droid reviewers

- The app does not use Firebase.
- The app does not use Google Play Services in the `fdroid` flavor.
- Sentry is not present in the `fdroid` flavor.
- The `fdroid` release APK is built from `fdroidRelease` and is unsigned.
- The only non-permissive license question is the `open-prayer-times` source
  itself, not a binary dependency.
