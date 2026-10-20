#!/usr/bin/env python3
"""Validate destination data and bilingual description lengths."""
from collections import Counter
from pathlib import Path
import re
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[2]
DATA = ROOT / "shared/src/commonMain/kotlin/hu/mostoha/mobile/kmp/huki/data"
RESOURCES = ROOT / "shared/src/commonMain/moko-resources"
DESCRIPTION_LENGTH = range(80, 161)
PROMOTIONAL_LANGUAGE = {
    "hu": r"jutalmaz|kínál|keretez|eleven(?:edik|ít)|hozz(?:a|ák) közel|vár(?:nak|ják|ja)\b|hívnak",
    "base": r"\brewards?\b|\binvites?\b|breathtaking|\bbrings?\b[^.]*\blife\b|\boffers?\b|\boffering\b",
}


def display_length(text):
    return len(re.sub(r"\\+(['\"])", r"\1", text))


def validate():
    source = (DATA / "Destinations.kt").read_text()
    ids = re.findall(r'^        id = "([^"]+)"', source, re.M)
    duplicates = [tag for tag, count in Counter(ids).items() if count > 1]
    assert not duplicates, f"Duplicate destination IDs: {duplicates}"
    invalid_ids = [tag for tag in ids if not re.fullmatch(r"[a-z0-9]+(?:_[a-z0-9]+)*", tag)]
    assert not invalid_ids, f"Destination IDs must be snake_case slugs: {invalid_ids}"
    descriptions = {}
    for locale in ("base", "hu"):
        items = ET.parse(RESOURCES / locale / "strings_destinations.xml").getroot()
        names = [item.attrib["name"] for item in items]
        assert len(names) == len(set(names)), f"Duplicate strings in {locale}"
        sections = []
        for name in names:
            prefix = name.split("_", 1)[0]
            if not sections or sections[-1] != prefix:
                sections.append(prefix)
        assert len(sections) == len(set(sections)), f"Split landscape sections in {locale}"
        descriptions[locale] = {item.attrib["name"]: item.text or "" for item in items}
    assert descriptions["base"].keys() == descriptions["hu"].keys(), "Translation keys differ"
    assert list(descriptions["base"]) == list(descriptions["hu"]), "Translation order differs"
    for locale, strings in descriptions.items():
        for tag, text in strings.items():
            length = display_length(text)
            assert length in DESCRIPTION_LENGTH, f"{locale}/{tag}: {length} characters"
            assert not re.search(PROMOTIONAL_LANGUAGE[locale], text, re.I), f"{locale}/{tag}: promotional wording"
    referenced = set(re.findall(r"description = SharedRes.strings.(\w+)", source))
    assert referenced <= descriptions["base"].keys(), "Missing description resources"
    enum_source = (ROOT / "shared/src/commonMain/kotlin/hu/mostoha/mobile/kmp/huki/model/domain/DestinationType.kt").read_text()
    enum_names = set(re.findall(r"^    (\w+)\(", enum_source, re.M))
    assert set(re.findall(r"DestinationType\.(\w+)", source)) <= enum_names
    for block in re.findall(r"    Destination\(\n.*?\n    \),", source, re.S):
        primary = re.search(r"type = DestinationType\.(\w+)", block)[1]
        categories = re.search(r"categories = setOf\((.*?)\)", block, re.S)
        members = set(re.findall(r"DestinationType\.(\w+)", categories[1])) if categories else {primary}
        assert primary in members, f"Missing primary category: {primary}"
        assert not {"CAVE_WITHOUT_GUIDE", "CAVE_GUIDE_REQUIRED"} <= members, "Conflicting cave access tags"
    groups = re.findall(r"val (DESTINATIONS_\w+) = listOf", source)
    all_groups = re.findall(r"    (DESTINATIONS_\w+),", source.split("val ALL_DESTINATIONS =")[1])
    landscape_groups = re.findall(r"destinations = (DESTINATIONS_\w+)", (DATA / "Landscapes.kt").read_text())
    landscape_groups.append("DESTINATIONS_WITHOUT_LANDSCAPE")
    assert Counter(groups) == Counter(all_groups) == Counter(landscape_groups), "Unlinked or repeated landscape groups"
    print(f"Validated {len(ids)} unique destinations, {len(groups)} landscape groups, "
          f"and {len(descriptions['base']) * 2} description texts.")


if __name__ == "__main__":
    validate()
