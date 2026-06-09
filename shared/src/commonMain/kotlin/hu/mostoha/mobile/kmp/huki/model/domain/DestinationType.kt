package hu.mostoha.mobile.kmp.huki.model.domain

import dev.icerock.moko.resources.ColorResource
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.StringResource
import hu.mostoha.mobile.huki.shared.SharedRes

enum class DestinationType(
    val iconRes: ImageResource,
    val title: StringResource,
    val colorRes: ColorResource,
) {
    HIGHEST_PEAK(
        iconRes = SharedRes.images.ic_place_category_peak,
        title = SharedRes.strings.destinations_type_highest_peak,
        colorRes = SharedRes.colors.colorPlaceCategoryPeak,
    ),
    PEAK(
        iconRes = SharedRes.images.ic_place_category_peak,
        title = SharedRes.strings.destinations_type_peak,
        colorRes = SharedRes.colors.colorPlaceCategoryPeak,
    ),
    VALLEY(
        iconRes = SharedRes.images.ic_place_category_valley,
        title = SharedRes.strings.destinations_type_valley,
        colorRes = SharedRes.colors.colorPlaceCategoryValley,
    ),
    LAKE(
        iconRes = SharedRes.images.ic_place_category_lake,
        title = SharedRes.strings.destinations_type_lake,
        colorRes = SharedRes.colors.colorPlaceCategoryLake,
    ),
    NATIONAL_PARK(
        iconRes = SharedRes.images.ic_place_category_forest,
        title = SharedRes.strings.destinations_type_national_park,
        colorRes = SharedRes.colors.colorPlaceCategoryNationalPark,
    ),
    CAVE(
        iconRes = SharedRes.images.ic_place_category_cave,
        title = SharedRes.strings.destinations_type_cave,
        colorRes = SharedRes.colors.colorPlaceCategoryCave,
    ),
    WATERFALL(
        iconRes = SharedRes.images.ic_place_category_waterfall,
        title = SharedRes.strings.destinations_type_waterfall,
        colorRes = SharedRes.colors.colorPlaceCategoryWaterfall,
    ),
    CASTLE(
        iconRes = SharedRes.images.ic_place_category_castle,
        title = SharedRes.strings.destinations_type_castle,
        colorRes = SharedRes.colors.colorPlaceCategoryCastle,
    ),
    ROCK(
        iconRes = SharedRes.images.ic_place_category_rock,
        title = SharedRes.strings.destinations_type_rock,
        colorRes = SharedRes.colors.colorPlaceCategoryRock,
    ),
    TOWN(
        iconRes = SharedRes.images.ic_place_category_settlement,
        title = SharedRes.strings.destinations_type_town,
        colorRes = SharedRes.colors.colorPlaceCategorySettlement,
    ),
    PLATEAU(
        iconRes = SharedRes.images.ic_place_category_plateau,
        title = SharedRes.strings.destinations_type_plateau,
        colorRes = SharedRes.colors.colorPlaceCategoryPlateau,
    ),
    TRAIL(
        iconRes = SharedRes.images.ic_place_category_trail,
        title = SharedRes.strings.destinations_type_trail,
        colorRes = SharedRes.colors.oktBlue,
    ),
    RIVER(
        iconRes = SharedRes.images.ic_place_category_river,
        title = SharedRes.strings.destinations_type_river,
        colorRes = SharedRes.colors.colorPlaceCategoryRiver,
    ),
    LOOKOUT(
        iconRes = SharedRes.images.ic_place_category_viewpoint,
        title = SharedRes.strings.destinations_type_lookout,
        colorRes = SharedRes.colors.colorPlaceCategoryViewpoint,
    ),
    ROCK_WITH_LOOKOUT(
        iconRes = SharedRes.images.ic_place_category_rock,
        title = SharedRes.strings.destinations_type_rock_with_lookout,
        colorRes = SharedRes.colors.colorPlaceCategoryRock,
    ),
    CHURCH(
        iconRes = SharedRes.images.ic_place_category_church,
        title = SharedRes.strings.destinations_type_church,
        colorRes = SharedRes.colors.colorPlaceCategoryChurch,
    ),
    STATUE(
        iconRes = SharedRes.images.ic_place_category_museum,
        title = SharedRes.strings.destinations_type_statue,
        colorRes = SharedRes.colors.colorPlaceCategoryMuseum,
    ),
    WILDLIFE_PARK(
        iconRes = SharedRes.images.ic_place_category_wildlife_park,
        title = SharedRes.strings.destinations_type_wildlife_park,
        colorRes = SharedRes.colors.colorPlaceCategoryWildlifePark,
    ),
    MEMORIAL_PARK(
        iconRes = SharedRes.images.ic_place_category_historic,
        title = SharedRes.strings.destinations_type_memorial_park,
        colorRes = SharedRes.colors.colorPlaceCategoryHistoric,
    ),
    ARBORETUM(
        iconRes = SharedRes.images.ic_place_category_garden,
        title = SharedRes.strings.destinations_type_arboretum,
        colorRes = SharedRes.colors.colorPlaceCategoryGarden,
    ),
    MUSEUM(
        iconRes = SharedRes.images.ic_place_category_museum,
        title = SharedRes.strings.destinations_type_museum,
        colorRes = SharedRes.colors.colorPlaceCategoryMuseum,
    ),
    MEADOW(
        iconRes = SharedRes.images.ic_place_category_meadow,
        title = SharedRes.strings.destinations_type_meadow,
        colorRes = SharedRes.colors.colorPlaceCategoryMeadow,
    ),
    GORGE(
        iconRes = SharedRes.images.ic_place_category_valley,
        title = SharedRes.strings.destinations_type_gorge,
        colorRes = SharedRes.colors.colorPlaceCategoryValley,
    ),
    OBSERVATORY(
        iconRes = SharedRes.images.ic_place_category_observatory,
        title = SharedRes.strings.destinations_type_observatory,
        colorRes = SharedRes.colors.colorPlaceCategoryObservatory,
    ),
    GARDEN(
        iconRes = SharedRes.images.ic_place_category_garden,
        title = SharedRes.strings.destinations_type_garden,
        colorRes = SharedRes.colors.colorPlaceCategoryGarden,
    ),
    SPRING(
        iconRes = SharedRes.images.ic_place_category_spring,
        title = SharedRes.strings.destinations_type_spring,
        colorRes = SharedRes.colors.colorPlaceCategorySpring,
    ),
    FOREST(
        iconRes = SharedRes.images.ic_place_category_forest,
        title = SharedRes.strings.destinations_type_forest,
        colorRes = SharedRes.colors.colorPlaceCategoryForest,
    ),
    CAMP(
        iconRes = SharedRes.images.ic_place_category_camp,
        title = SharedRes.strings.destinations_type_camp,
        colorRes = SharedRes.colors.colorPlaceCategoryCampSite,
    ),
    ISLAND(
        iconRes = SharedRes.images.ic_place_category_island,
        title = SharedRes.strings.destinations_type_island,
        colorRes = SharedRes.colors.colorPlaceCategoryIsland,
    ),
}
