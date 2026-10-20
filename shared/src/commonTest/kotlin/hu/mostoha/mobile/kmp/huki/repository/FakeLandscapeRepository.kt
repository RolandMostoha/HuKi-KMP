package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.model.domain.BoundingBox
import hu.mostoha.mobile.kmp.huki.model.domain.LandscapeOutlines

class FakeLandscapeRepository(
    private val outlines: LandscapeOutlines = TEST_LANDSCAPE_OUTLINES,
    private val exception: Exception? = null,
) : LandscapeRepository {
    override suspend fun getLandscapeOutlines(): LandscapeOutlines {
        exception?.let { throw it }
        return outlines
    }

    companion object {
        val TEST_LANDSCAPE_OUTLINES = LandscapeOutlines(
            geoJson = """{"type":"FeatureCollection","features":[]}""",
            bounds = mapOf(
                "matra" to BoundingBox(north = 47.95, east = 20.2, south = 47.8, west = 19.7),
                "bukk" to BoundingBox(north = 48.2, east = 20.8, south = 47.9, west = 20.2),
            ),
            labelsGeoJson = """{"type":"FeatureCollection","features":[]}""",
            hungaryMaskGeoJson = """{"type":"FeatureCollection","features":[]}""",
        )
    }
}
