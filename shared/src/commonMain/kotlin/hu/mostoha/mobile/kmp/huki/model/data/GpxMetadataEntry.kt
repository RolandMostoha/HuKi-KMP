package hu.mostoha.mobile.kmp.huki.model.data

import kotlinx.serialization.Serializable

/**
 * Persisted entry for a single GPX track.
 *
 * @property trackId content-derived id, survives file renames.
 * @property lastOpened ISO-8601 offset date-time, e.g. `2026-06-21T17:24:44+02:00`.
 */
@Serializable
data class GpxMetadataEntry(
    val trackId: String,
    val lastOpened: String,
)
