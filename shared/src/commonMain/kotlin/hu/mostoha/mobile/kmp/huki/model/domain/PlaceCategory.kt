package hu.mostoha.mobile.kmp.huki.model.domain

import co.touchlab.kermit.Logger
import dev.icerock.moko.resources.ColorResource
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.StringResource
import hu.mostoha.mobile.huki.shared.SharedRes

enum class PlaceCategory(
    val osmTags: List<String>,
    val categoryGroup: PlaceCategoryGroup,
    val title: StringResource,
    val iconRes: ImageResource,
    val categoryColorRes: ColorResource,
    val osmClasses: List<String> = emptyList(),
) {
    PEAK(
        osmTags = listOf(
            "peak",
            "saddle",
            "volcano",
            "ridge",
        ),
        categoryGroup = PlaceCategoryGroup.NATURE,
        title = SharedRes.strings.place_category_peak,
        iconRes = SharedRes.images.ic_place_category_peak,
        categoryColorRes = SharedRes.colors.colorPlaceCategoryPeak,
    ),
    FOREST(
        osmTags = listOf(
            "forest",
            "forest_planning",
            "wood",
        ),
        categoryGroup = PlaceCategoryGroup.NATURE,
        title = SharedRes.strings.place_category_forest,
        iconRes = SharedRes.images.ic_place_category_forest,
        categoryColorRes = SharedRes.colors.colorPlaceCategoryForest,
    ),
    RIVER(
        osmTags = listOf(
            "river",
            "stream",
            "ditch",
            "drain",
            "canal",
        ),
        categoryGroup = PlaceCategoryGroup.NATURE,
        title = SharedRes.strings.place_category_river,
        iconRes = SharedRes.images.ic_place_category_river,
        categoryColorRes = SharedRes.colors.colorPlaceCategoryRiver,
    ),
    WATERFALL(
        osmTags = listOf(
            "waterfall",
        ),
        categoryGroup = PlaceCategoryGroup.NATURE,
        title = SharedRes.strings.place_category_waterfall,
        iconRes = SharedRes.images.ic_place_category_waterfall,
        categoryColorRes = SharedRes.colors.colorPlaceCategoryWaterfall,
    ),
    LAKE(
        osmTags = listOf(
            "water",
            "lake",
            "pond",
            "reservoir",
            "basin",
            "bay",
            "dam",
        ),
        categoryGroup = PlaceCategoryGroup.NATURE,
        title = SharedRes.strings.place_category_lake,
        iconRes = SharedRes.images.ic_place_category_lake,
        categoryColorRes = SharedRes.colors.colorPlaceCategoryLake,
    ),
    SPRING(
        osmTags = listOf(
            "spring",
            "hot_spring",
        ),
        categoryGroup = PlaceCategoryGroup.NATURE,
        title = SharedRes.strings.place_category_spring,
        iconRes = SharedRes.images.ic_place_category_spring,
        categoryColorRes = SharedRes.colors.colorPlaceCategorySpring,
    ),
    CAVE(
        osmTags = listOf(
            "cave_entrance",
        ),
        categoryGroup = PlaceCategoryGroup.NATURE,
        title = SharedRes.strings.place_category_cave,
        iconRes = SharedRes.images.ic_place_category_cave,
        categoryColorRes = SharedRes.colors.colorPlaceCategoryCave,
    ),
    ROCK(
        osmTags = listOf(
            "rock",
            "cliff",
            "stone",
            "arch",
            "sinkhole",
        ),
        categoryGroup = PlaceCategoryGroup.NATURE,
        title = SharedRes.strings.place_category_rock,
        iconRes = SharedRes.images.ic_place_category_rock,
        categoryColorRes = SharedRes.colors.colorPlaceCategoryRock,
    ),
    VIEWPOINT(
        osmTags = listOf(
            "viewpoint",
            "tower",
            "observation_tower",
        ),
        categoryGroup = PlaceCategoryGroup.ATTRACTIONS,
        title = SharedRes.strings.place_category_viewpoint,
        iconRes = SharedRes.images.ic_place_category_viewpoint,
        categoryColorRes = SharedRes.colors.colorPlaceCategoryViewpoint,
    ),
    CASTLE(
        osmTags = listOf(
            "castle",
            "fortress",
            "manor",
        ),
        categoryGroup = PlaceCategoryGroup.ATTRACTIONS,
        title = SharedRes.strings.place_category_castle,
        iconRes = SharedRes.images.ic_place_category_castle,
        categoryColorRes = SharedRes.colors.colorPlaceCategoryCastle,
    ),
    HISTORIC(
        osmClasses = listOf("historic"),
        osmTags = listOf(
            "monument",
            "memorial",
            "ruins",
            "archaeological_site",
            "wayside_cross",
            "wayside_shrine",
            "boundary_stone",
        ),
        categoryGroup = PlaceCategoryGroup.ATTRACTIONS,
        title = SharedRes.strings.place_category_historic,
        iconRes = SharedRes.images.ic_place_category_historic,
        categoryColorRes = SharedRes.colors.colorPlaceCategoryHistoric,
    ),
    CHURCH(
        osmTags = listOf(
            "place_of_worship",
            "church",
            "chapel",
            "monastery",
            "cathedral",
        ),
        categoryGroup = PlaceCategoryGroup.ATTRACTIONS,
        title = SharedRes.strings.place_category_church,
        iconRes = SharedRes.images.ic_place_category_church,
        categoryColorRes = SharedRes.colors.colorPlaceCategoryChurch,
    ),
    MUSEUM(
        osmTags = listOf(
            "museum",
        ),
        categoryGroup = PlaceCategoryGroup.ATTRACTIONS,
        title = SharedRes.strings.place_category_museum,
        iconRes = SharedRes.images.ic_place_category_museum,
        categoryColorRes = SharedRes.colors.colorPlaceCategoryMuseum,
    ),
    SHOP(
        osmClasses = listOf("shop"),
        osmTags = listOf(
            "supermarket",
            "convenience",
            "greengrocer",
            "deli",
            "beverages",
            "alcohol",
            "general",
            "organic",
            "butcher",
            "bakery",
        ),
        categoryGroup = PlaceCategoryGroup.SHOPS,
        title = SharedRes.strings.place_category_grocery,
        iconRes = SharedRes.images.ic_place_category_shop,
        categoryColorRes = SharedRes.colors.colorPlaceCategoryShop,
    ),
    RESTAURANT(
        osmTags = listOf(
            "restaurant",
            "fast_food",
            "cafe",
            "bar",
            "pub",
        ),
        categoryGroup = PlaceCategoryGroup.SHOPS,
        title = SharedRes.strings.place_category_restaurant,
        iconRes = SharedRes.images.ic_place_category_restaurant,
        categoryColorRes = SharedRes.colors.colorPlaceCategoryRestaurant,
    ),
    PHARMACY(
        osmTags = listOf(
            "pharmacy",
        ),
        categoryGroup = PlaceCategoryGroup.SHOPS,
        title = SharedRes.strings.place_category_pharmacy,
        iconRes = SharedRes.images.ic_place_category_pharmacy,
        categoryColorRes = SharedRes.colors.colorPlaceCategoryPharmacy,
    ),
    PARKING(
        osmTags = listOf(
            "parking",
            "parking_entrance",
        ),
        categoryGroup = PlaceCategoryGroup.TRAVEL,
        title = SharedRes.strings.place_category_parking,
        iconRes = SharedRes.images.ic_place_category_parking,
        categoryColorRes = SharedRes.colors.colorPlaceCategoryParking,
    ),
    PUBLIC_TRANSPORT(
        osmTags = listOf(
            "public_transport",
            "bus_stop",
            "station",
            "halt",
            "tram_stop",
            "platform",
        ),
        categoryGroup = PlaceCategoryGroup.TRAVEL,
        title = SharedRes.strings.place_category_public_transport,
        iconRes = SharedRes.images.ic_place_category_public_transport,
        categoryColorRes = SharedRes.colors.colorPlaceCategoryPublicTransport,
    ),
    SETTLEMENT(
        osmTags = listOf(
            "city",
            "town",
            "village",
            "hamlet",
            "isolated_dwelling",
            "locality",
            "suburb",
            "neighbourhood",
            "residential",
        ),
        categoryGroup = PlaceCategoryGroup.TRAVEL,
        title = SharedRes.strings.place_category_settlement,
        iconRes = SharedRes.images.ic_place_category_settlement,
        categoryColorRes = SharedRes.colors.colorPlaceCategorySettlement,
    ),
    DRINKING_WATER(
        osmTags = listOf(
            "drinking_water",
        ),
        categoryGroup = PlaceCategoryGroup.USEFUL,
        title = SharedRes.strings.place_category_drinking_water,
        iconRes = SharedRes.images.ic_place_category_drinking_water,
        categoryColorRes = SharedRes.colors.colorPlaceCategoryDrinkingWater,
    ),
    FIREPLACE(
        osmTags = listOf(
            "fireplace",
            "firepit",
            "bbq",
        ),
        categoryGroup = PlaceCategoryGroup.USEFUL,
        title = SharedRes.strings.place_category_fireplace,
        iconRes = SharedRes.images.ic_place_category_fireplace,
        categoryColorRes = SharedRes.colors.colorPlaceCategoryFireplace,
    ),
    CAMP_SITE(
        osmTags = listOf(
            "camp_site",
            "picnic_site",
            "caravan_site",
            "camp_pitch",
            "alpine_hut",
            "wilderness_hut",
            "shelter",
        ),
        categoryGroup = PlaceCategoryGroup.USEFUL,
        title = SharedRes.strings.place_category_camp_site,
        iconRes = SharedRes.images.ic_place_category_camp,
        categoryColorRes = SharedRes.colors.colorPlaceCategoryCampSite,
    ),
    GUIDEPOST(
        osmTags = listOf(
            "guidepost",
            "information",
            "board",
            "map",
        ),
        categoryGroup = PlaceCategoryGroup.USEFUL,
        title = SharedRes.strings.place_category_guidepost,
        iconRes = SharedRes.images.ic_place_category_guidepost,
        categoryColorRes = SharedRes.colors.colorPlaceCategoryGuidepost,
    ),
    TOILET(
        osmTags = listOf(
            "toilets",
        ),
        categoryGroup = PlaceCategoryGroup.USEFUL,
        title = SharedRes.strings.place_category_toilets,
        iconRes = SharedRes.images.ic_place_category_toilets,
        categoryColorRes = SharedRes.colors.colorPlaceCategoryToilet,
    ),
    NATIONAL_PARK(
        osmTags = listOf(
            "park",
            "national_park",
            "protected_area",
            "nature_reserve",
        ),
        categoryGroup = PlaceCategoryGroup.NATURE,
        title = SharedRes.strings.place_category_national_park,
        iconRes = SharedRes.images.ic_place_category_forest,
        categoryColorRes = SharedRes.colors.colorPlaceCategoryNationalPark,
    ),
    VALLEY(
        osmTags = listOf(
            "valley",
            "gorge",
        ),
        categoryGroup = PlaceCategoryGroup.NATURE,
        title = SharedRes.strings.place_category_valley,
        iconRes = SharedRes.images.ic_place_category_valley,
        categoryColorRes = SharedRes.colors.colorPlaceCategoryValley,
    ),
    PLATEAU(
        osmTags = listOf(
            "plateau",
        ),
        categoryGroup = PlaceCategoryGroup.NATURE,
        title = SharedRes.strings.place_category_plateau,
        iconRes = SharedRes.images.ic_place_category_plateau,
        categoryColorRes = SharedRes.colors.colorPlaceCategoryPlateau,
    ),
    ISLAND(
        osmTags = listOf(
            "island",
            "islet",
            "archipelago",
        ),
        categoryGroup = PlaceCategoryGroup.NATURE,
        title = SharedRes.strings.place_category_island,
        iconRes = SharedRes.images.ic_place_category_island,
        categoryColorRes = SharedRes.colors.colorPlaceCategoryIsland,
    ),
    MEADOW(
        osmTags = listOf(
            "meadow",
            "grassland",
            "heath",
        ),
        categoryGroup = PlaceCategoryGroup.NATURE,
        title = SharedRes.strings.place_category_meadow,
        iconRes = SharedRes.images.ic_place_category_meadow,
        categoryColorRes = SharedRes.colors.colorPlaceCategoryMeadow,
    ),
    GARDEN(
        osmTags = listOf(
            "garden",
            "arboretum",
        ),
        categoryGroup = PlaceCategoryGroup.ATTRACTIONS,
        title = SharedRes.strings.place_category_garden,
        iconRes = SharedRes.images.ic_place_category_garden,
        categoryColorRes = SharedRes.colors.colorPlaceCategoryGarden,
    ),
    WILDLIFE_PARK(
        osmTags = listOf(
            "zoo",
            "wildlife_park",
            "aquarium",
        ),
        categoryGroup = PlaceCategoryGroup.ATTRACTIONS,
        title = SharedRes.strings.place_category_wildlife_park,
        iconRes = SharedRes.images.ic_place_category_wildlife_park,
        categoryColorRes = SharedRes.colors.colorPlaceCategoryWildlifePark,
    ),
    OBSERVATORY(
        osmTags = listOf(
            "observatory",
            "planetarium",
        ),
        categoryGroup = PlaceCategoryGroup.ATTRACTIONS,
        title = SharedRes.strings.place_category_observatory,
        iconRes = SharedRes.images.ic_place_category_observatory,
        categoryColorRes = SharedRes.colors.colorPlaceCategoryObservatory,
    ),
    FARM(
        osmTags = listOf(
            "farm",
            "farmyard",
            "farmland",
        ),
        categoryGroup = PlaceCategoryGroup.TRAVEL,
        title = SharedRes.strings.place_category_farm,
        iconRes = SharedRes.images.ic_place_category_farm,
        categoryColorRes = SharedRes.colors.colorPlaceCategoryFarm,
    ),
    TRAIL(
        osmTags = listOf(
            "path",
            "footway",
            "track",
            "steps",
            "hiking",
        ),
        categoryGroup = PlaceCategoryGroup.TRAVEL,
        title = SharedRes.strings.place_category_trail,
        iconRes = SharedRes.images.ic_place_category_trail,
        categoryColorRes = SharedRes.colors.oktBlue,
    ),
    ;

    companion object {
        private val warnedTypes = mutableSetOf<String>()

        /**
         * Resolves from the [osmTag] of a place ("peak" of `natural=peak`), falling back to its
         * broader [osmClass] ("shop" of `shop=fishing`) when the tag itself is unmapped.
         */
        fun fromString(osmTag: String?, osmClass: String? = null): PlaceCategory? {
            if (osmTag == null && osmClass == null) return null
            val tagCategory = entries.firstOrNull { osmTag != null && osmTag in it.osmTags }
            val category = tagCategory ?: entries.firstOrNull { osmClass != null && osmClass in it.osmClasses }
            if (tagCategory == null && osmTag != null && warnedTypes.add(osmTag)) {
                val fallback = category?.let { " (fell back to $it)" }.orEmpty()
                Logger.w { "PlaceCategory: unmapped OSM tag '$osmClass=$osmTag'$fallback" }
            }
            return category
        }
    }
}
