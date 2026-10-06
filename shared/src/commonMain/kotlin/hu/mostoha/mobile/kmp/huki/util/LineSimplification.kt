package hu.mostoha.mobile.kmp.huki.util

import hu.mostoha.mobile.kmp.huki.model.domain.Location
import org.maplibre.spatialk.units.Length
import org.maplibre.spatialk.units.extensions.inMeters
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sqrt

private const val EARTH_RADIUS_METERS = 6_371_000.0

/**
 * Douglas–Peucker simplification: drops every location closer than [tolerance] to the simplified line.
 *
 * Iterative, so tracks with tens of thousands of points cannot overflow the stack.
 */
fun List<Location>.simplify(tolerance: Length): List<Location> {
    if (size < 3) return this

    val referenceLatitude = first().latitude * PI / 180
    val xs = DoubleArray(size) { this[it].longitude * PI / 180 * cos(referenceLatitude) * EARTH_RADIUS_METERS }
    val ys = DoubleArray(size) { this[it].latitude * PI / 180 * EARTH_RADIUS_METERS }
    val toleranceMeters = tolerance.inMeters

    val kept = BooleanArray(size)
    kept[0] = true
    kept[lastIndex] = true

    val stack = ArrayDeque<Pair<Int, Int>>()
    stack.addLast(0 to lastIndex)
    while (stack.isNotEmpty()) {
        val (start, end) = stack.removeLast()
        var farthestIndex = -1
        var farthestDistance = toleranceMeters
        for (index in start + 1 until end) {
            val distance = segmentDistance(xs[index], ys[index], xs[start], ys[start], xs[end], ys[end])
            if (distance > farthestDistance) {
                farthestDistance = distance
                farthestIndex = index
            }
        }
        if (farthestIndex != -1) {
            kept[farthestIndex] = true
            stack.addLast(start to farthestIndex)
            stack.addLast(farthestIndex to end)
        }
    }

    return filterIndexed { index, _ -> kept[index] }
}

private fun segmentDistance(
    px: Double,
    py: Double,
    ax: Double,
    ay: Double,
    bx: Double,
    by: Double,
): Double {
    val dx = bx - ax
    val dy = by - ay
    val lengthSquared = dx * dx + dy * dy
    val t = if (lengthSquared == 0.0) 0.0 else (((px - ax) * dx + (py - ay) * dy) / lengthSquared).coerceIn(0.0, 1.0)
    val cx = ax + t * dx - px
    val cy = ay + t * dy - py
    return sqrt(cx * cx + cy * cy)
}
