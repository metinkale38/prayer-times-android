# Flavor Differences

The app has one dimension, `store`, with two flavors.

| Area | `fdroid` | `play` |
| --- | --- | --- |
| Distribution | F-Droid | Google Play |
| Application ID | `com.metinkale.prayer` | `com.metinkale.prayer` |
| Version name | `3.9.0` | `3.9.0 Play Store` |
| Crash reporting | No-op `CrashReporter` | Sentry (`io.sentry:sentry-android`) |
| Sentry manifest metadata | Not present | Present in `app/src/play/AndroidManifest.xml` |
| Google Maps | Not used; placeholder map fragment | `play-services-maps` |
| Fused location | Not used; platform location manager | `play-services-location` |
| Google API key | Not required | `GOOGLE_API_KEY` in `secrets.xml` |
| Google Advertising ID removal | Not present (no GMS) | Declared in the Play manifest |
| `google-services` Gradle plugin | Not applied | Not applied (no Firebase) |
| Google Play Services dependency | None | Maps, Location, GCM (unused) |

## Source sets

- `app/src/main`: shared code and resources.
- `app/src/fdroid`: FOSS implementations. `CrashReporter` is a no-op and the
  map screen uses a non-Google placeholder.
- `app/src/play`: Sentry `CrashReporter` and the Google Maps/Location map
  screen.

## Build tasks

```bash
./gradlew :app:assembleFdroidDebug
./gradlew :app:assembleFdroidRelease
./gradlew :app:assemblePlayDebug
./gradlew :app:assemblePlayRelease
```

F-Droid builds only `fdroidRelease`.

## Version name note

The Play flavor adds the suffix ` Play Store` to the version name. The F-Droid
flavor keeps the plain version name, which is what the F-Droid metadata should
use.
