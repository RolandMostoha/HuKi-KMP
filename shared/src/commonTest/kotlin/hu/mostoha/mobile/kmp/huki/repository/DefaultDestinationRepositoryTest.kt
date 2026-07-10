package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.data.ALL_DESTINATIONS
import hu.mostoha.mobile.kmp.huki.model.domain.Destination
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.util.distanceBetween
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.maplibre.spatialk.units.extensions.inMeters
import kotlin.random.Random
import kotlin.test.Test

class DefaultDestinationRepositoryTest {

    private val repository = DefaultDestinationRepository(random = Random(SEED))

    @Test
    fun `Given default limit, When getTopDestinations, Then returns 20 distinct known destinations`() {
        val actual = repository.getTopDestinations()

        actual.size shouldBe 20
        actual.map { it.osmId }.toSet().size shouldBe 20
        ALL_DESTINATIONS shouldContainAll actual
    }

    @Test
    fun `Given custom limit, When getTopDestinations, Then returns exactly that many destinations`() {
        val limit = 5

        val actual = repository.getTopDestinations(limit = limit)

        actual.size shouldBe limit
    }

    @Test
    fun `Given default limit, When getTopDestinations, Then top destinations cover varied types`() {
        val actual = repository.getTopDestinations()

        val distinctTypes = actual.map { it.type }.toSet()

        distinctTypes.size shouldBeGreaterThanOrEqual MIN_DISTINCT_TYPES
    }

    @Test
    fun `Given a user location, When getTopDestinations, Then nearby destinations are favoured`() {
        val location = Location(BUDAPEST_LATITUDE, BUDAPEST_LONGITUDE)
        val withLocationRepository = DefaultDestinationRepository(random = Random(SEED))
        val noLocationRepository = DefaultDestinationRepository(random = Random(SEED))

        val withLocation = withLocationRepository.getTopDestinations(location = location)
        val noLocation = noLocationRepository.getTopDestinations()

        val withLocationAvgKm = withLocation.averageDistanceKm(location)
        val noLocationAvgKm = noLocation.averageDistanceKm(location)

        withLocationAvgKm shouldBeLessThan noLocationAvgKm
    }

    @Test
    fun `Given a location was available, When getTopDestinations is called again, Then the order is stable`() {
        val location = Location(BUDAPEST_LATITUDE, BUDAPEST_LONGITUDE)

        val first = repository.getTopDestinations(location = location)
        val second = repository.getTopDestinations(location = location)

        second shouldBe first
    }

    @Test
    fun `Given no location yet, When location becomes available, Then ranking is recomputed with distance`() {
        val location = Location(BUDAPEST_LATITUDE, BUDAPEST_LONGITUDE)

        val withoutLocation = repository.getTopDestinations()
        val withLocation = repository.getTopDestinations(location = location)

        withLocation.averageDistanceKm(location) shouldBeLessThan withoutLocation.averageDistanceKm(location)
    }

    @Test
    fun `Given different random seeds, When getTopDestinations, Then orderings differ`() {
        val firstRepository = DefaultDestinationRepository(random = Random(SEED))
        val secondRepository = DefaultDestinationRepository(random = Random(SEED + 1))

        val first = firstRepository.getTopDestinations()
        val second = secondRepository.getTopDestinations()

        second shouldNotBe first
    }

    @Test
    fun `When getPopularDestinations, Then all destinations are returned sorted by popularity descending`() {
        val actual = repository.getPopularDestinations()

        actual.size shouldBe ALL_DESTINATIONS.size
        actual shouldContainAll ALL_DESTINATIONS
        actual.map { it.popularity } shouldBe actual.map { it.popularity }.sortedDescending()
    }

    @Test
    fun `Given a location, When getNearbyDestinations, Then all destinations returned sorted by distance ascending`() {
        val location = Location(BUDAPEST_LATITUDE, BUDAPEST_LONGITUDE)

        val actual = repository.getNearbyDestinations(location)

        actual.size shouldBe ALL_DESTINATIONS.size
        val distances = actual.map { location.distanceBetween(it.location).inMeters }
        distances shouldBe distances.sorted()
    }

    @Test
    fun `Given an existing osmId, When requireDestination, Then the matching destination is returned`() {
        val expected = ALL_DESTINATIONS.first()

        val actual = repository.requireDestination(expected.osmId)

        actual shouldBe expected
    }

    @Test
    fun `Given an unknown osmId, When requireDestination, Then NoSuchElementException is thrown`() {
        shouldThrow<NoSuchElementException> {
            repository.requireDestination("unknown_osm_id")
        }
    }

    @Test
    fun `When getLandscapes, Then all landscapes are returned and each has destinations`() {
        val actual = repository.getLandscapes()

        actual.shouldNotBeEmpty()
        actual.forEach { it.destinations.shouldNotBeEmpty() }
    }

    private fun List<Destination>.averageDistanceKm(location: Location): Double = map { location.distanceBetween(it.location).inMeters }.average() / 1000.0

    private companion object {
        const val SEED = 42L
        const val MIN_DISTINCT_TYPES = 6
        const val BUDAPEST_LATITUDE = 47.4979
        const val BUDAPEST_LONGITUDE = 19.0402
    }
}
