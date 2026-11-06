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
        val hex = colorRgb(slopePercent).toString(radix = HEX_RADIX).padStart(length = HEX_LENGTH, padChar = '0')
        return "#${hex.uppercase()}"
    }

    /**
     * Returns the `0xRRGGBB` color of [colorHex].
     */
    fun colorRgb(slopePercent: Double): Int {
        val slope = slopePercent.coerceIn(knots.first().first, knots.last().first)
        val upperIndex = knots.indexOfFirst { (knotSlope, _) -> slope <= knotSlope }.coerceAtLeast(1)
        val (lowerSlope, lowerColor) = knots[upperIndex - 1]
        val (upperSlope, upperColor) = knots[upperIndex]
        val fraction = (slope - lowerSlope) / (upperSlope - lowerSlope)

        return RGB_CHANNEL_SHIFTS.fold(0) { rgb, shift ->
            val lower = lowerColor shr shift and CHANNEL_MASK
            val upper = upperColor shr shift and CHANNEL_MASK
            val channel = (lower + (upper - lower) * fraction).roundToInt()
            rgb or (channel shl shift)
        }
    }

    /**
     * The knots placed on a `0..1` axis from -[HIGH_SLOPE_PERCENT] to +[HIGH_SLOPE_PERCENT], so a linear gradient
     * through them matches [colorRgb].
     */
    fun gradientStops(): List<SlopeGradientStop> {
        val range = knots.last().first - knots.first().first
        return knots.map { (slope, color) ->
            SlopeGradientStop(fraction = ((slope - knots.first().first) / range).toFloat(), rgb = color)
        }
    }

    private companion object {
        const val RGB_MASK = 0xFFFFFF
        const val CHANNEL_MASK = 0xFF
        const val HEX_RADIX = 16
        const val HEX_LENGTH = 6
        val RGB_CHANNEL_SHIFTS = listOf(16, 8, 0)

        const val DESCENT_HIGH_COLOR = 0x063D68
        const val DESCENT_MID_COLOR = 0x2096F3
        const val ASCENT_MID_COLOR = 0xF0B800
        const val ASCENT_HIGH_COLOR = 0xD50001
    }
}

data class SlopeGradientStop(
    val fraction: Float,
    val rgb: Int,
)
