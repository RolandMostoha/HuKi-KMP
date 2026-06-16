package hu.mostoha.mobile.kmp.huki.repository

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.copyTo
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.list
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.nameWithoutExtension
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.startAccessingSecurityScopedResource
import io.github.vinceglb.filekit.stopAccessingSecurityScopedResource
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

    override suspend fun saveToFileSystem(sourceUri: String): PlatformFile =
        withContext(Dispatchers.IO) {
            val source = PlatformFile(sourceUri)
            val accessing = source.startAccessingSecurityScopedResource()
            try {
                val destination = resolveDestination(source)
                if (!destination.exists()) {
                    source.copyTo(destination)
                }
                destination
            } finally {
                if (accessing) {
                    source.stopAccessingSecurityScopedResource()
                }
            }
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
    private suspend fun resolveDestination(source: PlatformFile): PlatformFile {
        val sourceBytes = source.readBytes()
        val candidate = gpxDir() / source.name
        if (!candidate.exists() || candidate.readBytes().contentEquals(sourceBytes)) {
            return candidate
        }
        val baseName = source.nameWithoutExtension
        val extension = source.extension.ifEmpty { GPX_EXTENSION }
        var index = COLLISION_START_INDEX
        while (true) {
            val suffixed = gpxDir() / "$baseName ($index).$extension"
            if (!suffixed.exists() || suffixed.readBytes().contentEquals(sourceBytes)) {
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
