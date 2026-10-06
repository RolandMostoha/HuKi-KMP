package hu.mostoha.mobile.kmp.huki.model.mapper

import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.OktLine
import hu.mostoha.mobile.kmp.huki.model.domain.OktMarker
import hu.mostoha.mobile.kmp.huki.model.domain.OktMarkerType
import hu.mostoha.mobile.kmp.huki.model.domain.OktSectionGeometry
import hu.mostoha.mobile.kmp.huki.model.domain.OktSectionItem
import hu.mostoha.mobile.kmp.huki.model.domain.OktStamp
import hu.mostoha.mobile.kmp.huki.model.domain.OktStampItem
import hu.mostoha.mobile.kmp.huki.model.domain.OktStampTag
import hu.mostoha.mobile.kmp.huki.model.domain.OktType
import hu.mostoha.mobile.kmp.huki.model.domain.RouteStats
import hu.mostoha.mobile.kmp.huki.util.TrackPosition
import hu.mostoha.mobile.kmp.huki.util.TrackProgressIndex
import hu.mostoha.mobile.kmp.huki.util.formatter.DistanceFormatter
import hu.mostoha.mobile.kmp.huki.util.legDistancesTo
import org.maplibre.spatialk.geojson.GeoJsonObject
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.gpx.Waypoint

private val STAMP_TAG_REGEX = """[A-Z]+PH(?:_[A-Z0-9]+)+""".toRegex()
private const val SECTION_NAME_SEPARATOR = " - "
private const val SECTION_ID_SEPARATOR = "-"

/**
 * Parses the stamp code at the end of a stamping point description, e.g. `(OKTPH_01_2)` is `1.2`.
 *
 * A code shared between trails, e.g. `OKTPH_01_DDKPH_01_2`, is kept whole and numbered by [stampPrefix].
 */
fun String.toOktStampTag(stampPrefix: String): OktStampTag? {
    val match = """${stampPrefix}_(\d+)(?:_(\d+))?""".toRegex().find(this) ?: return null
    val tag = STAMP_TAG_REGEX.findAll(this).map { it.value }.lastOrNull { match.value in it } ?: match.value
    val (major, minor) = match.destructured
    val number = if (minor.isEmpty()) major.toDouble() else "$major.$minor".toDouble()

    return OktStampTag(tag = tag, number = number)
}

fun Waypoint.toOktStamp(type: OktType): OktStamp? {
    val description = description ?: return null
    val stampTag = description.toOktStampTag(type.stampPrefix) ?: return null

    return OktStamp(
        title = name ?: stampTag.tag,
        description = description,
        location = Location(latitude, longitude, elevation),
        tag = stampTag.tag,
        number = stampTag.number,
    )
}

fun OktSectionGeometry.toOktSectionItem(type: OktType): OktSectionItem {
    val isFullTrail = section.id == type.fullTrailId

    return OktSectionItem(
        id = section.id,
        badgePrefix = section.id.substringBefore(SECTION_ID_SEPARATOR),
        badgeNumber = section.id.substringAfter(SECTION_ID_SEPARATOR, missingDelimiterValue = "").ifEmpty { null },
        name = section.name,
        routeStats = RouteStats(
            travelTime = section.travelTime,
            distance = section.distance,
            incline = section.incline,
            decline = section.decline,
        ),
        stamps = stamps.toOktStampItems(locations),
        reversedStamps = stamps.asReversed().toOktStampItems(locations.asReversed()),
        websiteUrl = if (isFullTrail) {
            type.websiteUrl
        } else {
            type.sectionUrlTemplate.replace("%s", section.id.lowercase())
        },
    )
}

private fun List<OktStamp>.toOktStampItems(sectionLocations: List<Location>): List<OktStampItem> {
    val legDistances = sectionLocations.legDistancesTo(map { it.location })
    return zip(legDistances) { stamp, legDistance ->
        OktStampItem(
            marker = stamp.toOktMarker(),
            tag = stamp.tag,
            legDistance = DistanceFormatter.formatRelative(legDistance),
        )
    }
}

fun OktSectionGeometry.toOktMarkers(): List<OktMarker> =
    listOf(
        OktMarker(
            id = "${section.id}_start",
            location = section.start,
            type = OktMarkerType.START,
            name = section.name.substringBefore(SECTION_NAME_SEPARATOR),
        ),
    ) + stamps.map { it.toOktMarker() } + OktMarker(
        id = "${section.id}_end",
        location = section.end,
        type = OktMarkerType.END,
        name = section.name.substringAfterLast(SECTION_NAME_SEPARATOR),
    )

fun List<OktSectionGeometry>.toMarkerPositions(progressIndex: TrackProgressIndex): Map<String, TrackPosition> =
    flatMap { it.toOktMarkers() }.associate { it.id to progressIndex.positionOf(it.location) }

fun OktStamp.toOktMarker(): OktMarker =
    OktMarker(
        id = tag,
        location = location,
        type = OktMarkerType.STAMP,
        name = title,
        description = description,
    )

fun OktMarker.toInfoWindowTitle(): StringDesc =
    when (type) {
        OktMarkerType.STAMP -> StringDesc.ResourceFormatted(SharedRes.strings.okt_stamp_info_window_title, name)
        OktMarkerType.START -> StringDesc.ResourceFormatted(SharedRes.strings.okt_start_info_window_title, name)
        OktMarkerType.END -> StringDesc.ResourceFormatted(SharedRes.strings.okt_end_info_window_title, name)
    }

fun List<Location>.toOktLine(id: String): OktLine =
    OktLine(
        id = id,
        geoJson = GeoJsonObject.toJson(LineString(map { Position(it.longitude, it.latitude) })),
        bounds = toBounds(),
    )

private fun List<Location>.toBounds(): List<Location> =
    listOf(
        Location(latitude = minOf { it.latitude }, longitude = minOf { it.longitude }),
        Location(latitude = maxOf { it.latitude }, longitude = maxOf { it.longitude }),
    )
