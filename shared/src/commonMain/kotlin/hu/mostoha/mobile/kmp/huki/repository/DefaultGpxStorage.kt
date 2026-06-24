package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.model.data.GpxFileSource
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.list
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.startAccessingSecurityScopedResource
import io.github.vinceglb.filekit.stopAccessingSecurityScopedResource
import io.github.vinceglb.filekit.write
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class DefaultGpxStorage : GpxStorage {

    private fun gpxDir(): PlatformFile =
        (FileKit.filesDir / GPX_DIR / EXTERNAL_DIR).also {
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

    override suspend fun saveToSandbox(source: GpxFileSource): PlatformFile =
        withContext(Dispatchers.IO) {
            val destination = resolveDestination(source.fileName, source.content)
            if (!destination.exists()) {
                destination.write(source.content)
            }
            destination
        }

    override suspend fun listGpxFiles(): List<PlatformFile> = gpxDir().list()

    override suspend fun delete(fileName: String) {
        withContext(Dispatchers.IO) {
            (gpxDir() / fileName).delete(mustExist = false)
        }
    }

    /**
     * Returns the sandbox file to write to: the existing file when its content is identical,
     * otherwise the next free name suffixed with " (n)".
     */
    private suspend fun resolveDestination(fileName: String, content: ByteArray): PlatformFile {
        val candidate = gpxDir() / fileName
        if (!candidate.exists() || candidate.readBytes().contentEquals(content)) {
            return candidate
        }
        val baseName = fileName.substringBeforeLast('.', fileName)
        val extension = fileName.substringAfterLast('.', "").ifEmpty { GPX_EXTENSION }
        var index = COLLISION_START_INDEX
        while (true) {
            val suffixed = gpxDir() / "$baseName ($index).$extension"
            if (!suffixed.exists() || suffixed.readBytes().contentEquals(content)) {
                return suffixed
            }
            index++
        }
    }

    companion object {
        private const val GPX_DIR = "gpx"
        private const val EXTERNAL_DIR = "external"
        private const val GPX_EXTENSION = "gpx"
        private const val COLLISION_START_INDEX = 2
    }
}
