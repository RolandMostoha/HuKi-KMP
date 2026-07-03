package hu.mostoha.mobile.kmp.huki.model.data

import kotlinx.serialization.Serializable

/**
 * Persisted, cached snapshot of a GPX track so the "Recent GPX files" section needs no file I/O.
 *
 * @property trackId content-derived id, survives file renames.
 * @property lastOpened ISO-8601 offset date-time, e.g. `2026-06-21T17:24:44+02:00`.
 * @property lastModified ISO-8601 offset date-time of the sandbox file at record time.
 * @property distanceMeters total track distance in meters.
 * @property travelTimeSeconds estimated travel time in whole seconds.
 */
@Serializable
data class GpxMetadataEntry(
    val trackId: String,
    val lastOpened: String,
    val lastModified: String,
    val fileName: String,
    val title: String? = null,
    val distanceMeters: Double,
    val travelTimeSeconds: Long,
    val incline: Int,
    val decline: Int,
)
