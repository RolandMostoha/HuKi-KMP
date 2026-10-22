package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.LandscapeOutlines
import hu.mostoha.mobile.kmp.huki.model.mapper.toLandscapeOutlines
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class DefaultLandscapeRepository(
    private val bundledFileReader: BundledFileReader,
    private val dispatcher: CoroutineDispatcher,
) : LandscapeRepository {

    private val mutex = Mutex()
    private var outlines: LandscapeOutlines? = null

    override suspend fun getLandscapeOutlines(): LandscapeOutlines =
        mutex.withLock {
            outlines ?: withContext(dispatcher) {
                bundledFileReader.readText(SharedRes.files.landscapes_geojson).toLandscapeOutlines(
                    labelsGeoJson = bundledFileReader.readText(SharedRes.files.landscape_labels_geojson),
                    hungaryMaskGeoJson = bundledFileReader.readText(SharedRes.files.hungary_mask_geojson),
                )
            }.also { outlines = it }
        }
}
