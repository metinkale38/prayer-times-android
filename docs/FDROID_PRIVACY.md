# Privacy Notes for the F-Droid Flavor

This is a technical summary for the F-Droid metadata and review. It is not a
replacement for the project's privacy policy.

Existing policy: `PRIVACY-POLICY.md`.

## Data stored on the device

- Prayer time settings, selected cities, calculation settings, alarm and
  notification settings are stored in `SharedPreferences`.
- A random UUID is generated once and stored in `SharedPreferences`. In the
  Play flavor it is used as the Sentry user identifier. In the F-Droid flavor
  `CrashReporter.setUserId` is a no-op, so the UUID is not transmitted.
- Downloaded sounds and hadith databases are stored in the app's private or
  external-files directories.
- Backups are created only when the user explicitly uses the backup feature.

## Location

The app requests `ACCESS_COARSE_LOCATION` and `ACCESS_FINE_LOCATION` for
automatic location and the Qibla compass. Location is used locally to calculate
prayer times and the Qibla direction.

When automatic location is enabled, the coordinates are sent to
`https://opt.mk38.de/search?lat=...&lng=...` to find matching cities and
providers. This happens for both flavors. The app does not request background
location.

The Play flavor also uses Google Play Services fused location in the map screen.
The F-Droid flavor does not use Google Play Services.

## Network requests

| Destination | Data | When |
| --- | --- | --- |
| `https://opt.mk38.de/search?lat=...&lng=...` | Coordinates | Automatic location |
| `https://opt.mk38.de/search?q=...` | Search text | Manual city search |
| `https://opt.mk38.de/<provider>/<key>/json` | Provider and city key | Selecting a non-calculated provider |
| `https://metinkale38.github.io/prayer-times-android/...` | None (static download) | Downloading sounds or hadith data |
| Sentry DSN | Crash reports, app version, device/OS info, user UUID, tags | Play flavor only |

The F-Droid flavor has no crash reporting and no analytics SDK. It does not
send the UUID.

## Permissions

| Permission | Purpose |
| --- | --- |
| `INTERNET`, `ACCESS_NETWORK_STATE` | City search, prayer time downloads, sound/hadith downloads |
| `ACCESS_COARSE_LOCATION`, `ACCESS_FINE_LOCATION` | Automatic location, Qibla compass |
| `POST_NOTIFICATIONS`, `VIBRATE`, `WAKE_LOCK` | Prayer notifications and alarms |
| `USE_EXACT_ALARM`, `SCHEDULE_EXACT_ALARM`, `SET_ALARM` | Exact prayer alarms |
| `RECEIVE_BOOT_COMPLETED` | Restore alarms after reboot |
| `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK`, `FOREGROUND_SERVICE_SPECIAL_USE` | Adhan playback and ongoing prayer-time service |
| `DISABLE_KEYGUARD` | Alarm screen over the lock screen |
| `ACCESS_NOTIFICATION_POLICY` | Silent mode around prayer times |
| `com.android.launcher.permission.INSTALL_SHORTCUT` | Home-screen shortcuts |

## Tracking and advertising

- No advertising SDK.
- No analytics SDK.
- No Firebase.
- No Google Play Services in the F-Droid flavor.
- No advertising ID permission in the F-Droid flavor.
- No Sentry in the F-Droid flavor.

## Third-party services and TetheredNet

The city search and non-calculated prayer times depend on a developer-hosted
server. The server source is in `open-prayer-times`, but the URL is not
configurable in the app. This is the reason the app may need the `TetheredNet`
anti-feature in F-Droid. The app can still calculate prayer times offline with
the built-in calculation methods, but city search and provider data require the
server.
