---
name: destination-website
description: Find, verify and add a website link (`Destination.website`) for a HuKi destination, which backs the "Website" button in the InfoWindow. Use when adding a destination with an operator (scenic railway, wildlife park, adventure park, guided cave, observatory), or when adding, fixing or reviewing a destination's website link.
---

# Destination website

`Destination.website` is an optional https URL in `shared/**/data/Destinations.kt`. When it is set, the
InfoWindow shows a "Website" button that opens the link in the external browser. Hikers use it to check
**opening hours, prices, timetables and booking** before they go.

## Which destinations get one

Only places with an operator that publishes visitor information:

| Type                  | Source                                                                       |
|-----------------------|------------------------------------------------------------------------------|
| `SCENIC_RAILWAY`      | Always the aktivkalandor.hu page: `https://aktivkalandor.hu/kisvasut/<slug>/` |
| `WILDLIFE_PARK`       | Official site of the operator                                                |
| `ADVENTURE_PARK`      | Official site of the operator                                                |
| `CAVE_GUIDE_REQUIRED` | Official site of the cave, else the national park directorate's page for it  |
| `OBSERVATORY`         | Official site, else the national park directorate's page for it              |
| `CASTLE`              | Official site of the castle or its museum, else the operator's page for it; free-access ruins get none |
| `PALACE`              | Official site of the palace, else the operator's page for it (often NÖF, nof.hu) |
| `CANOPY_WALK`         | Official site, else the operator's page for it (often the state forestry company) |

Peaks, rocks, lookouts, valleys and other open nature get no website.

## Choosing the link

- **Official first:** the operator's own domain, or the national park directorate's page for this exact place.
- **The page for this place**, not a generic list. A park's "all cave tours" page is not good enough: leave the
  website out instead.
- **No aggregators** (programturizmus.hu, szallas.hu, utazzitthon.hu...) and no Facebook pages, except
  aktivkalandor.hu for scenic railways.
- **Hungarian version:** when a site has a language prefix, prefer `/hu`.
- **Final URL:** use the address after redirects, with `https://`.
- **No website is fine.** If there is no official page, leave the field out.

To find an aktivkalandor slug, list them all from the sitemap:

```bash
curl -sL https://aktivkalandor.hu/kisvasut-sitemap.xml | grep -o '<loc>[^<]*</loc>'
```

## Verifying

Before adding a link, check all three:

1. **It loads.** HTTP 200 and the page title names the place:
   ```bash
   curl -sL -A "Mozilla/5.0" -o /dev/null -w "%{http_code} %{url_effective}\n" "<url>"
   ```
2. **It is the right place.** Same town and the same attraction, not a namesake.
3. **The place is still open.** Look for current-year timetables, opening hours, events or news. A seasonal
   break ("see you next season") is fine. A closed business is not: remove the destination instead.

## Adding it

1. Add `website` as the last argument of the `Destination(...)`.
2. Run `python3 tools/destinations/validate_destinations.py`. It fails on a non-https website.

## Examples

Scenic railway:

```kotlin
Destination(
    id = "budapest_gyermekvasut",
    name = "Széchenyi-hegyi Gyermekvasút",
    // ...
    website = "https://aktivkalandor.hu/kisvasut/mav-szechenyi-hegyi-gyermekvasut/",
),
```

Guided cave, with the national park directorate's page for the cave:

```kotlin
website = "https://www.dunaipoly.hu/hu/helyek/bemutatohelyek/szemlo-hegyi-barlang",
```

Adventure park with its own site:

```kotlin
website = "https://zemplenkalandpark.hu",
```

Decisions from earlier reviews:

| Destination                    | Decision                                                              |
|--------------------------------|-----------------------------------------------------------------------|
| Abaligeti Barlang              | `https://www.abaligetibarlang.hu`: the cave's own site wins over the DDNP page |
| Bükki Csillagda                | `https://www.bukkicsillagda.hu/hu`: Hungarian path, not the `/en` redirect |
| Petesmalmi Vidrapark           | The operator's (Somogy Természetvédelmi Szervezet) page, which posts hours and prices |
| Szalafői bölényles             | The Őrség NP page under "Bemutatóhelyeink" (presentation sites): national park pages for a place often live there |
| Csillagoségbolt-Park           | The HNP dark-sky park page: not an observatory, but it lists the stargazing events |
| Vass Imre-barlang              | No website: anp.hu only has a generic cave tours list                 |
| Szécsiszigeti Bivalyrezervátum | No website: the operator has no site, only directory listings exist   |
| Lővér kalandpark               | Closed: the destination was removed                                   |
| Visegrádi fellegvár            | The operator's (Pilisi Parkerdő) page: fellegvar.hu serves an empty page |
| Regéci vár, Egervár            | No website: their own sites stopped updating years ago                 |
| Diósgyőri vár                  | Not added: closed for reconstruction                                   |
| Héderváry-kastély              | Closed since 2012: the destination was removed                         |
| Fehérvárcsurgó Károlyi-kastély | No website: karolyikastely.hu is the hotel's booking site              |
| Fertőd, Nagycenk               | Their pages on eszterhaza.hu: one operator runs both                   |
| Sás-völgyi lombkorona          | Mecsekerdő's 2025 reopening post: the only page for this walkway       |
| Tardos, Pannonhalma, Annafürdő, Hévíz canopy walks | No website: the forestry operators have no page for them |
