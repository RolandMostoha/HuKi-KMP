#!/usr/bin/env python3
"""Generate the dark-mode colour-grading LUT used by the Outdoors base layer.

Mapbox applies a LUT (lookup table) as a "colour theme" over a style, remapping every colour the
style draws. The classic Outdoors style has no night light preset, so this is how HuKi turns it
into a dark map.

The transform: invert lightness through a gamma curve (so paper-white fills drop into the shadows
while near-black labels stay bright), cut saturation, then tint everything towards a cool navy.

Run it to regenerate the constant in
`shared/src/commonMain/kotlin/hu/mostoha/mobile/kmp/huki/theme/OutdoorsColorTheme.kt`:

    python3 tools/python/generate_dark_map_lut.py

It prints the base64 PNG to stdout. The LUT is a 32x32x32 cube laid out as a 1024x32 image:
x = blue * 32 + red, y = green.
"""

import base64
import struct
import zlib

SIZE = 32

# Tuning constants. Keep in sync with the doc comment in OutdoorsColorTheme.kt.
LIGHTNESS_FLOOR = 0.04
LIGHTNESS_RANGE = 0.86
LIGHTNESS_GAMMA = 1.6
SATURATION_SCALE = 0.35
TINT = (0.06, 0.09, 0.17)
TINT_AMOUNT = 0.22


def rgb_to_hsl(r, g, b):
    high, low = max(r, g, b), min(r, g, b)
    lightness = (high + low) / 2
    delta = high - low

    if delta == 0:
        return 0.0, 0.0, lightness

    saturation = delta / (1 - abs(2 * lightness - 1))

    if high == r:
        hue = 60 * (((g - b) / delta) % 6)
    elif high == g:
        hue = 60 * (((b - r) / delta) + 2)
    else:
        hue = 60 * (((r - g) / delta) + 4)

    return hue, saturation, lightness


def hsl_to_rgb(hue, saturation, lightness):
    c = (1 - abs(2 * lightness - 1)) * saturation
    x = c * (1 - abs((hue / 60) % 2 - 1))
    m = lightness - c / 2

    if hue < 60:
        r, g, b = c, x, 0
    elif hue < 120:
        r, g, b = x, c, 0
    elif hue < 180:
        r, g, b = 0, c, x
    elif hue < 240:
        r, g, b = 0, x, c
    elif hue < 300:
        r, g, b = x, 0, c
    else:
        r, g, b = c, 0, x

    return r + m, g + m, b + m


# Inverting lightness maps the basemap's light area fills (forest, land, water: l > 0.7) to dark,
# and its dark ink (label text, contour lines, road casings: l < 0.5) to light. Those two bands do
# not overlap, so lifting the dark end of the curve brightens everything drawn *on* the map without
# altering the terrain tone underneath it. That is what makes the labels readable: a plain
# inversion leaves them at a mid grey, well below the near-white Standard uses at night.
DARK_INK_PIVOT = 0.6
DARK_INK_TARGET = 0.95
DARK_INK_LIFT = 0.9
DARK_INK_TINT_RELIEF = 1.0


def darken(r, g, b):
    hue, saturation, lightness = rgb_to_hsl(r, g, b)

    # 1 for black input, falling to 0 at DARK_INK_PIVOT and above.
    inkiness = max(0.0, min(1.0, (DARK_INK_PIVOT - lightness) / DARK_INK_PIVOT))
    lift = DARK_INK_LIFT * inkiness

    # White always maps to LIGHTNESS_FLOOR whatever else happens, so the dark background the
    # basemap sits on is untouched by the lift.
    inverted = LIGHTNESS_FLOOR + pow(1 - lightness, LIGHTNESS_GAMMA) * LIGHTNESS_RANGE
    inverted = inverted * (1 - lift) + DARK_INK_TARGET * lift

    muted = saturation * SATURATION_SCALE
    tint_amount = TINT_AMOUNT * (1 - DARK_INK_TINT_RELIEF * inkiness)

    out = hsl_to_rgb(hue, muted, inverted)

    return tuple(
        channel * (1 - tint_amount) + tint * tint_amount
        for channel, tint in zip(out, TINT)
    )


def build_lut():
    width, height = SIZE * SIZE, SIZE
    rows = []

    for g in range(height):
        row = bytearray()
        for b in range(SIZE):
            for r in range(SIZE):
                graded = darken(r / (SIZE - 1), g / (SIZE - 1), b / (SIZE - 1))
                row.extend(min(255, max(0, round(channel * 255))) for channel in graded)
        rows.append(row)

    return width, height, rows


def filter_row(row, previous):
    """Pick the PNG row filter with the smallest absolute sum, the standard heuristic.

    Without filtering the LUT's red ramp repeats 32 times per row and compresses badly (~80 KB);
    with it the payload drops to a few KB, which matters because the result is embedded as a
    Kotlin string constant (the JVM caps those at 64 KB).
    """
    candidates = []

    candidates.append((0, bytes(row)))

    sub = bytes((row[i] - (row[i - 3] if i >= 3 else 0)) & 0xFF for i in range(len(row)))
    candidates.append((1, sub))

    up = bytes((row[i] - previous[i]) & 0xFF for i in range(len(row)))
    candidates.append((2, up))

    paeth = bytearray()
    for i in range(len(row)):
        left = row[i - 3] if i >= 3 else 0
        above = previous[i]
        upper_left = previous[i - 3] if i >= 3 else 0
        estimate = left + above - upper_left
        da, db, dc = (
            abs(estimate - left),
            abs(estimate - above),
            abs(estimate - upper_left),
        )
        if da <= db and da <= dc:
            predictor = left
        elif db <= dc:
            predictor = above
        else:
            predictor = upper_left
        paeth.append((row[i] - predictor) & 0xFF)
    candidates.append((4, bytes(paeth)))

    def score(candidate):
        return sum(byte if byte < 128 else 256 - byte for byte in candidate[1])

    return min(candidates, key=score)


def encode_png(width, height, rows):
    raw = bytearray()
    previous = bytes(len(rows[0]))
    for row in rows:
        filter_type, filtered = filter_row(row, previous)
        raw.append(filter_type)
        raw.extend(filtered)
        previous = bytes(row)
    raw = bytes(raw)

    def chunk(tag, payload):
        data = tag + payload
        return struct.pack(">I", len(payload)) + data + struct.pack(">I", zlib.crc32(data))

    header = struct.pack(">IIBBBBB", width, height, 8, 2, 0, 0, 0)  # 8-bit truecolour RGB

    return (
        b"\x89PNG\r\n\x1a\n"
        + chunk(b"IHDR", header)
        + chunk(b"IDAT", zlib.compress(raw, 9))
        + chunk(b"IEND", b"")
    )


if __name__ == "__main__":
    print(base64.b64encode(encode_png(*build_lut())).decode("ascii"))
