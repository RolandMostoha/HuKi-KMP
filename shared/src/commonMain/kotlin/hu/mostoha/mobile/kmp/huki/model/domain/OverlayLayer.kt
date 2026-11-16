package hu.mostoha.mobile.kmp.huki.model.domain

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

enum class OverlayLayer(
    val layerId: String,
    val tiles: List<String>,
    val tileSize: Long,
    val minZoom: Long,
    val maxZoom: Long,
    val prefetchZoomDelta: Long,
    val maxOverscaleFactorForParentTiles: Long,
    /**
     * Added when the tile server sends no cache headers, `null` keeps the server's own.
     */
    val cacheMaxAge: Duration?,
    /**
     * Listed in the Mapbox attribution dialog, which opens HTML links.
     */
    val attribution: String? = null,
) {
    TURISTAUTAK(
        layerId = "turistautak",
        tiles = listOf(
            "https://a.tile.openstreetmap.hu/tt/{z}/{x}/{y}.png",
            "https://b.tile.openstreetmap.hu/tt/{z}/{x}/{y}.png",
            "https://c.tile.openstreetmap.hu/tt/{z}/{x}/{y}.png",
        ),
        tileSize = 256,
        minZoom = 5,
        maxZoom = 17,
        // Requests only tiles of the current zoom level, no lower-zoom tiles loaded ahead as a quick preview
        prefetchZoomDelta = 0,
        // Limits how much an already loaded lower-zoom tile can be scaled up to fill in for a loading tile
        maxOverscaleFactorForParentTiles = 1,
        cacheMaxAge = 14.days,
    ),
    WAYMARKED_CYCLING(
        layerId = "waymarked-cycling",
        tiles = listOf("https://tile.waymarkedtrails.org/cycling/{z}/{x}/{y}.png"),
        tileSize = 256,
        minZoom = 5,
        maxZoom = 18,
        prefetchZoomDelta = 0,
        maxOverscaleFactorForParentTiles = 1,
        cacheMaxAge = null,
        attribution = "<a href=\"https://waymarkedtrails.org/\">© Waymarked Trails (CC BY-SA 3.0)</a> " +
            "<a href=\"https://www.openstreetmap.org/copyright\">© OpenStreetMap</a>",
    ),
}
