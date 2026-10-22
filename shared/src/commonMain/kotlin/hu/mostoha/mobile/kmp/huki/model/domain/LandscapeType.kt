package hu.mostoha.mobile.kmp.huki.model.domain

import dev.icerock.moko.resources.ImageResource
import hu.mostoha.mobile.huki.shared.SharedRes

enum class LandscapeType(val iconRes: ImageResource) {
    MOUNTAIN_LOW(SharedRes.images.ic_landscapes_mountain_low),
    MOUNTAIN_MEDIUM(SharedRes.images.ic_landscapes_mountain_medium),
    MOUNTAIN_HIGH(SharedRes.images.ic_landscapes_mountain_high),
    MOUNTAIN_WITH_LAKE(SharedRes.images.ic_landscapes_lake),
    CAVE_SYSTEM(SharedRes.images.ic_landscapes_cave),
    WINE_AREA(SharedRes.images.ic_landscapes_grape),
    STAR_GAZING_AREA(SharedRes.images.ic_landscapes_telescope),
    MOUNTAIN_WITH_CASTLE(SharedRes.images.ic_landscapes_castle),
    FOREST_AREA(SharedRes.images.ic_landscapes_forest),
    PLAIN_LAND(SharedRes.images.ic_landscapes_plain_land),
    LAKE(SharedRes.images.ic_landscapes_kayak),
}
