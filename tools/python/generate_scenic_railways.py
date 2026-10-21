#!/usr/bin/env python3
"""Generate the bundled scenic (narrow-gauge) railway GPX files from OpenStreetMap.

Every railway in `OSM_SOURCES` is an OSM train route relation plus its stops, in timetable order (first is the start):

- the line is built like the educational trails (chained, simplified, EU-DEM elevation), see
  `generate_educational_trails.py`,
- each stop is geocoded by name with Nominatim, inside the line's bounding box, and the first result is taken as is:
  forest railway stops are places in nature (a spring, a hut), not always a station node,
- the stops must follow the line in timetable order, and lie close to it, otherwise the script fails.

The output is one `rail_<railway>.gpx` per railway in `moko-resources/files/`, with the stops as `<wpt>`:

    python3 tools/python/generate_scenic_railways.py

To regenerate from a saved Overpass response, pass `--input response.json` (`--save response.json` stores a fresh one).
"""

import argparse
import json
import math
import pathlib
import sys
import time
import urllib.parse
import urllib.request
from xml.sax.saxutils import escape, quoteattr

from generate_educational_trails import (
    COORDINATE_DECIMALS,
    ELEVATION_DECIMALS,
    OUTPUT_DIR,
    USER_AGENT,
    distance_m,
    fetch_elevations,
    fetch_overpass,
    relation_lines,
    to_xy,
)

OUTPUT_PREFIX = "rail_"

NOMINATIM_URL = "https://nominatim.openstreetmap.org/search"
# Nominatim usage policy: max 1 request per second
NOMINATIM_REQUEST_DELAY_S = 1.1
# Search area around the line's bounding box, in degrees
GEOCODE_PADDING_DEG = 0.01
MAX_STOP_DISTANCE_M = 500

# Railway slug (output file `rail_<slug>.gpx`) -> OSM train route relation id (in timetable direction) and stop names.
OSM_SOURCES = {
    "felsotarkany": (
        2372022,
        ["Felsőtárkány Fűtőház", "Felsőtárkány Szikla-forrás", "Egeresvölgy - Varróház", "Stimecz-ház"],
    ),
}


def overpass_query():
    ids = ",".join(str(relation_id) for relation_id, _ in OSM_SOURCES.values())
    return f"[out:json][timeout:180];relation(id:{ids});out geom;"


def geocode(name, points):
    lons, lats = [lon for lon, _ in points], [lat for _, lat in points]
    viewbox = (
        min(lons) - GEOCODE_PADDING_DEG,
        max(lats) + GEOCODE_PADDING_DEG,
        max(lons) + GEOCODE_PADDING_DEG,
        min(lats) - GEOCODE_PADDING_DEG,
    )
    query = {
        # "Egeresvölgy - Varróház" is "Egeresvölgy-Varróház" in OSM
        "q": name.replace(" - ", " "),
        "countrycodes": "hu",
        "viewbox": ",".join(str(value) for value in viewbox),
        "bounded": 1,
        "format": "jsonv2",
        "limit": 1,
    }
    request = urllib.request.Request(f"{NOMINATIM_URL}?{urllib.parse.urlencode(query)}", headers={"User-Agent": USER_AGENT})
    with urllib.request.urlopen(request, timeout=60) as response:
        results = json.loads(response.read().decode())
    time.sleep(NOMINATIM_REQUEST_DELAY_S)
    if not results:
        sys.exit(f"Nominatim: no result for '{name}'")
    result = results[0]
    return (float(result["lon"]), float(result["lat"])), f"{result['category']}={result['type']}"


def project(point, points):
    """Returns the distance along the line of the point's nearest spot on it, and the point's distance from it."""
    px, py = to_xy(*point)
    best_offset, best_along, along = math.inf, 0.0, 0.0
    for a, b in zip(points, points[1:]):
        (ax, ay), (bx, by) = to_xy(*a), to_xy(*b)
        dx, dy = bx - ax, by - ay
        length = math.hypot(dx, dy)
        t = 0.0 if length == 0 else max(0.0, min(1.0, ((px - ax) * dx + (py - ay) * dy) / (length * length)))
        offset = math.hypot(px - ax - t * dx, py - ay - t * dy)
        if offset < best_offset:
            best_offset, best_along = offset, along + t * length
        along += length
    return best_along, best_offset


def railway_stops(railway, names, lines):
    """Geocodes the stops and flips the line if needed, so the track runs from the first stop to the last."""
    points = [point for line in lines for point in line]
    stops = []
    for name in names:
        location, osm_type = geocode(name, points)
        along, offset = project(location, points)
        print(f"{railway}: {name} -> {osm_type}, {offset:.0f} m from the line, at {along / 1000:.2f} km")
        if offset > MAX_STOP_DISTANCE_M:
            sys.exit(f"{railway}: '{name}' is {offset:.0f} m from the line, check the stop name")
        stops.append((name, location, along))
    positions = [along for _, _, along in stops]
    if positions == sorted(positions, reverse=True):
        lines = [line[::-1] for line in reversed(lines)]
    elif positions != sorted(positions):
        sys.exit(f"{railway}: the stops are not in line order: {positions}")
    return lines, [(name, location) for name, location, _ in stops]


def to_gpx(relation, lines, stops, elevations):
    tags = relation["tags"]
    relation_url = f"https://www.openstreetmap.org/relation/{relation['id']}"
    name = f"{stops[0][0]} – {stops[-1][0]}"
    elevations = iter(elevations)
    waypoints = [
        f'  <wpt lat="{lat:.{COORDINATE_DECIMALS}f}" lon="{lon:.{COORDINATE_DECIMALS}f}">'
        f"<ele>{round(next(elevations), ELEVATION_DECIMALS)}</ele><name>{escape(stop)}</name></wpt>"
        for stop, (lon, lat) in stops
    ]
    segments = []
    for line in lines:
        points = [
            f'      <trkpt lat="{lat:.{COORDINATE_DECIMALS}f}" lon="{lon:.{COORDINATE_DECIMALS}f}">'
            f"<ele>{round(next(elevations), ELEVATION_DECIMALS)}</ele></trkpt>"
            for lon, lat in line
        ]
        segments.append("    <trkseg>\n" + "\n".join(points) + "\n    </trkseg>")
    return f"""<?xml version="1.0" encoding="UTF-8"?>
<gpx xmlns="http://www.topografix.com/GPX/1/1" version="1.1" creator="HuKi generate_scenic_railways.py">
  <metadata>
    <name>{escape(name)}</name>
    <copyright author="OpenStreetMap contributors"><license>https://opendatacommons.org/licenses/odbl/</license></copyright>
  </metadata>
{chr(10).join(waypoints)}
  <trk>
    <name>{escape(name)}</name>
    <src>OpenStreetMap, elevation: EU-DEM v1.1</src>
    <link href={quoteattr(relation_url)}/>
    <type>{escape(tags.get("route", "train"))}</type>
{chr(10).join(segments)}
  </trk>
</gpx>
"""


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--input", type=pathlib.Path, help="use a saved Overpass JSON response")
    parser.add_argument("--save", type=pathlib.Path, help="store the fetched Overpass JSON response")
    args = parser.parse_args()

    response = json.loads(args.input.read_text()) if args.input else fetch_overpass(overpass_query())
    if args.save:
        args.save.write_text(json.dumps(response))

    relations = {element["id"]: element for element in response["elements"] if element["type"] == "relation"}
    for railway, (relation_id, names) in OSM_SOURCES.items():
        relation = relations.get(relation_id)
        if relation is None:
            sys.exit(f"{railway}: relation {relation_id} missing from the Overpass response")
        lines, stops = railway_stops(railway, names, relation_lines(relation))
        points = [point for line in lines for point in line]
        elevations = fetch_elevations([location for _, location in stops] + points)
        output = OUTPUT_DIR / f"{OUTPUT_PREFIX}{railway}.gpx"
        output.write_text(to_gpx(relation, lines, stops, elevations))
        length_km = sum(distance_m(a, b) for line in lines for a, b in zip(line, line[1:])) / 1000
        print(
            f"{railway}: {len(lines)} segment(s), {len(points)} points, {len(stops)} stops, {length_km:.1f} km, "
            f"{output.stat().st_size / 1024:.1f} KB"
        )


if __name__ == "__main__":
    main()
