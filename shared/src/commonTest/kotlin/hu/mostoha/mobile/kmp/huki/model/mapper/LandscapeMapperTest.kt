package hu.mostoha.mobile.kmp.huki.model.mapper

import hu.mostoha.mobile.kmp.huki.model.domain.BoundingBox
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class LandscapeMapperTest {

    @Test
    fun `Given Polygon and MultiPolygon features - When mapped to outlines - Then bounds cover every ring per id`() {
        val geoJson = """
            {"type":"FeatureCollection","features":[
              {"type":"Feature","properties":{"id":"matra"},"geometry":{"type":"Polygon","coordinates":[
                [[19.7,47.8],[20.2,47.8],[20.0,47.95],[19.7,47.8]]
              ]}},
              {"type":"Feature","properties":{"id":"hortobagy"},"geometry":{"type":"MultiPolygon","coordinates":[
                [[[21.0,47.4],[21.2,47.4],[21.1,47.6],[21.0,47.4]]],
                [[[20.8,47.7],[20.9,47.7],[20.85,47.8],[20.8,47.7]]]
              ]}}
            ]}
        """.trimIndent()

        val labelsGeoJson = """{"type":"FeatureCollection","features":[]}"""
        val maskGeoJson = """{"type":"FeatureCollection","features":[]}"""

        val actual = geoJson.toLandscapeOutlines(labelsGeoJson = labelsGeoJson, hungaryMaskGeoJson = maskGeoJson)

        actual.geoJson shouldBe geoJson
        actual.labelsGeoJson shouldBe labelsGeoJson
        actual.hungaryMaskGeoJson shouldBe maskGeoJson
        actual.bounds shouldBe mapOf(
            "matra" to BoundingBox(north = 47.95, east = 20.2, south = 47.8, west = 19.7),
            "hortobagy" to BoundingBox(north = 47.8, east = 21.2, south = 47.4, west = 20.8),
        )
    }
}
