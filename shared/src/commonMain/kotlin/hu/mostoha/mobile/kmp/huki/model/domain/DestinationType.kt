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
    HIGHEST_PEAK(
        placeCategory = PlaceCategory.PEAK,
        titleOverride = SharedRes.strings.destinations_type_highest_peak,
    ),
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
    NATIONAL_PARK(
        placeCategory = PlaceCategory.NATIONAL_PARK,
        titleOverride = SharedRes.strings.destinations_type_national_park,
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
    TOWN(
        placeCategory = PlaceCategory.SETTLEMENT,
        titleOverride = SharedRes.strings.destinations_type_town,
    ),
    PLATEAU(
        placeCategory = PlaceCategory.PLATEAU,
    ),
    TRAIL(
        placeCategory = PlaceCategory.TRAIL,
    ),
    RIVER(
        placeCategory = PlaceCategory.RIVER,
        titleOverride = SharedRes.strings.destinations_type_river,
    ),
    LOOKOUT(
        placeCategory = PlaceCategory.VIEWPOINT,
        titleOverride = SharedRes.strings.destinations_type_lookout,
    ),
    ROCK_WITH_LOOKOUT(
        placeCategory = PlaceCategory.ROCK,
        titleOverride = SharedRes.strings.destinations_type_rock_with_lookout,
    ),
    CHURCH(
        placeCategory = PlaceCategory.CHURCH,
    ),
    STATUE(
        placeCategory = PlaceCategory.MUSEUM,
        titleOverride = SharedRes.strings.destinations_type_statue,
    ),
    WILDLIFE_PARK(
        placeCategory = PlaceCategory.WILDLIFE_PARK,
    ),
    MEMORIAL_PARK(
        placeCategory = PlaceCategory.HISTORIC,
        titleOverride = SharedRes.strings.destinations_type_memorial_park,
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
    ),
    GARDEN(
        placeCategory = PlaceCategory.GARDEN,
    ),
    SPRING(
        placeCategory = PlaceCategory.SPRING,
    ),
    FOREST(
        placeCategory = PlaceCategory.FOREST,
    ),
    CAMP(
        placeCategory = PlaceCategory.CAMP_SITE,
        titleOverride = SharedRes.strings.destinations_type_camp,
    ),
    ISLAND(
        placeCategory = PlaceCategory.ISLAND,
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
