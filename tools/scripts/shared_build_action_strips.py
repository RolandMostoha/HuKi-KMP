#!/usr/bin/env python3
"""Cuts a frame strip per user action out of a Maestro run's screen recording.

Maestro's commands.json holds each command's start time and duration, the recording list holds each video segment's
wall-clock start. Maestro can wait seconds before it acts (it waits for the screen to settle first), so the strip
starts at the first clear screen change within the command and every tap / long press / swipe gets a grid of the
frames after it: a camera that moves twice, a sheet that jumps or a flash shows up as separate motion in the strip.

Usage: shared_build_action_strips.py <commands.json> <recordings.txt> <out-dir>
recordings.txt: one "<start epoch seconds> <video path>" line per segment, in recording order.
"""

import json
import subprocess
import sys
from pathlib import Path

ACTION_COMMANDS = ("tapOnElement", "tapOnPointV2Command", "tapOnPoint", "swipeCommand", "backPressCommand")
FPS = 10
COLUMNS = 10
FRAME_WIDTH = 140
LEAD_IN_S = 0.3
WINDOW_S = 4.0
# Scene change score that a tap's response clears but a marker's pulse or the location puck does not
CHANGE_THRESHOLD = 0.02


def describe(command: dict) -> str:
    kind, body = next(iter(command.items()))
    if kind == "tapOnElement":
        selector = body.get("selector", {})
        target = selector.get("idRegex") or selector.get("textRegex") or "element"
        return ("long_press_" if body.get("longPress") else "tap_") + target
    if kind.startswith("tapOnPoint"):
        return ("long_press_" if body.get("longPress") else "tap_") + str(body.get("point", "point"))
    if kind == "swipeCommand":
        selector = (body.get("elementSelector") or {}).get("idRegex", "")
        return f"swipe_{body.get('direction', '')}_{selector}".rstrip("_")
    return kind


def first_change_s(video: str, offset_s: float, duration_s: float) -> float | None:
    result = subprocess.run(
        [
            "ffmpeg", "-v", "info", "-ss", f"{offset_s:.2f}", "-t", f"{duration_s:.2f}", "-i", video,
            "-vf", f"scale=270:-2,select='gt(scene,{CHANGE_THRESHOLD})',showinfo", "-f", "null", "-",
        ],
        capture_output=True,
        text=True,
        check=False,
    )
    for line in result.stderr.splitlines():
        if "pts_time:" in line:
            return offset_s + float(line.split("pts_time:")[1].split()[0])
    return None


def slug(text: str) -> str:
    return "".join(c if c.isalnum() else "_" for c in text).strip("_")[:60]


def main() -> int:
    commands_path, recordings_path, out_dir = (Path(arg) for arg in sys.argv[1:4])
    segments = []
    for line in recordings_path.read_text().splitlines():
        start, path = line.split(" ", 1)
        if Path(path).exists():
            segments.append((float(start), path))
    if not segments:
        print("No recordings found", file=sys.stderr)
        return 1
    out_dir.mkdir(parents=True, exist_ok=True)

    actions = 0
    for entry in json.loads(commands_path.read_text()):
        command, metadata = entry["command"], entry["metadata"]
        if next(iter(command)) not in ACTION_COMMANDS or metadata.get("status") != "COMPLETED":
            continue
        command_start_s = metadata["timestamp"] / 1000
        segment = next(((s, p) for s, p in reversed(segments) if s <= command_start_s), segments[0])
        command_offset_s = max(command_start_s - segment[0], 0)
        change_s = first_change_s(segment[1], command_offset_s, metadata["duration"] / 1000 + 1)
        offset_s = max((change_s if change_s is not None else command_offset_s) - LEAD_IN_S, 0)
        window_s = WINDOW_S
        rows = -(-int(window_s * FPS) // COLUMNS)
        actions += 1
        target = out_dir / f"{metadata['sequenceNumber']:03d}_{slug(describe(command))}.png"
        subprocess.run(
            [
                "ffmpeg", "-v", "error", "-y", "-ss", f"{offset_s:.2f}", "-t", f"{window_s:.2f}", "-i", segment[1],
                "-vf", f"fps={FPS},scale={FRAME_WIDTH}:-2,tile={COLUMNS}x{rows}:padding=2", "-frames:v", "1",
                str(target),
            ],
            check=False,
        )
    print(f"{actions} action strips in {out_dir}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
