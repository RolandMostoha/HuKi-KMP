package hu.mostoha.mobile.kmp.huki.model.domain

import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.StringResource

data class InfoViewData(
    val infoViewType: InfoViewType,
    val icon: ImageResource,
    val title: StringResource,
    val message: StringResource,
)
