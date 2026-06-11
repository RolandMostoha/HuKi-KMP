# HuKi-KMP — Plan Board

## Legend

| Status | Meaning                   |
|--------|---------------------------|
| `[ ]`  | Not started               |
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
| `[ ]`  | Analytics - Google Analytics                                                                     |
| `[ ]`  | LogLevel.ALL only in debug                                                                       |
| `[ ]`  | Update Kotlin + Gradle 9                                                                         |
| `[ ]`  | Register Apple Developer Account                                                                 |
| `[ ]`  | CD on Apple Store                                                                                |
| `[ ]`  | Sonar? free for open source projects                                                             |
| `[ ]`  | GitHub smart labels, E.g.: https://github.com/balazsgerlei/ScreenLit/blob/main/README.md?plain=1 |

### Bugs

| Status | Scope | Bug                                                                                       |
|--------|-------|-------------------------------------------------------------------------------------------|
| `[ ]`  | Map   | Bug: zooming deep (17+) removes the hiking layer, it should force scale instead           |
| `[ ]`  | CI    | Bug: iOS Simulator 18 is used (preferred: 26) and only smoke test suite is runnable on CI |

### FEATURE: Map

| Status | Scope | Task                                                                                     |
|--------|-------|------------------------------------------------------------------------------------------|
| `[ ]`  | Map   | While Following or FollowingLiveCompass, show "+" -> zoom in and "-" -> zoom out buttons |
| `[ ]`  | Map   | After state restoration / app kill -> restore last camera state + last opened GPX        |

### FEATURE: My Location

| Status | Scope      | Task                                                                                                                                                                                          |
|--------|------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `[ ]`  | MyLocation | Use My Location for search result distance                                                                                                                                                    |
| `[x]`  | MyLocation | Bug: Loading state. At the moment there is no loading state while the GPS is searching for location.                                                                                          |
| `[x]`  | MyLocation | Bug: if location permission is not enabled, GPX->Start does nothing. If location permission is not granted, and there is any my location related request, first it has to ask for permission. |
| `[x]`  | MyLocation | Hide SearchBar if GPX is visible. Hide SearchBar if FollowingLiveCompass.                                                                                                                     |
| `[x]`  | MyLocation | Reset pinch as well clicking on compass (Mapbox driven) icon                                                                                                                                  |

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
| `[ ]`  | GPX   | Save all imported GPX files in app's local sandbox, so GPX files can be reused  |
| `[ ]`  | GPX   | Wire iOS file picker error branch to ViewModel                                  |
| `[ ]`  | GPX   | Display start and end location: "Around Bükk..." -> on import we can do geocode |
| `[ ]`  | GPX   | Colored GPX                                                                     |
| `[ ]`  | GPX   | Display waypoint comments in a window                                           |

### FEATURE: GPX Info Panel (remaining time, distance, elevation gain/loss)

| Status | Scope    | Task                                           |
|--------|----------|------------------------------------------------|
| `[ ]`  | GpxPanel | Use GPX panel on Start / GPX Details -> hidden |
| `[ ]`  | GpxPanel | Elapsed time                                   |
| `[ ]`  | GpxPanel | Distance from End                              |
| `[ ]`  | GpxPanel | AVG Speed                                      |
| `[ ]`  | GpxPanel | Expected arrival based on dist/AVG speed       |

### FEATURE: GPX Collection

Show a GPX Collection menu button in Settings.

| Status | Scope          | Task                                                                                      |
|--------|----------------|-------------------------------------------------------------------------------------------|
| `[ ]`  | GPX Collection | Show a GPX Collection menu button in Settings.                                            |
| `[ ]`  | GPX Collection | Show a new screen "GPX Collection" on clicking it.                                        |
| `[ ]`  | GPX Collection | Show all imported GPX file in a list view. (File Sandbox saving is pre-requisite)         |
| `[ ]`  | GPX Collection | In the list view show the basic info for the GPX, what is already displayed in GPXDetails |

### FEATURE: Place Details (from Search + Long Tap)

- Show a bottom sheet when place details is invoked (from Search + Long Tap)
- Register long tap action, do reverse geocode with LocationIQ and show the place details

### FEATURE: Support + Billing

- Support + Billing is necessary to help me keep the app free for everyone and support the
  development.
- Google: Google Play Billing API
- Apple: Apple App Store Connect, Apple Pay

### FEATURE: Destinations

| Status | Scope        | Task                                              |
|--------|--------------|---------------------------------------------------|
| `[ ]`  | Destinations | Move Destinations from legacy HuKI                |
| `[ ]`  | Destinations | Show Destinations in Search                       |
| `[ ]`  | Destinations | Add "popularity" flag so it can be an order param |

### FEATURE: Route Planner

- Graphhopper API
- Most of the code can be reused from legacy HuKi

---

## Completed

| Feature     | Notes                                     |
|-------------|-------------------------------------------|
| Map         | Mapbox                                    |
| My location | Mapbox + Google Fused Location Provider   |
| Layers      | Mapbox Outdoor, Street, Satellite, Hiking |
| GPX         | GPX Import, added in Layers               |
| GPX Details | GPX Details Sheet with basic info         |
| Settings    | Contact, Supporters                       |
