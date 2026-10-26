package hu.mostoha.mobile.kmp.huki.model.domain

import kotlin.math.roundToInt

data class MapLabelScale(
    val followSystem: Boolean,
    val factor: Float,
) {
    val percent: Int
        get() = toPercent(factor)

    fun effectiveFactor(systemScale: Float): Float = if (followSystem) coerceFactor(systemScale) else factor

    fun effectivePercent(systemScale: Float): Int = toPercent(effectiveFactor(systemScale))

    companion object {
        const val MIN_FACTOR = 1.0f
        const val MAX_FACTOR = 2.0f
        const val FACTOR_STEPS = 10
        private const val PERCENT = 100

        val DEFAULT = MapLabelScale(followSystem = true, factor = MIN_FACTOR)

        /**
         * Clamps a scale into the supported range, rounded to the slider steps.
         */
        fun coerceFactor(scale: Float): Float =
            (scale.coerceIn(MIN_FACTOR, MAX_FACTOR) * FACTOR_STEPS).roundToInt() / FACTOR_STEPS.toFloat()

        fun toPercent(factor: Float): Int = (factor * PERCENT).roundToInt()
    }
}
