package hu.mostoha.mobile.kmp.huki.model.data

import kotlinx.serialization.Serializable

/**
 * Root document persisted to `gpx/metadata.json`, holding the [GpxMetadataEntry] attributes of all GPX files.
 */
@Serializable
data class GpxMetadataModel(val gpxFiles: List<GpxMetadataEntry> = emptyList())
