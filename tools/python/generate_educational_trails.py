#!/usr/bin/env python3
"""Generate the bundled educational trail GPX files from OpenStreetMap.

Every trail in `OSM_SOURCES` is an OSM hiking route relation, fetched with one Overpass query, then:

- the relation's member ways are chained into ordered lines (a gap starts a new `<trkseg>`),
- lines are simplified (Douglas-Peucker, `SIMPLIFY_TOLERANCE_M`),
- every point gets an elevation from the EU-DEM 25 m model via the OpenTopoData public API
  (max 100 points per request, 1 request per second),
- the trail marking goes into the track `<type>` as the OSM `jel` value (the Hungarian marking code, e.g. `zt`).

Trails without a route relation come from `PLANNER_SOURCES` (a saved route of the turistautak.openstreetmap.hu
planner, which routes over OSM data) or `WAY_SOURCES` (the shortest walk between two nodes over the given OSM ways).

The output is one `edut_<trail>.gpx` per trail in `moko-resources/files/`, referenced by `Destination.routeGpxFile`:

    python3 tools/python/generate_educational_trails.py

Overpass is often overloaded; the script retries over a few public instances. To regenerate from a
saved response instead, pass `--input response.json` (`--save response.json` stores a fresh one).
"""

import argparse
import heapq
import json
import math
import pathlib
import re
import sys
import time
import urllib.parse
import urllib.request
from xml.sax.saxutils import escape, quoteattr

ROOT = pathlib.Path(__file__).resolve().parents[2]
OUTPUT_DIR = ROOT / "shared/src/commonMain/moko-resources/files"
OUTPUT_PREFIX = "edut_"

OVERPASS_URLS = [
    "https://overpass-api.de/api/interpreter",
    "https://overpass.kumi.systems/api/interpreter",
    "https://overpass.private.coffee/api/interpreter",
]
ELEVATION_URL = "https://api.opentopodata.org/v1/eudem25m"
ELEVATION_BATCH_SIZE = 100
ELEVATION_REQUEST_DELAY_S = 1.1
USER_AGENT = "HuKi-tools/1.0"

SIMPLIFY_TOLERANCE_M = 3
# Member ways closer than this are treated as connected
JOIN_TOLERANCE_M = 15
COORDINATE_DECIMALS = 6
ELEVATION_DECIMALS = 1

# Trail name slug (output file `edut_<slug>.gpx`) -> OSM route relation id.
OSM_SOURCES = {
    "aqua_colun_tanosveny": 21405137,
    "baranypirosito_tanosveny": 6256406,
    "csiga_tura_tanosveny": 5621422,
    "csodabogyo_tanosveny": 12883946,
    "forgo_tavi_tanosveny": 8106665,
    "jagi_tanosveny": 3262881,
    "madardal_tanosveny": 8033198,
    "sag_hegy_tanosveny": 12712495,
    "siraly_tanosveny": 15258734,
    "szilvas_koi_geologiai_tanosveny": 10156055,
    "tiszavirag_tanosveny": 5362813,
    "tozegmohas_lapret_tanosveny": 6446525,
    "vertesi_panorama_tanosveny": 4713367,
}

# For trails without an OSM route relation: trail name slug -> (name, saved route id of the
# turistautak.openstreetmap.hu planner). The planner routes over OSM data, so the track stays ODbL.
PLANNER_SOURCES = {
    "fenyes_tanosveny": ("Fényes tanösvény", "155652855985im"),
    "selyem_reti_tanosveny": ("Selyem-réti tanösvény", "1569515572gwex"),
    "toreki_tavak_tanosveny": ("Töreki-tavak tanösvény", "1751744585d9te"),
}
PLANNER_URL = "https://turistautak.openstreetmap.hu/"

# For trails mapped only as OSM ways: trail name slug -> (name, way ids, start node id, end node id).
# The track is the shortest walk between the two nodes over the ways.
WAY_SOURCES = {
    "tisza_tavi_vizi_setany": (
        "Tisza-tavi Vízi Sétány",
        [117675955, 117675961, 117675978, 117675989, 492658527, 492724649],
        1325195435,
        4847129087,
    ),
}
OSM_API_URL = "https://api.openstreetmap.org/api/0.6/"


def overpass_query():
    ids = ",".join(str(osm_id) for osm_id in OSM_SOURCES.values())
    return f"[out:json][timeout:180];relation(id:{ids});out geom;"


def fetch_overpass(query):
    data = urllib.parse.urlencode({"data": query}).encode()
    for attempt in range(3):
        for url in OVERPASS_URLS:
            request = urllib.request.Request(url, data=data, headers={"User-Agent": USER_AGENT})
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
    """Equirectangular metres around Hungary's latitude; accurate enough for simplification and joins."""
    return lon * 111_320 * math.cos(math.radians(47.2)), lat * 110_540


def distance_m(a, b):
    ax, ay = to_xy(*a)
    bx, by = to_xy(*b)
    return math.hypot(ax - bx, ay - by)


def simplify(line, tolerance_m):
    if len(line) < 3:
        return line
    points = [to_xy(*point) for point in line]
    keep = [False] * len(line)
    keep[0] = keep[-1] = True
    stack = [(0, len(line) - 1)]
    while stack:
        start, end = stack.pop()
        (x1, y1), (x2, y2) = points[start], points[end]
        length = math.hypot(x2 - x1, y2 - y1)
        max_distance, max_index = 0.0, start
        for index in range(start + 1, end):
            x0, y0 = points[index]
            if length == 0:
                distance = math.hypot(x0 - x1, y0 - y1)
            else:
                distance = abs((x2 - x1) * (y1 - y0) - (x1 - x0) * (y2 - y1)) / length
            if distance > max_distance:
                max_distance, max_index = distance, index
        if max_distance > tolerance_m:
            keep[max_index] = True
            stack += [(start, max_index), (max_index, end)]
    return [point for point, kept in zip(line, keep) if kept]


def chain_ways(ways):
    """Chains the member ways in relation order, flipping each to continue the previous one."""
    lines = []
    for way in ways:
        if not lines:
            lines.append(list(way))
            continue
        line = lines[-1]
        if distance_m(line[-1], way[0]) <= JOIN_TOLERANCE_M:
            line.extend(way[1:])
        elif distance_m(line[-1], way[-1]) <= JOIN_TOLERANCE_M:
            line.extend(reversed(way[:-1]))
        elif len(lines) == 1 and distance_m(line[0], way[0]) <= JOIN_TOLERANCE_M:
            # The first way was stored backwards
            line.reverse()
            line.extend(way[1:])
        elif len(lines) == 1 and distance_m(line[0], way[-1]) <= JOIN_TOLERANCE_M:
            line.reverse()
            line.extend(reversed(way[:-1]))
        else:
            lines.append(list(way))
    return lines


def merge_lines(lines):
    """Joins lines whose ends touch, for relations with members out of order."""
    lines = [list(line) for line in lines]
    merged = True
    while merged:
        merged = False
        for i, j in ((i, j) for i in range(len(lines)) for j in range(len(lines)) if i != j):
            a, b = lines[i], lines[j]
            if distance_m(a[-1], b[0]) <= JOIN_TOLERANCE_M:
                a.extend(b[1:])
            elif distance_m(a[-1], b[-1]) <= JOIN_TOLERANCE_M:
                a.extend(reversed(b[:-1]))
            elif distance_m(a[0], b[-1]) <= JOIN_TOLERANCE_M:
                a[:0] = b[:-1]
            elif distance_m(a[0], b[0]) <= JOIN_TOLERANCE_M:
                a[:0] = reversed(b[1:])
            else:
                continue
            del lines[j]
            merged = True
            break
    return lines


def order_lines(lines):
    """
    Orders and flips the gapped lines so each one starts nearest to where the previous ended, keeping the
    start that gives the shortest gaps: the app draws a track's segments as one line.
    """

    def ordered_from(index, flipped):
        remaining = lines[:index] + lines[index + 1:]
        ordered = [lines[index][::-1] if flipped else lines[index]]
        while remaining:
            end = ordered[-1][-1]
            line = min(remaining, key=lambda line: min(distance_m(end, line[0]), distance_m(end, line[-1])))
            remaining.remove(line)
            ordered.append(line if distance_m(end, line[0]) <= distance_m(end, line[-1]) else line[::-1])
        return ordered

    def gaps_m(ordered):
        return sum(distance_m(a[-1], b[0]) for a, b in zip(ordered, ordered[1:]))

    return min((ordered_from(index, flipped) for index in range(len(lines)) for flipped in (False, True)), key=gaps_m)


def relation_lines(relation):
    ways = [
        [(point["lon"], point["lat"]) for point in member["geometry"]]
        for member in relation["members"]
        if member["type"] == "way" and member.get("role", "") == "" and member.get("geometry")
    ]
    return [simplify(line, SIMPLIFY_TOLERANCE_M) for line in order_lines(merge_lines(chain_ways(ways)))]


def fetch(url):
    request = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
    with urllib.request.urlopen(request, timeout=60) as response:
        return response.read().decode()


def planner_lines(route_id):
    track = fetch(f"{PLANNER_URL}mentettutv/{route_id}/shortest-track.gpx")
    segments = [
        [(float(lon), float(lat)) for lat, lon in re.findall(r'<trkpt lat="([-\d.]+)" lon="([-\d.]+)"', segment)]
        for segment in re.findall(r"<trkseg>(.*?)</trkseg>", track, re.S)
    ]
    # The planner splits the track at every via point
    return [simplify(line, SIMPLIFY_TOLERANCE_M) for line in order_lines(merge_lines(chain_ways(segments)))]


def way_lines(way_ids, start_node, end_node):
    nodes, graph = {}, {}
    for way_id in way_ids:
        elements = json.loads(fetch(f"{OSM_API_URL}way/{way_id}/full.json"))["elements"]
        nodes.update({element["id"]: (element["lon"], element["lat"]) for element in elements if element["type"] == "node"})
        way_nodes = next(element["nodes"] for element in elements if element["type"] == "way")
        for a, b in zip(way_nodes, way_nodes[1:]):
            graph.setdefault(a, set()).add(b)
            graph.setdefault(b, set()).add(a)
    distances, previous, queue = {start_node: 0}, {}, [(0, start_node)]
    while queue:
        distance, node = heapq.heappop(queue)
        if node == end_node:
            break
        for neighbour in graph[node]:
            candidate = distance + distance_m(nodes[node], nodes[neighbour])
            if candidate < distances.get(neighbour, math.inf):
                distances[neighbour], previous[neighbour] = candidate, node
                heapq.heappush(queue, (candidate, neighbour))
    if end_node not in distances:
        sys.exit(f"Ways {way_ids}: node {end_node} is not reachable from {start_node}")
    path = [end_node]
    while path[-1] != start_node:
        path.append(previous[path[-1]])
    return [simplify([nodes[node] for node in reversed(path)], SIMPLIFY_TOLERANCE_M)]


def fetch_elevations(points):
    elevations = []
    for start in range(0, len(points), ELEVATION_BATCH_SIZE):
        batch = points[start:start + ELEVATION_BATCH_SIZE]
        locations = "|".join(f"{lat:.{COORDINATE_DECIMALS}f},{lon:.{COORDINATE_DECIMALS}f}" for lon, lat in batch)
        url = f"{ELEVATION_URL}?{urllib.parse.urlencode({'locations': locations})}"
        request = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
        with urllib.request.urlopen(request, timeout=60) as response:
            body = json.loads(response.read().decode())
        if body.get("status") != "OK":
            sys.exit(f"Elevation: {body}")
        for result in body["results"]:
            if result["elevation"] is None:
                sys.exit(f"Elevation: no data at {result['location']}")
            elevations.append(result["elevation"])
        time.sleep(ELEVATION_REQUEST_DELAY_S)
    return elevations


def trail_name(tags):
    # Hungarian route names are "<marking>, <name> (<from> – <to>)"
    name = tags.get("name", "")
    name = name.split(", ", 1)[-1] if tags.get("ref") and name.startswith(tags["ref"]) else name
    return name.split(" (", 1)[0]


def to_gpx(name, url, jel, lines, elevations):
    trail_type = f"\n    <type>{escape(jel)}</type>" if jel else ""
    segments = []
    elevation_index = 0
    for line in lines:
        points = []
        for lon, lat in line:
            elevation = round(elevations[elevation_index], ELEVATION_DECIMALS)
            elevation_index += 1
            points.append(
                f'      <trkpt lat="{lat:.{COORDINATE_DECIMALS}f}" lon="{lon:.{COORDINATE_DECIMALS}f}">'
                f"<ele>{elevation}</ele></trkpt>"
            )
        segments.append("    <trkseg>\n" + "\n".join(points) + "\n    </trkseg>")
    return f"""<?xml version="1.0" encoding="UTF-8"?>
<gpx xmlns="http://www.topografix.com/GPX/1/1" version="1.1" creator="HuKi generate_educational_trails.py">
  <metadata>
    <name>{escape(name)}</name>
    <copyright author="OpenStreetMap contributors"><license>https://opendatacommons.org/licenses/odbl/</license></copyright>
  </metadata>
  <trk>
    <name>{escape(name)}</name>
    <src>OpenStreetMap, elevation: EU-DEM v1.1</src>
    <link href={quoteattr(url)}/>{trail_type}
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
    for trail, relation_id in OSM_SOURCES.items():
        relation = relations.get(relation_id)
        if relation is None:
            sys.exit(f"{trail}: relation {relation_id} missing from the Overpass response")
        url = f"https://www.openstreetmap.org/relation/{relation_id}"
        write_trail(trail, trail_name(relation["tags"]), url, relation["tags"].get("jel"), relation_lines(relation))
    for trail, (name, route_id) in PLANNER_SOURCES.items():
        write_trail(trail, name, f"{PLANNER_URL}mentettutv-{route_id}", None, planner_lines(route_id))
    for trail, (name, way_ids, start_node, end_node) in WAY_SOURCES.items():
        url = f"https://www.openstreetmap.org/way/{way_ids[0]}"
        write_trail(trail, name, url, None, way_lines(way_ids, start_node, end_node))


def write_trail(trail, name, url, jel, lines):
    points = [point for line in lines for point in line]
    elevations = fetch_elevations(points)
    output = OUTPUT_DIR / f"{OUTPUT_PREFIX}{trail}.gpx"
    output.write_text(to_gpx(name, url, jel, lines, elevations))
    length_km = sum(distance_m(a, b) for line in lines for a, b in zip(line, line[1:])) / 1000
    print(
        f"{trail}: {len(lines)} segment(s), {len(points)} points, {length_km:.1f} km, "
        f"jel={jel}, {output.stat().st_size / 1024:.1f} KB"
    )


if __name__ == "__main__":
    main()
