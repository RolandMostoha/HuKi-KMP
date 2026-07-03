package hu.mostoha.mobile.kmp.huki.features.destinations

import dev.icerock.moko.resources.StringResource
import hu.mostoha.mobile.huki.shared.SharedRes

enum class DestinationsTab(val title: StringResource) {
    POPULAR(SharedRes.strings.destinations_tab_popular),
    LANDSCAPES(SharedRes.strings.destinations_tab_landscapes),
    NEARBY(SharedRes.strings.destinations_tab_nearby),
}
