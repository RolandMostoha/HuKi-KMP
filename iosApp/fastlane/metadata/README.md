# App Store Connect metadata (fastlane `deliver`)

Version-tracked source of truth for everything set on the App Store Connect
**product page**. Laid out in the exact structure `fastlane deliver` expects, so a
future integration is just `fastlane deliver` (run from `iosApp/`) — no restructuring.

## Layout

```
metadata/
  copyright.txt            # global
  primary_category.txt     # global (NAVIGATION)
  secondary_category.txt   # global (SPORTS)
  <locale>/
    name.txt               # app name          (max 30)
    subtitle.txt           # subtitle          (max 30)
    promotional_text.txt   # promo text        (max 170, editable without review)
    description.txt        # full description  (max 4000)
    keywords.txt           # search keywords   (max 100, comma-separated, no spaces)
    release_notes.txt      # "What's New"      (max 4000, per version)
    support_url.txt
    marketing_url.txt
    privacy_url.txt
```

## Locales

App Store locale codes (NOT the app's `hu-HU` / moko codes):

| Language  | App Store / fastlane code |
|-----------|---------------------------|
| English   | `en-US`                   |
| Hungarian | `hu`                      |

## Field notes

- **promotional_text** — shows *above* the description; can be updated anytime
  without a new build/review. Keep it current-highlight focused.
- **release_notes** — tied to a version. Mirrors the bullets in
  `tools/release/whatsnew/v<version>/whatsnew-*.md` (the in-app What's New source).
  Update both on each release.
- **keywords** — not shown to users; drives App Store search. Comma-separated,
  no spaces, ≤100 chars total.

## TODO before first `deliver`

- [ ] `support_url.txt` — required by App Store (currently empty).
- [ ] `privacy_url.txt` — privacy policy URL (currently empty).
- [ ] `marketing_url.txt` — optional; fill or leave empty.
- [ ] Confirm `primary_category` / `secondary_category` match App Store Connect.
- [ ] Screenshots: `deliver` expects `iosApp/fastlane/screenshots/<locale>/`.
      Current store assets live in `tools/screenshots/` — wire these up (copy or
      symlink) when adding fastlane.
