package hu.mostoha.mobile.kmp.huki.util.formatter

import hu.mostoha.mobile.kmp.huki.model.domain.Location
import kotlin.math.abs
import kotlin.math.round

private const val COORDINATE_DECIMALS = 5
private const val COORDINATE_FACTOR = 100_000L

object CoordinateFormatter {

    fun formatCoordinates(location: Location): String =
        "(${location.latitude.toFixed()}, ${location.longitude.toFixed()})"

    private fun Double.toFixed(): String {
        val scaled = round(abs(this) * COORDINATE_FACTOR).toLong()
        val whole = scaled / COORDINATE_FACTOR
        val fraction = (scaled % COORDINATE_FACTOR).toString().padStart(COORDINATE_DECIMALS, '0')
        val sign = if (this < 0 && scaled != 0L) "-" else ""

        return "$sign$whole.$fraction"
    }
}
