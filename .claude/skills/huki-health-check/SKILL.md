---
name: huki-health-check
description: Check how a live HuKi release performs — Crashlytics crashes, ANRs and non-fatals, crash-free rates, Google Analytics adoption and error / failure events — and report what needs fixing. Use when asked how a version performs, to look for crashes or issues after a release, to check Crashlytics / non-fatals / GA, or for a post-release health check on Android or iOS.
---

# HuKi health check

A read-only investigation: pull the numbers from Firebase, read sample events, cross-check Google Analytics, and report. 
Fix nothing unless asked.

## IDs

| What                                                 | Value                                           |
|------------------------------------------------------|-------------------------------------------------|
| Firebase project                                     | `huki-c8bc7`                                    |
| Android app (prod, `hu.mostoha.mobile.android.huki`) | `1:550684341915:android:20d746c1b1d2618e44383e` |
| Android app (debug, ignore)                          | `1:550684341915:android:129afd9aa921d2f044383e` |
| iOS app (`hu.mostoha.mobile.ios.huki`)               | `1:550684341915:ios:8f47b79e94064e4044383e`     |
| GA4 property                                         | `290844763`                                     |

## Tools

Load in one `ToolSearch` call:
`select:mcp__firebase__crashlytics_get_report,mcp__firebase__crashlytics_get_issue,mcp__firebase__crashlytics_list_events,mcp__firebase__crashlytics_batch_get_events,mcp__firebase__firebase_read_resources,mcp__analytics-mcp__run_report`

The Crashlytics MCP requires reading `firebase://guides/crashlytics/reports` (`firebase_read_resources`)
before the first report call.

## 1. Find the version and release date

- `version.properties` → `androidAppVersion` / `iosAppVersion` + build number.
- Release dates: `git tag -l 'android/*' 'ios/*' --format='%(refname:short) %(creatordate:short)'`.
- Commits since the previous tag (`git log android/v<prev>..android/v<cur> --oneline`) tell which
  issues are already fixed in a newer build.

## 2. Crashlytics

1. **`topVersions`** with `intervalStartTime` = release date. Version display names differ by era:
   KMP builds are `2.0 (1001)`, the legacy HuKi-Android app is `v1.2.4 (688)`. Take display names
   from this response, never guess them.
   Rows give `crashFreeUsersPercentage` / `crashFreeSessionsPercentage` and `totalUsersCount`;
   compare the new version against the previous one over the same interval.
   Brand-new versions may sit low in the list — use `pageSize: 25`.
2. **`topIssues`** filtered by `versionDisplayNames: [<new version>]`, no `issueErrorTypes` filter, so
   FATAL, NON_FATAL and ANR all show. Run it once more with `issueErrorTypes: ["NON_FATAL", "ANR"]`
   for all versions if the new one has few users.
3. For every issue on the new version: `crashlytics_batch_get_events` with its `sampleEvent`. Read
   the stack trace, `customKeys.last_screen`, `logs`, `breadcrumbs`, device + OS, `buildStamp.revision`.
4. Note `firstSeenVersion`: an issue that started in the legacy app (`v1.x`) is inherited, not a
   regression.
5. `crashlytics_list_events` needs an `issueId` filter — it can't list everything.

### Spotting robots
Play pre-launch / review devices are not real users. Signs: crash within seconds of `session_start`
on the first screen (often `whats_new`), within hours of release, an odd device / OS pair (e.g.
Pixel 8 Pro on Android 13), OS name `Unknown`, GA rows with blank device model from the US.
Report them, but rank them below real-user issues.

## 3. Google Analytics

All via `run_report` on property `290844763`, filter `platform` = `Android` / `iOS` and
`appVersion` (GA uses the plain version name: `2.0`, `v1.2.4`).

1. **Adoption**: dimensions `appVersion`, metrics `activeUsers`, `newUsers`, `sessions`,
   `averageSessionDuration`, since the release date. Low updates vs many `newUsers` means a staged
   rollout or users who haven't updated yet.
2. **All events of the new version** by `eventName` (`limit` 80, then page with `offset`). Look for:
   - `app_exception` — Crashlytics logs it to GA. Compare its count with Crashlytics events; a gap
     means a delayed upload, a robot, or crashes Crashlytics grouped differently. Say so, don't
     guess.
   - HuKi failure events: anything ending in `_unavailable`, `_failed`, `_error`, `_empty`
     (`support_prices_unavailable`, `support_purchase_failed`, `search_empty`, …). Look up where
     they fire in `AnalyticsEvent.kt` and whether a non-fatal is recorded on the same path.
   - `app_remove` vs `first_open` — compare the ratio with the previous version, not absolute
     numbers.
   - `app_clear_data` bursts from 1–2 users in one hour are testing.
3. **Who triggers a suspicious event**: dimensions `dateHour`, `mobileDeviceModel`,
   `operatingSystemVersion`, `country`, filtered to the event — tells real users from robots and
   the developer's own devices.
4. Feature usage (top custom events) gives a quick read on what the release's users actually do.

## 4. Check the code

For each finding, look at the code path in this repo:
- Is the failure recorded? `crashlyticsService.recordException(...)` — a path that only calls
  `Logger.w` is invisible in Crashlytics.
- Zero non-fatals on a new version with real users is suspicious; confirm `recordException` is
  called on the relevant paths. Non-fatals upload on the next app launch, so the first day or two
  are incomplete.
- Already fixed on `main` / a newer tag? Say which commit.

When suggesting a new non-fatal or event, check the analytics PII rule in AGENTS.md: no free text,
coordinates or user-derived place names in GA. Crashlytics messages must not carry account data
or purchase tokens.

## 5. Report

Keep it short, findings first:
- Headline: is the release healthy, and is anything new and real-user-facing.
- Numbers: adoption, crash-free users / sessions vs the previous version.
- Crashes: one line per issue — issue id, title, users, inherited or new, robot or real, fixed or not.
- Non-fatals / ANRs on the new version.
- GA signals worth acting on, each with a concrete next step.
- When to check again (newer build too young to have data → say so and suggest a date).
