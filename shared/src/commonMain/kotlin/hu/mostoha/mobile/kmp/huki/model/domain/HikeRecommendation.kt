package hu.mostoha.mobile.kmp.huki.model.domain

import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.StringResource
import hu.mostoha.mobile.huki.shared.SharedRes

enum class HikeRecommendation(
    val title: StringResource,
    val iconRes: ImageResource,
    val baseUrl: String,
    val areaUrl: String,
) {
    AKTIVKALANDOR(
        title = SharedRes.strings.hike_recommender_aktivkalandor,
        iconRes = SharedRes.images.ic_aktivkalandor,
        baseUrl = "https://aktivkalandor.hu/turak-a-terkepen/",
        areaUrl = "https://aktivkalandor.hu/tajegysegek/%s",
    ),
    KIRANDULASTIPPEK(
        title = SharedRes.strings.hike_recommender_kirandulastippek,
        iconRes = SharedRes.images.ic_kirandulastippek,
        baseUrl = "https://kirandulastippek.hu",
        areaUrl = "https://kirandulastippek.hu/%s?tag=gyalogtura",
    ),
    TERMESZETJARO(
        title = SharedRes.strings.hike_recommender_termeszetjaro,
        iconRes = SharedRes.images.ic_termeszetjaro,
        baseUrl = "https://www.termeszetjaro.hu",
        areaUrl = "https://www.termeszetjaro.hu/hu/tours/?cat=22729870#area=%s&wt=%s",
    ),
}
