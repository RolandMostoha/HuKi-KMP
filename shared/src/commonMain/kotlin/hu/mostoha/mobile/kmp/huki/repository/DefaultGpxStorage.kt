package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.model.data.GpxFileSource
import hu.mostoha.mobile.kmp.huki.model.domain.GpxOrigin
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.list
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.path
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.startAccessingSecurityScopedResource
import io.github.vinceglb.filekit.stopAccessingSecurityScopedResource
import io.github.vinceglb.filekit.write
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class DefaultGpxStorage : GpxStorage {

    private fun gpxDir(origin: GpxOrigin): PlatformFile =
        (FileKit.filesDir / GPX_DIR / origin.dirName).also {
            if (!it.exists()) {
                it.createDirectories()
            }
        }

    override suspend fun readGpx(sourceUri: String): GpxFileSource =
        withContext(Dispatchers.IO) {
            val source = PlatformFile(sourceUri)
            val accessing = source.startAccessingSecurityScopedResource()
            try {
                GpxFileSource(fileName = source.name, content = source.readBytes())
            } finally {
                if (accessing) {
                    source.stopAccessingSecurityScopedResource()
                }
            }
        }

    override suspend fun saveToSandbox(source: GpxFileSource, origin: GpxOrigin): PlatformFile =
        withContext(Dispatchers.IO) {
            val destination = resolveDestination(gpxDir(origin), source.fileName, source.content)
            if (!destination.exists()) {
                destination.write(source.content)
            }
            destination
        }

    override suspend fun listGpxFiles(): List<PlatformFile> =
        withContext(Dispatchers.IO) {
            GpxOrigin.entries.flatMap { gpxDir(it).list() }
        }

    override suspend fun resolveGpxFile(fileName: String): PlatformFile? =
        withContext(Dispatchers.IO) {
            GpxOrigin.entries.firstNotNullOfOrNull { origin ->
                (gpxDir(origin) / fileName).takeIf { it.exists() }
            }
        }

    override suspend fun resolveSandboxFile(uri: String): PlatformFile? =
        withContext(Dispatchers.IO) {
            GpxOrigin.entries.firstNotNullOfOrNull { origin ->
                val dir = gpxDir(origin)
                val fileName = uri.removePrefix("${dir.path}/")
                if (fileName == uri || fileName.isEmpty() || fileName.contains('/')) {
                    return@firstNotNullOfOrNull null
                }
                (dir / fileName).takeIf { it.exists() }
            }
        }

    override suspend fun delete(uri: String) {
        withContext(Dispatchers.IO) {
            resolveSandboxFile(uri)?.delete(mustExist = false)
        }
    }

    /**
     * Returns the sandbox file to write to: the existing file when its content is identical,
     * otherwise the next free name suffixed with " (n)".
     */
    private suspend fun resolveDestination(dir: PlatformFile, fileName: String, content: ByteArray): PlatformFile {
        val candidate = dir / fileName
        if (!candidate.exists() || candidate.readBytes().contentEquals(content)) {
            return candidate
        }
        val baseName = fileName.substringBeforeLast('.', fileName)
        val extension = fileName.substringAfterLast('.', "").ifEmpty { GPX_EXTENSION }
        var index = COLLISION_START_INDEX
        while (true) {
            val suffixed = dir / "$baseName ($index).$extension"
            if (!suffixed.exists() || suffixed.readBytes().contentEquals(content)) {
                return suffixed
            }
            index++
        }
    }

    companion object {
        private const val GPX_DIR = "gpx"
        private const val GPX_EXTENSION = "gpx"
        private const val COLLISION_START_INDEX = 2
    }
}
