# Open Questions and Blockers

These items need a maintainer decision or an upstream change before the F-Droid
metadata is submitted.

## 1. `open-prayer-times` has no license file (blocker)

`https://github.com/metinkale38/open-prayer-times` has no top-level `LICENSE`,
`COPYING`, or `NOTICE` file. The app is Apache-2.0, but the separately published
library source does not declare a license. F-Droid requires a clear license for
all source that is built into the APK.

Options:

- Add an explicit license to `open-prayer-times` (must be compatible with
  Apache-2.0 and with the PrayTimes port).
- Replace the library with an in-repo, clearly licensed implementation.
- Do not submit until the license question is resolved.

This repository cannot fix this without modifying `open-prayer-times`, which is
out of scope.

## 2. PrayTimes port license (blocker)

`open-prayer-times/calctimes/NOTICE.txt` contains only:

```text
This module is a kotlin port of http://praytimes.org/
```

The PrayTimes project changed licenses over time:

- v1: GPL-3.0
- v2: LGPL-3.0
- v3: MIT

The port does not state which version it is based on. If it is based on v1 or
v2, the current Apache-2.0 app license may be incompatible. This needs a
license review and an explicit attribution/license in `open-prayer-times`.

## 3. Screenshots and feature graphic (screenshots resolved)

Eight phone screenshots from the Play Store listing are stored in:

```text
fastlane/metadata/android/en-US/images/phoneScreenshots/01.png
...
fastlane/metadata/android/en-US/images/phoneScreenshots/08.png
```

A feature graphic is optional and is not included.

## 4. TetheredNet anti-feature

The server URL `https://opt.mk38.de` is hard-coded in `open-prayer-times`.
Users cannot point the app at another server. F-Droid will likely require the
`TetheredNet` anti-feature. Removing it would require an upstream change in
`open-prayer-times` (configurable base URL).

## 5. Unused API keys

`IGMG_API_KEY` and `LONDON_PRAYER_TIMES_API_KEY` are still declared in the
secrets template and `BUILD_INSTRUCTIONS.md`, but the current code paths use
the central server instead. Confirm whether they can be removed from the
secrets template and documentation.

## 6. Signing and reproducible builds

- Release APKs are unsigned. F-Droid signs its own build.
- No `AllowedAPKSigningKeys` value exists yet. Add the F-Droid signing key
  fingerprints to the metadata after the first reproducible build.
- A full `fdroidserver` reproducibility check was not possible in this
  environment.

## 7. Metadata commit after merge

The draft metadata must reference a commit that contains the F-Droid build
changes. If this branch is squash-merged, update the `commit:` field in
`metadata/com.metinkale.prayer.yml` to the merge commit on `master` before
opening the `fdroiddata` PR.

## 8. SDK and toolchain availability on the F-Droid buildserver

The app compiles with `compileSdk 37`, `targetSdk 37`, AGP 9.3.1, Gradle 9.6.1,
and Kotlin 2.4.10. Confirm that the F-Droid buildserver has the matching Android
platform and a supported JDK before submission. If not, the build may need a
newer buildserver image or a temporary SDK downgrade.

## 9. Version code / release process

F-Droid uses `versionCode 257` and `versionName 3.9.0`. Future releases must
update both values in `app/build.gradle` and add a matching fastlane changelog.
