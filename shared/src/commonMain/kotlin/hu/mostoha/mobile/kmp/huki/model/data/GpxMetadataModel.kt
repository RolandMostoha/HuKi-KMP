package hu.mostoha.mobile.kmp.huki.model.data

import kotlinx.serialization.Serializable

/**
 * Root document persisted to `gpx/metadata.json`, holding the [GpxMetadataEntry] attributes of the opened GPX
 * files and the [GpxLandscapeEntry] lookups of every GPX file.
 */
@Serializable
data class GpxMetadataModel(
    val gpxFiles: List<GpxMetadataEntry> = emptyList(),
    val landscapes: List<GpxLandscapeEntry> = emptyList(),
)
