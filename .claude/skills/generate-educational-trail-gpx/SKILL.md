---
name: generate-educational-trail-gpx
description: Generate bundled route GPX files (track + elevation + jel marking) for HuKi educational trails (tanösvények) from OpenStreetMap or turistautak.openstreetmap.hu. Use when adding, regenerating, fixing or removing an `edut_*.gpx` route, or wiring one to an EDUCATIONAL_TRAIL destination's "Open route" button.
---

# Educational trail (tanösvény) GPX

`tools/python/generate_educational_trails.py` writes `shared/src/commonMain/moko-resources/files/edut_<slug>.gpx`
for every configured trail. `generate_scenic_railways.py` imports its pipeline, so keep the function names stable.

- **Track:** chained into ordered lines, merged where the ends touch, ordered for the smallest total gap, then
  simplified (Douglas-Peucker, 3 m). A gap starts a new `<trkseg>`. The app flattens all segments into one line, so a
  gap shows as a straight connector. The script prints the segment count; check that the gaps are short (< ~250 m).
- **Elevation:** EU-DEM 25 m via the OpenTopoData public API (100 points per request, 1 request per second).
- **Marking:** the OSM `jel` tag goes into the track `<type>` (e.g. `zt`). The app maps it to `JelSymbol` and shows
  it next to the GPX details title and inside the start/end/round-trip markers. A `jel` with no `JelSymbol` entry
  (e.g. `ztfl`) just shows no icon.

## Sources (pick the first that exists)

1. **`OSM_SOURCES`**: `"<slug>": <relation id>`, an OSM `route=hiking` relation. Fetched in one Overpass query.
2. **`PLANNER_SOURCES`**: `"<slug>": ("<name>", "<saved route id>")`, a saved route of the
   turistautak.openstreetmap.hu planner (`https://turistautak.openstreetmap.hu/mentettutv-<id>`). It routes over OSM
   data, so the track stays ODbL. Other GPX sources (e.g. kirandulastervezo.hu, natureparks' own files) are not free
   to bundle; don't copy them.
3. **`WAY_SOURCES`**: `"<slug>": ("<name>", [way ids], <start node>, <end node>)`. The track is the shortest walk
   between the two nodes over the given ways (OSM API, no Overpass). Use it for trails only mapped as ways, e.g. a
   boardwalk.

If none of these has the trail, leave the destination without a route rather than drawing one by hand.

## Adding a trail

1. **Is it still walkable?** Before adding a route, check that the trail is maintained and open. Search for recent
   news (closures, rebuilds, floods) and visitor comments, e.g. on kirandulastervezo.hu (green = positive,
   red = negative comments). An abandoned or closed trail is a trap for hikers: ask the user whether to drop the
   destination instead (see **Removing a trail**).
2. **Find the relation.** Use the destination's coordinates from `Destinations.kt`. Overpass often times out on
   broad queries, so query one trail at a time:
   ```
   [out:json][timeout:60];rel(around:2500,<lat>,<lon>)[type=route];out tags center;
   ```
   Ignore bus/bicycle/road relations. Name matches can be loose (e.g. "Fényes-forrás tanösvény" vs "Tatai Fényes
   tanösvény"); compare the route with the trail's official length and description.
3. **No relation?** Search the planner's saved routes: POST `queryString=<name fragment>` to
   `https://turistautak.openstreetmap.hu/mutvhop.php`. Hits are `href="mentettutv-<id>"` (ids are alphanumeric, not
   only digits). The track is at `mentettutv/<id>/shortest-track.gpx`. Check that it passes near the destination
   and its length is plausible. The user may also paste a saved-route link.
4. **Still nothing?** Look for ways named after the trail
   (`way(around:1500,<lat>,<lon>)[name~"tanösvény|sétány",i];out tags;`) and use `WAY_SOURCES`. The OSM API is a fast
   fallback when Overpass is down: `https://api.openstreetmap.org/api/0.6/map.json?bbox=<w>,<s>,<e>,<n>` and
   `.../way/<id>/full.json`.
5. **Config:** add the entry. The slug is the trail name in snake_case without accents
   (`Vértesi panoráma tanösvény` → `vertesi_panorama_tanosveny`).
6. **Run:** `python3 tools/python/generate_educational_trails.py`. `--save r.json` keeps the Overpass response and
   `--input r.json` reuses it, so re-runs only hit the planner, OSM API and elevation APIs. The script retries across
   Overpass instances; run it in the background if it's slow. Expect about 1 s per 100 points for the elevations.
7. **Review the log:** one line per trail with segments, points, length, `jel` and file size. Compare the length with
   the official one, and check that segment gaps are short.

## Wiring to the app

- Set `routeGpxFile = SharedRes.files.edut_<slug>_gpx` on the `EDUCATIONAL_TRAIL` destination in `Destinations.kt`,
  right after its `description` line. This alone shows the "Open route" button in the destination info window.
- Only commit `edut_*.gpx` files that a destination references.
- `EducationalTrailGpxTest` (`shared/src/androidHostTest/.../data/`) checks that every `edut_*.gpx` decodes and that
  every point has an elevation. Run `./gradlew :shared:testAndroidHostTest`.
- Update the educational trail item in `tools/planning/PLANNING.md` (trail count, skipped trails and why).

## Removing a trail

When a trail is abandoned or closed for good, and the user agrees:

- Delete the `Destination` block from `Destinations.kt`, its description strings from both
  `strings_destinations.xml` files (base and `hu`), its source entry from the script, and its `edut_*.gpx`.
- Never reuse its `Destination.id`. Keep its entry in the frozen map in `database/migration/Migration1To2.kt`.
- `git grep` the slug and the destination id to check that nothing still references it.
