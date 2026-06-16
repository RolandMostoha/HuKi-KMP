package hu.mostoha.mobile.kmp.huki.repository

import io.github.vinceglb.filekit.PlatformFile

/**
 * Persists imported GPX files in the app sandbox so they can be reused without re-import
 */
interface GpxStorage {

    /**
     * Copies the picked file into the File Systems - "App Sandbox" if an identical copy is not already present.
     *
     * @return the sandbox [PlatformFile] to read from.
     */
    suspend fun saveToFileSystem(sourceUri: String): PlatformFile

    suspend fun listGpxFiles(): List<PlatformFile>

    suspend fun delete(fileName: String)
}
