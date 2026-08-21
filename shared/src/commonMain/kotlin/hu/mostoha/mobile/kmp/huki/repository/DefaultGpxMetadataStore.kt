package hu.mostoha.mobile.kmp.huki.repository

import co.touchlab.kermit.Logger
import hu.mostoha.mobile.kmp.huki.model.data.GpxMetadataEntry
import hu.mostoha.mobile.kmp.huki.model.data.GpxMetadataModel
import hu.mostoha.mobile.kmp.huki.service.CrashlyticsService
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.readString
import io.github.vinceglb.filekit.writeString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * JSON-backed [GpxMetadataStore] persisting to `FileKit.filesDir/gpx/metadata.json`.
 */
class DefaultGpxMetadataStore(private val crashlyticsService: CrashlyticsService) : GpxMetadataStore {

    private val json = Json { ignoreUnknownKeys = true }
    private val mutex = Mutex()
    private var cache: GpxMetadataModel? = null

    override suspend fun recordOpened(entry: GpxMetadataEntry) {
        withContext(Dispatchers.IO) {
            mutex.withLock {
                val model = loadLocked()
                val gpxFiles = model.gpxFiles
                    .filterNot { it.trackId == entry.trackId }
                    .plus(entry)

                persistLocked(model.copy(gpxFiles = gpxFiles))
            }
        }
    }

    override suspend fun getMetadata(): GpxMetadataModel =
        withContext(Dispatchers.IO) {
            mutex.withLock {
                loadLocked()
            }
        }

    override suspend fun remove(trackIds: Set<String>) {
        withContext(Dispatchers.IO) {
            mutex.withLock {
                val model = loadLocked()
                val gpxFiles = model.gpxFiles.filter { it.trackId in trackIds }
                if (gpxFiles.size != model.gpxFiles.size) {
                    persistLocked(model.copy(gpxFiles = gpxFiles))
                }
            }
        }
    }

    override suspend fun removeByTrackId(trackId: String) {
        withContext(Dispatchers.IO) {
            mutex.withLock {
                val model = loadLocked()
                val gpxFiles = model.gpxFiles.filterNot { it.trackId == trackId }
                if (gpxFiles.size != model.gpxFiles.size) {
                    persistLocked(model.copy(gpxFiles = gpxFiles))
                }
            }
        }
    }

    private suspend fun loadLocked(): GpxMetadataModel {
        cache?.let { return it }
        val model = runCatching {
            val file = metadataFile()
            if (file.exists()) json.decodeFromString<GpxMetadataModel>(file.readString()) else GpxMetadataModel()
        }.getOrElse {
            Logger.e(it) { "GpxMetadata: failed to read metadata, rebuilding empty" }
            crashlyticsService.recordException(it)
            GpxMetadataModel()
        }
        cache = model
        return model
    }

    private suspend fun persistLocked(model: GpxMetadataModel) {
        cache = model
        runCatching { metadataFile().writeString(json.encodeToString(model)) }
            .onFailure {
                Logger.e(it) { "GpxMetadata: failed to persist metadata" }
                crashlyticsService.recordException(it)
            }
    }

    private fun metadataFile(): PlatformFile =
        (FileKit.filesDir / GPX_DIR).also {
            if (!it.exists()) {
                it.createDirectories()
            }
        } / METADATA_FILE

    companion object {
        private const val GPX_DIR = "gpx"
        private const val METADATA_FILE = "metadata.json"
    }
}
