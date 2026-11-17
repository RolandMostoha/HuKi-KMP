package hu.mostoha.mobile.kmp.huki.theme

import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * A notched arrowhead pointing along +x, encoded as a Mapbox SDF icon: the alpha channel holds the
 * distance to the arrow's edge, so `icon-halo-width` can draw an outline around it.
 */
class DirectionArrowSdf(
    val widthPx: Int,
    val heightPx: Int,
    /**
     * Row-major, one 0..255 alpha value per pixel.
     */
    val alpha: IntArray,
) {
    companion object {
        /**
         * Mapbox's SDF shader puts the edge at alpha 0.75 and steps 1/8 alpha per logical pixel.
         */
        private const val EDGE_ALPHA = 192.0
        private const val ALPHA_PER_DP = 32.0
        private const val MAX_ALPHA = 255.0

        fun create(density: Float): DirectionArrowSdf {
            val length = SharedDimens.GPX_DIRECTION_ARROW_LENGTH
            val width = SharedDimens.GPX_DIRECTION_ARROW_WIDTH
            val padding = SharedDimens.GPX_DIRECTION_ARROW_SDF_PADDING
            val notch = length * SharedDimens.GPX_DIRECTION_ARROW_NOTCH_RATIO
            val polygon = listOf(
                padding to padding,
                padding + length to padding + width / 2,
                padding to padding + width,
                padding + notch to padding + width / 2,
            )
            val widthPx = ceil((length + 2 * padding) * density).toInt()
            val heightPx = ceil((width + 2 * padding) * density).toInt()
            val alpha = IntArray(widthPx * heightPx) { index ->
                val x = (index % widthPx + 0.5) / density
                val y = (index / widthPx + 0.5) / density
                val distance = distanceToEdge(x, y, polygon)
                val signedDistance = if (isInside(x, y, polygon)) -distance else distance
                (EDGE_ALPHA - signedDistance * ALPHA_PER_DP).coerceIn(0.0, MAX_ALPHA).roundToInt()
            }
            return DirectionArrowSdf(widthPx, heightPx, alpha)
        }

        private fun distanceToEdge(x: Double, y: Double, polygon: List<Pair<Double, Double>>): Double =
            polygon.indices.minOf { i ->
                val (ax, ay) = polygon[i]
                val (bx, by) = polygon[(i + 1) % polygon.size]
                val dx = bx - ax
                val dy = by - ay
                val t = (((x - ax) * dx + (y - ay) * dy) / (dx * dx + dy * dy)).coerceIn(0.0, 1.0)
                val px = ax + t * dx - x
                val py = ay + t * dy - y
                sqrt(px * px + py * py)
            }

        private fun isInside(x: Double, y: Double, polygon: List<Pair<Double, Double>>): Boolean {
            var inside = false
            polygon.indices.forEach { i ->
                val (ax, ay) = polygon[i]
                val (bx, by) = polygon[(i + 1) % polygon.size]
                if ((ay > y) != (by > y) && x < (bx - ax) * (y - ay) / (by - ay) + ax) {
                    inside = !inside
                }
            }
            return inside
        }
    }
}
