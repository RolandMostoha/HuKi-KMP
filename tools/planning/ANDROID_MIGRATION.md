# Android Migration: HuKi-Android (1.X) → HuKi-KMP (2.0)

## Goal

Ship HuKi-KMP to Google Play as an in-place update of HuKi-Android, so existing users upgrade to 2.0 without
reinstalling and keep their GPX files and purchases.

## Baseline

| Topic           | HuKi-Android (1.X)                                         | HuKi-KMP (today)                         |
|-----------------|------------------------------------------------------------|------------------------------------------|
| applicationId   | `hu.mostoha.mobile.android.huki`                           | `hu.mostoha.mobile.android.huki` ✅      |
| min / target    | 26 / 37                                                    | 26 / 37 ✅                               |
| versionCode     | git commit count (`v1.2.4` = **688**)                      | `androidBuildNumber=1` ❌                |
| versionName     | `git describe` (`1.2.4`)                                   | `appVersion=1.4` (shared with iOS)       |
| Release signing | CI: `r0adkll/sign-android-release` + `SIGNING_KEY` secrets | none ❌                                  |
| Play upload     | CI: `r0adkll/upload-google-play`, track `production`       | none ❌                                  |
| GPX files       | `filesDir/gpx/external`, `filesDir/gpx/routeplanner`       | same paths ✅                            |
| GPX history     | Room `gpx_history` table (`huki-database`)                 | `filesDir/gpx/metadata.json`             |
| Billing         | Play Billing, 4 products                                   | none ❌                                  |
| Map tiles       | OsmDroid cache, up to 800MB                                | Mapbox                                   |

## Out of scope

- Place history import (`place_history`), including favourites and comments
- Settings import (`user_preferences` DataStore)

Their legacy files are deleted by the cleanup step, so the data is lost on upgrade. This is intentional.

## Tasks

### 1. Versioning `[G]`

- [ ] Bump `androidBuildNumber` above the latest versionCode in Play Console (check the console, it is ≥ 688). Use
  `1000` as a clean start.
- [ ] Split `appVersion` per platform: Android `2.0`, iOS stays on its own line (next: `1.5`).
  - Update `version.properties`, `composeApp/build.gradle.kts`, the iOS build config, and the release scripts.
- [ ] WhatsNew per platform: a dedicated Android 2.0 message (see the `[G]` WhatsNew task in `PLANNING.md`).
  - Show it to upgraded users only, detected by the presence of legacy files (see task 4), checked before cleanup.

### 2. Release build `[G]`

- [x] Add `proguard-rules.pro` to `:composeApp` (release already has `isMinifyEnabled` and `isShrinkResources`).
  - Use HuKi-Android's `app/proguard-rules.pro` as the reference. Keep rules for kotlinx-serialization, Ktor, Koin,
    Room, Mapbox, Firebase.
- [ ] Build `bundleRelease` locally and smoke-test the minified build: map, search, GPX import, route planner, billing.
- [x] Confirm `google-services.json` for release points to the same Firebase project as legacy, so Analytics and
  Crashlytics history continue. Confirmed: same Firebase project as legacy, already live with iOS.

### 3. CI: sign and publish `[G]`

- [ ] New workflow `github-workflow-android-release.yml`, modelled on HuKi-Android's `github-workflow-deploy.yml`:
  - Trigger: manual (`workflow_dispatch`) or release tag, not every push to `main`.
  - Needs lint, unit tests, and the debug build to pass.
  - `./gradlew :composeApp:bundleRelease`
  - Sign with the **same upload key** as legacy: copy the secrets `SIGNING_KEY`, `ALIAS`, `KEY_STORE_PASSWORD`,
    `KEY_PASSWORD` to the HuKi-KMP repo.
  - Upload the AAB and `mapping.txt` as artifacts.
  - Publish with `GOOGLE_PLAY_PUBLISHER_SERVICE_ACCOUNT`, package `hu.mostoha.mobile.android.huki`. Start on the
    `internal` track, and pass `whatsNewDirectory` from `tools/release/whatsnew`.

### 4. GPX collection `[G]`

GPX files already carry over: both apps use `filesDir/gpx/external` and `filesDir/gpx/routeplanner`, and
`DefaultGpxStorage.listGpxFiles()` lists those directories.

- [ ] Verify on a device that legacy GPX files show up in KMP after upgrade, and that `GpxOrigin.fromPath` maps them
  to the right origin.
- [ ] Optional: import `last_opened` from legacy `gpx_history` into `metadata.json`, so the GPX list keeps its order.
  Skip it if the default ordering is good enough.

### 5. Purchases / Support `[G]`

Play keeps purchases server-side, so they restore when KMP queries purchases with the **same product IDs**:

| Type                 | Product ID                       | Legacy name |
|----------------------|----------------------------------|-------------|
| One-time (`INAPP`)   | `huki_support_one_time_level_1`  | Owl         |
| One-time (`INAPP`)   | `huki_support_one_time_level_2`  | Squirrel    |
| Subscription (`SUBS`)| `huki_support_recurring_level_1` | Boar        |
| Subscription (`SUBS`)| `huki_support_recurring_level_2` | Deer        |

- [ ] Support feature on Android with Play Billing, using the IDs above. This is a release blocker: active
  subscribers keep being charged, so 2.0 must let them see and manage their subscription.
- [ ] Restore purchases on startup via `queryPurchasesAsync`, without relying on the legacy local cache
  (`support_purchased_one_time_products`).
- [ ] Acknowledge unacknowledged purchases, matching legacy `BillingResponseHandler`.

### 6. Legacy cleanup `[G]`

A one-time `LegacyCleanup` in `androidMain`, guarded by a flag in the KMP DataStore.

**When / UX**: no migration screen, no loading bar. It only deletes a handful of files (OsmDroid's default
`SqlTileWriter` keeps tiles in a single `osmdroid/tiles/cache.db`), so it is expected to take well under a second.

- [ ] Launch from `HuKiApplication.onCreate` on an app-scoped coroutine (`Dispatchers.IO`), never blocking the first
  frame.
- [ ] Set the done flag only after it completes; deletes are idempotent, so a run killed midway simply repeats on the
  next start.
- [ ] Before deleting, check synchronously whether legacy files exist (cheap `exists()`) and keep the result as
  "upgraded from 1.X" for the 2.0 WhatsNew (task 1).

- [ ] Delete the Room DB: `getDatabasePath("huki-database")` plus `-wal` / `-shm` files.
- [ ] Delete the legacy DataStore: `filesDir/datastore/user_preferences.preferences_pb`.
- [ ] Delete the OsmDroid directory `osmdroid/` (including `tiles/`) from every candidate storage location. Legacy
  picks it with `StorageUtils.getBestWritableStorage`, which can be internal or external app storage.
- [ ] Delete the OsmDroid shared prefs `KEY_GLOBAL_SHARED_PREFERENCES`.
- [ ] Keep `filesDir/gpx/**` untouched.
- [ ] Log the freed size and failures to Crashlytics as non-fatal; never crash on cleanup.
- [ ] Unit or instrumentation test: seed legacy files, run cleanup, assert they are gone and GPX files remain.

### 7. Rollout

- [ ] Upgrade test on the internal track: install 1.2.4 from Play, add GPX files, buy a test product, then update to
  the signed 2.0 build.
  - Check: GPX files present, purchases restored, legacy files cleaned up, 2.0 WhatsNew shown once.
- [ ] Closed testing with a few real users.
- [ ] Production staged rollout: 10% → 50% → 100%, watching Crashlytics and Play vitals between steps.
- [ ] Archive HuKi-Android: freeze `master`, disable its deploy workflow.

## Open questions

- What is the latest versionCode in Play Console right now (any builds after `v1.2.4`)?
