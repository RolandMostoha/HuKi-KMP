package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.model.data.GpxFileSource
import hu.mostoha.mobile.kmp.huki.model.domain.GpxOrigin
import io.github.vinceglb.filekit.PlatformFile

/**
 * Persists GPX files in the app sandbox at `FileKit.filesDir/gpx/<origin>/` so they can be reused without re-import.
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
    suspend fun saveToSandbox(source: GpxFileSource, origin: GpxOrigin): PlatformFile

    suspend fun listGpxFiles(): List<PlatformFile>

    /**
     * Resolves a sandbox GPX by [fileName] to its current path, or `null` when it no longer exists.
     */
    suspend fun resolveGpxFile(fileName: String): PlatformFile?

    /**
     * Resolves the sandbox file [uri] already points at, or `null` when it lives outside the sandbox.
     * Callers use this to skip re-importing a file that is already committed.
     */
    suspend fun resolveSandboxFile(uri: String): PlatformFile?

    /**
     * Deletes the sandbox file [uri] points at. A file name alone is ambiguous, since the same name can exist
     * under more than one [GpxOrigin].
     */
    suspend fun delete(uri: String)
}
