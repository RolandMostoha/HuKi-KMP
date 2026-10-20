package hu.mostoha.mobile.kmp.huki.model.domain

import dev.icerock.moko.resources.ColorResource
import dev.icerock.moko.resources.StringResource

data class Landscape(
    val id: String,
    val nameRes: StringResource,
    val colorRes: ColorResource,
    val landscapeType: LandscapeType,
    val center: Location,
    val destinations: List<Destination>,
    val areaTags: Map<HikeRecommendation, String>,
    val termeszetjaroTag: TermeszetjaroTag? = null,
)
