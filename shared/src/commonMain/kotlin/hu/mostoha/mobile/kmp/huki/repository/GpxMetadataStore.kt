package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.model.data.GpxMetadataModel
import kotlin.time.Instant

/**
 * Persists lightweight attributes for imported GPX files, keyed by a content-derived
 * trackId so they survive file renames.
 */
interface GpxMetadataStore {

    /**
     * Records the moment a track was opened (first import or re-open). Overwrites any earlier value.
     */
    suspend fun recordOpened(trackId: String, openedAt: Instant)

    /**
     * @return the full [GpxMetadataModel] so callers can pick the attributes they need.
     */
    suspend fun getMetadata(): GpxMetadataModel

    /**
     * Drops attributes whose trackId is not present in [trackIds] (files that no longer exist).
     */
    suspend fun clear(trackIds: Set<String>)
}
