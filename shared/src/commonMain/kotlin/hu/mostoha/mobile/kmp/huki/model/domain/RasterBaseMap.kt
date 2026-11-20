package hu.mostoha.mobile.kmp.huki.model.domain

/**
 * A base map from raster tiles, drawn on the empty [STYLE_JSON].
 */
class RasterBaseMap(
    val id: String,
    val tiles: List<String>,
    val minZoom: Long,
    /**
     * Mapbox overscales the tiles past it.
     */
    val maxZoom: Long,
    /**
     * Listed in the Mapbox attribution dialog, which opens HTML links.
     */
    val attribution: String,
) {
    /**
     * Mapbox only uses the tile size to pick a whole zoom level, so raster tiles scale in powers of two: a doubled size
     * loads the next lower level at 2x, any size in between flips between 1x and 2x with the camera zoom.
     */
    fun tileSize(scaleFactor: Float): Long =
        if (scaleFactor >= LARGE_TILES_MIN_SCALE_FACTOR) BASE_TILE_SIZE * 2L else BASE_TILE_SIZE.toLong()

    /**
     * A source's tile size can't change, so every scale gets its own source and layer.
     */
    fun layerId(tileSize: Long): String = "$id-$tileSize"

    companion object {
        const val BACKGROUND_LAYER_ID = "background"
        private const val BASE_TILE_SIZE = 256
        private const val LARGE_TILES_MIN_SCALE_FACTOR = 1.5f

        /**
         * Only a background, shared by every raster base map, so switching between them or changing the tile scale
         * re-creates the raster source instead of reloading the style.
         *
         * Glyphs are needed by HuKi's own symbol layers (e.g. landscape labels).
         */
        const val STYLE_JSON = """{
  "version": 8,
  "name": "HuKi Raster",
  "glyphs": "mapbox://fonts/mapbox/{fontstack}/{range}.pbf",
  "sources": {},
  "layers": [
    { "id": "$BACKGROUND_LAYER_ID", "type": "background", "paint": { "background-color": "#FFFFFF" } }
  ]
}"""

        val TUHU = RasterBaseMap(
            id = "tuhu",
            tiles = listOf(
                "https://a.map.turistautak.hu/tiles/turistautak/{z}/{x}/{y}.png",
                "https://b.map.turistautak.hu/tiles/turistautak/{z}/{x}/{y}.png",
                "https://c.map.turistautak.hu/tiles/turistautak/{z}/{x}/{y}.png",
            ),
            minZoom = 5L,
            maxZoom = 18L,
            attribution = "<a href=\"https://turistautak.hu/\">© turistautak.hu</a>",
        )

        val MAPNIK = RasterBaseMap(
            id = "mapnik",
            tiles = listOf("https://tile.openstreetmap.org/{z}/{x}/{y}.png"),
            minZoom = 0L,
            maxZoom = 19L,
            attribution = "<a href=\"https://www.openstreetmap.org/copyright\">© OpenStreetMap contributors</a>",
        )
    }
}
