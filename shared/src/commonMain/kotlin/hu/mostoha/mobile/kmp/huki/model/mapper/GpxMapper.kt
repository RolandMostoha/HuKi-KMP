package hu.mostoha.mobile.kmp.huki.model.mapper

import hu.mostoha.mobile.kmp.huki.model.data.GpxMetadataEntry
import hu.mostoha.mobile.kmp.huki.model.domain.GpxDetails
import hu.mostoha.mobile.kmp.huki.model.domain.GpxFileItem
import hu.mostoha.mobile.kmp.huki.util.toInstantFromIsoOffset
import hu.mostoha.mobile.kmp.huki.util.toIsoOffsetString
import org.maplibre.spatialk.units.extensions.inMeters
import org.maplibre.spatialk.units.extensions.meters
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

fun GpxDetails.toGpxFileItem(trackId: String, lastModified: Instant, lastOpened: Instant?): GpxFileItem =
    GpxFileItem(
        fileName = fileName,
        fileUri = fileUri,
        trackId = trackId,
        title = title,
        totalDistance = totalDistance,
        travelTime = travelTime,
        incline = incline,
        decline = decline,
        lastModified = lastModified,
        lastOpened = lastOpened,
    )

fun GpxDetails.toMetadataEntry(trackId: String, lastModified: Instant, openedAt: Instant): GpxMetadataEntry =
    GpxMetadataEntry(
        trackId = trackId,
        lastOpened = openedAt.toIsoOffsetString(),
        lastModified = lastModified.toIsoOffsetString(),
        fileName = fileName,
        fileUri = fileUri,
        title = title,
        distanceMeters = totalDistance.inMeters,
        travelTimeSeconds = travelTime.inWholeSeconds,
        incline = incline,
        decline = decline,
    )

fun GpxMetadataEntry.toGpxFileItem(): GpxFileItem? {
    val openedAt = lastOpened.toInstantFromIsoOffset() ?: return null
    return GpxFileItem(
        fileName = fileName,
        fileUri = fileUri,
        trackId = trackId,
        title = title,
        totalDistance = distanceMeters.meters,
        travelTime = travelTimeSeconds.seconds,
        incline = incline,
        decline = decline,
        lastModified = lastModified.toInstantFromIsoOffset() ?: openedAt,
        lastOpened = openedAt,
    )
}
