---
name: update-blue-trail-gpx
description: Update a bundled full-trail GPX file of the National Blue Trails (OKT, RPDDK, AKT / Kéktúra) and compact it with `compact_bundled_gpx.py`. Use when a new OKT / RPDDK / AKT route export is released, when swapping a `*_teljes_*.gpx` file referenced by `OktType.gpxFile`, or when a bundled blue trail GPX needs shrinking.
---

# Blue Trail (OKT / RPDDK / AKT) GPX update

Each blue trail is one bundled GPX in `shared/src/commonMain/moko-resources/files/`, referenced by `OktType.gpxFile`:

| Type  | File pattern                     | Stamp code in `<wpt><desc>` |
|-------|----------------------------------|-----------------------------|
| OKT   | `okt_teljes_bh_<yyyyMMdd>.gpx`   | `OKTPH_…`                   |
| RPDDK | `rpddk_teljes_bh_<yyyyMMdd>.gpx` | `DDKPH_…`                   |
| AKT   | `ak_teljes_bh_<yyyyMMdd>.gpx`    | `AKPH_…`                    |

The file holds the full trail as one track (with elevation) plus every stamp place as a `<wpt>`.
`DefaultOktRepository` slices the track into the sections of `OktSections.kt` (nearest point to each section's
`start` / `end`) and parses stamps from the waypoint descriptions, so the export must keep both.

## Updating a trail

1. **Download** the new full-trail GPX ("teljes") from kektura.hu (`OktType.websiteUrl`). Keep the export date in
   the file name, e.g. `okt_teljes_bh_20261105.gpx`. moko-resources turns it into `SharedRes.files.<name>_gpx`.
2. **Replace** the old file: delete it from `moko-resources/files/` and add the new one. Never keep both, as each
   one is several MB in the app bundle.
3. **Compact** it:
   ```bash
   python3 tools/python/compact_bundled_gpx.py
   ```
   With no arguments it rewrites every `*_teljes_*.gpx` in place: coordinates to 6 decimals (~11 cm), elevation
   to 0.1 m, one line per `<trkpt>`. No point is dropped, so the file shrinks by about 45% (zipped by about 55%).
   It's idempotent, so rerunning on already compact files is a no-op.
   - Don't simplify or decimate the track (Douglas-Peucker etc.). Dropped points lose elevation samples (section
     ascent / descent, elevation profile) and make trail progress and section slicing less precise. The map line
     is already simplified at runtime (`BASE_LINE_TOLERANCE`).
4. **Re-point** the code at the new file name:
   - `OktType.gpxFile` in `shared/**/model/domain/OktType.kt`
   - the `gpxFiles` map in `shared/src/androidHostTest/**/repository/DefaultOktRepositoryTest.kt`
5. **Check the export** before trusting it:
   ```bash
   F=shared/src/commonMain/moko-resources/files/okt_teljes_bh_<date>.gpx
   grep -c '<trkpt' $F; grep -c '<wpt' $F; grep -o 'OKTPH_[0-9_]*' $F | sort -u | wc -l
   ```
   Compare the counts with the old file (`git show HEAD:<old path> | grep -c …`). The stamp count changes only when
   stamp places changed. If kektura.hu also changed sections (new / moved start or end, renumbered stamps), update
   `OktSections.kt` too.
6. **Test**:
   ```bash
   ./gradlew :shared:testAndroidHostTest --tests '*DefaultOktRepositoryTest*'
   ```
   The test parses the real bundled files and checks that every section gets geometry and stamps.
   Then open each updated trail in the app (Discover → blue trails), and check a few sections and stamps on the map.
   Check the `Okt: loaded … in …` log for the load time.
