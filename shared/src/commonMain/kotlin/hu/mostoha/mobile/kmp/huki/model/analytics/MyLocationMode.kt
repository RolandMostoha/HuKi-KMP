package hu.mostoha.mobile.kmp.huki.model.analytics

enum class MyLocationMode(val value: String) {
    // Camera follows the location with a fixed north heading
    FOLLOWING("following"),

    // Camera follows the location and rotates with the live compass (real navigation)
    LIVE_COMPASS("live_compass"),
}
