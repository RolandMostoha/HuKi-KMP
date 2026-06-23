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

1. Recent Places -> from LIQ Autocomplete / Destinations
2. Place Details -> long tap on map AND destinations
3. Display (distance + time) in an InfoWindow on top Start / End / Waypoint points
4. While Following or FollowingLiveCompass, show "+" -> zoom in and "-" -> zoom out buttons

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
| `[ ]`  | GitHub smart labels, E.g.: https://github.com/balazsgerlei/ScreenLit/blob/main/README.md?plain=1 |

### Bugs

| Status | Scope  | Bug                                                                                                                                                                                                                                                                                                             |
|--------|--------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `[L]`  | Map    | Bug: zooming deep (17+) removes the hiking layer, it should force scale instead                                                                                                                                                                                                                                 |
| `[L]`  | CI     | Bug: Android. If open SearchBottomSheet and close it with swipe-to-dismiss all FABs and SearchBar are missing. Dead-end.                                                                                                                                                                                        |
| `[ ]`  | CI     | Bug: iOS Simulator 18 is used (preferred: 26) and only smoke test suite is runnable on CI                                                                                                                                                                                                                       |
| `[ ]`  | Search | Bug: Android. DestinationsSection->overscrollEffect = null is used because of this bug. LazyRow shows spurious stretch-overscroll mid-list on fling (cards widen/shake even when not at an edge). Only on fling, not on controlled drag (scroll-to-stop). (possibly a Compose foundation fling/overscroll bug). |
| `[ ]`  | Search | UI Bug: Android. In GpxCollection + Settings, it use group dividers as separators, it's more like iOS design, it should be transparent sapces instead.                                                                                                                                                          |

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
| `[L]`  | Layers | Save picked layer state permanently for users (multiplatform-settings) |

### FEATURE: Search

| Status | Scope  | Task                                                                                                  |
|--------|--------|-------------------------------------------------------------------------------------------------------|
| `[L]`  | Search | Recent places - store searched places in local DB. Show them in Search Sheet.                         |
| `[x]`  | Search | Recent GPX files, show them in Search Sheet. (pre-requisite: GPX files in sandbox)                    |
| `[L]`  | Search | iOS: if search sheet is scrolled, the list should hide beneath the search bar in a "liquid glass" way |
| `[ ]`  | Search | Show GPX Trail collection (Természetjáró, AktívMagyarország)                                          |
| `[ ]`  | Search | No mic/voice icon. Search by voice Consider adding one between the text and hamburger.                |

### FEATURE: GPX

| Status | Scope | Task                                                                              |
|--------|-------|-----------------------------------------------------------------------------------|
| `[L]`  | GPX   | Display (distance + time) in an InfoWindow on top Start / End / Waypoint points   |
| `[ ]`  | GPX   | Wire iOS file picker error branch to ViewModel                                    |
| `[ ]`  | GPX   | Colored GPX                                                                       |
| `[ ]`  | GPX   | Display direction arrows. Add an option to toggle direction in GpxMenu            |
| `[ ]`  | GPX   | Display waypoint comments in a window                                             |
| `[ ]`  | GPX   | ? Display start and end location: "Around Bükk..." -> on import we can do geocode |

### FEATURE: GPX Details

| Status | Scope      | Task                                                       |
|--------|------------|------------------------------------------------------------|
| `[L]`  | GPXDetails | Show as secondary button "Google Maps navigation to Start" |
| `[L]`  | GPXDetails | Show as secondary button "Google Maps navigation to End"   |
| `[ ]`  | GPXDetails | Show as secondary button "Share GPX file"                  |

### FEATURE: GPX Collection

| Status | Scope         | Task                                                                                                                                                               |
|--------|---------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `[x]`  | GPXMetadata   | Goal: Populate "Recent GPX files" section in Search.                                                                                                               |                                                                                                                   |
| `[x]`  | GPXMetadata   | Save "lastOpened" date-time when user opens (for the first time) or re-opens the GPX file. The current "lastModified" from File is only useful for date of import. |                                                                                                                   |
| `[x]`  | GPXMetadata   | Create a JSON `GpxMetadataStore` (commonMain), in `/gpx/`.                                                                                                         |
| `[x]`  | GPXMetadata   | Add a new entry to metadata on GPX import                                                                                                                          |
| `[x]`  | GPXMetadata   | The metadata should contain a trackId (unique ID based on file content). File Rename-survival is important                                                         |
| `[x]`  | GPXMetadata   | The metadata should contain a lastOpened offset date time in human readable format                                                                                 |
| `[x]`  | GPXMetadata   | Make the store rebuildable: regenerate stats from files on missing/corrupt; keep only completion marks                                                             |
| `[x]`  | Search        | Implement "Recent GPX files" section in Search default state (no-user input state), based on "lastOpened"                                                          |
| `[ ]`  | GPXMetadata   | Add GPX stats to GpxMetadataEntry as a cache -> no need to re-compute every time                                                                                   |
| `[L]`  | GPXTutorial   | T&C link                                                                                                                                                           |                                                                                                                   |
| `[ ]`  | GPXCollection | Implement share                                                                                                                                                    |
| `[ ]`  | GPXCollection | Implement rename                                                                                                                                                   |
| `[ ]`  | GPXCollection | "Imported vs Route Planner" badges / chips OR icon to start                                                                                                        |
| `[ ]`  | GPXCollection | Searchbar, free search text by gpx name                                                                                                                            |
| `[ ]`  | GPXCollection | Filter by distance, open date                                                                                                                                      |
| `[ ]`  | GPXCollection | "Mark Completed" GPX files                                                                                                                                         |

### FEATURE: Place Details (from Search + Long Tap)

| Status | Scope        | Task                                                                      |
|--------|--------------|---------------------------------------------------------------------------|
| `[L]`  | PlaceDetails | On long click show a PlacePicker marker with (CheckMark: done, X: cancel) |
| `[L]`  | PlaceDetails | On CheckMark: done show PlaceDetails sheet                                |
| `[L]`  | PlaceDetails | Reverse geocode with LocationIQ                                           |
| `[L]`  | PlaceDetails | Show content what is already shown with autocomplete: @Place model        |
| `[L]`  | PlaceDetails | Search nearby button                                                      |
| `[L]`  | PlaceDetails | Show Place Details for Destinations                                       |

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

### FEATURE: Destinations

| Status | Scope        | Task                                                                      |
|--------|--------------|---------------------------------------------------------------------------|
| `[ ]`  | Destinations | Add a dedicated DescrinationsScreen which lists all destinations.         |
| `[ ]`  | Destinations | Add filter options to destinations (popularity, landscape, distance etc.) |

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

Format: `huki-collection-<date>.hukigpx` (a zip) containing `manifest.json`
(`schemaVersion`, app version, file list + optional cached stats) plus the `.gpx` files.
Accept plain `.zip` as a fallback. Import merge reuses existing dedup
(same name+bytes → reuse, same name+different bytes → suffix `name (2).gpx`).

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

| Feature        | Notes                                         |
|----------------|-----------------------------------------------|
| Map            | Mapbox                                        |
| My location    | Mapbox + Google Fused Location Provider       |
| Layers         | Mapbox Outdoor, Street, Satellite, Hiking     |
| Search         | Place Autocomplete, Destinations, Recent GPXs |
| GPX            | GPX Import, added in Layers                   |
| GPX Details    | GPX Details Sheet with basic info             |
| GPX Menu       | GPX Visibility, Overview, Clear               |
| GPX Collection | GPX File list view, GPX Tutorial screen       |
| GPX Metadata   | Store GPX Metadata in a JSON                  |
| Settings       | Contact, Supporters                           |
