package hu.mostoha.mobile.kmp.huki.model.mapper

import hu.mostoha.mobile.kmp.huki.model.db.PlaceHistoryEntity
import hu.mostoha.mobile.kmp.huki.model.domain.BoundingBox
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.OsmType
import hu.mostoha.mobile.kmp.huki.model.domain.Place
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceCategory
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceSource
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class PlaceHistoryMapperTest {

    @Test
    fun `Given place with bounding box, When toPlaceHistoryEntity, Then all fields are mapped`() {
        val boundingBox = BoundingBox(north = 47.72, east = 18.90, south = 47.71, west = 18.88)
        val place = Place(
            osmId = "123",
            location = Location(47.7181, 18.8948),
            name = "Dobogókő",
            placeSource = PlaceSource.SEARCH_AUTOCOMPLETE,
            address = "Pilis, Hungary",
            placeCategory = PlaceCategory.PEAK,
            osmType = OsmType.NODE,
            boundingBox = boundingBox,
        )

        val actual = place.toPlaceHistoryEntity(visitedAt = 1_700_000_000_000)

        actual.osmType shouldBe OsmType.NODE
        actual.osmId shouldBe "123"
        actual.name shouldBe "Dobogókő"
        actual.nameNormalized shouldBe "dobogoko"
        actual.address shouldBe "Pilis, Hungary"
        actual.latitude shouldBe 47.7181
        actual.longitude shouldBe 18.8948
        actual.placeCategory shouldBe PlaceCategory.PEAK
        actual.placeSource shouldBe PlaceSource.SEARCH_AUTOCOMPLETE
        actual.boundingBox shouldBe boundingBox
        actual.lastVisited shouldBe 1_700_000_000_000
    }

    @Test
    fun `Given place without osmType or bounding box, When toPlaceHistoryEntity, Then osmType falls back to NODE`() {
        val place = Place(
            osmId = "1",
            location = Location(47.0, 19.0),
            name = "Somewhere",
            placeSource = PlaceSource.MY_LOCATION,
        )

        val actual = place.toPlaceHistoryEntity(visitedAt = 1L)

        actual.osmType shouldBe OsmType.NODE
        actual.placeSource shouldBe PlaceSource.MY_LOCATION
        actual.placeCategory.shouldBeNull()
        actual.boundingBox.shouldBeNull()
        actual.address.shouldBeNull()
    }

    @Test
    fun `Given entity, When toPlace, Then domain place fields are mapped`() {
        val boundingBox = BoundingBox(north = 47.72, east = 18.90, south = 47.71, west = 18.88)
        val entity = PlaceHistoryEntity(
            osmType = OsmType.RELATION,
            osmId = "123",
            name = "Dobogókő",
            nameNormalized = "dobogoko",
            address = "Pilis, Hungary",
            latitude = 47.7181,
            longitude = 18.8948,
            placeCategory = PlaceCategory.PEAK,
            placeSource = PlaceSource.SEARCH_AUTOCOMPLETE,
            boundingBox = boundingBox,
            lastVisited = 1_700_000_000_000,
        )

        val actual = entity.toPlace()

        actual shouldBe Place(
            osmId = "123",
            location = Location(47.7181, 18.8948),
            name = "Dobogókő",
            placeSource = PlaceSource.SEARCH_AUTOCOMPLETE,
            address = "Pilis, Hungary",
            placeCategory = PlaceCategory.PEAK,
            osmType = OsmType.RELATION,
            boundingBox = boundingBox,
        )
    }
}
