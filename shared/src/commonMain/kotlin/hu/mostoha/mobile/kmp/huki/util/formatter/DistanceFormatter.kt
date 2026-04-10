package hu.mostoha.mobile.kmp.huki.util.formatter

import org.maplibre.spatialk.units.Length
import org.maplibre.spatialk.units.extensions.inMeters
import kotlin.math.round
import kotlin.math.roundToInt

object DistanceFormatter {

    fun formatDistance(distance: Length): String {
        val meters = distance.inMeters

        return if (meters < 1000) {
            "${meters.roundToInt()} m"
        } else {
            "${formatKilometers(meters / 1000)} km"
        }
    }

    fun formatMeters(meters: Int): String = "$meters m"

    private fun formatKilometers(kilometers: Double): String {
        val rounded = round(kilometers * 10) / 10

        return if (rounded % 1.0 == 0.0) {
            rounded.roundToInt().toString()
        } else {
            rounded.toString()
        }
    }
}
