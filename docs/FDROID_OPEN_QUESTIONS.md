# Open Questions and Blockers

These items need a maintainer decision or an upstream change before the F-Droid
metadata is submitted.

## 1. Licensing (resolved upstream)

`open-prayer-times` PR #3 adds the missing license files and POM metadata:

- Apache-2.0 for the root, `client`, `hijri`, `diyanet-client`, and `server`.
- LGPL-3.0 for `calctimes`.
- Full LGPL-3.0 and GPL-3.0 texts for `calctimes`.
- PrayTimes.js 2.3 attribution in `calctimes/NOTICE.txt`.

The build pins `2891167b2896c17e98145b7e8258ed3c7a213eeb`, which contains these
files. The overall F-Droid metadata license is `LGPL-3.0-only`. Merge PR #3
before submission and update the pin if the merge commit differs.

## 2. PrayTimes port license (resolved)

`open-prayer-times/calctimes/NOTICE.txt` now states:

```text
This module is a Kotlin port of PrayTimes.js 2.3.
PrayTimes.js: Prayer Times Calculator (ver 2.3)
Copyright (C) 2007-2011 PrayTimes.org
License: GNU LGPL v3.0
```

The module is licensed under LGPL-3.0 and includes the GPL-3.0 terms
incorporated by reference.

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
