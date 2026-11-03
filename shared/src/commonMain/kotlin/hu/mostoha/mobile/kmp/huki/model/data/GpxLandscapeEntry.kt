package hu.mostoha.mobile.kmp.huki.model.data

import kotlinx.serialization.Serializable

/**
 * Cached landscape lookup of a GPX track, so the point-in-polygon search runs once per file.
 *
 * @property trackId content-derived id, survives file renames.
 * @property landscapeId the landscape the track runs in, `null` when it's outside of every landscape.
 */
@Serializable
data class GpxLandscapeEntry(
    val trackId: String,
    val landscapeId: String? = null,
)
