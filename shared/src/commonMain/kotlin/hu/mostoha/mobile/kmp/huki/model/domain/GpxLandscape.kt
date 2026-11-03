package hu.mostoha.mobile.kmp.huki.model.domain

import dev.icerock.moko.resources.ColorResource
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.StringResource

/**
 * The [Landscape] a GPX runs in, trimmed to what its tag shows.
 */
data class GpxLandscape(
    val id: String,
    val nameRes: StringResource,
    val colorRes: ColorResource,
    val iconRes: ImageResource,
)
