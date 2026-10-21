package hu.mostoha.mobile.kmp.huki.model.domain

import dev.icerock.moko.resources.ColorResource
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.StringResource
import hu.mostoha.mobile.huki.shared.SharedRes

enum class DestinationType(
    val placeCategory: PlaceCategory?,
    private val titleOverride: StringResource? = null,
    private val iconResOverride: ImageResource? = null,
    private val colorResOverride: ColorResource? = null,
) {
    PEAK(
        placeCategory = PlaceCategory.PEAK,
        titleOverride = SharedRes.strings.destinations_type_peak,
    ),
    VALLEY(
        placeCategory = PlaceCategory.VALLEY,
    ),
    LAKE(
        placeCategory = PlaceCategory.LAKE,
    ),
    CAVE(
        placeCategory = PlaceCategory.CAVE,
    ),
    WATERFALL(
        placeCategory = PlaceCategory.WATERFALL,
    ),
    CASTLE(
        placeCategory = PlaceCategory.CASTLE,
        titleOverride = SharedRes.strings.destinations_type_castle,
    ),
    ROCK(
        placeCategory = PlaceCategory.ROCK,
    ),
    PLATEAU(
        placeCategory = PlaceCategory.PLATEAU,
    ),
    RIVER(
        placeCategory = PlaceCategory.RIVER,
        titleOverride = SharedRes.strings.destinations_type_river,
    ),
    LOOKOUT(
        placeCategory = PlaceCategory.VIEWPOINT,
        titleOverride = SharedRes.strings.destinations_type_lookout,
    ),
    CHURCH(
        placeCategory = PlaceCategory.CHURCH,
    ),
    STATUE(
        placeCategory = PlaceCategory.MUSEUM,
        titleOverride = SharedRes.strings.destinations_type_statue,
        iconResOverride = SharedRes.images.ic_destination_type_statue,
    ),
    WILDLIFE_PARK(
        placeCategory = PlaceCategory.WILDLIFE_PARK,
    ),
    ARBORETUM(
        placeCategory = PlaceCategory.GARDEN,
        titleOverride = SharedRes.strings.destinations_type_arboretum,
    ),
    MUSEUM(
        placeCategory = PlaceCategory.MUSEUM,
    ),
    MEADOW(
        placeCategory = PlaceCategory.MEADOW,
    ),
    GORGE(
        placeCategory = PlaceCategory.VALLEY,
        titleOverride = SharedRes.strings.destinations_type_gorge,
    ),
    OBSERVATORY(
        placeCategory = PlaceCategory.OBSERVATORY,
        titleOverride = SharedRes.strings.destinations_type_stargazing,
    ),
    SPRING(
        placeCategory = PlaceCategory.SPRING,
        iconResOverride = SharedRes.images.ic_destination_type_spring,
    ),
    FOREST(
        placeCategory = PlaceCategory.FOREST,
        titleOverride = SharedRes.strings.destinations_type_forest,
    ),
    CAMP(
        placeCategory = PlaceCategory.CAMP_SITE,
        titleOverride = SharedRes.strings.destinations_type_camp,
    ),
    ISLAND(
        placeCategory = PlaceCategory.ISLAND,
    ),
    EDUCATIONAL_TRAIL(
        placeCategory = PlaceCategory.TRAIL,
        titleOverride = SharedRes.strings.destinations_type_educational_trail,
    ),
    CANOPY_WALK(
        placeCategory = PlaceCategory.VIEWPOINT,
        titleOverride = SharedRes.strings.destinations_type_canopy_walk,
    ),
    SCENIC_RAILWAY(
        placeCategory = PlaceCategory.SCENIC_RAILWAY,
        titleOverride = SharedRes.strings.destinations_type_scenic_railway,
        colorResOverride = SharedRes.colors.colorDestinationTypeScenicRailway,
    ),
    GEOLOGICAL_SITE(
        placeCategory = PlaceCategory.ROCK,
        titleOverride = SharedRes.strings.destinations_type_geological_site,
    ),
    VISITOR_CENTRE(
        placeCategory = PlaceCategory.MUSEUM,
        titleOverride = SharedRes.strings.destinations_type_visitor_centre,
    ),
    HISTORIC_SITE(
        placeCategory = PlaceCategory.HISTORIC,
        titleOverride = SharedRes.strings.destinations_type_historic_site,
    ),
    PALACE(
        placeCategory = PlaceCategory.CASTLE,
        titleOverride = SharedRes.strings.destinations_type_palace,
        iconResOverride = SharedRes.images.ic_destination_type_palace,
    ),
    CASTLE_RUIN(
        placeCategory = PlaceCategory.CASTLE,
        titleOverride = SharedRes.strings.destinations_type_castle_ruin,
    ),
    ADVENTURE_PARK(
        placeCategory = PlaceCategory.CAMP_SITE,
        titleOverride = SharedRes.strings.destinations_type_adventure_park,
        iconResOverride = SharedRes.images.ic_destination_type_adventure_park,
    ),
    RELIGIOUS_SITE(
        placeCategory = PlaceCategory.HISTORIC,
        titleOverride = SharedRes.strings.destinations_type_religious_site,
    ),
    WATER_TOUR(
        placeCategory = PlaceCategory.RIVER,
        titleOverride = SharedRes.strings.destinations_type_water_tour,
    ),
    CAVE_GUIDE_REQUIRED(
        placeCategory = PlaceCategory.CAVE,
        titleOverride = SharedRes.strings.destinations_type_cave_guide_required,
    ),
    ;

    val title: StringResource
        get() = titleOverride
            ?: placeCategory?.title
            ?: error("$name has no title override nor a placeCategory")

    val iconRes: ImageResource
        get() = iconResOverride
            ?: placeCategory?.iconRes
            ?: error("$name has no icon override nor a placeCategory")

    val colorRes: ColorResource
        get() = colorResOverride
            ?: placeCategory?.categoryColorRes
            ?: error("$name has no color override nor a placeCategory")
}
