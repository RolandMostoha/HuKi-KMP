package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.model.data.GpxMetadataEntry
import hu.mostoha.mobile.kmp.huki.model.data.GpxMetadataModel

/**
 * Persists lightweight attributes in `FileKit.filesDir/gpx/metadata.json` for imported GPX files.
 * Goals: cache for GPX attributes + non-cloud based GPX migration.
 */
interface GpxMetadataStore {

    /**
     * Records a freshly opened track, overwriting any earlier entry with the same trackId.
     */
    suspend fun recordOpened(entry: GpxMetadataEntry)

    /**
     * @return the full [GpxMetadataModel] so callers can pick the attributes they need.
     */
    suspend fun getMetadata(): GpxMetadataModel

    /**
     * Drops attributes whose trackId is not present in [trackIds] (files that no longer exist).
     */
    suspend fun remove(trackIds: Set<String>)

    /**
     * Drops the attributes of a single deleted file, matched by its sandbox [fileName].
     */
    suspend fun remove(fileName: String)
}
