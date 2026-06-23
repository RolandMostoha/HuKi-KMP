package hu.mostoha.mobile.kmp.huki.repository

import co.touchlab.kermit.Logger
import hu.mostoha.mobile.kmp.huki.model.data.GpxMetadataEntry
import hu.mostoha.mobile.kmp.huki.model.domain.EmptyGpxContentException
import hu.mostoha.mobile.kmp.huki.model.domain.GpxDetails
import hu.mostoha.mobile.kmp.huki.model.domain.GpxFileItem
import hu.mostoha.mobile.kmp.huki.model.domain.GpxWaypoint
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.MalformedGpxException
import hu.mostoha.mobile.kmp.huki.model.domain.NonGpxFileException
import hu.mostoha.mobile.kmp.huki.model.domain.UnreadableGpxFileException
import hu.mostoha.mobile.kmp.huki.model.domain.WaypointType
import hu.mostoha.mobile.kmp.huki.model.mapper.toGpxFileItem
import hu.mostoha.mobile.kmp.huki.model.mapper.toMetadataEntry
import hu.mostoha.mobile.kmp.huki.util.calculateDecline
import hu.mostoha.mobile.kmp.huki.util.calculateIncline
import hu.mostoha.mobile.kmp.huki.util.calculateTotalDistance
import hu.mostoha.mobile.kmp.huki.util.calculateTravelTime
import hu.mostoha.mobile.kmp.huki.util.formatter.GpxFormatter
import hu.mostoha.mobile.kmp.huki.util.isCloseWithThreshold
import hu.mostoha.mobile.kmp.huki.util.toGpxTrackId
import hu.mostoha.mobile.kmp.huki.util.toInstantFromIsoOffset
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.lastModified
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.path
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import org.maplibre.spatialk.gpx.Document
import org.maplibre.spatialk.gpx.Gpx
import org.maplibre.spatialk.gpx.Waypoint
import org.maplibre.spatialk.units.extensions.meters
import kotlin.time.Clock

open class DefaultGpxRepository(
    private val gpxStorage: GpxStorage,
    private val metadataStore: GpxMetadataStore,
) : GpxRepository {

    override suspend fun readGpxFile(uri: String): GpxDetails =
        withContext(Dispatchers.IO) {
            val source = runCatching { gpxStorage.readGpx(uri) }.getOrElse { throw UnreadableGpxFileException(it) }
            val gpx = validateGpx(source.content.decodeToString())
            val file = gpxStorage.saveToSandbox(source)
            val details = mapGpxDetails(file.name, file.path, gpx)

            metadataStore.recordOpened(
                details.toMetadataEntry(
                    trackId = source.content.toGpxTrackId(),
                    lastModified = file.lastModified(),
                    openedAt = Clock.System.now(),
                ),
            )

            details
        }

    override suspend fun getGpxFiles(): List<GpxFileItem> =
        withContext(Dispatchers.IO) {
            val entriesByTrackId = metadataStore.getMetadata().gpxFiles.associateBy { it.trackId }
            val items = gpxStorage.listGpxFiles().mapNotNull { file -> scanGpxFile(file, entriesByTrackId) }

            metadataStore.remove(items.map { it.trackId }.toSet())

            items
        }

    override suspend fun getRecentGpxFiles(limit: Int): List<GpxFileItem> =
        metadataStore.getMetadata().gpxFiles
            .mapNotNull { it.toGpxFileItem() }
            .sortedByDescending { it.lastOpened }
            .take(limit)

    override suspend fun deleteGpxFile(fileName: String) {
        gpxStorage.delete(fileName)
        metadataStore.remove(fileName)
    }

    /**
     * Parses a sandbox GPX, deleting it when it's unreadable or corrupt.
     */
    private suspend fun scanGpxFile(file: PlatformFile, entriesByTrackId: Map<String, GpxMetadataEntry>): GpxFileItem? {
        val bytes = runCatching { file.readBytes() }.getOrNull()
        if (bytes == null) {
            Logger.e { "Gpx: failed to read ${file.name}, deleting" }
            gpxStorage.delete(file.name)
            return null
        }
        val trackId = bytes.toGpxTrackId()
        return runCatching {
            parseGpxDetails(file.name, file.path, bytes.decodeToString()).toGpxFileItem(
                trackId = trackId,
                lastModified = file.lastModified(),
                lastOpened = entriesByTrackId[trackId]?.lastOpened?.toInstantFromIsoOffset(),
            )
        }.onFailure {
            Logger.e(it) { "Gpx: failed to parse ${file.name}, deleting" }
            gpxStorage.delete(file.name)
        }.getOrNull()
    }

    private fun parseGpxDetails(fileName: String, filePath: String, xml: String): GpxDetails =
        mapGpxDetails(fileName, filePath, validateGpx(xml))

    private fun validateGpx(xml: String): Document {
        if (xml.isBlank()) {
            throw UnreadableGpxFileException()
        }
        val gpx = decodeGpx(xml)
        if (gpx.tracks.isEmpty() && gpx.routes.isEmpty() && gpx.waypoints.isEmpty()) {
            throw EmptyGpxContentException()
        }
        return gpx
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
