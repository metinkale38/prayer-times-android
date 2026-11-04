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
- [ ] `fdroidserver` build verified in an `fdroiddata` checkout
- [ ] Reproducible-build comparison with the F-Droid signing key

## License

- [x] App license file present (Apache-2.0)
- [ ] `open-prayer-times` top-level license added (blocker)
- [ ] PrayTimes port license clarified (blocker)
- [x] Dependency license audit documented

## Metadata and store listing

- [x] Draft F-Droid metadata (`metadata/com.metinkale.prayer.yml`)
- [x] Fastlane title, short description, full description
- [x] Fastlane changelog for version 257
- [x] 512x512 icon copied to fastlane
- [x] Eight phone screenshots added from the Play Store listing
- [ ] Feature graphic added (optional)
- [ ] Localized fastlane metadata reviewed
- [ ] `TetheredNet` anti-feature confirmed by F-Droid reviewers
- [ ] `AllowedAPKSigningKeys` filled after first reproducible build

## Repository hygiene

- [x] No secrets committed
- [x] `secrets.xml` remains ignored
- [x] `open-prayer-times` checkout remains ignored
- [x] `open-prayer-times` commit pinned
- [ ] `repomix-output.xml` removed from the working tree (untracked local file)

## Submission

- [ ] Branch merged to `master`
- [ ] Metadata `commit:` updated to the merge commit
- [ ] `fdroiddata` fork created
- [ ] Metadata file added under `metadata/com.metinkale.prayer.yml`
- [ ] `fdroid lint com.metinkale.prayer` passes
- [ ] `fdroid build com.metinkale.prayer` passes
- [ ] Merge request opened against `fdroiddata`
