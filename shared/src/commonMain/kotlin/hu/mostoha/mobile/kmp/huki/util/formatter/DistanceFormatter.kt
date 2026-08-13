package hu.mostoha.mobile.kmp.huki.util.formatter

import org.maplibre.spatialk.units.Length
import org.maplibre.spatialk.units.extensions.inMeters
import kotlin.math.round
import kotlin.math.roundToInt

object DistanceFormatter {

    private const val METERS_IN_KILOMETER = 1000.0
    private const val WHOLE_KILOMETERS_THRESHOLD = 100.0

    /**
     * 1. Distance below a kilometer in meters,
     * 2. Above in kilometers with one decimal
     * 3. Above 100 kilometers without decimal
     * E.g. `850 m`, `12.4 km`, `15 km`, `220 km`.
     */
    fun formatDistance(distance: Length): String {
        val meters = distance.inMeters

        return if (meters < METERS_IN_KILOMETER) {
            "${meters.roundToInt()} m"
        } else {
            "${formatKilometers(meters / METERS_IN_KILOMETER)} km"
        }
    }

    fun formatMeters(meters: Int): String = "$meters m"

    private fun formatKilometers(kilometers: Double): String {
        if (kilometers >= WHOLE_KILOMETERS_THRESHOLD) {
            return kilometers.roundToInt().toString()
        }

        val rounded = round(kilometers * 10) / 10

        return if (rounded % 1.0 == 0.0) {
            rounded.roundToInt().toString()
        } else {
            rounded.toString()
        }
    }
}
