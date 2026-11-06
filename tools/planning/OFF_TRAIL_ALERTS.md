# Off-trail alerts (haptic / sound) — feasibility notes

Goal: warn the user with haptics and/or sound when they leave the track of an opened GPX file,
including when the phone is in their pocket with the screen off.

## TL;DR

- Real **background location** permissions are **not needed** on either platform.
- Updates keep flowing while the app isn't visible if tracking is started by the user while the app
  is in the foreground:
  - Android: a `location` type **foreground service**, with the normal while-in-use permission.
  - iOS: **When In Use** authorization plus the `location` background mode. No **Always** needed.
- The hard part is the platform plumbing and getting alerts delivered (iOS haptics don't work in
  the background), not the detection logic.
- Rough effort: **~1.5–2 weeks**, plus store review time.

## Android

### Foreground service vs background location

- A `location` type foreground service started while the app is visible only needs
  `ACCESS_FINE_LOCATION` (while-in-use). It does **not** need `ACCESS_BACKGROUND_LOCATION`.
- It keeps receiving updates with the screen off and the app in the background, as long as its
  notification is showing. Navigation and fitness apps work this way.
- Avoid `ACCESS_BACKGROUND_LOCATION`: a separate "Allow all the time" step in Settings, a Play
  Console background location declaration with a video, a prominent disclosure screen, and frequent
  rejections. It's only needed to *start* tracking while the app isn't visible (e.g. geofence
  triggers).

### Changes

- Manifest: `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_LOCATION`, and a
  `<service android:foregroundServiceType="location">`.
- `POST_NOTIFICATIONS` (Android 13+): the service runs without it, but it's needed for the
  persistent notification and for the off-trail alert itself.
- Android 14+: the service must be started while the app is visible and the while-in-use permission
  is already granted. Starting it from the background throws.
- Play Console: a **foreground service type declaration** for `location`, with a short justification
  and a video. Much lighter than the background location declaration, and approved routinely for
  "track a route the user started."
- Alerts: `Vibrator` / `VibratorManager` and a high-importance notification channel both work from
  the foreground service.
- Coarse-only permission (`ACCESS_COARSE_LOCATION`) makes detection useless, so handle it.

## iOS

### When In Use is enough

- `Info.plist`: `UIBackgroundModes` → `location`, and a clearer
  `NSLocationWhenInUseUsageDescription` that mentions off-trail alerts.
- `CLLocationManager.allowsBackgroundLocationUpdates = true`, set before the app goes to the
  background. On iOS 17+ a `CLBackgroundActivitySession` can be used instead.
- `pausesLocationUpdatesAutomatically = false`. **Important:** if iOS pauses updates on its own
  (e.g. during a break), a When In Use app can't resume them from the background.
- `activityType = .fitness` (or `.otherNavigation`).
- Optionally `showsBackgroundLocationIndicator = true` (the blue status-bar pill). With When In Use
  it's shown anyway.
- App Review checks that background location is tied to a feature the user can see. An explicit
  "Start off-trail alerts" toggle plus the blue pill covers that.

### Where When In Use stops working (Always would help)

- **The app gets terminated** (user swipe-away, or iOS kills it under memory pressure): updates stop
  for good until the user opens the app again. Only Always plus region monitoring or
  significant-change can relaunch a killed app. While location updates are running, iOS rarely kills
  the app, so this is fine in practice. It's the same trade-off every hiking or navigation app makes
  without Always.
- **Starting from the background** isn't possible, so geofence-style triggers are out.

### Precise Location

- If the user turned Precise Location off, fixes are only accurate to a few kilometres, which makes
  detection useless.
- Check `accuracyAuthorization`. If it's `.reducedAccuracy`, call
  `requestTemporaryFullAccuracyAuthorization(withPurposeKey:)`. This needs an
  `NSLocationTemporaryUsageDescriptionDictionary` entry in `Info.plist`.

### Haptics and sound in the background

- `UIFeedbackGenerator` and Core Haptics **don't work in the background**.
- Reliable option: a **local notification** with sound, using `.timeSensitive` so it gets through
  Focus modes. Needs notification permission and the Time Sensitive Notifications capability.
- Playing a short sound through an `AVAudioSession` is possible, but more fragile.

## Both platforms

- The alert is a notification with sound and vibration, plus in-app haptics and a banner when the
  app is visible.
- Privacy policy: add that location is used in the background while off-trail alerts are on, and is
  processed on-device only. Play's Data safety form probably doesn't change if nothing leaves the
  device.
- Analytics: only events like `offtrail_alert_started` / `offtrail_triggered`, never coordinates
  (matches the existing PII rule).
- Battery: continuous GPS for hours. Use a sensible interval (~3–5 s, ~5 m minimum distance) and stop
  automatically when the GPX is closed.

## Detection logic (commonMain)

- Find the distance from each fix to the nearest point on the GPX track (Spatial K `turf`
  nearest-point-on-line).
- Hysteresis: off-trail at more than ~40 m, back on trail at less than ~20 m.
- Debounce: N consecutive fixes or ~20 s off-trail before alerting, so GPS jitter doesn't cause
  false alarms.
- Ignore fixes whose horizontal accuracy is worse than the threshold.
- Optionally search only near the last matched segment. Performance isn't really a problem even for
  long OKT tracks.
- Optionally send a "back on trail" alert.
- Pure logic, unit-testable with fakes. Test end to end with `ios_simulate_gpx_walk.sh` /
  `android_simulate_gpx_walk.sh`.

## Effort estimate

| Part                                                              | Estimate                         |
|-------------------------------------------------------------------|----------------------------------|
| Shared detector + tests                                           | ~1–2 days                        |
| Android service, notification, permission flow                    | ~2–3 days                        |
| iOS background mode, notifications, permission flow               | ~2–3 days                        |
| UI toggle, settings (threshold?), translations, a11y, Maestro     | ~1–2 days                        |
| Play declaration, privacy policy, App Review                      | paperwork, plus review time      |

## Cheap first version

Foreground-only: alerts only while the app is open, with no new permissions or declarations. Of
limited use for hikers who keep the phone in a pocket, but it lets the thresholds be tuned before
committing to the background work.
