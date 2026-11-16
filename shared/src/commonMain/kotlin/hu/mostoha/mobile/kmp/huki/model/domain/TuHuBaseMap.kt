package hu.mostoha.mobile.kmp.huki.model.domain

object TuHuBaseMap {
    const val LAYER_ID = "tuhu"
    const val BACKGROUND_LAYER_ID = "background"
    val tiles = listOf(
        "https://a.map.turistautak.hu/tiles/turistautak/{z}/{x}/{y}.png",
        "https://b.map.turistautak.hu/tiles/turistautak/{z}/{x}/{y}.png",
        "https://c.map.turistautak.hu/tiles/turistautak/{z}/{x}/{y}.png",
    )
    const val MIN_ZOOM = 5L

    /**
     * Listed in the Mapbox attribution dialog, which opens HTML links.
     */
    const val ATTRIBUTION = "<a href=\"https://turistautak.hu/\">© turistautak.hu</a>"

    /**
     * The tile server has no tiles above zoom 18; Mapbox overscales them past it.
     */
    const val MAX_ZOOM = 18L
    private const val BASE_TILE_SIZE = 256

    /**
     * Only a background, so changing the tile scale re-creates the TuHu source instead of reloading the style.
     *
     * Glyphs are needed by HuKi's own symbol layers (e.g. landscape labels).
     */
    const val STYLE_JSON = """{
  "version": 8,
  "name": "TuHu",
  "glyphs": "mapbox://fonts/mapbox/{fontstack}/{range}.pbf",
  "sources": {},
  "layers": [
    { "id": "$BACKGROUND_LAYER_ID", "type": "background", "paint": { "background-color": "#FFFFFF" } }
  ]
}"""

    private const val LARGE_TILES_MIN_SCALE_FACTOR = 1.5f

    /**
     * Mapbox only uses the tile size to pick a whole zoom level, so raster tiles scale in powers of two: a doubled size
     * loads the next lower level at 2x, any size in between flips between 1x and 2x with the camera zoom.
     */
    fun tileSize(scaleFactor: Float): Long =
        if (scaleFactor >= LARGE_TILES_MIN_SCALE_FACTOR) BASE_TILE_SIZE * 2L else BASE_TILE_SIZE.toLong()

    /**
     * A source's tile size can't change, so every scale gets its own source and layer.
     */
    fun layerId(tileSize: Long): String = "$LAYER_ID-$tileSize"
}
