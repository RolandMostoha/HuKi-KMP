package hu.mostoha.mobile.kmp.huki.model.mapper

import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.Destination
import hu.mostoha.mobile.kmp.huki.model.domain.DestinationType
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.OsmType
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceCategory
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceSource
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class DestinationMapperTest {

    @Test
    fun `Given destination - When toPlace - Then osmType is NODE and address is town`() {
        val destination = Destination(
            osmId = "456",
            name = "Kékestető",
            town = "Mátraszentimre",
            type = DestinationType.HIGHEST_PEAK,
            location = Location(47.8721, 20.0102),
            description = SharedRes.strings.destinations_type_peak,
            popularity = 10,
        )

        val actual = destination.toPlace()

        actual.osmId shouldBe "456"
        actual.name shouldBe "Kékestető"
        actual.location shouldBe Location(47.8721, 20.0102)
        actual.address shouldBe "Mátraszentimre"
        actual.osmType shouldBe OsmType.NODE
        actual.placeCategory shouldBe PlaceCategory.PEAK
        actual.placeSource shouldBe PlaceSource.DESTINATIONS
        actual.distance.shouldBeNull()
        actual.boundingBox.shouldBeNull()
    }
}
