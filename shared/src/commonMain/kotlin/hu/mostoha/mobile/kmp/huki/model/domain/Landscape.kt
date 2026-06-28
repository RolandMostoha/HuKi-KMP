package hu.mostoha.mobile.kmp.huki.model.domain

import dev.icerock.moko.resources.StringResource

data class Landscape(
    val osmId: String,
    val osmType: OsmType,
    val nameRes: StringResource,
    val landscapeType: LandscapeType,
    val center: Location,
    val destinations: List<Destination>,
    val areaTags: Map<HikeRecommendation, String>,
    val termeszetjaroTag: TermeszetjaroTag? = null,
)
