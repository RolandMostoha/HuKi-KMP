package hu.mostoha.mobile.kmp.huki.model.mapper

import hu.mostoha.mobile.kmp.huki.data.Landscapes
import hu.mostoha.mobile.kmp.huki.model.domain.HikeRecommendation
import hu.mostoha.mobile.kmp.huki.model.domain.Landscape
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.util.distanceBetween
import io.ktor.http.encodeURLParameter

private const val AREA_ID_PLACEHOLDER = "{areaId}"
private const val AREA_NAME_PLACEHOLDER = "{areaName}"

/**
 * Links to the page of the landscape closest to [location], falling back to [HikeRecommendation.baseUrl].
 */
fun HikeRecommendation.toUrl(location: Location, landscapes: List<Landscape> = Landscapes): String {
    val landscape = landscapes.minBy { it.center.distanceBetween(location) }
    val url = when (this) {
        HikeRecommendation.TERMESZETJARO -> landscape.termeszetjaroTag?.let { tag ->
            areaUrl
                .replace(AREA_ID_PLACEHOLDER, tag.areaId)
                .replace(AREA_NAME_PLACEHOLDER, tag.areaName.encodeURLParameter(spaceToPlus = true))
        }
        HikeRecommendation.AKTIVKALANDOR,
        HikeRecommendation.KIRANDULASTIPPEK,
        -> landscape.areaTags[this]?.let { areaTag -> areaUrl.replace(AREA_ID_PLACEHOLDER, areaTag) }
    }
    return url ?: baseUrl
}
