package hu.mostoha.mobile.kmp.huki.repository

import co.touchlab.kermit.Logger
import dev.icerock.moko.resources.FileResource
import hu.mostoha.mobile.kmp.huki.model.data.GpxFileSource
import hu.mostoha.mobile.kmp.huki.model.data.GpxLandscapeEntry
import hu.mostoha.mobile.kmp.huki.model.data.GpxMetadataEntry
import hu.mostoha.mobile.kmp.huki.model.domain.EmptyGpxContentException
import hu.mostoha.mobile.kmp.huki.model.domain.GpxDetails
import hu.mostoha.mobile.kmp.huki.model.domain.GpxFileItem
import hu.mostoha.mobile.kmp.huki.model.domain.GpxOrigin
import hu.mostoha.mobile.kmp.huki.model.domain.GpxWaypoint
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.MalformedGpxException
import hu.mostoha.mobile.kmp.huki.model.domain.NonGpxFileException
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlan
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerProfile
import hu.mostoha.mobile.kmp.huki.model.domain.UnreadableGpxFileException
import hu.mostoha.mobile.kmp.huki.model.domain.WaypointType
import hu.mostoha.mobile.kmp.huki.model.mapper.randomFileNameToken
import hu.mostoha.mobile.kmp.huki.model.mapper.toGpxDocument
import hu.mostoha.mobile.kmp.huki.model.mapper.toGpxFileItem
import hu.mostoha.mobile.kmp.huki.model.mapper.toJelSymbol
import hu.mostoha.mobile.kmp.huki.model.mapper.toMetadataEntry
import hu.mostoha.mobile.kmp.huki.model.mapper.toRoutePlanFileName
import hu.mostoha.mobile.kmp.huki.model.mapper.toRoutePlanTitle
import hu.mostoha.mobile.kmp.huki.service.CrashlyticsService
import hu.mostoha.mobile.kmp.huki.util.RoutePlanConstants
import hu.mostoha.mobile.kmp.huki.util.calculateDecline
import hu.mostoha.mobile.kmp.huki.util.calculateIncline
import hu.mostoha.mobile.kmp.huki.util.calculateSlopeSegments
import hu.mostoha.mobile.kmp.huki.util.calculateTotalDistance
import hu.mostoha.mobile.kmp.huki.util.calculateTravelTime
import hu.mostoha.mobile.kmp.huki.util.formatter.GpxFormatter
import hu.mostoha.mobile.kmp.huki.util.isCloseWithThreshold
import hu.mostoha.mobile.kmp.huki.util.toGpxTrackId
import hu.mostoha.mobile.kmp.huki.util.toInstantFromIsoOffset
import hu.mostoha.mobile.kmp.huki.util.withNamedEdges
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

private const val GPX_EXTENSION = "gpx"

open class DefaultGpxRepository(
    private val gpxStorage: GpxStorage,
    private val metadataStore: GpxMetadataStore,
    private val crashlyticsService: CrashlyticsService,
    private val bundledFileReader: BundledFileReader,
    private val landscapeLocator: LandscapeLocator,
) : GpxRepository {

    override suspend fun readGpxFile(uri: String): GpxDetails =
        withContext(Dispatchers.IO) {
            val source = runCatching { gpxStorage.readGpx(uri) }.getOrElse { throw UnreadableGpxFileException(it) }
            val sandboxFile = gpxStorage.resolveSandboxFile(uri)
            openGpx(source) { sandboxFile ?: gpxStorage.saveToSandbox(source, GpxOrigin.EXTERNAL) }
        }

    override suspend fun readBundledGpxFile(file: FileResource): GpxDetails =
        withContext(Dispatchers.IO) {
            val source = GpxFileSource(
                fileName = "${bundledFileReader.baseName(file)}.$GPX_EXTENSION",
                content = bundledFileReader.readText(file).encodeToByteArray(),
            )
            openGpx(source) { replaceBundledGpx(source) }
        }

    override suspend fun peekGpxFile(uri: String): GpxDetails =
        withContext(Dispatchers.IO) {
            val source = runCatching { gpxStorage.readGpx(uri) }.getOrElse { throw UnreadableGpxFileException(it) }
            parseGpxDetails(source.fileName, uri, source.content.decodeToString())
        }

    override suspend fun getGpxFiles(): List<GpxFileItem> =
        withContext(Dispatchers.IO) {
            val metadata = metadataStore.getMetadata()
            val entriesByTrackId = metadata.gpxFiles.associateBy { it.trackId }
            val landscapeScan = LandscapeScan(cached = metadata.landscapes.associateBy { it.trackId })
            val items = gpxStorage.listGpxFiles().mapNotNull { file ->
                scanGpxFile(file, entriesByTrackId, landscapeScan)
            }

            metadataStore.recordLandscapes(landscapeScan.newEntries)
            metadataStore.remove(items.map { it.trackId }.toSet())

            items
        }

    override suspend fun getRecentGpxFiles(limit: Int): List<GpxFileItem> =
        withContext(Dispatchers.IO) {
            metadataStore.getMetadata().gpxFiles
                .mapNotNull { entry ->
                    val file = gpxStorage.resolveGpxFile(entry.fileName)
                    if (file == null) {
                        metadataStore.removeByTrackId(entry.trackId)
                        return@mapNotNull null
                    }
                    entry.toGpxFileItem(fileUri = file.path)
                }
                .sortedByDescending { it.lastOpened }
                .take(limit)
        }

    override suspend fun saveRoutePlan(
        routePlan: RoutePlan,
        stopPlaceNames: List<String?>,
        routeProfile: RoutePlannerProfile,
    ): String =
        withContext(Dispatchers.IO) {
            val createdAt = Clock.System.now()
            val document = routePlan.toGpxDocument(
                stopPlaceNames.toRoutePlanTitle(),
                stopPlaceNames,
                routeProfile,
                createdAt,
            )
            val source = GpxFileSource(
                fileName = stopPlaceNames.toRoutePlanFileName(randomFileNameToken()),
                content = Gpx.encodeToString(document).encodeToByteArray(),
            )

            gpxStorage.saveToSandbox(source, GpxOrigin.ROUTE_PLANNER).path
        }

    override suspend fun deleteGpxFile(fileUri: String, trackId: String) {
        gpxStorage.delete(fileUri)
        metadataStore.removeByTrackId(trackId)
    }

    private suspend fun openGpx(source: GpxFileSource, saveToSandbox: suspend () -> PlatformFile): GpxDetails {
        val gpx = validateGpx(source.content.decodeToString())
        val file = saveToSandbox()
        val details = mapGpxDetails(file.name, file.path, gpx)

        metadataStore.recordOpened(
            details.toMetadataEntry(
                trackId = source.content.toGpxTrackId(),
                lastModified = file.lastModified(),
                openedAt = Clock.System.now(),
            ),
        )

        return details
    }

    /**
     * Keeps a single, up-to-date copy of a bundled GPX: an app update that ships a changed file replaces the
     * previous copy and its metadata, keyed on the old content.
     */
    private suspend fun replaceBundledGpx(source: GpxFileSource): PlatformFile {
        val replacement = gpxStorage.replaceInSandbox(source, GpxOrigin.BUNDLED)
        replacement.replacedContent?.let { metadataStore.removeByTrackId(it.toGpxTrackId()) }

        return replacement.file
    }

    /**
     * Parses a sandbox GPX, deleting it when it's unreadable or corrupt. A landscape missing from
     * [landscapeScan] is looked up and recorded in it.
     */
    private suspend fun scanGpxFile(
        file: PlatformFile,
        entriesByTrackId: Map<String, GpxMetadataEntry>,
        landscapeScan: LandscapeScan,
    ): GpxFileItem? {
        val bytes = runCatching { file.readBytes() }.getOrElse { throwable ->
            Logger.e(throwable) { "Gpx: failed to read ${file.name}, deleting" }
            crashlyticsService.recordException(throwable)
            gpxStorage.delete(file.path)
            return null
        }
        val trackId = bytes.toGpxTrackId()
        val details = runCatching {
            parseGpxDetails(file.name, file.path, bytes.decodeToString())
        }.onFailure { throwable ->
            Logger.e(throwable) { "Gpx: failed to parse ${file.name}, deleting" }
            crashlyticsService.recordException(throwable)
            gpxStorage.delete(file.path)
        }.getOrNull() ?: return null

        return details.toGpxFileItem(
            trackId = trackId,
            lastModified = file.lastModified(),
            lastOpened = entriesByTrackId[trackId]?.lastOpened?.toInstantFromIsoOffset(),
            landscapeId = when (val cached = landscapeScan.cached[trackId]) {
                null -> lookUpLandscape(trackId, details, landscapeScan)
                else -> cached.landscapeId
            },
        )
    }

    private suspend fun lookUpLandscape(trackId: String, details: GpxDetails, landscapeScan: LandscapeScan): String? {
        if (landscapeScan.isFailed) return null
        val landscapeId = findLandscapeId(details).getOrElse {
            landscapeScan.isFailed = true
            return null
        }
        landscapeScan.newEntries += GpxLandscapeEntry(trackId = trackId, landscapeId = landscapeId)
        return landscapeId
    }

    /**
     * Landscape lookups of one collection scan. A failed lookup, most likely the outlines failing to load, skips
     * the rest of the scan instead of retrying and reporting it for every file.
     */
    private class LandscapeScan(val cached: Map<String, GpxLandscapeEntry>) {
        val newEntries = mutableListOf<GpxLandscapeEntry>()
        var isFailed = false
    }

    /**
     * A failed lookup only costs the landscape tag, so it never deletes the file like a parse failure does, and
     * it isn't cached, so the next scan retries it.
     */
    private suspend fun findLandscapeId(details: GpxDetails): Result<String?> =
        runCatching {
            landscapeLocator.findLandscapeId(details.locations.ifEmpty { details.waypoints.map { it.location } })
        }.onFailure { throwable ->
            Logger.e(throwable) { "Gpx: failed to locate the landscape of ${details.fileName}" }
            crashlyticsService.recordException(throwable)
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
            waypoints = waypoints.withNamedEdges(edgeLocations, locations),
            bounds = locations + waypoints.map { it.location },
            totalDistance = locations.calculateTotalDistance(),
            travelTime = locations.calculateTravelTime(),
            altitudeRange = minAltitude.toInt() to maxAltitude.toInt(),
            incline = locations.calculateIncline(),
            decline = locations.calculateDecline(),
            jelSymbol = gpx.toJelSymbol(),
            slopeSegments = locations.calculateSlopeSegments(),
            isHukiAuthored = gpx.metadata?.author?.name?.trim() == RoutePlanConstants.GPX_AUTHOR_NAME,
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
