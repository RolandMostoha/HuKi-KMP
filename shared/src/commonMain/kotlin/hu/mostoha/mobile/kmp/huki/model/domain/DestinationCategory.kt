package hu.mostoha.mobile.kmp.huki.model.domain

import dev.icerock.moko.resources.ColorResource
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.StringResource

data class DestinationCategory(
    val id: String,
    val title: StringResource,
    val iconRes: ImageResource,
    val colorRes: ColorResource,
    val destinations: List<Destination>,
)
