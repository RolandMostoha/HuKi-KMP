package hu.mostoha.mobile.kmp.huki.model.domain

import dev.icerock.moko.resources.StringResource
import hu.mostoha.mobile.huki.shared.SharedRes

enum class PlaceCategoryGroup(val title: StringResource) {
    NATURE(SharedRes.strings.place_category_group_nature),
    ATTRACTIONS(SharedRes.strings.place_category_group_attractions),
    SHOPS(SharedRes.strings.place_category_group_shops),
    TRAVEL(SharedRes.strings.place_category_group_travel),
    USEFUL(SharedRes.strings.place_category_group_useful),
}
