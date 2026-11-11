# Map Tile Cache

## How Mapbox caches

- Every viewed tile is saved on the device (up to 1 GB, least recently used deleted first).
- A cached tile is always shown first. Once expired, Mapbox re-checks it with the server when online:
  - It sends the tile's stored `ETag` with the request ("I have this version").
  - `304 Not Modified`: the cached tile is kept and gets a new expiry, no image is downloaded.
  - `200 OK`: the tile changed, the new one replaces it.
- Offline, cached tiles are shown even when expired.

## How Mapbox expects tile servers to work

- The server tells how long a tile stays fresh with a `Cache-Control: max-age` header.
- After that, the app asks "has it changed?" (ETag). The server answers "not modified" or sends the new tile.
- `tile.openstreetmap.hu/tt` sends no `Cache-Control`, so Mapbox re-checked every tile on every view.

## What we needed to do

- Mapbox base map: nothing, its servers send proper headers.
- Hiking layer (`tile.openstreetmap.hu/tt`): add `Cache-Control` to simulate expiration dates
- Fix: `OverlayTileCacheInterceptor` (Android + iOS) adds `Cache-Control: max-age` of 14 days
