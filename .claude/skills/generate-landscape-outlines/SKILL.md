---
name: generate-landscape-outlines
description: Generate the bundled landscape outlines, label points and Hungary mask GeoJSON for the HuKi map from OpenStreetMap or hand-drawn outlines. Use when adding a landscape to Landscapes.kt, fixing or smoothing a landscape outline or label position, or regenerating landscapes.geojson / landscape_labels.geojson / hungary_mask.geojson.
---

# Landscape outlines

`tools/python/generate_landscape_outlines.py` writes three files to `shared/src/commonMain/moko-resources/files/`.
They are generated, so never edit them by hand:

- **`landscapes.geojson`**: one Polygon/MultiPolygon per landscape, with `properties.id` = `Landscape.id`.
- **`landscape_labels.geojson`**: one Point per landscape inside its largest piece, so a split landscape gets one
  label. The point balances depth (distance to the edge, `LABEL_MIN_DEPTH_RATIO`) against closeness to the centroid.
- **`hungary_mask.geojson`**: a world-sized polygon with Hungary's border (`HUNGARY_SOURCE`, relation 21335) as its
  hole, used to dim the map outside the country.

The landscape ids are read from `shared/**/data/Landscapes.kt`. The script fails when a landscape has no source, or
a source has no landscape.

## Pipeline

- Relation member ways are stitched into closed rings. Only `outer` rings are kept, because a landscape fill has
  no holes.
- Rings are simplified (Douglas-Peucker, `SIMPLIFY_TOLERANCE_M`). Islands under `MIN_RING_AREA_KM2` are dropped.
- Corners are rounded with Chaikin's algorithm (`CHAIKIN_ITERATIONS`) and then thinned again
  (`SMOOTHED_SIMPLIFY_TOLERANCE_M`), so the outlines read as natural edges.
- Coordinates are rounded to `COORDINATE_DECIMALS` (4 decimals, about 10 m). Rings run counter-clockwise
  (RFC 7946), so a line offset always shifts to the same side of the edge.

## Sources (per `Landscape.id`)

1. **`OSM_SOURCES`**: `"<id>": ("way" | "relation", <osm id>)`. Most landscapes are closed landform ways; some
   are relations (e.g. a national park). Add a short `# comment` when the OSM element isn't named after the landscape
   (e.g. `# Közép-Zalai-dombság`).
2. **Hand-drawn**: `tools/osm/landscape_hand_drawn_outlines/<id>.geojson`. Use it for landscapes OSM has no usable
   area for, e.g. the Alföld, Gemenc or the Fertő region. It's a FeatureCollection with a Polygon/MultiPolygon feature
   whose `properties` hold `id`, `name` and `source` (where the shape comes from, e.g. "east: OSM Duna centreline
   (ODbL) +~400m, west: hand-drawn floodplain margin"). It's smoothed and rounded, but not simplified, and it wins
   over an OSM source with the same id.

## Adding or fixing a landscape

1. **Find the area.** Look up the landform by name near the landscape:
   ```
   [out:json][timeout:60];(way(around:20000,<lat>,<lon>)[natural][name~"<name>",i];relation(around:20000,<lat>,<lon>)[name~"<name>",i][type~"multipolygon|boundary"];);out tags center;
   ```
   Prefer the closed landform area over admin boundaries. Check the shape on openstreetmap.org before using it.
2. **No usable area?** Draw the outline by hand (e.g. on geojson.io), tracing OSM features such as rivers, lake
   shores or forest edges where you can. Save it as `<id>.geojson` in `tools/osm/landscape_hand_drawn_outlines/`, and
   record what it's based on in `source`.
3. **Run:** `python3 tools/python/generate_landscape_outlines.py`. `--save r.json` keeps the Overpass response, and
   `--input r.json` reuses it. That's enough when only a hand-drawn outline or a tuning constant changed. Overpass
   is often overloaded; the script retries across instances.
4. **Review the log:** each landscape prints its geometry type, point count and source, then each file prints its
   size. A sudden jump in points or a MultiPolygon where you expected one piece means the wrong element. Keep
   `landscapes.geojson` small (~80 KB today), because it's bundled with the app.
5. **Check it on the map:** run the app on both platforms and look at the outline, the fill and the label position,
   with neighbouring landscapes in view.
6. **Tests:** run `./gradlew :shared:testAndroidHostTest`, plus `DefaultLandscapeRepositoryTest`
   (`composeApp/src/androidTest`, needs a device). It checks that every landscape has an outline and a label.
