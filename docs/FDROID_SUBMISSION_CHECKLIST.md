# F-Droid Submission Checklist

Status: not submitted.

## Source and build

- [x] FOSS `fdroid` flavor builds without Google Play Services or Sentry
- [x] Play flavor still builds with its proprietary dependencies
- [x] `open-prayer-times` removed as a git submodule
- [x] CI and `ci_build.sh` clone the pinned `open-prayer-times` checkout and run `publishToMavenLocal`
- [x] App consumes `dev.metinkale:*:1.0-SNAPSHOT` from `mavenLocal()`
- [x] `jitpack.io` removed
- [x] `google-services` plugin removed (no Firebase)
- [x] Sentry moved to the Play flavor
- [x] `CrashReporter` no-op in the F-Droid flavor
- [x] Unused direct dependencies removed and preference library updated
- [x] CI builds each flavor in a separate matrix job with bounded memory
- [x] `fdroidDebug`, `fdroidRelease`, `playDebug`, and `playRelease` verified
- [ ] `fdroidserver` build verified in the prepared Docker/SSH environment
- [ ] Reproducible-build comparison with the F-Droid signing key

## License

- [x] App license file present (Apache-2.0)
- [x] `open-prayer-times` root, `client`, `hijri`, `diyanet-client`, and `server` license files present (Apache-2.0)
- [x] `calctimes` license and PrayTimes.js attribution present (LGPL-3.0)
- [x] F-Droid metadata uses the combined LGPL-3.0-only license
- [x] `open-prayer-times` PR #3 merged and pinned ref updated
- [x] Dependency license audit documented

## Metadata and store listing

- [x] Final fdroiddata metadata and srclib prepared on `fdroid-prayer-times`
- [x] Fastlane title, short description, full description
- [x] Fastlane changelog for version 257
- [x] 512x512 icon copied to fastlane
- [x] Eight phone screenshots added from the Play Store listing
- [ ] Feature graphic added (optional)
- [ ] Localized fastlane metadata reviewed
- [x] Server URL configurable; no `TetheredNet` anti-feature declared
- [ ] `AllowedAPKSigningKeys` filled after first reproducible build

## Repository hygiene

- [x] No secrets committed
- [x] `secrets.xml` remains ignored
- [x] `open-prayer-times` checkout remains ignored
- [x] `open-prayer-times` commit pinned
- [x] `repomix-output.xml` removed from the working tree

## Submission

- [ ] Branch merged to `master`
- [ ] Metadata `commit:` updated to the merge commit
- [x] `fdroiddata` fork cloned and feature branch created
- [x] Metadata file added under `metadata/com.metinkale.prayer.yml`
- [x] `fdroid lint -f com.metinkale.prayer` passes
- [ ] `fdroid build com.metinkale.prayer` passes in the Docker/SSH environment
- [ ] Merge request opened against `fdroiddata`
