---
name: destination-descriptions
description: Editorial rules for HuKi destination descriptions (HU + EN). Use when adding, editing or reviewing destinations in Destinations.kt or their description strings in strings_destinations.xml.
---

# Destination descriptions

## Editorial rules

- Start with what the place is and where it is.
- State observable features, views, historical details and access conditions.
- Keep elevations, written as `(895 m)`, and only verified "highest peak" claims, scoped correctly
  (e.g. highest in the **Buda Hills**, not Budapest).
- Use short sentences. No generic praise, metaphors, invitations or promises about the visitor's experience.
- Don't pad with marketing to reach the length target.
- Hungarian and English carry the same facts in the same plain style.
- Keep access notes: guided-only caves, closures, seasonal restrictions.
- Length: aim for 100–150 visible characters in both languages; 80–160 is the hard limit.

Banned phrases: `jutalmazza a túrát`, `kínál`, `várnak`, `túrákra hívnak`, `múltja elevenedik meg`, `hozza közel`,
`keretezik`, `rewards`, `invites`, `offers`, `breathtaking`, `brings to life`.

Examples:

> A Zemplén legmagasabb magyarországi csúcsa (895 m), a szlovák határon. A közeli Kis-Milic kilátójából a Hegyközre látni.

> A Mátra legvadabb szurdoka, sziklás tereppel és látványos geológiai formákkal. Erdei ösvénye köves terepen halad.

## Workflow

1. Add the `Destination` to its landscape's `DESTINATIONS_*` list in `shared/**/data/Destinations.kt`
   (large file: use grep / ranged reads).
2. Add the description to both `moko-resources/base/strings_destinations.xml` (EN) and
   `moko-resources/hu/strings_destinations.xml` (HU):
   - key prefixed with the landscape, e.g. `matra_kekesteto`,
   - placed beside the landscape's other entries, in the same order in both files.
3. Verify facts (elevation, location, access) against a reliable source before writing them.
4. Run `python3 tools/destinations/validate_destinations.py`.
