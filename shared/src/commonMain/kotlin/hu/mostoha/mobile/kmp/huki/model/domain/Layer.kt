package hu.mostoha.mobile.kmp.huki.model.domain

enum class Layer(
    val layerId: String,
    val tiles: List<String>,
    val tileSize: Long,
    val minZoom: Long,
    val maxZoom: Long,
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
    ),
}
