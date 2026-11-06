package hu.mostoha.mobile.kmp.huki.theme

import dev.icerock.moko.resources.StringResource
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.util.SlopeConstants.FLAT_SLOPE_PERCENT
import hu.mostoha.mobile.kmp.huki.util.SlopeConstants.HIGH_SLOPE_PERCENT
import hu.mostoha.mobile.kmp.huki.util.SlopeConstants.MID_SLOPE_PERCENT

object SlopeLegend {
    val minLabel: String = "-${HIGH_SLOPE_PERCENT.toInt()}%"
    val maxLabel: String = "+${HIGH_SLOPE_PERCENT.toInt()}%"

    val rows: List<SlopeLegendRow> = listOf(
        SlopeLegendRow(-HIGH_SLOPE_PERCENT, minLabel, SharedRes.strings.slope_legend_descent_steep),
        SlopeLegendRow(
            -MID_SLOPE_PERCENT,
            "-${MID_SLOPE_PERCENT.toInt()}%",
            SharedRes.strings.slope_legend_descent_moderate,
        ),
        SlopeLegendRow(0.0, "±${FLAT_SLOPE_PERCENT.toInt()}%", SharedRes.strings.slope_legend_flat),
        SlopeLegendRow(MID_SLOPE_PERCENT, "+${MID_SLOPE_PERCENT.toInt()}%", SharedRes.strings.slope_legend_climb_moderate),
        SlopeLegendRow(HIGH_SLOPE_PERCENT, maxLabel, SharedRes.strings.slope_legend_climb_steep),
    )
}

data class SlopeLegendRow(
    val slopePercent: Double,
    val label: String,
    val description: StringResource,
)
