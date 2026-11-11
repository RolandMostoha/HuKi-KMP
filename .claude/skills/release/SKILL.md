---
name: release
description: Prepare and ship a HuKi release — bump the platform's version in `version.properties`, write its WhatsNew notes (EN + HU), update fastlane store content, verify, tag `ios/v<version>` / `android/v<version>` and hand off to TestFlight / App Store review or the Google Play internal track / production. Use when cutting a release, bumping the app version, adding WhatsNew / release notes, signing the Android release, or running fastlane for store content, TestFlight or Google Play builds.
---

# Release

`version.properties` (repo root) is the single source of truth for the versions — fastlane reads it, never writes it.

Each platform has its own version line, so a platform-only release (e.g. an iOS-only feature) bumps only that platform:
- Android: `androidAppVersion` + `androidBuildNumber` (the Play `versionCode`)
- iOS: `iosAppVersion` + `iosBuildNumber`

## WhatsNew

Release content lives under `tools/release/whatsnew/<platform>/v<version>/` (`android` / `ios`), matching that platform's version in `version.properties`:
- `whatsnew-en-US.md` (required) and `whatsnew-hu-HU.md` — one release-note bullet per line.
- `metadata.json` — `releaseDate` (ISO `yyyy-MM-dd`) and an optional per-locale `message` (`title`/`body`, optional `footer` shown as "— <footer>" in italics, omitted when missing) for the personal message card.

How it works:
- **Generation**: the `generateWhatsNew` Gradle task (in `:shared`, runs before compile) generates `WhatsNewContent` (a common `expect`, plus an `actual` per platform holding only that platform's releases and `currentVersion`) and the moko `strings_whatsnew.xml` files (keys `whatsnew_<platform>_v<version>`). All generated — never edit them by hand.
- **Show-once**: the sheet auto-shows on the first launch after an update. `DefaultWhatsNewRepository` compares `currentVersion` against the `WHATS_NEW_LAST_SEEN_VERSION` DataStore key.
- **Debugging**: `FeatureFlags.DEBUG_ALWAYS_SHOW_WHATSNEW` forces the sheet (keep `false` on commit).
- **E2E**: the sheet auto-shows on a clean `clearState` launch, so every Maestro flow except `maestro_whats_new.yaml` passes `arguments: { skipWhatsNew: true }` to `launchApp` (`AppLaunchConfig.skipWhatsNew`, read from the intent extra on Android and `ProcessInfo.arguments` on iOS).

## Runbook (iOS)

Store content lives in `iosApp/fastlane/` (metadata, screenshots, review notes) and is delivered by fastlane.

1. One release commit on `main` (`release(v<X>-iOS): update store content, versioning`):
    - `version.properties` — bump `iosAppVersion` and `iosBuildNumber`
    - `tools/release/whatsnew/ios/v<X>/` — EN + HU notes and `metadata.json`
    - `iosApp/fastlane/metadata/` — (if changed) description, promo text, keywords
    - `iosApp/fastlane/screenshots/` — (if changed)
    - `tools/screenshots/` — (if changed) raw captures, `SCREENSHOTS.md` and the AppMockUp `.mockup` design sources, so the store assets and their sources stay in one commit
2. `bundle exec fastlane verify` — read-only; checks App Store Connect agrees with `version.properties`.
3. Tag that commit: `git tag ios/v<X> && git push origin ios/v<X>`.
4. The tag runs `.github/workflows/github-workflow-ios-release.yml`: uploads to TestFlight and stages all store content. Nothing is submitted for review.
5. Test the TestFlight build, then in App Store Connect select the build and press **Add for Review**.

Other paths:
- Ad-hoc internal TestFlight builds: `bundle exec fastlane beta` locally (never reviewed).
- Store content only (build already on TestFlight): run the **iOS Release** workflow manually on `main` with `metadata_only` checked.

## Runbook (Android)

Store content lives in `composeApp/fastlane/metadata/android/<locale>/` (`en-US`, `hu-HU`) and is delivered by fastlane
(run from `composeApp/`): `title.txt` (30 chars), `short_description.txt` (80), `full_description.txt` (4000),
`images/` (`icon.png`, `featureGraphic.png`, `phoneScreenshots/`). Release notes are generated, never edited:
`generateAndroidStoreReleaseNotes` renders `tools/release/whatsnew/android/v<X>/` into the gitignored
`changelogs/default.txt` (Play limit: 500 chars).

Play has one store listing for every track, so the listing is only sent for review with the production release
(step 6): an earlier review would show the new listing while production still serves the previous version. Managed
publishing is off, an approved release goes live right away.

1. One release commit on `main` (`release(v<X>-Android): update store content, versioning`):
    - `version.properties` — bump `androidAppVersion` and `androidBuildNumber` (the Play `versionCode`, must be higher than
      any code ever uploaded; legacy HuKi-Android ended at 688)
    - `tools/release/whatsnew/android/v<X>/` — EN + HU notes and `metadata.json`
    - `composeApp/fastlane/metadata/android/` — (if changed) listing text, screenshots, feature graphic
2. `bundle exec fastlane verify` — read-only; checks the service account key and that `androidBuildNumber` is unused on Play.
3. Tag that commit: `git tag android/v<X> && git push origin android/v<X>`. The tag runs
   `.github/workflows/github-workflow-android-release.yml` → `fastlane beta`: signed `bundleRelease`, upload to the
   **internal** track with the release notes and `mapping.txt`, then the Crashlytics native symbols. The listing is
   not touched.
4. Test the internal build on a device (and the Play Console pre-launch report).
5. `bundle exec fastlane listing`: stages the listing (`changes_not_sent_for_review`, only changed images) without
   sending it for review. Check the preview in Play Console.
6. Run the **Android Release** workflow manually on `main` with track `production` (fastlane `release`): promotes that
   `versionCode` from internal to production **and** uploads the listing (`sync_image_upload`, only changed images) in
   one Play edit, so Google reviews both together. After approval it goes live to every user (no staged rollout).
   Don't use Play Console's "Promote release" button, it doesn't upload the listing.

Signing:
- The release must be signed with the legacy HuKi-Android **upload key** (SHA-256 `C6:39:0A:16:…:E9:94`), Play rejects
  anything else. `beta` checks the certificate before uploading.
- Gradle reads `ANDROID_KEYSTORE_PATH`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD` from env
  vars, falling back to `local.properties` (gitignored). Without them `bundleRelease` stays unsigned.
- CI secrets: `ANDROID_UPLOAD_KEYSTORE_BASE64` (decoded to a temp file), the three values above and
  `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON`. Locally fastlane reads the key from `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON_PATH`, set in
  the gitignored `composeApp/fastlane/.env`.

Other paths:
- Local upload: `bundle exec fastlane beta` with the signing values and the service account path set.
- Dry runs: `bundle exec fastlane beta validate_only:true` builds and lets Play validate the upload without publishing
  (skips the Crashlytics mapping and symbol uploads); `bundle exec fastlane release validate_only:true` validates the
  promotion and the listing; `bundle exec fastlane listing validate_only:true` validates the listing alone.
- If `release` refuses to commit while the staged listing waits, send both for review from Play Console's
  "Publishing overview".
- Refresh the listing baseline from Play: `bundle exec fastlane supply init` (fails if `metadata/android` exists; move it
  aside first, then delete the downloaded `changelogs/`).

## Tagging convention

`ios/v<iosAppVersion>` and `android/v<androidAppVersion>`; move the tag to re-release the same version.

Never commit, tag or push without the user's go-ahead — leave the release commit for review first.
