#!/usr/bin/env python3
"""Generate the bundled landscape outlines (GeoJSON) from OpenStreetMap.

Every landscape in `shared/**/data/Landscapes.kt` is drawn on the map as a filled / stroked outline.
The outlines come from the OSM elements listed in `OSM_SOURCES` below, fetched with one Overpass query,
then:

- relation member ways are stitched into closed rings (only `outer` rings are kept: a landscape fill
  has no holes),
- rings are simplified (Douglas-Peucker, `SIMPLIFY_TOLERANCE_M`) and tiny islands are dropped,
- corners are rounded off with Chaikin's algorithm (`CHAIKIN_ITERATIONS`), then the extra points are
  thinned again (`SMOOTHED_SIMPLIFY_TOLERANCE_M`), so outlines read as natural edges, not polygons,
- coordinates are rounded to `COORDINATE_DECIMALS` (~10 m),
- rings run counter-clockwise (RFC 7946), so a line offset always shifts to the same side of the edge.

Landscapes that OSM has no usable area for are hand-drawn GeoJSON files in
`tools/osm/landscape_hand_drawn_outlines/<id>.geojson` (a Polygon/MultiPolygon feature with `properties.id`).

The output is a FeatureCollection with one feature per landscape, `properties.id` = `Landscape.id`.
A label file holds one Point per landscape, inside its largest piece, so split landscapes get a
single name. The Hungary mask is a world-sized polygon with Hungary's border as its hole, so the
map outside the country can be dimmed:

    python3 tools/python/generate_landscape_outlines.py

Overpass is often overloaded; the script retries over a few public instances. To regenerate from a
saved response instead, pass `--input response.json` (`--save response.json` stores a fresh one).
"""

import argparse
import json
import math
import pathlib
import re
import sys
import time
import urllib.parse
import urllib.request

ROOT = pathlib.Path(__file__).resolve().parents[2]
LANDSCAPES_KT = ROOT / "shared/src/commonMain/kotlin/hu/mostoha/mobile/kmp/huki/data/Landscapes.kt"
HAND_DRAWN_OUTLINES_DIR = ROOT / "tools/osm/landscape_hand_drawn_outlines"
OUTPUT = ROOT / "shared/src/commonMain/moko-resources/files/landscapes.geojson"
LABELS_OUTPUT = ROOT / "shared/src/commonMain/moko-resources/files/landscape_labels.geojson"
LABEL_GRID_STEPS = 60
# The deepest point alone drifts into the widest lobe; the centroid alone can fall near an edge
LABEL_MIN_DEPTH_RATIO = 0.5
MASK_OUTPUT = ROOT / "shared/src/commonMain/moko-resources/files/hungary_mask.geojson"
HUNGARY_SOURCE = ("relation", 21335)
MASK_SIMPLIFY_TOLERANCE_M = 300
# Web Mercator stops at ~85.05°, a larger outer ring is clipped anyway
WORLD_RING = [[-180, -85], [180, -85], [180, 85], [-180, 85], [-180, -85]]

OVERPASS_URLS = [
    "https://overpass-api.de/api/interpreter",
    "https://overpass.kumi.systems/api/interpreter",
    "https://overpass.private.coffee/api/interpreter",
]

SIMPLIFY_TOLERANCE_M = 150
CHAIKIN_ITERATIONS = 3
SMOOTHED_SIMPLIFY_TOLERANCE_M = 50
MIN_RING_AREA_KM2 = 1.0
COORDINATE_DECIMALS = 4

# Landscape.id -> OSM element the outline is taken from.
OSM_SOURCES = {
    "aggteleki_karszt": ("way", 279660398),
    "bakony": ("way", 279665156),
    "balaton_felvidek": ("way", 279665387),
    "borzsony": ("way", 279564162),
    "budai_hegyseg": ("relation", 3716160),
    "bukk": ("way", 279573777),
    "cserhat": ("way", 279590728),
    "gerecse_hegyseg": ("way", 279663379),
    "hortobagy": ("relation", 6503266),  # Hortobágy National Park
    "keszthelyi_hegyseg": ("way", 279665573),
    "koszegi_hegyseg": ("way", 300323308),
    "matra": ("way", 279583932),
    "mecsek": ("way", 279665961),
    "orseg": ("relation", 8545511),  # Őrség National Park
    "pilis_hegyseg": ("way", 279561562),
    "soproni_hegyseg": ("relation", 11073175),
    "szigetkoz": ("way", 380535854),
    "velencei_hegyseg": ("way", 279664160),
    "vertes": ("way", 279663918),
    "villanyi_hegyseg": ("way", 279667079),
    "visegradi_hegyseg": ("way", 279561563),
    "zalai_dombsag": ("way", 380535667),  # Közép-Zalai-dombság
    "zempleni_hegyseg": ("way", 279656184),
}


def overpass_query():
    ids = {"way": [], "relation": []}
    for osm_type, osm_id in [*OSM_SOURCES.values(), HUNGARY_SOURCE]:
        ids[osm_type].append(str(osm_id))
    return (
        "[out:json][timeout:180];("
        f"way(id:{','.join(ids['way'])});"
        f"relation(id:{','.join(ids['relation'])});"
        ");out geom;"
    )


def fetch_overpass():
    data = urllib.parse.urlencode({"data": overpass_query()}).encode()
    for attempt in range(3):
        for url in OVERPASS_URLS:
            request = urllib.request.Request(url, data=data, headers={"User-Agent": "HuKi-tools/1.0"})
            try:
                with urllib.request.urlopen(request, timeout=240) as response:
                    body = response.read().decode()
                if body.lstrip().startswith("{"):
                    return json.loads(body)
                print(f"Overpass busy: {url}", file=sys.stderr)
            except OSError as error:
                print(f"Overpass failed: {url}: {error}", file=sys.stderr)
        time.sleep(10 * (attempt + 1))
    sys.exit("Overpass: no instance answered, retry later or use --input")


def to_xy(lon, lat):
    """Equirectangular metres around Hungary's latitude; accurate enough for simplification and areas."""
    return lon * 111_320 * math.cos(math.radians(47.2)), lat * 110_574


def ring_area_km2(ring):
    xy = [to_xy(*p) for p in ring]
    return abs(sum(x1 * y2 - x2 * y1 for (x1, y1), (x2, y2) in zip(xy, xy[1:]))) / 2 / 1e6


def simplify(ring, tolerance_m):
    xy = [to_xy(*p) for p in ring]
    keep = [False] * len(ring)
    keep[0] = keep[-1] = True
    stack = [(0, len(ring) - 1)]
    while stack:
        start, end = stack.pop()
        (ax, ay), (bx, by) = xy[start], xy[end]
        dx, dy = bx - ax, by - ay
        length_sq = dx * dx + dy * dy
        max_dist, index = 0.0, None
        for i in range(start + 1, end):
            px, py = xy[i]
            t = 0.0 if length_sq == 0 else max(0.0, min(1.0, ((px - ax) * dx + (py - ay) * dy) / length_sq))
            dist = math.hypot(px - ax - t * dx, py - ay - t * dy)
            if dist > max_dist:
                max_dist, index = dist, i
        if index is not None and max_dist > tolerance_m:
            keep[index] = True
            stack += [(start, index), (index, end)]
    return [p for p, k in zip(ring, keep) if k]


def chaikin(ring, iterations):
    for _ in range(iterations):
        smoothed = []
        for (x1, y1), (x2, y2) in zip(ring, ring[1:]):
            smoothed += [(0.75 * x1 + 0.25 * x2, 0.75 * y1 + 0.25 * y2), (0.25 * x1 + 0.75 * x2, 0.25 * y1 + 0.75 * y2)]
        ring = smoothed + smoothed[:1]
    return ring


def smooth(ring):
    return simplify(chaikin(ring, CHAIKIN_ITERATIONS), SMOOTHED_SIMPLIFY_TOLERANCE_M)


def stitch_rings(ways):
    """Joins open ways sharing endpoints into closed rings; unclosable chains are dropped."""
    ways = [list(w) for w in ways if len(w) > 1]
    rings = []
    while ways:
        ring = ways.pop()
        while ring[0] != ring[-1]:
            for i, way in enumerate(ways):
                if way[0] == ring[-1]:
                    ring += way[1:]
                elif way[-1] == ring[-1]:
                    ring += way[-2::-1]
                elif way[-1] == ring[0]:
                    ring = way[:-1] + ring
                elif way[0] == ring[0]:
                    ring = way[:0:-1] + ring
                else:
                    continue
                ways.pop(i)
                break
            else:
                print(f"  dropped an unclosed chain of {len(ring)} points", file=sys.stderr)
                ring = None
                break
        if ring:
            rings.append(ring)
    return rings


def element_rings(element):
    if element["type"] == "way":
        return stitch_rings([[(g["lon"], g["lat"]) for g in element["geometry"]]])
    outer_ways = [
        [(g["lon"], g["lat"]) for g in member["geometry"]]
        for member in element["members"]
        if member["type"] == "way" and member["role"] == "outer" and "geometry" in member
    ]
    return stitch_rings(outer_ways)


def counter_clockwise(ring):
    signed_area = sum(x1 * y2 - x2 * y1 for (x1, y1), (x2, y2) in zip(ring, ring[1:]))
    return ring if signed_area > 0 else ring[::-1]


def round_ring(ring):
    rounded = []
    for lon, lat in ring:
        point = [round(lon, COORDINATE_DECIMALS), round(lat, COORDINATE_DECIMALS)]
        if not rounded or rounded[-1] != point:
            rounded.append(point)
    return rounded


def outline_geometry(rings):
    polygons = []
    for ring in rings:
        if ring_area_km2(ring) < MIN_RING_AREA_KM2:
            continue
        simplified = counter_clockwise(round_ring(smooth(simplify(ring, SIMPLIFY_TOLERANCE_M))))
        if len(simplified) >= 4:
            polygons.append([simplified])
    if not polygons:
        return None
    if len(polygons) == 1:
        return {"type": "Polygon", "coordinates": polygons[0]}
    return {"type": "MultiPolygon", "coordinates": polygons}


def point_in_ring(x, y, ring):
    inside = False
    for (x1, y1), (x2, y2) in zip(ring, ring[1:]):
        if (y1 > y) != (y2 > y) and x < (x2 - x1) * (y - y1) / (y2 - y1) + x1:
            inside = not inside
    return inside


def distance_to_ring_m(x, y, ring):
    px, py = to_xy(x, y)
    best = math.inf
    for a, b in zip(ring, ring[1:]):
        (ax, ay), (bx, by) = to_xy(*a), to_xy(*b)
        dx, dy = bx - ax, by - ay
        length_sq = dx * dx + dy * dy
        t = 0.0 if length_sq == 0 else max(0.0, min(1.0, ((px - ax) * dx + (py - ay) * dy) / length_sq))
        best = min(best, math.hypot(px - ax - t * dx, py - ay - t * dy))
    return best


def label_point(geometry):
    """Grid point of the largest piece closest to its centroid, among those at least half as deep as the deepest."""
    polygons = [geometry["coordinates"]] if geometry["type"] == "Polygon" else geometry["coordinates"]
    ring = max((polygon[0] for polygon in polygons), key=ring_area_km2)
    lons, lats = [p[0] for p in ring], [p[1] for p in ring]
    candidates = []
    for i in range(LABEL_GRID_STEPS + 1):
        for j in range(LABEL_GRID_STEPS + 1):
            x = min(lons) + (max(lons) - min(lons)) * i / LABEL_GRID_STEPS
            y = min(lats) + (max(lats) - min(lats)) * j / LABEL_GRID_STEPS
            if point_in_ring(x, y, ring):
                candidates.append((distance_to_ring_m(x, y, ring), x, y))
    deepest = max(candidate[0] for candidate in candidates)
    cx, cy = centroid_xy(ring)

    def centroid_distance(candidate):
        x, y = to_xy(candidate[1], candidate[2])
        return math.hypot(x - cx, y - cy)

    deep_enough = [candidate for candidate in candidates if candidate[0] >= LABEL_MIN_DEPTH_RATIO * deepest]
    _, x, y = min(deep_enough, key=centroid_distance)
    return [round(x, COORDINATE_DECIMALS), round(y, COORDINATE_DECIMALS)]


def centroid_xy(ring):
    xy = [to_xy(*p) for p in ring]
    area = cx = cy = 0.0
    for (x1, y1), (x2, y2) in zip(xy, xy[1:]):
        cross = x1 * y2 - x2 * y1
        area += cross
        cx += (x1 + x2) * cross
        cy += (y1 + y2) * cross
    return cx / (3 * area), cy / (3 * area)


def mask_geometry(element):
    border = max(element_rings(element), key=ring_area_km2)
    hole = round_ring(simplify(border, MASK_SIMPLIFY_TOLERANCE_M))
    return {"type": "Polygon", "coordinates": [WORLD_RING, hole]}


def load_hand_drawn_outlines():
    hand_drawn_outlines = {}
    for path in sorted(HAND_DRAWN_OUTLINES_DIR.glob("*.geojson")):
        for feature in json.loads(path.read_text())["features"]:
            geometry = feature["geometry"]
            if geometry["type"] not in ("Polygon", "MultiPolygon"):
                continue
            rings = geometry["coordinates"] if geometry["type"] == "Polygon" else [p[0] for p in geometry["coordinates"]]
            coordinates = [[counter_clockwise(round_ring(smooth(ring)))] for ring in rings]
            hand_drawn_outlines[feature["properties"]["id"]] = (
                {"type": "Polygon", "coordinates": coordinates[0]}
                if len(coordinates) == 1
                else {"type": "MultiPolygon", "coordinates": coordinates}
            )
    return hand_drawn_outlines


def point_count(geometry):
    polygons = [geometry["coordinates"]] if geometry["type"] == "Polygon" else geometry["coordinates"]
    return sum(len(ring) for polygon in polygons for ring in polygon)


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--input", type=pathlib.Path, help="use a saved Overpass JSON response")
    parser.add_argument("--save", type=pathlib.Path, help="store the fetched Overpass JSON response")
    args = parser.parse_args()

    landscape_ids = re.findall(r'^\s+id = "([a-z_]+)",', LANDSCAPES_KT.read_text(), re.M)
    hand_drawn_outlines = load_hand_drawn_outlines()

    unknown = (set(OSM_SOURCES) | set(hand_drawn_outlines)) - set(landscape_ids)
    if unknown:
        sys.exit(f"Not in Landscapes.kt: {sorted(unknown)}")
    missing = set(landscape_ids) - set(OSM_SOURCES) - set(hand_drawn_outlines)
    if missing:
        sys.exit(f"No OSM source or hand-drawn outline for: {sorted(missing)}")

    if args.input:
        response = json.loads(args.input.read_text())
    else:
        response = fetch_overpass()
        if args.save:
            args.save.write_text(json.dumps(response, ensure_ascii=False))
    elements = {(e["type"], e["id"]): e for e in response["elements"]}

    features = []
    for landscape_id in landscape_ids:
        if landscape_id in hand_drawn_outlines:
            geometry, source = hand_drawn_outlines[landscape_id], "hand-drawn"
        else:
            key = OSM_SOURCES[landscape_id]
            if key not in elements:
                sys.exit(f"{landscape_id}: OSM {key[0]} {key[1]} missing from the Overpass response")
            geometry, source = outline_geometry(element_rings(elements[key])), f"OSM {key[0]} {key[1]}"
            if geometry is None:
                sys.exit(f"{landscape_id}: no closed outer ring in {source}")
        print(f"{landscape_id:<24} {geometry['type']:<12} {point_count(geometry):>5} points  ({source})")
        features.append({"type": "Feature", "properties": {"id": landscape_id}, "geometry": geometry})

    collection = {"type": "FeatureCollection", "features": features}
    OUTPUT.write_text(json.dumps(collection, separators=(",", ":")) + "\n")
    print(f"Wrote {OUTPUT.relative_to(ROOT)} ({OUTPUT.stat().st_size / 1024:.1f} KB)")

    labels = [
        {
            "type": "Feature",
            "properties": feature["properties"],
            "geometry": {"type": "Point", "coordinates": label_point(feature["geometry"])},
        }
        for feature in features
    ]
    LABELS_OUTPUT.write_text(json.dumps({"type": "FeatureCollection", "features": labels}, separators=(",", ":")) + "\n")
    print(f"Wrote {LABELS_OUTPUT.relative_to(ROOT)} ({LABELS_OUTPUT.stat().st_size / 1024:.1f} KB)")

    if HUNGARY_SOURCE not in elements:
        sys.exit(f"Hungary: OSM {HUNGARY_SOURCE[0]} {HUNGARY_SOURCE[1]} missing from the Overpass response")
    mask = mask_geometry(elements[HUNGARY_SOURCE])
    print(f"{'hungary_mask':<24} {mask['type']:<12} {point_count(mask):>5} points")
    mask_collection = {"type": "FeatureCollection", "features": [{"type": "Feature", "properties": {}, "geometry": mask}]}
    MASK_OUTPUT.write_text(json.dumps(mask_collection, separators=(",", ":")) + "\n")
    print(f"Wrote {MASK_OUTPUT.relative_to(ROOT)} ({MASK_OUTPUT.stat().st_size / 1024:.1f} KB)")


if __name__ == "__main__":
    main()
