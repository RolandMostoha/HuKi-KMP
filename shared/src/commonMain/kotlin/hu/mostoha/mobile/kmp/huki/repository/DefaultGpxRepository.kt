package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.model.domain.GpxDetails
import hu.mostoha.mobile.kmp.huki.model.domain.GpxWaypoint
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.WaypointType
import hu.mostoha.mobile.kmp.huki.util.calculateDecline
import hu.mostoha.mobile.kmp.huki.util.calculateIncline
import hu.mostoha.mobile.kmp.huki.util.calculateTotalDistance
import hu.mostoha.mobile.kmp.huki.util.calculateTravelTime
import hu.mostoha.mobile.kmp.huki.util.formatter.GpxFormatter
import hu.mostoha.mobile.kmp.huki.util.isCloseWithThreshold
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import org.maplibre.spatialk.gpx.Document
import org.maplibre.spatialk.gpx.Gpx
import org.maplibre.spatialk.gpx.Waypoint
import org.maplibre.spatialk.units.extensions.meters

open class DefaultGpxRepository : GpxRepository {

    override suspend fun readGpxFile(uri: String): GpxDetails {
        return withContext(Dispatchers.IO) {
            val file = PlatformFile(uri)
            val xml = file.readString()
            val gpx = Gpx.decodeFromString(xml)
            val gpxDetails = mapGpxDetails(file.name, uri, gpx)

            return@withContext gpxDetails
        }
    }

    private fun mapGpxDetails(fileName: String, uri: String, gpx: Document): GpxDetails {
        val locations = mapLocations(gpx)
        val edgeLocations = mapEdgeLocations(locations)
        val waypoints = mapGpxWaypoints(gpx.waypoints)
        val minAltitude = locations.mapNotNull { it.altitude }.minOrNull() ?: 0.0
        val maxAltitude = locations.mapNotNull { it.altitude }.maxOrNull() ?: 0.0

        return GpxDetails(
            fileName = fileName,
            fileUri = uri,
            title = GpxFormatter.formatTitle(gpx),
            locations = locations,
            waypoints = waypoints + edgeLocations,
            bounds = locations + waypoints.map { it.location },
            totalDistance = locations.calculateTotalDistance(),
            travelTime = locations.calculateTravelTime(),
            altitudeRange = minAltitude.toInt() to maxAltitude.toInt(),
            incline = locations.calculateIncline(),
            decline = locations.calculateDecline(),
        )
    }

    private fun mapLocations(gpx: Document): List<Location> =
        when {
            gpx.tracks.isNotEmpty() ->
                gpx.tracks
                    .flatMap { it.segments }
                    .flatMap { it.points }
                    .map { trackPoint ->
                        Location(trackPoint.latitude, trackPoint.longitude, trackPoint.elevation)
                    }
            gpx.routes.isNotEmpty() ->
                gpx.routes
                    .flatMap { it.points }
                    .map { routePoint ->
                        Location(routePoint.latitude, routePoint.longitude, routePoint.elevation)
                    }
            else -> emptyList()
        }

    private fun mapEdgeLocations(locations: List<Location>): List<GpxWaypoint> =
        if (locations.size >= 2) {
            val startLocation = locations.first()
            val endLocation = locations.last()
            if (startLocation.isCloseWithThreshold(endLocation, 20.meters)) {
                listOf(GpxWaypoint(startLocation, WaypointType.ROUND_TRIP))
            } else {
                listOf(
                    GpxWaypoint(startLocation, WaypointType.START),
                    GpxWaypoint(endLocation, WaypointType.END),
                )
            }
        } else {
            emptyList()
        }

    private fun mapGpxWaypoints(waypoints: List<Waypoint>): List<GpxWaypoint> =
        waypoints.map { wayPoint ->
            val description = listOfNotNull(
                wayPoint.description,
                wayPoint.comment,
            )
                .joinToString("\n")
                .ifEmpty { null }

            GpxWaypoint(
                location = Location(wayPoint.latitude, wayPoint.longitude, wayPoint.elevation),
                type = WaypointType.INTERMEDIATE,
                name = wayPoint.name,
                description = description,
            )
        }
}
