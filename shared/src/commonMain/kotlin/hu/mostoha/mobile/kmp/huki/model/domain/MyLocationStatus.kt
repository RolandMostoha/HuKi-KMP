package hu.mostoha.mobile.kmp.huki.model.domain

import dev.icerock.moko.resources.StringResource
import hu.mostoha.mobile.huki.shared.SharedRes

sealed class MyLocationStatus(val accessibilityId: StringResource) {
    /**
     * My location is not available.
     */
    data object NotAvailable : MyLocationStatus(SharedRes.strings.my_location_a11y_not_available)

    /**
     * My location is displayed on the map, without following.
     */
    data object Default : MyLocationStatus(SharedRes.strings.my_location_a11y_default)

    /**
     * My location is followed with fixed heading=0.
     */
    data object Following : MyLocationStatus(SharedRes.strings.my_location_a11y_following)

    /**
     * My location is followed with dynamic heading, based on live compass.
     */
    data object FollowingLiveCompass : MyLocationStatus(SharedRes.strings.my_location_a11y_live_compass)
}
