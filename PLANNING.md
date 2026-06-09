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

## Backlog

### General / tech tasks

| Status | Feature                                                                                          |
|--------|--------------------------------------------------------------------------------------------------|
| `[ ]`  | Launcher icon Android + iOS                                                                      |
| `[ ]`  | Google Analytics                                                                                 |                                                 
| `[ ]`  | LogLevel.ALL only in debug                                                                       |
| `[ ]`  | Update Kotlin + Gradle 9                                                                         |
| `[ ]`  | Register Apple Developer Account                                                                 |
| `[ ]`  | CD on Apple Store                                                                                |
| `[ ]`  | Sonar? free for open source projects                                                             |
| `[ ]`  | GitHub smart labels, E.g.: https://github.com/balazsgerlei/ScreenLit/blob/main/README.md?plain=1 |

### Bugs

| Status | Scope  | Bug                                                                                                                                                                                                                                                                                                             |
|--------|--------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `[ ]`  | Map    | Bug: zooming deep (17+) removes the hiking layer, it should force scale instead                                                                                                                                                                                                                                 |
| `[ ]`  | CI     | Bug: iOS Simulator 18 is used (preferred: 26) and only smoke test suite is runnable on CI                                                                                                                                                                                                                       |
| `[ ]`  | CI     | Bug: Android. If open SearchBottomSheet and close it with swipe-to-dismiss all FABs and SearchBar are missing. Dead-end.                                                                                                                                                                                        |
| `[ ]`  | Search | Bug: Android. DestinationsSection->overscrollEffect = null is used because of this bug. LazyRow shows spurious stretch-overscroll mid-list on fling (cards widen/shake even when not at an edge). Only on fling, not on controlled drag (scroll-to-stop). (possibly a Compose foundation fling/overscroll bug). |

### FEATURE: Map

| Status | Scope | Task                                                                                     |
|--------|-------|------------------------------------------------------------------------------------------|
| `[ ]`  | Map   | While Following or FollowingLiveCompass, show "+" -> zoom in and "-" -> zoom out buttons |
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

| Status | Scope  | Task                                                                                   |
|--------|--------|----------------------------------------------------------------------------------------|
| `[ ]`  | Search | Recent places - store searched places in local DB. Show them in Search Sheet.          |
| `[ ]`  | Search | Recent GPX files, show them in Search Sheet. (pre-requisite: GPX files in sandbox)     |
| `[ ]`  | Search | Show top Destinations order by distance + popularity.                                  |
| `[ ]`  | Search | Show GPX Trail collection (Természetjáró, AktívMagyarország)                           |
| `[ ]`  | Search | No mic/voice icon. Search by voice Consider adding one between the text and hamburger. |

### FEATURE: GPX

| Status | Scope | Task                                                                            |
|--------|-------|---------------------------------------------------------------------------------|
| `[ ]`  | GPX   | Wire iOS file picker error branch to ViewModel                                  |
| `[ ]`  | GPX   | Display (distance + time) in an InfoWindow on top Start / End / Waypoint points |
| `[ ]`  | GPX   | Colored GPX                                                                     |
| `[ ]`  | GPX   | Display direction arrows. Add an option to toggle direction in GpxMenu          |
| `[ ]`  | GPX   | Display waypoint comments in a window                                           |
| `[ ]`  | GPX   | Display start and end location: "Around Bükk..." -> on import we can do geocode |

### FEATURE: GPX Collection

| Status | Scope          | Task                                                                                              |
|--------|----------------|---------------------------------------------------------------------------------------------------|
| `[ ]`  | GPX Collection | Save all imported GPX files in app's local sandbox, so GPX files can be reused without re-import. |
| `[ ]`  | GPX Collection | Copy file to internal storage if not exists (first time import).                                  |
| `[ ]`  | GPX Collection | Create /APP_SANDBOX/gpx/external if not exist. This will be the directory for imported GPX files. |
| `[ ]`  | GPX Collection | Rename SettingsScreen to MenuScreen                                                               |
| `[ ]`  | GPX Collection | Show a GPX Collection menu button in Settings.                                                    |
| `[ ]`  | GPX Collection | Show a new screen "GPX Collection" on clicking it.                                                |
| `[ ]`  | GPX Collection | Show all imported GPX file in a list view. (File Sandbox saving is pre-requisite)                 |
| `[ ]`  | GPX Collection | In the list view show the basic info for the GPX, what is already displayed in GPXDetails         |

### FEATURE: Place Details (from Search + Long Tap)

| Status | Scope        | Task                                                                      |
|--------|--------------|---------------------------------------------------------------------------|
| `[ ]`  | PlaceDetails | On long click show a PlacePicker marker with (CheckMark: done, X: cancel) |
| `[ ]`  | PlaceDetails | On CheckMark: done show PlaceDetails sheet                                |
| `[ ]`  | PlaceDetails | Reverse geocode with LocationIQ                                           |
| `[ ]`  | PlaceDetails | Show what is already shown with autocomplete: @Place                      |
| `[ ]`  | PlaceDetails | Search nearby button                                                      |

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

| Status | Scope        | Task                                                              |
|--------|--------------|-------------------------------------------------------------------|
| `[ ]`  | Destinations | Add a dedicated DescrinationsScreen which lists all destinations. |

### FEATURE: Route Planner

- Graphhopper API
- Most of the code can be reused from legacy HuKi

### FEATURE: GPX Info Panel (remaining time, distance, elevation gain/loss)

| Status | Scope    | Task                                           |
|--------|----------|------------------------------------------------|
| `[ ]`  | GpxPanel | Use GPX panel on Start / GPX Details -> hidden |
| `[ ]`  | GpxPanel | Elapsed time                                   |
| `[ ]`  | GpxPanel | Distance from Start                            |
| `[ ]`  | GpxPanel | Distance from End                              |
| `[ ]`  | GpxPanel | AVG Speed                                      |
| `[ ]`  | GpxPanel | Expected arrival based on dist/AVG speed       |

---

## Completed

| Feature     | Notes                                     |
|-------------|-------------------------------------------|
| Map         | Mapbox                                    |
| My location | Mapbox + Google Fused Location Provider   |
| Layers      | Mapbox Outdoor, Street, Satellite, Hiking |
| GPX         | GPX Import, added in Layers               |
| GPX Details | GPX Details Sheet with basic info         |
| GPX Menu    | GPX Visibility, Overview, Clear           |
| Settings    | Contact, Supporters                       |

### FEATURE: My Location

| Status | Scope      | Task                                                                                                                                                                                          |
|--------|------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `[x]`  | MyLocation | Use My Location for search result distance                                                                                                                                                    |
| `[x]`  | MyLocation | Bug-Android: Compass icon has rectangular touch feedback but its a circle                                                                                                                     |
| `[x]`  | MyLocation | Bug: Loading state. At the moment there is no loading state while the GPS is searching for location.                                                                                          |
| `[x]`  | MyLocation | Bug: if location permission is not enabled, GPX->Start does nothing. If location permission is not granted, and there is any my location related request, first it has to ask for permission. |
| `[x]`  | MyLocation | Hide SearchBar if GPX is visible. Hide SearchBar if FollowingLiveCompass.                                                                                                                     |
| `[x]`  | MyLocation | Reset pinch as well clicking on compass (Mapbox driven) icon                                                                                                                                  |

### FEATURE: GPX Menu

| Status | Scope   | Task                                                                                         |
|--------|---------|----------------------------------------------------------------------------------------------|
| `[x]`  | GPXMenu | When GPX is open, show a GPX-Control FAB (A:ic_tune iOS:slider.horizontal.3) in bottom-left. |
| `[x]`  | GPXMenu | It triggers a menu (A: FloatingActionButtonMenu iOS: Morphing Menu) with 3 actions.          |
| `[x]`  | GPXMenu | Eye -> shows/hide the GPX Line layer, but it keeps showing the Start/End/Waypoints markers   |
| `[x]`  | GPXMenu | Maximize (A:ic_maximize iOS:rectangle.expand.diagonal) -> GPX.overview                       |
| `[x]`  | GPXMenu | Clear -> Remove GPX layer                                                                    |

### FEATURE: Destinations

| Status | Scope        | Task                                                                                                                                                                                                                                                                                                                   |
|--------|--------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `[x]`  | Destinations | Move Destinations from legacy HuKI                                                                                                                                                                                                                                                                                     |
| `[x]`  | Destinations | Add popularity [1..10] for destinations                                                                                                                                                                                                                                                                                |
| `[x]`  | Destinations | Add new Destinations                                                                                                                                                                                                                                                                                                   |
| `[x]`  | Destinations | Show Destinations in Search in a section named Destinations/Kirándulóhelyek. Dont handle "See all" yet.                                                                                                                                                                                                                |
| `[x]`  | Destinations | Show Destination items. Its a horizontally scrollable list. Show top 20 based on popularity.                                                                                                                                                                                                                           |
| `[x]`  | Destinations | Use @DestinationType for icon/text. Use Destination.name, town, description. Initial state is description is line=1                                                                                                                                                                                                    |
| `[x]`  | Destinations | Chevron button on top right corner. On click scroll up the text so the description is visible with multiple lines. Also hide the category to have more space te read.                                                                                                                                                  |
| `[x]`  | Destinations | Card background color is @DestinationType.color. There is a black gradient from top to bottom so the White texts in the bottom are more visible.                                                                                                                                                                       |
| `[x]`  | Destinations | When clicking inside the card, fire a new Event SearchDestiantionSelected and move camera to its location.                                                                                                                                                                                                             |
| `[x]`  | Destinations | ATM "Powered by LocationIq" is always displayed. Remove it for search default (no search results) state. Only show when real LIQ search started with loading or results.                                                                                                                                               |
| `[x]`  | Destinations | @DestinationRepository. Create an order logic for top 20 Search-Destinations: Weight by Distance(if my location is available)+Popularity+Type(prefer multiple types to avoid repetitive categories like 3 PEAKs next to each other). Also do some randomization not to show the same destinations over and over again. |

