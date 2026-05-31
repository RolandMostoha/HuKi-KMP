package hu.mostoha.mobile.kmp.huki.model.domain

import hu.mostoha.mobile.kmp.huki.model.network.LocationIqPlace
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class PlaceTest {
    @Test
    fun `Given LocationIqPlace with known type and osmType, when toPlaceSearchResult, then both resolved`() {
        val networkPlace = locationIqPlace(type = "peak", osmType = "N")

        val place = networkPlace.toPlaceSearchResult()

        place.placeCategory shouldBe PlaceCategory.PEAK
        place.osmType shouldBe OsmType.NODE
    }

    @Test
    fun `Given LocationIqPlace with unknown type, when toPlaceSearchResult, then placeCategory is null`() {
        val networkPlace = locationIqPlace(type = "unmapped_tag", osmType = "way")

        val place = networkPlace.toPlaceSearchResult()

        place.placeCategory shouldBe null
        place.osmType shouldBe OsmType.WAY
    }

    @Test
    fun `Given LocationIqPlace with null type and unrecognized osmType, when toPlaceSearchResult, then both null`() {
        val networkPlace = locationIqPlace(type = null, osmType = "garbage")

        val place = networkPlace.toPlaceSearchResult()

        place.placeCategory shouldBe null
        place.osmType shouldBe null
    }

    private fun locationIqPlace(type: String?, osmType: String) =
        LocationIqPlace(
            placeId = "1",
            osmId = "osm-1",
            osmType = osmType,
            licence = "licence",
            lat = 47.0,
            lon = 19.0,
            displayName = "Display Name",
            type = type,
        )
}
