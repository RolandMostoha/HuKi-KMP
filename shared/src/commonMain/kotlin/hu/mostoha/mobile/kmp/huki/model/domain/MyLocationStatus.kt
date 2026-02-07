package hu.mostoha.mobile.kmp.huki.model.domain

sealed interface MyLocationStatus {
    /**
     * My location is not available.
     */
    data object NotAvailable : MyLocationStatus

    /**
     * My location is displayed on the map, without following.
     */
    data object Default : MyLocationStatus

    /**
     * My location is followed with fixed heading=0.
     */
    data object Following : MyLocationStatus

    /**
     * My location is followed with dynamic heading, based on live compass.
     */
    data object FollowingLiveCompass : MyLocationStatus
}
