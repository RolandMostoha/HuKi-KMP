package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.model.data.GpxFileSource
import io.github.vinceglb.filekit.PlatformFile

/**
 * Persists imported GPX files in the app sandbox at `FileKit.filesDir/gpx/` so they can be reused without re-import.
 */
interface GpxStorage {

    /**
     * Reads the picked file's bytes under a security scope.
     */
    suspend fun readGpx(sourceUri: String): GpxFileSource

    /**
     * Commits an already-read [source] into the "App Sandbox" if an identical copy is not already present.
     *
     * @return the sandbox [PlatformFile] to read from.
     */
    suspend fun saveToSandbox(source: GpxFileSource): PlatformFile

    suspend fun listGpxFiles(): List<PlatformFile>

    suspend fun delete(fileName: String)
}
