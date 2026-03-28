package hu.mostoha.mobile.kmp.huki.model.domain

import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.StringResource
import hu.mostoha.mobile.huki.shared.SharedRes

enum class BaseLayer(
    val title: StringResource,
    val image: ImageResource,
) {
    OUTDOORS(
        title = SharedRes.strings.layers_base_outdoors_title,
        image = SharedRes.images.ic_layers_outdoors,
    ),
    STREET(
        title = SharedRes.strings.layers_base_streets_title,
        image = SharedRes.images.ic_layers_streets,
    ),
    SATELLITE(
        title = SharedRes.strings.layers_base_satellite_title,
        image = SharedRes.images.ic_layers_satellite,
    ),
}
