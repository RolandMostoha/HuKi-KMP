# HuKi-KMP — Plan Board

## Legend

| Status | Meaning                   |
|--------|---------------------------|
| `[ ]`  | Not started               |
| `[L]`  | Required for Go-Live      |
| `[~]`  | In progress               |
| `[x]`  | Done                      |
| `[-]`  | Cancelled / deprioritized |

---

## Most important features in HuKi

1. Map, text visibility
2. My location, GPS accuracy, navigating in trails, battery consumption
3. Layers (especially hiking Layer)
4. Search for places
5. Place Details
6. GPX
7. Route planner
8. Support / Billing
9. Destinations - Locally stored POIs (Peaks, Waterfalls, Valleys etc.)
10. OKT/AKT/RPDDK routes
11. Landscapes

## Release plan

1. Feature complete for iOS Go-Live - Map, My Location, Layers, Search, GPX, Settings
2. Release process setup: CD - Fastlane, versioning, release notes, store presence etc.
3. Apple developer account registration
4. iOS Go-Live
5. Android Go-Live: will only happen if legacy HuKi's feature set is mostly covered

### Go-live remaining features

- Recent Places -> from LIQ Autocomplete / Destinations / Long tap place details
- Place Details -> long tap on map AND destinations
- Display (distance + time) in an InfoWindow on top Start / End / Waypoint points
- While Following or FollowingLiveCompass, show "+" -> zoom in and "-" -> zoom out buttons
- Destinations screen with filtering
- Versioning, WhatsNews

## Backlog

### General / tech tasks

| Status | Feature                                                                                          |
|--------|--------------------------------------------------------------------------------------------------|
| `[L]`  | Launcher icon Android + iOS                                                                      |
| `[L]`  | Google Analytics                                                                                 |                                                 
| `[L]`  | LogLevel.ALL only in debug                                                                       |
| `[L]`  | Register Apple Developer Account                                                                 |
| `[L]`  | CD on Apple Store                                                                                |
| `[L]`  | Implement T&C on huki.hu                                                                         |
| `[ ]`  | Update Kotlin + Gradle 9                                                                         |
| `[ ]`  | Sonar? free for open source projects                                                             |
| `[ ]`  | Check project against Swift agent skills in XCode                                                |
| `[ ]`  | GitHub smart labels, E.g.: https://github.com/balazsgerlei/ScreenLit/blob/main/README.md?plain=1 |

### Bugs

| Status | Scope      | Bug                                                                                                                                                                                                                                                                                                             |
|--------|------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `[L]`  | Map        | Bug: zooming deep (17+) removes the hiking layer, it should force scale instead                                                                                                                                                                                                                                 |
| `[ ]`  | CI         | Bug: iOS Simulator 18 is used (preferred: 26) and only smoke test suite is runnable on CI                                                                                                                                                                                                                       |
| `[ ]`  | Search     | Bug: Android. DestinationsSection->overscrollEffect = null is used because of this bug. LazyRow shows spurious stretch-overscroll mid-list on fling (cards widen/shake even when not at an edge). Only on fling, not on controlled drag (scroll-to-stop). (possibly a Compose foundation fling/overscroll bug). |
| `[ ]`  | Search     | UI Bug: Android. In GpxCollection + Settings, it use group dividers as separators, it's more like iOS design, it should be transparent sapces instead.                                                                                                                                                          |
| `[ ]`  | MyLocation | There is no hard timeout for a location fix. If My Location button is clicked and location fix doesnt come, it loads inifinitely. After a fixed timeout, we should show an alert "Couldn't find location, try again later"                                                                                      |

### FEATURE: Map

| Status | Scope | Task                                                                                     |
|--------|-------|------------------------------------------------------------------------------------------|
| `[L]`  | Map   | While Following or FollowingLiveCompass, show "+" -> zoom in and "-" -> zoom out buttons |
| `[ ]`  | Map   | After state restoration / app kill -> restore last camera state + last opened GPX        |

### FEATURE: My Location

| Status | Scope      | Task                    |
|--------|------------|-------------------------|
| `[ ]`  | MyLocation | Show altitude somewhere |

### FEATURE: Layers

| Status | Scope  | Task                                                                   |
|--------|--------|------------------------------------------------------------------------|
| `[ ]`  | Layers | Save picked layer state permanently for users (multiplatform-settings) |

### FEATURE: Search

| Status | Scope  | Task                                                                                                                                                                          |
|--------|--------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `[L]`  | Search | Use PlaceHistory + Destinations as well as data sources in SearchResults. They populate search results immediately (without LIQ debounce and without meeting minChar=3 limit) |
| `[ ]`  | Search | Show GPX Trail collection (Természetjáró, AktívMagyarország)                                                                                                                  |
| `[ ]`  | Search | No mic/voice icon. Search by voice Consider adding one between the text and hamburger.                                                                                        |

#### Add Place History items to Search -> Autocomplete

- Local DB: fire on every keystroke, no debounce — it's an instant indexed read, that's the whole
  point.
- clock or pin icon), remote results below
- LocationIQ: debounced (~300ms) — don't spam the API mid-typing.
- Dedupe, so results from Place History is not shown again.

### FEATURE: Destinations

| Status | Scope        | Task                                                                                                                                                                               |
|--------|--------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `[x]`  | Destinations | Add a dedicated DescrinationsScreen which lists all destinations.                                                                                                                  |
| `[x]`  | Destinations | Add a "Destinations" button to menu as a main feature, above GPX Collection. A:ic_backpack, iOS:backpack.fill                                                                      |
| `[x]`  | Destinations | Add "Destinations" screen, copy the style of GpxCollection                                                                                                                         |
| `[x]`  | Destinations | Empty view is not necessary, @Destinations are always there                                                                                                                        |
| `[x]`  | Destinations | Add title as en:"Destinations" hu:"Kirándulóhelyek", and "[Count] destinations"                                                                                                    |
| `[x]`  | Destinations | Top right "map" button is not needed for the moment.                                                                                                                               |
| `[x]`  | Destinations | Add a Segmented Control to the top with "Popular", "By area", "Nearby"                                                                                                             |
| `[x]`  | Destinations | Add list view for "Popular". Rows should be numbered. Distance is not needed.                                                                                                      |
| `[x]`  | Destinations | Add list view for "Nearby". Row numbers are not needed. Distance is needed. If Permission is not granted for location, show InfoView.Warning, with a link to location permissions. |
| `[x]`  | Destinations | Do not implement "By area" yet, just have a "blank" state when the segmented control is clicked.                                                                                   |
| `[x]`  | Destinations | Do not implement item click yet.                                                                                                                                                   |
| `[x]`  | Destinations | Add "See all" click handling to Search/Destinations -> Navigates to "Destinations"                                                                                                 |
| `[~]`  | Destinations | Add Preview destination card before navigating to maps                                                                                                                             |
| `[ ]`  | Destinations | Add Map based destinations with Landscapes                                                                                                                                         |

### FEATURE: WhatsNew

| Status | Scope   | Task                                                           |
|--------|---------|----------------------------------------------------------------|
| `[L]`  | Version | Add proper versioning to app, which works for Android and iOS. |

### FEATURE: GPX

| Status | Scope | Task                                                                              |
|--------|-------|-----------------------------------------------------------------------------------|
| `[ ]`  | GPX   | Display (distance + time) in an InfoWindow on top Start / End / Waypoint points   |
| `[ ]`  | GPX   | Wire iOS file picker error branch to ViewModel                                    |
| `[ ]`  | GPX   | Colored GPX                                                                       |
| `[ ]`  | GPX   | Display direction arrows. Add an option to toggle direction in GpxMenu            |
| `[ ]`  | GPX   | Display waypoint comments in a window                                             |
| `[ ]`  | GPX   | ? Display start and end location: "Around Bükk..." -> on import we can do geocode |

### FEATURE: GPX Details

| Status | Scope        | Task                                                |
|--------|--------------|-----------------------------------------------------|
| `[L]`  | GPXDetails   | Show as secondary button "Maps Navigation to Start" |
| `[L]`  | GPXDetails   | Show as secondary button "Maps Navigation to End"   |
| `[ ]`  | GPXDetails   | Show as secondary button "Share GPX file"           |
| `[ ]`  | Destinations | Show context menu "Navigate with Maps" in preview   |

### FEATURE: GPX Collection

| Status | Scope         | Task                                                        |
|--------|---------------|-------------------------------------------------------------|
| `[L]`  | GPXTutorial   | T&C link                                                    |                                                                                                                   |
| `[ ]`  | GPXCollection | Implement share                                             |
| `[ ]`  | GPXCollection | Implement rename                                            |
| `[ ]`  | GPXCollection | "Imported vs Route Planner" badges / chips OR icon to start |
| `[ ]`  | GPXCollection | Searchbar, free search text by gpx name                     |
| `[ ]`  | GPXCollection | Filter by distance, open date                               |
| `[ ]`  | GPXCollection | "Mark Completed" GPX files                                  |

### FEATURE: Place Details (from Search + Long Tap)

| Status | Scope        | Task                                                                      |
|--------|--------------|---------------------------------------------------------------------------|
| `[ ]`  | PlaceDetails | On long click show a PlacePicker marker with (CheckMark: done, X: cancel) |
| `[ ]`  | PlaceDetails | On CheckMark: done show PlaceDetails sheet                                |
| `[ ]`  | PlaceDetails | Reverse geocode with LocationIQ                                           |
| `[ ]`  | PlaceDetails | Show content what is already shown with autocomplete: @Place model        |
| `[ ]`  | PlaceDetails | Search nearby button                                                      |
| `[ ]`  | PlaceDetails | Show Place Details for Destinations                                       |

### FEATURE: Settings

| Status | Scope    | Task                                            |
|--------|----------|-------------------------------------------------|
| `[ ]`  | Settings | Add "Settings" to the top section of MenuScreen |
| `[ ]`  | Settings | Create SettingsScreen                           |
| `[ ]`  | Settings | Add Increase map font size in Settings          |
| `[ ]`  | Settings | Add Show/Hide +- zooming in Settings            |

### FEATURE: Support + Billing

- Support + Billing is necessary to help me keep the app free for everyone and support the
  development.
- Google: Google Play Billing API
- Apple: Apple App Store Connect, Apple Pay

### FEATURE: Route Planner

- Graphhopper API
- Most of the code can be reused from legacy HuKi
- Storage: `gpx/routeplanner/` (sibling of `gpx/external/`)

| Status | Scope        | Task                                                                                     |
|--------|--------------|------------------------------------------------------------------------------------------|
| `[ ]`  | RoutePlanner | Save created routes to `gpx/routeplanner/` (sibling of `gpx/external/`)                  |
| `[ ]`  | RoutePlanner | Serialize a created route to `.gpx` (persist to collection sandbox or temp for share)    |
| `[ ]`  | RoutePlanner | Share created track via share sheet (iOS ShareLink / Android ACTION_SEND + FileProvider) |

### FEATURE: GPX Info Panel (remaining time, distance, elevation gain/loss)

| Status | Scope    | Task                                           |
|--------|----------|------------------------------------------------|
| `[ ]`  | GpxPanel | Use GPX panel on Start / GPX Details -> hidden |
| `[ ]`  | GpxPanel | Elapsed time                                   |
| `[ ]`  | GpxPanel | Distance from Start                            |
| `[ ]`  | GpxPanel | Distance from End                              |
| `[ ]`  | GpxPanel | AVG Speed                                      |
| `[ ]`  | GpxPanel | Expected arrival based on dist/AVG speed       |

### FEATURE: GPX Collection Import/Export (device-to-device, no cloud)

Goal: share/back up the whole GPX collection between devices without cloud or auth.
Principle: separate **format** (a portable collection file) from **transport** — let the OS own
transport (AirDrop, Quick Share, Bluetooth, Files/USB, email, chat). A zipped bundle is plain data,
so an Android export imports on iOS and vice-versa.

| Status | Scope      | Task                                                                                               |
|--------|------------|----------------------------------------------------------------------------------------------------|
| `[ ]`  | GPX Export | `GpxCollectionArchiver` (expect/actual): zip `gpx/external` + generated `manifest.json`            |
| `[ ]`  | GPX Export | Export action → native share sheet (iOS ShareLink / Android ACTION_SEND + FileProvider)            |
| `[ ]`  | GPX Import | `GpxCollectionArchiver` unzip to temp, then loop each `.gpx` through `GpxStorage.saveToFileSystem` |
| `[ ]`  | GPX Import | iOS: declare imported/exported UTI + `CFBundleDocumentTypes`, handle inbound via `.onOpenURL`      |
| `[ ]`  | GPX Import | Android: `<intent-filter>` ACTION_VIEW/ACTION_SEND for `.hukigpx`/zip, read URI in `onNewIntent`   |
| `[ ]`  | GPX Import | Extend existing file pickers to also accept `.hukigpx`/`.zip`                                      |
| `[ ]`  | GPX Import | Manifest-driven import preview/summary ("X new, Y duplicates")                                     |
| `[ ]`  | GPX Import | iOS unzip needs a lib (ZIPFoundation via SPM or libarchive); Android uses `java.util.zip`          |

### FEATURE: Report problem on route

- Use MTSZ (Magyar Természetjáró Szövetség) email's to report a problem
- Include location (latitude, longitude)
- Possible link to openstreetmap.org which include the problematic point

---

## Completed

| Feature        | Notes                                                        |
|----------------|--------------------------------------------------------------|
| Map            | Mapbox                                                       |
| My location    | Mapbox + Google Fused Location Provider                      |
| Layers         | Mapbox Outdoor, Street, Satellite, Hiking                    |
| Search         | Place Autocomplete, Destinations, Recent GPXs, Recent Places |
| Menu           | Features, Contact, Supporters                                |
| GPX            | GPX Import, added in Layers                                  |
| GPX Details    | GPX Details Sheet with basic info                            |
| GPX Menu       | GPX Visibility, Overview, Clear                              |
| GPX Collection | GPX File list view, GPX Tutorial screen                      |
| GPX Metadata   | Store GPX Metadata in a JSON                                 |
| Place History  | Place History list view                                      |
