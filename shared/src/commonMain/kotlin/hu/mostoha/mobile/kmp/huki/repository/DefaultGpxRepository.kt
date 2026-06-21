package hu.mostoha.mobile.kmp.huki.repository

import co.touchlab.kermit.Logger
import hu.mostoha.mobile.kmp.huki.model.domain.EmptyGpxContentException
import hu.mostoha.mobile.kmp.huki.model.domain.GpxDetails
import hu.mostoha.mobile.kmp.huki.model.domain.GpxFileItem
import hu.mostoha.mobile.kmp.huki.model.domain.GpxWaypoint
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.MalformedGpxException
import hu.mostoha.mobile.kmp.huki.model.domain.NonGpxFileException
import hu.mostoha.mobile.kmp.huki.model.domain.UnreadableGpxFileException
import hu.mostoha.mobile.kmp.huki.model.domain.WaypointType
import hu.mostoha.mobile.kmp.huki.model.domain.toGpxFileItem
import hu.mostoha.mobile.kmp.huki.util.calculateDecline
import hu.mostoha.mobile.kmp.huki.util.calculateIncline
import hu.mostoha.mobile.kmp.huki.util.calculateTotalDistance
import hu.mostoha.mobile.kmp.huki.util.calculateTravelTime
import hu.mostoha.mobile.kmp.huki.util.formatter.GpxFormatter
import hu.mostoha.mobile.kmp.huki.util.isCloseWithThreshold
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.lastModified
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.path
import io.github.vinceglb.filekit.readString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import org.maplibre.spatialk.gpx.Document
import org.maplibre.spatialk.gpx.Gpx
import org.maplibre.spatialk.gpx.Waypoint
import org.maplibre.spatialk.units.extensions.meters

open class DefaultGpxRepository(private val gpxStorage: GpxStorage) : GpxRepository {

    override suspend fun readGpxFile(uri: String): GpxDetails =
        withContext(Dispatchers.IO) {
            val file = gpxStorage.saveToFileSystem(uri)
            parseGpxDetails(file)
        }

    override suspend fun getGpxFiles(): List<GpxFileItem> =
        withContext(Dispatchers.IO) {
            gpxStorage.listGpxFiles()
                .mapNotNull { file ->
                    runCatching { parseGpxDetails(file).toGpxFileItem(file.lastModified()) }
                        .onFailure { Logger.e(it) { "GpxCollection: failed to parse ${file.name}" } }
                        .getOrNull()
                }
        }

    override suspend fun deleteGpxFile(fileName: String) {
        gpxStorage.delete(fileName)
    }

    private suspend fun parseGpxDetails(file: PlatformFile): GpxDetails {
        val xml = readXml(file)
        val gpx = decodeGpx(xml)

        if (gpx.tracks.isEmpty() && gpx.routes.isEmpty() && gpx.waypoints.isEmpty()) {
            throw EmptyGpxContentException()
        }

        return mapGpxDetails(file.name, file.path, gpx)
    }

    private suspend fun readXml(file: PlatformFile): String =
        try {
            val xml = file.readString()
            if (xml.isBlank()) {
                throw UnreadableGpxFileException()
            }
            return xml
        } catch (exception: Throwable) {
            throw UnreadableGpxFileException(exception)
        }

    private fun decodeGpx(xml: String): Document =
        try {
            if (!hasGpxXmlTag(xml)) {
                throw NonGpxFileException()
            }
            Gpx.decodeFromString(xml)
        } catch (exception: Throwable) {
            if (hasGpxXmlTag(xml)) {
                throw MalformedGpxException(exception)
            }
            throw NonGpxFileException(exception)
        }

    private fun hasGpxXmlTag(xml: String): Boolean = xml.trimStart().lowercase().contains("<gpx")

    private fun mapGpxDetails(fileName: String, filePath: String, gpx: Document): GpxDetails {
        val locations = mapLocations(gpx)
        val edgeLocations = mapEdgeLocations(locations)
        val waypoints = mapGpxWaypoints(gpx.waypoints)
        val minAltitude = locations.mapNotNull { it.altitude }.minOrNull() ?: 0.0
        val maxAltitude = locations.mapNotNull { it.altitude }.maxOrNull() ?: 0.0

        return GpxDetails(
            fileName = fileName,
            fileUri = filePath,
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
