---
name: release
description: Prepare and ship a HuKi release — bump the platform's version in `version.properties`, write its WhatsNew notes (EN + HU), update fastlane store content, verify, tag `ios/v<version>` and hand off to TestFlight / App Store review. Use when cutting a release, bumping the app version, adding WhatsNew / release notes, or running fastlane for store content or TestFlight builds.
---

# Release

`version.properties` (repo root) is the single source of truth for the versions — fastlane reads it, never writes it.

Each platform has its own version line, so a platform-only release (e.g. an iOS-only feature) bumps only that platform:
- Android: `androidAppVersion` + `androidBuildNumber` (the Play `versionCode`)
- iOS: `iosAppVersion` + `iosBuildNumber`

## WhatsNew

Release content lives under `tools/release/whatsnew/<platform>/v<version>/` (`android` / `ios`), matching that platform's version in `version.properties`:
- `whatsnew-en-US.md` (required) and `whatsnew-hu-HU.md` — one release-note bullet per line.
- `metadata.json` — `releaseDate` (ISO `yyyy-MM-dd`) and an optional per-locale `message` (`title`/`body`) for the personal message card.

How it works:
- **Generation**: the `generateWhatsNew` Gradle task (in `:shared`, runs before compile) generates `WhatsNewContent` (a common `expect`, plus an `actual` per platform holding only that platform's releases and `currentVersion`) and the moko `strings_whatsnew.xml` files (keys `whatsnew_<platform>_v<version>`). All generated — never edit them by hand.
- **Show-once**: the sheet auto-shows on the first launch after an update. `DefaultWhatsNewRepository` compares `currentVersion` against the `WHATS_NEW_LAST_SEEN_VERSION` DataStore key.
- **Debugging**: `FeatureFlags.ALWAYS_SHOW_WHATSNEW` forces the sheet (keep `false` on commit).
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
- **Tagging convention**: `ios/v<iosAppVersion>` (Android, once its release pipeline exists: `android/v<androidAppVersion>`).

Never commit, tag or push without the user's go-ahead — leave the release commit for review first.
