package hu.mostoha.mobile.kmp.huki.model.mapper

import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.OsmType
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceCategory
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceSource
import hu.mostoha.mobile.kmp.huki.model.network.LocationIqAddress
import hu.mostoha.mobile.kmp.huki.model.network.LocationIqPlace
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class PlaceMapperTest {
    private companion object {
        private val TAPPED_LOCATION = Location(47.7181, 18.8948)
    }

    @Test
    fun `Given LocationIqPlace with known type and osmType - when toPlaceSearchResult - then both resolved`() {
        val networkPlace = locationIqPlace(type = "peak", osmType = "N")

        val place = networkPlace.toPlaceSearchResult()

        place.placeCategory shouldBe PlaceCategory.PEAK
        place.osmType shouldBe OsmType.NODE
    }

    @Test
    fun `Given LocationIqPlace with unknown type - when toPlaceSearchResult - then placeCategory is null`() {
        val networkPlace = locationIqPlace(type = "unmapped_tag", osmType = "way")

        val place = networkPlace.toPlaceSearchResult()

        place.placeCategory shouldBe null
        place.osmType shouldBe OsmType.WAY
    }

    @Test
    fun `Given LocationIqPlace with null type and unrecognized osmType - when toPlaceSearchResult - then both null`() {
        val networkPlace = locationIqPlace(type = null, osmType = "garbage")

        val place = networkPlace.toPlaceSearchResult()

        place.placeCategory shouldBe null
        place.osmType shouldBe null
    }

    @Test
    fun `Given reverse geocoded place with address details - when toReverseGeocodedPlace - then name and address built`() {
        val networkPlace = locationIqPlace(type = "residential", osmType = "way").copy(
            lat = 47.9,
            lon = 18.5,
            address = LocationIqAddress(
                houseNumber = "17",
                road = "Napsugár utca",
                city = "Pilisszentkereszt",
                county = "Pest vármegye",
                postcode = "2099",
            ),
        )

        val place = networkPlace.toReverseGeocodedPlace(TAPPED_LOCATION)

        place.name shouldBe "Napsugár utca 17"
        place.address shouldBe "2099, Pilisszentkereszt, Pest vármegye"
        place.placeSource shouldBe PlaceSource.LONG_TAP_ON_MAP
    }

    @Test
    fun `Given reverse geocoded place without address details - when toReverseGeocodedPlace - then displayName used`() {
        val networkPlace = locationIqPlace(type = null, osmType = "node")

        val place = networkPlace.toReverseGeocodedPlace(TAPPED_LOCATION)

        place.name shouldBe "Display Name"
        place.address shouldBe "Display Name"
    }

    @Test
    fun `Given reverse geocoded place - when toReverseGeocodedPlace - then location is the tapped one`() {
        val networkPlace = locationIqPlace(type = "peak", osmType = "N").copy(lat = 47.9, lon = 18.5)

        val place = networkPlace.toReverseGeocodedPlace(TAPPED_LOCATION)

        place.location shouldBe TAPPED_LOCATION
    }

    @Test
    fun `Given user location - when toReverseGeocodedPlace - then distance is formatted`() {
        val networkPlace = locationIqPlace(type = "peak", osmType = "N")
        val userLocation = Location(TAPPED_LOCATION.latitude - 0.1, TAPPED_LOCATION.longitude)

        val place = networkPlace.toReverseGeocodedPlace(TAPPED_LOCATION, userLocation)

        place.distance shouldBe "11.1 km"
    }

    @Test
    fun `Given no user location - when toReverseGeocodedPlace - then distance is null`() {
        val networkPlace = locationIqPlace(type = "peak", osmType = "N")

        val place = networkPlace.toReverseGeocodedPlace(TAPPED_LOCATION, userLocation = null)

        place.distance shouldBe null
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
