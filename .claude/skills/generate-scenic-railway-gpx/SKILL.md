---
name: generate-scenic-railway-gpx
description: Generate bundled GPX files (line + stops as waypoints) for Hungarian scenic / narrow-gauge railways (kisvasutak, erdei vasutak) from OpenStreetMap. Use when adding or regenerating a kisvasút route GPX, or wiring one to a SCENIC_RAILWAY destination.
---

# Scenic railway (kisvasút) GPX

`tools/python/generate_scenic_railways.py` writes `shared/src/commonMain/moko-resources/files/rail_<slug>.gpx`:

- **Track:** the line from an OSM train route relation. Ways are chained, simplified and given EU-DEM elevations
  with the pipeline it imports from `generate_educational_trails.py`.
- **Waypoints:** one `<wpt>` per stop, in timetable order. Each stop name is geocoded with Nominatim inside the
  line's bounding box, and the **first result is used as is**. Forest railway stops are places in nature, so a
  `natural=spring` or `tourism=wilderness_hut` hit is correct, not a mistake.
- **Checks:** the script fails if a stop is more than 500 m from the line or the stops don't follow the line in
  order. If the stops run backwards, it flips the track so it starts at the first stop.

## Adding a railway

1. **Stop names:** open the railway's page on aktivkalandor.hu (`https://aktivkalandor.hu/kisvasut/<slug>/`) and copy
   the outbound timetable's stops in order, exactly as written. The first stop is the start. Their own GPX files have
   no stops and are OSM-derived (ODbL), so don't copy them.
2. **Relation:** find the OSM `route=train` relation whose `from`/`to` match the timetable direction. Lines often have
   one relation per direction, and branches (e.g. Mátravasút) are separate relations. Look it up by name, or find the
   relations that contain the line's ways:
   ```
   [out:json][timeout:60];way["railway"="narrow_gauge"](<south>,<west>,<north>,<east>)->.l;rel(bw.l);out tags;
   ```
   A `route=railway` relation (the physical line) works too, if no `route=train` one exists.
3. **Config:** add `"<slug>": (<relation id>, ["<first stop>", …, "<last stop>"])` to `OSM_SOURCES`.
4. **Run:** `python3 tools/python/generate_scenic_railways.py` (`--save r.json` keeps the Overpass response, and
   `--input r.json` reuses it). Overpass is often overloaded and the script retries across instances. Run it in the
   background if needed.
5. **Review the log:** for each stop it prints the OSM type of the match, the distance from the line and the
   position along it. If a match is far off or the run fails, rename the stop as it is in OSM.
   ` - ` is already rewritten to a space, e.g. "Egeresvölgy - Varróház" becomes "Egeresvölgy Varróház".

## Wiring to the app

The kisvasút route feature in the app is postponed (see `tools/planning/PLANNING.md`). When it resumes:

- Set `routeGpxFile = SharedRes.files.rail_<slug>_gpx` on the `SCENIC_RAILWAY` destination in `Destinations.kt`.
  This alone shows the generic "Open route" button in the destination info window.
- The app reads `<wpt>` `name` / `desc` / `cmt` and ignores `<type>`. It adds START/END markers at the track ends
  itself, so the first and last stops show next to those markers.
- Only commit `rail_*.gpx` files that a destination references.
