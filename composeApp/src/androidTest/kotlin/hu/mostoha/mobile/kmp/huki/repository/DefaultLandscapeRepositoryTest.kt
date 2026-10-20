package hu.mostoha.mobile.kmp.huki.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import hu.mostoha.mobile.kmp.huki.TestContext.appContext
import hu.mostoha.mobile.kmp.huki.data.Landscapes
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.string.shouldContain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class DefaultLandscapeRepositoryTest {

    private val repository = DefaultLandscapeRepository(
        bundledFileReader = { file -> file.readText(appContext) },
        dispatcher = Dispatchers.IO,
    )

    @Test
    fun givenBundledGeoJsonFiles_whenGetLandscapeOutlines_thenEveryLandscapeHasOutlineAndLabel() {
        runTest {
            val landscapeIds = Landscapes.map { it.id }

            val outlines = repository.getLandscapeOutlines()

            outlines.bounds.keys shouldContainAll landscapeIds
            landscapeIds.forEach { id ->
                outlines.geoJson shouldContain "\"$id\""
                outlines.labelsGeoJson shouldContain "\"$id\""
            }
            outlines.hungaryMaskGeoJson shouldContain "FeatureCollection"
        }
    }
}
