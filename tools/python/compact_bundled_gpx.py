#!/usr/bin/env python3
"""Compact the bundled full-trail GPX files (OKT / AKT / RPDDK) in place, without dropping any point.

The source exports carry float noise (`lat="47.352755340000044"`, `<ele>880.1505000000034</ele>`) and put every
trackpoint on three indented lines. This script rewrites them losslessly at mapping precision:

- `lat` / `lon` rounded to 6 decimals (~11 cm), on trackpoints and waypoints,
- `<ele>` rounded to 0.1 m,
- every `<trkpt>` collapsed onto a single unindented line.

Points, their order, waypoint names / descriptions and the document structure are unchanged. The script is
idempotent, so run it on every new trail export before swapping the file referenced by `OktType.gpxFile`:

    python3 tools/python/compact_bundled_gpx.py                # all bundled *_teljes_*.gpx files
    python3 tools/python/compact_bundled_gpx.py path/to/a.gpx  # specific files
"""

import pathlib
import re
import sys

FILES_DIR = pathlib.Path(__file__).resolve().parents[2] / "shared/src/commonMain/moko-resources/files"
DEFAULT_GLOB = "*_teljes_*.gpx"

COORD_DECIMALS = 6
ELE_DECIMALS = 1

COORD_RE = re.compile(r'\b(lat|lon)="(-?\d+(?:\.\d+)?)"')
ELE_RE = re.compile(r"<ele>(-?\d+(?:\.\d+)?)</ele>")
TRKPT_RE = re.compile(r"[ \t]*<trkpt ([^>]*)>\s*(.*?)\s*</trkpt>", re.S)
TAG_GAP_RE = re.compile(r">\s+<")


def round_number(value: str, decimals: int) -> str:
    rounded = f"{float(value):.{decimals}f}".rstrip("0").rstrip(".")
    return "0" if rounded == "-0" else rounded


def compact(text: str) -> str:
    text = COORD_RE.sub(lambda m: f'{m[1]}="{round_number(m[2], COORD_DECIMALS)}"', text)
    text = ELE_RE.sub(lambda m: f"<ele>{round_number(m[1], ELE_DECIMALS)}</ele>", text)
    return TRKPT_RE.sub(lambda m: f"<trkpt {m[1]}>{TAG_GAP_RE.sub('><', m[2])}</trkpt>", text)


def main() -> None:
    paths = [pathlib.Path(arg) for arg in sys.argv[1:]] or sorted(FILES_DIR.glob(DEFAULT_GLOB))
    if not paths:
        sys.exit(f"No GPX files matching {DEFAULT_GLOB} in {FILES_DIR}")

    for path in paths:
        original = path.read_text(encoding="utf-8")
        compacted = compact(original)
        before, after = len(original.encode()), len(compacted.encode())
        if original == compacted:
            print(f"{path.name}: already compact ({before / 1e6:.2f} MB)")
            continue
        path.write_text(compacted, encoding="utf-8")
        print(f"{path.name}: {before / 1e6:.2f} MB -> {after / 1e6:.2f} MB ({100 * (before - after) / before:.0f}% smaller)")


if __name__ == "__main__":
    main()
