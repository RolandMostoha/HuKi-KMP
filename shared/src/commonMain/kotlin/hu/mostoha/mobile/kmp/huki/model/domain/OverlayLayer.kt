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
    val cacheMaxAge: Duration,
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
}
