package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.data.ALL_DESTINATIONS
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.util.NameNormalizer
import hu.mostoha.mobile.kmp.huki.util.distanceBetween
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.doubles.shouldBeLessThanOrEqual
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.maplibre.spatialk.units.extensions.inMeters
import org.maplibre.spatialk.units.extensions.kilometers
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
    fun `Given the same repository, When getTopDestinations twice, Then the ranking is re-rolled`() {
        val first = repository.getTopDestinations()
        val second = repository.getTopDestinations()

        second shouldNotBe first
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
    fun `Given a radius, When getNearbyDestinations, Then only destinations inside it are returned`() {
        val location = Location(BUDAPEST_LATITUDE, BUDAPEST_LONGITUDE)
        val radius = 30.kilometers

        val actual = repository.getNearbyDestinations(location, radius = radius)

        actual.shouldNotBeEmpty()
        actual.size shouldBeLessThan ALL_DESTINATIONS.size
        actual.forEach { location.distanceBetween(it.location).inMeters shouldBeLessThanOrEqual radius.inMeters }
        val excluded = ALL_DESTINATIONS - actual.toSet()
        excluded.forEach { location.distanceBetween(it.location).inMeters shouldBeGreaterThan radius.inMeters }
    }

    @Test
    fun `Given a limit, When getNearbyDestinations, Then the closest ones are returned in order`() {
        val location = Location(BUDAPEST_LATITUDE, BUDAPEST_LONGITUDE)

        val actual = repository.getNearbyDestinations(location, limit = 5)

        actual.size shouldBe 5
        actual shouldBe repository.getNearbyDestinations(location).take(5)
    }

    @Test
    fun `Given a radius with no destination inside, When getNearbyDestinations, Then nothing is returned`() {
        val actual = repository.getNearbyDestinations(NORTH_POLE, radius = 30.kilometers)

        actual.shouldBeEmpty()
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
    fun `Given an ascii query, When searchDestinations, Then diacritic destination names match`() {
        val actual = repository.searchDestinations(query = "dobogoko", limit = 10)

        actual.map { it.name } shouldContain "Dobogókő"
    }

    @Test
    fun `Given a query with diacritics and uppercase, When searchDestinations, Then it still matches`() {
        val actual = repository.searchDestinations(query = "DOBOGÓKŐ", limit = 10)

        actual.map { it.name } shouldContain "Dobogókő"
    }

    @Test
    fun `Given a town query, When searchDestinations, Then destinations in that town match`() {
        val actual = repository.searchDestinations(query = "lillafured", limit = 20)

        actual.shouldNotBeEmpty()
        actual.forEach { destination ->
            val matches = NameNormalizer.normalize(destination.name).contains("lillafured") ||
                NameNormalizer.normalize(destination.town).contains("lillafured")
            matches shouldBe true
        }
    }

    @Test
    fun `Given a limit, When searchDestinations, Then at most that many results are returned`() {
        val limit = 3

        val actual = repository.searchDestinations(query = "k", limit = limit)

        actual.size shouldBeLessThanOrEqual limit
    }

    @Test
    fun `When searchDestinations, Then results are sorted by popularity descending`() {
        val actual = repository.searchDestinations(query = "k", limit = 10)

        actual.map { it.popularity } shouldBe actual.map { it.popularity }.sortedDescending()
    }

    @Test
    fun `Given a query with no match, When searchDestinations, Then an empty list is returned`() {
        val actual = repository.searchDestinations(query = "zzqxnomatch", limit = 10)

        actual shouldBe emptyList()
    }

    @Test
    fun `Given a blank query, When searchDestinations, Then an empty list is returned`() {
        val actual = repository.searchDestinations(query = "   ", limit = 10)

        actual shouldBe emptyList()
    }

    @Test
    fun `When getLandscapes, Then all landscapes are returned and each has destinations`() {
        val actual = repository.getLandscapes()

        actual.shouldNotBeEmpty()
        actual.forEach { it.destinations.shouldNotBeEmpty() }
    }

    private companion object {
        const val SEED = 42L
        const val MIN_DISTINCT_TYPES = 6
        const val BUDAPEST_LATITUDE = 47.4979
        const val BUDAPEST_LONGITUDE = 19.0402
        val NORTH_POLE = Location(90.0, 0.0)
    }
}
