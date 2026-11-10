# Android Migration: HuKi-Android (1.X) → HuKi-KMP (2.0)

## Goal

Ship HuKi-KMP to Google Play as an in-place update of HuKi-Android, so existing users upgrade to 2.0 without
reinstalling and keep their GPX files and purchases.

## Baseline

| Topic           | HuKi-Android (1.X)                                         | HuKi-KMP (today)                         |
|-----------------|------------------------------------------------------------|------------------------------------------|
| applicationId   | `hu.mostoha.mobile.android.huki`                           | `hu.mostoha.mobile.android.huki` ✅      |
| min / target    | 26 / 37                                                    | 26 / 37 ✅                               |
| versionCode     | git commit count (`v1.2.4` = **688**)                      | `androidBuildNumber=1000` ✅             |
| versionName     | `git describe` (`1.2.4`)                                   | `androidAppVersion=2.0` ✅               |
| Release signing | CI: `r0adkll/sign-android-release` + `SIGNING_KEY` secrets | none ❌                                  |
| Play upload     | CI: `r0adkll/upload-google-play`, track `production`       | none ❌                                  |
| GPX files       | `filesDir/gpx/external`, `filesDir/gpx/routeplanner`       | same paths ✅                            |
| GPX history     | Room `gpx_history` table (`huki-database`)                 | `filesDir/gpx/metadata.json`             |
| Room DB         | `huki-database`                                            | `huki.db` (no name clash) ✅             |
| Billing         | Play Billing, 4 products                                   | Play Billing, 6 products ✅              |
| Map tiles       | OsmDroid cache, up to 800MB                                | Mapbox                                   |

## Migrated data

Only two things carry over from 1.X:

- **GPX files**: same sandbox directories, no import step (task 4).
- **One-time support purchases**: imported from the legacy DataStore (task 5). Subscriptions restore from Play.

## Out of scope

Everything else is dropped on upgrade, and its legacy files are deleted by the cleanup step (task 6):

- Place history (`place_history`), including favourites and comments
- GPX history (`gpx_history`): "Recent GPX files" starts empty, the GPX Collection still lists every file
- Settings: theme, base layer, map scale, GPX slope coloring, last map position

## Tasks

### 1. Versioning `[G]`

- [x] Bump `androidBuildNumber` to `1000`, above legacy v1.2.4 (versionCode 688, its latest Play release).
- [x] Split `appVersion` per platform: `androidAppVersion=2.0`, `iosAppVersion` on its own line.
- [x] WhatsNew per platform: a dedicated Android 2.0 message (`tools/release/whatsnew/android/v2.0/`).
- [x] The 2.0 message is shown to everyone, fresh installs included.

### 2. Release build `[G]`

- [x] Add `proguard-rules.pro` to `:composeApp` (release already has `isMinifyEnabled` and `isShrinkResources`).
  - Use HuKi-Android's `app/proguard-rules.pro` as the reference. Keep rules for kotlinx-serialization, Ktor, Koin,
    Room, Mapbox, Firebase.
- [x] Disable AAB language splits, so a per-app language that differs from the device language has its resources.
- [x] 16 KB page size: every native lib (Mapbox, Crashlytics, SQLite) is 16 KB aligned (`zipalign -c -P 16`, ELF
  `LOAD` alignment `2**14`).
- [x] Crashlytics NDK (`firebase-crashlytics-ndk`, `nativeSymbolUploadEnabled`), so Mapbox native crashes reach
  Crashlytics.
- [x] Build `bundleRelease` locally and smoke-test the minified build: map, search, GPX import, route planner, billing.
  Passed `maestro_smoke.yaml` + `maestro_support.yaml` on a debug-signed release APK. The `importGpx` launch hook is
  debug-only, so on release the GPX is imported through the file picker instead.
- [x] Confirm `google-services.json` for release points to the same Firebase project as legacy, so Analytics and
  Crashlytics history continue. Confirmed: same Firebase project as legacy, already live with iOS.

### 3. CI: sign and publish with fastlane `[G]`

Legacy CD (`github-workflow-deploy.yml`: `r0adkll/sign-android-release` + `r0adkll/upload-google-play`) is not
carried over. Play publication moves to fastlane, like the iOS release (`iosApp/fastlane`).

- [ ] Fastlane setup in `composeApp/fastlane` (`Appfile`, `Fastfile`), package `hu.mostoha.mobile.android.huki`,
  authenticated with the `GOOGLE_PLAY_PUBLISHER_SERVICE_ACCOUNT` JSON key.
- [ ] Lanes, mirroring iOS:
  - `verify`: `validate_play_store_json_key` and a dry run, no upload.
  - `beta`: `bundleRelease` → `upload_to_play_store` on the `internal` track, AAB + `mapping.txt`.
  - `beta` also runs `uploadCrashlyticsSymbolFileRelease` after `bundleRelease`: `nativeSymbolUploadEnabled` only
    configures the task, without it Mapbox native crashes stay unsymbolicated in Crashlytics.
  - `release`: promote the internal build to `production` with a staged `rollout` (task 7).
- [ ] Store content via `supply`: download the current listing (`fastlane supply init`) into
  `composeApp/fastlane/metadata/android` (HU + EN title, descriptions, screenshots) and keep it in git.
- [ ] Release notes: generate `changelogs/<versionCode>.txt` per locale from `tools/release/whatsnew/android/v<version>/`
  (Play limit: 500 characters).
- [ ] Release signing in Gradle (`signingConfigs.release` from env vars) with the **same upload key** as legacy: copy
  the secrets `SIGNING_KEY`, `ALIAS`, `KEY_STORE_PASSWORD`, `KEY_PASSWORD` to the HuKi-KMP repo.
- [ ] New workflow `github-workflow-android-release.yml`, modelled on `github-workflow-ios-release.yml`:
  - Trigger: manual (`workflow_dispatch`, lane input `beta` / `release` / metadata only) or release tag, not every
    push to `main`.
  - Needs lint, unit tests, and the debug build to pass.
  - Runs `bundle exec fastlane <lane>`, uploads the AAB and `mapping.txt` as artifacts.
  - Crashlytics: `uploadCrashlyticsMappingFileRelease` runs with the release build, but
    `uploadCrashlyticsSymbolFileRelease` (native symbols) must be called explicitly after `bundleRelease`.
- [ ] Update the `release` skill with the Android fastlane lanes.

### 4. GPX collection `[G]`

GPX files already carry over: both apps use `filesDir/gpx/external` and `filesDir/gpx/routeplanner`, and
`DefaultGpxStorage.listGpxFiles()` lists those directories.

- [ ] Verify on a device that legacy GPX files show up in KMP after upgrade, and that `GpxOrigin.fromPath` maps them
  to the right origin.
- [ ] No `metadata.json` is needed: the collection scans the directories, files without an entry fall back to their
  file date, and landscapes are looked up and cached on the first scan.
- [ ] Check legacy GPX files parse in KMP: `DefaultGpxRepository.scanGpxFile` deletes files it can't parse, so a file
  the legacy parser accepted but Spatial K rejects would be lost. Use a mix of real-world files in the upgrade test.

### 5. Purchases / Support `[G]`

Support is implemented (`PlayBillingService`, `SupportAnimal`). Legacy users carry over in two ways:

- **Subscriptions** stay in Play and restore via `queryPurchasesAsync` with the same product IDs.
- **One-time purchases** were consumed by legacy (v1.2.4 records then consumes, like KMP), so Play no longer
  reports them. The only record is the legacy DataStore (`support_purchased_one_time_products`,
  `support_recorded_purchase_tokens`), which `LegacySupportMigration` imports into the KMP settings DataStore.
  Unconsumed purchases from older legacy versions still show up in Play and get recorded and consumed by KMP.

| Type                  | Product ID                       | Animal   | Origin       |
|-----------------------|----------------------------------|----------|--------------|
| One-time (`INAPP`)    | `huki_support_one_time_level_0`  | Hedgehog | **new (2.0)**|
| One-time (`INAPP`)    | `huki_support_one_time_level_1`  | Owl      | legacy       |
| One-time (`INAPP`)    | `huki_support_one_time_level_2`  | Squirrel | legacy       |
| Subscription (`SUBS`) | `huki_support_recurring_level_0` | Bear     | **new (2.0)**|
| Subscription (`SUBS`) | `huki_support_recurring_level_1` | Boar     | legacy       |
| Subscription (`SUBS`) | `huki_support_recurring_level_2` | Deer     | legacy       |

- [x] Support feature on Android with Play Billing, including "Manage subscriptions" in Play.
- [x] Restore purchases on startup via `queryPurchasesAsync`.
- [x] Acknowledge subscriptions; one-time purchases are recorded, then consumed (consume acknowledges them).
- [x] Import legacy one-time purchases from the legacy DataStore (`LegacySupportMigration`).
- [x] Several legacy subscriptions: a new purchase replaces the highest one (`WITH_TIME_PRORATION`).
- [x] Play Console: create `huki_support_one_time_level_0` and `huki_support_recurring_level_0`.
- [x] Play Console: the legacy subscriptions have an active base plan without an offer ID, which
  `toBillingFlowParams` buys.
- [x] E2E upgrade with real test purchases (2026-11-10): legacy v1.2.4 and KMP release (R8) both debug-signed under
  `hu.mostoha.mobile.android.huki`, so Play Billing serves the real products.
  - Legacy: bought Squirrel + Deer → update to 2.0: Squirrel imported, Deer restored as Active, all 6 products load.
  - KMP: bought Hedgehog (recorded, consumed) and upgraded Deer → Bear (replaced, Bear supporter).

### 6. Legacy cleanup `[G]`

A one-time `LegacyCleanup` in `androidMain`, guarded by `legacy_cleanup_done` in the KMP DataStore.
Verified on the `legacy_1_2_4_seeded` snapshot: freed 4.7 MB from 7 files, GPX files and purchases kept.

**When / UX**: no migration screen, no loading bar. It only deletes a handful of files (OsmDroid's default
`SqlTileWriter` keeps tiles in a single `osmdroid/tiles/cache.db`), so it is expected to take well under a second.

- [x] Launch from `HuKiApplication.onCreate` on an app-scoped coroutine (`Dispatchers.IO`), never blocking the first
  frame.
- [x] Set the done flag only after it completes; deletes are idempotent, so a run killed midway simply repeats on the
  next start.

- [x] Delete the legacy Room DB: `getDatabasePath("huki-database")` plus `-wal` / `-shm` files. Never touch
  `huki.db`, the KMP database.
- [x] Delete the legacy DataStore `filesDir/datastore/user_preferences.preferences_pb` **only after**
  `support_legacy_migrated` is `true` in the KMP settings DataStore. Read the settings DataStore first, which runs
  `LegacySupportMigration`, otherwise one-time supporters lose their animal.
- [x] Delete the OsmDroid directory `osmdroid/` (including `tiles/`) from every candidate storage location. Legacy
  picks it with `StorageUtils.getBestWritableStorage`, which can be internal or external app storage.
- [x] Delete the OsmDroid shared prefs `KEY_GLOBAL_SHARED_PREFERENCES`.
- [x] Keep `filesDir/gpx/**` untouched.
- [x] Log the freed size and failures to Crashlytics as non-fatal; never crash on cleanup.
- [x] Unit or instrumentation test: seed legacy files, run cleanup, assert they are gone and GPX files remain.
  Also assert the legacy DataStore survives while `support_legacy_migrated` is unset.

### 7. Rollout

- [ ] Upgrade test on the internal track: install 1.2.4 from Play, add GPX files, buy a one-time product and a
  subscription with a license tester account, then update to the signed 2.0 build.
  - Check: GPX files present, one-time animal imported (from the legacy DataStore), subscription shown with
    "Manage subscriptions", legacy files cleaned up, 2.0 WhatsNew shown once.
- [ ] Closed testing with a few real users.
- [ ] Production staged rollout: 10% → 50% → 100%, watching Crashlytics and Play vitals between steps.
- [ ] Archive HuKi-Android: freeze `master`, disable its deploy workflow.
