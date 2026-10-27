package hu.mostoha.mobile.kmp.huki.theme

import hu.mostoha.mobile.kmp.huki.util.SlopeConstants.FLAT_SLOPE_PERCENT
import hu.mostoha.mobile.kmp.huki.util.SlopeConstants.HIGH_SLOPE_PERCENT
import hu.mostoha.mobile.kmp.huki.util.SlopeConstants.MID_SLOPE_PERCENT
import kotlin.math.roundToInt

/**
 * Slopes within ±[FLAT_SLOPE_PERCENT] keep the RGB [flatColor], so flat routes look like an uncolored route.
 */
class SlopeColorScale(flatColor: Int) {

    private val knots = listOf(
        -HIGH_SLOPE_PERCENT to DESCENT_HIGH_COLOR,
        -MID_SLOPE_PERCENT to DESCENT_MID_COLOR,
        -FLAT_SLOPE_PERCENT to (flatColor and RGB_MASK),
        FLAT_SLOPE_PERCENT to (flatColor and RGB_MASK),
        MID_SLOPE_PERCENT to ASCENT_MID_COLOR,
        HIGH_SLOPE_PERCENT to ASCENT_HIGH_COLOR,
    )

    /**
     * Returns a `#RRGGBB` color linearly blended between the neighbouring knots, clamped beyond the steepest ones.
     */
    fun colorHex(slopePercent: Double): String {
        val slope = slopePercent.coerceIn(knots.first().first, knots.last().first)
        val upperIndex = knots.indexOfFirst { (knotSlope, _) -> slope <= knotSlope }.coerceAtLeast(1)
        val (lowerSlope, lowerColor) = knots[upperIndex - 1]
        val (upperSlope, upperColor) = knots[upperIndex]
        val fraction = (slope - lowerSlope) / (upperSlope - lowerSlope)

        return RGB_CHANNEL_SHIFTS.joinToString(separator = "", prefix = "#") { shift ->
            val lower = lowerColor shr shift and CHANNEL_MASK
            val upper = upperColor shr shift and CHANNEL_MASK
            val channel = (lower + (upper - lower) * fraction).roundToInt()
            channel.toString(radix = HEX_RADIX).padStart(length = 2, padChar = '0').uppercase()
        }
    }

    private companion object {
        const val RGB_MASK = 0xFFFFFF
        const val CHANNEL_MASK = 0xFF
        const val HEX_RADIX = 16
        val RGB_CHANNEL_SHIFTS = listOf(16, 8, 0)

        const val DESCENT_HIGH_COLOR = 0x063D68
        const val DESCENT_MID_COLOR = 0x2096F3
        const val ASCENT_MID_COLOR = 0xF0B800
        const val ASCENT_HIGH_COLOR = 0xD50001
    }
}
