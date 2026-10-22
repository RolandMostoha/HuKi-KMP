package hu.mostoha.mobile.kmp.huki.model.domain

import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.StringResource
import hu.mostoha.mobile.huki.shared.SharedRes

enum class DestinationChallenge(
    val placeCategory: PlaceCategory,
    val title: StringResource,
    private val iconResOverride: ImageResource? = null,
) {
    HIGHEST_PEAKS(
        placeCategory = PlaceCategory.PEAK,
        title = SharedRes.strings.destinations_challenge_highest_peaks,
    ),
    PANORAMIC_VIEWS(
        placeCategory = PlaceCategory.VIEWPOINT,
        title = SharedRes.strings.destinations_challenge_panoramic_views,
    ),
    BIRDWATCHING(
        placeCategory = PlaceCategory.WILDLIFE_PARK,
        title = SharedRes.strings.destinations_challenge_birdwatching,
        iconResOverride = SharedRes.images.ic_challenge_birdwatching,
    ),
    ;

    val iconRes: ImageResource
        get() = iconResOverride ?: placeCategory.iconRes
}
