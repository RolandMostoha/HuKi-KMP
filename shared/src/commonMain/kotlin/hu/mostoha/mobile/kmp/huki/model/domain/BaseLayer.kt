package hu.mostoha.mobile.kmp.huki.model.domain

import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.StringResource
import hu.mostoha.mobile.huki.shared.SharedRes

enum class BaseLayer(
    val title: StringResource,
    val image: ImageResource,
    val hasDarkVariant: Boolean,
) {
    OUTDOORS(
        title = SharedRes.strings.layers_base_outdoors_title,
        image = SharedRes.images.ic_layers_outdoors,
        hasDarkVariant = true,
    ),
    CITY(
        title = SharedRes.strings.layers_base_city_title,
        image = SharedRes.images.ic_layers_streets,
        hasDarkVariant = true,
    ),
    SATELLITE(
        title = SharedRes.strings.layers_base_satellite_title,
        image = SharedRes.images.ic_layers_satellite,
        hasDarkVariant = true,
    ),
    TUHU(
        title = SharedRes.strings.layers_base_tuhu_title,
        image = SharedRes.images.ic_layers_tuhu,
        hasDarkVariant = false,
    ),
    ;

    /**
     * Whether the map renders dark, a light-only layer stays light in dark mode.
     */
    fun isDarkMap(isDarkMode: Boolean): Boolean = isDarkMode && hasDarkVariant
}
