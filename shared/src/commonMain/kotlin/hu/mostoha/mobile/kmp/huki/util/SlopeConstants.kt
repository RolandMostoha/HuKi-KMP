package hu.mostoha.mobile.kmp.huki.util

object SlopeConstants {
    /**
     * Absorbs GPS / DEM noise on flat sections.
     */
    const val FLAT_SLOPE_PERCENT = 3.0
    const val MID_SLOPE_PERCENT = 10.0
    const val HIGH_SLOPE_PERCENT = 25.0

    /**
     * Minimum distance a slope is measured over, as point-to-point slopes amplify GPS / DEM altitude noise.
     */
    const val WINDOW_METERS = 100.0
    const val SEGMENT_LENGTH_METERS = 20.0

    /**
     * Caps the map features of long tracks, which get longer segments instead.
     */
    const val MAX_SEGMENTS = 500
}
