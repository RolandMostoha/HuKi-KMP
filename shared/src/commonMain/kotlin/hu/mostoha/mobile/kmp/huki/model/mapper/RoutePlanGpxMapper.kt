package hu.mostoha.mobile.kmp.huki.model.mapper

import hu.mostoha.mobile.kmp.huki.WhatsNewContent
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlan
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerProfile
import org.maplibre.spatialk.gpx.Author
import org.maplibre.spatialk.gpx.Document
import org.maplibre.spatialk.gpx.Link
import org.maplibre.spatialk.gpx.Metadata
import org.maplibre.spatialk.gpx.Track
import org.maplibre.spatialk.gpx.TrackSegment
import org.maplibre.spatialk.gpx.Waypoint
import kotlin.time.Instant

private const val APP_NAME = "HuKi"
private const val APP_URL = "https://huki.hu"
private const val UNTITLED_ROUTE_PLAN = "Route plan"

private const val MAX_STOP_NAME_LENGTH = 25
private const val WORD_BOUNDARY_RATIO = 0.6
private const val ELLIPSIS = '…'
private const val TITLE_SEPARATOR = " → "
private const val FILE_NAME_FLAG = "HuKi"
private const val FILE_NAME_SEPARATOR = "_"
private const val GPX_EXTENSION = "gpx"
private val FILE_NAME_TOKEN_RANGE = 100..999

private val ILLEGAL_FILE_NAME_CHARS = charArrayOf('/', '\\', ':', '*', '?', '"', '<', '>', '|', ELLIPSIS)

/**
 * The plan's geometry becomes a single track, so re-reading it takes the same path as any external GPX.
 * Only the intermediate stops become waypoints — start and end markers are derived from the track's edges.
 */
fun RoutePlan.toGpxDocument(
    title: String,
    stopPlaceNames: List<String?>,
    routeProfile: RoutePlannerProfile,
    createdAt: Instant,
): Document =
    Document(
        creator = "$APP_NAME ${WhatsNewContent.currentVersion}",
        metadata = Metadata(
            name = title,
            author = Author(name = APP_NAME, link = Link(href = APP_URL, text = APP_NAME)),
            timestamp = createdAt,
        ),
        tracks = listOf(
            Track(
                name = title,
                type = routeProfile.toGpxActivityType(),
                segments = listOf(TrackSegment(points = locations.map { it.toGpxWaypoint() })),
            ),
        ),
        waypoints = toIntermediateGpxWaypoints(stopPlaceNames),
    )

/**
 * Names the plan by its ends. An end without a geocoded place is left out rather than guessed at, so a route
 * from an unresolved start reads as the destination alone.
 */
fun List<String?>.toRoutePlanTitle(): String {
    val start = firstOrNull().toStopNameOrNull()
    val end = if (size > 1) lastOrNull().toStopNameOrNull() else null

    return listOfNotNull(start, end).joinToString(TITLE_SEPARATOR).ifEmpty { UNTITLED_ROUTE_PLAN }
}

fun randomFileNameToken(): Int = FILE_NAME_TOKEN_RANGE.random()

/**
 * Names the file after the destination alone — for a round trip that is the stop it started from. An unresolved
 * destination leaves only the flag, e.g. `HuKi_482.gpx`.
 *
 * @param token disambiguates plans sharing a destination; the content-derived trackId remains the real identity.
 */
fun List<String?>.toRoutePlanFileName(token: Int): String {
    val flag = "$FILE_NAME_FLAG$FILE_NAME_SEPARATOR$token.$GPX_EXTENSION"
    val destination = lastOrNull()
        .toStopNameOrNull()
        ?.toSanitizedFileName()
        ?.ifEmpty { null }
        ?: return flag

    return "$destination$FILE_NAME_SEPARATOR$flag"
}

private fun RoutePlan.toIntermediateGpxWaypoints(stopPlaceNames: List<String?>): List<Waypoint> =
    waypoints.mapIndexedNotNull { index, location ->
        if (index == 0 || index == waypoints.lastIndex) {
            null
        } else {
            location.toGpxWaypoint().copy(name = stopPlaceNames.getOrNull(index)?.trim()?.ifEmpty { null })
        }
    }

private fun String?.toStopNameOrNull(): String? = this?.trim()?.ifEmpty { null }?.toTruncatedStopName()

private fun Location.toGpxWaypoint(): Waypoint =
    Waypoint(latitude = latitude, longitude = longitude, elevation = altitude)

private fun RoutePlannerProfile.toGpxActivityType(): String =
    when (this) {
        RoutePlannerProfile.ON_TRAILS, RoutePlannerProfile.SHORTEST_ROUTE -> "hiking"
        RoutePlannerProfile.BIKE -> "cycling"
    }

private fun String.toSanitizedFileName(): String =
    filterNot { it in ILLEGAL_FILE_NAME_CHARS || it.isISOControl() }.trim()

/**
 * Reverse-geocoded stop names run long, so they are cut back to a label that still fits a file name,
 * preferring a word boundary once past [WORD_BOUNDARY_RATIO] of the budget.
 */
private fun String.toTruncatedStopName(): String {
    val name = trim()
    if (name.length <= MAX_STOP_NAME_LENGTH) {
        return name
    }
    val cut = name.take(MAX_STOP_NAME_LENGTH)
    val boundary = cut.lastIndexOf(' ')
    val kept = if (boundary >= MAX_STOP_NAME_LENGTH * WORD_BOUNDARY_RATIO) cut.take(boundary) else cut

    return kept.trimEnd() + ELLIPSIS
}
