package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.data.ALL_DESTINATIONS
import hu.mostoha.mobile.kmp.huki.model.domain.DestinationChallenge
import hu.mostoha.mobile.kmp.huki.model.domain.DestinationType
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
    fun `Given a feature of another type - When filtering by type - Then only main types are listed`() {
        val lookouts = repository.getDestinationsByType(DestinationType.LOOKOUT)
        val arboretums = repository.getDestinationsByType(DestinationType.ARBORETUM).map { it.name }

        lookouts.map { it.id } shouldContain "balatonboglar_gombkilato"
        lookouts.map { it.id }.contains("doba_somlo_kilato") shouldBe false
        lookouts.all { it.type == DestinationType.LOOKOUT } shouldBe true
        arboretums.contains("Deseda-tó és parkerdő") shouldBe false
    }

    @Test
    fun `When getDestinationCategories - Then every challenge and type with members is listed with its destinations`() {
        val actual = repository.getDestinationCategories()

        actual.forEach { it.destinations.shouldNotBeEmpty() }
        actual.first { it.id == "highest_peaks" }.destinations shouldBe
            repository.getDestinationsByChallenge(DestinationChallenge.HIGHEST_PEAKS)
        actual.first { it.id == "scenic_railway" }.destinations shouldBe
            repository.getDestinationsByType(DestinationType.SCENIC_RAILWAY)
        actual.map { it.id }.toSet().size shouldBe actual.size
    }

    @Test
    fun `Given panoramic views - When filtering - Then include physical types and exclude unconfirmed views`() {
        val actual = repository.getDestinationsByChallenge(DestinationChallenge.PANORAMIC_VIEWS)

        actual.map { it.name } shouldContainAll listOf("Kékestető", "Nagy-Szénás", "Apáthy-szikla", "Kisoroszi szigetcsúcs")
        actual.filter { it.name in setOf("Szilvási-kő", "Gömbölyű-kő", "Teve-szikla", "Sas-szikla") }.shouldBeEmpty()
        actual.first { it.name == "Kékestető" }.type shouldBe DestinationType.PEAK
        actual.first { it.name == "Apáthy-szikla" }.type shouldBe DestinationType.ROCK
        actual.first { it.name == "Kisoroszi szigetcsúcs" }.type shouldBe DestinationType.ISLAND
        actual.map { it.popularity } shouldBe actual.map { it.popularity }.sortedDescending()
    }

    @Test
    fun `Given corrected attractions - When filtering - Then exclude misleading types`() {
        categoryTestCases().forEach { (name, type, excludedType) ->
            val actual = repository.getDestinationsByType(type).map { it.name }
            val excluded = repository.getDestinationsByType(excludedType).map { it.name }

            actual shouldContain name
            excluded.contains(name) shouldBe false
        }
    }

    @Test
    fun `Given landscape summits - When filtering - Then include scoped maxima and exclude Tubes`() {
        val actual = repository.getDestinationsByChallenge(DestinationChallenge.HIGHEST_PEAKS)

        actual.size shouldBe 17
        actual.map { it.name } shouldContainAll listOf("Kékestető", "Szilvási-kő", "Csóványos", "Pilis-tető", "Nagy-Gerecse")
        actual.map { it.name }.contains("Tubes") shouldBe false
        actual.all { it.type == DestinationType.PEAK } shouldBe true
    }

    @Test
    fun `Given cave access groups - When filtering - Then distinguish guided tours and entrance visits`() {
        val guided = repository.getDestinationsByType(DestinationType.CAVE_GUIDE_REQUIRED)
        val independent = repository.getDestinationsByType(DestinationType.CAVE_WITHOUT_GUIDE)

        guided.map { it.name } shouldContainAll listOf("Szent István-barlang", "Abaligeti Barlang")
        independent.map { it.name } shouldContainAll listOf("Leány-barlang", "Szelim-barlang")
        guided.filter { it in independent }.shouldBeEmpty()
        repository.getDestinationsByType(DestinationType.CAVE) shouldContainAll guided + independent
        ALL_DESTINATIONS.any { it.name == "Róka-hegyi barlang" } shouldBe false
    }

    @Test
    fun `Given castle ruins - When filtering castles - Then keep ruins and separate residential palaces`() {
        val castles = repository.getDestinationsByType(DestinationType.CASTLE)
        val palaces = repository.getDestinationsByType(DestinationType.PALACE)

        castles shouldContainAll repository.getDestinationsByType(DestinationType.CASTLE_RUIN)
        palaces.map { it.name } shouldContain "Wenckheim-kastély"
        castles.map { it.name }.contains("Wenckheim-kastély") shouldBe false
    }

    @Test
    fun `Given a tagged destination - When resolving it - Then the verified location reaches the map`() {
        val actual = repository.requireDestination("tapolca_tavasbarlang")

        actual.location shouldBe Location(46.883241, 17.443424)
        actual.town shouldBe "Tapolca"
    }

    @Test
    fun `Given default limit - When getTopDestinations - Then returns 20 distinct known destinations`() {
        val actual = repository.getTopDestinations()

        actual.size shouldBe 20
        actual.map { it.id }.toSet().size shouldBe 20
        ALL_DESTINATIONS shouldContainAll actual
    }

    @Test
    fun `Given custom limit - When getTopDestinations - Then returns exactly that many destinations`() {
        val limit = 5

        val actual = repository.getTopDestinations(limit = limit)

        actual.size shouldBe limit
    }

    @Test
    fun `Given default limit - When getTopDestinations - Then top destinations cover varied types`() {
        val actual = repository.getTopDestinations()

        val distinctTypes = actual.map { it.type }.toSet()

        distinctTypes.size shouldBeGreaterThanOrEqual MIN_DISTINCT_TYPES
    }

    @Test
    fun `Given the same repository - When getTopDestinations twice - Then the ranking is re-rolled`() {
        val first = repository.getTopDestinations()
        val second = repository.getTopDestinations()

        second shouldNotBe first
    }

    @Test
    fun `Given different random seeds - When getTopDestinations - Then orderings differ`() {
        val firstRepository = DefaultDestinationRepository(random = Random(SEED))
        val secondRepository = DefaultDestinationRepository(random = Random(SEED + 1))

        val first = firstRepository.getTopDestinations()
        val second = secondRepository.getTopDestinations()

        second shouldNotBe first
    }

    @Test
    fun `When getPopularDestinations - Then all destinations are returned sorted by popularity descending`() {
        val actual = repository.getPopularDestinations()

        actual.size shouldBe ALL_DESTINATIONS.size
        actual shouldContainAll ALL_DESTINATIONS
        actual.map { it.popularity } shouldBe actual.map { it.popularity }.sortedDescending()
    }

    @Test
    fun `Given a location - When getNearbyDestinations - Then all destinations returned sorted by distance ascending`() {
        val location = Location(BUDAPEST_LATITUDE, BUDAPEST_LONGITUDE)

        val actual = repository.getNearbyDestinations(location)

        actual.size shouldBe ALL_DESTINATIONS.size
        val distances = actual.map { location.distanceBetween(it.location).inMeters }
        distances shouldBe distances.sorted()
    }

    @Test
    fun `Given a radius - When getNearbyDestinations - Then only destinations inside it are returned`() {
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
    fun `Given a limit - When getNearbyDestinations - Then the closest ones are returned in order`() {
        val location = Location(BUDAPEST_LATITUDE, BUDAPEST_LONGITUDE)

        val actual = repository.getNearbyDestinations(location, limit = 5)

        actual.size shouldBe 5
        actual shouldBe repository.getNearbyDestinations(location).take(5)
    }

    @Test
    fun `Given a radius with no destination inside - When getNearbyDestinations - Then nothing is returned`() {
        val actual = repository.getNearbyDestinations(NORTH_POLE, radius = 30.kilometers)

        actual.shouldBeEmpty()
    }

    @Test
    fun `Given an existing id - When requireDestination - Then the matching destination is returned`() {
        val expected = ALL_DESTINATIONS.first()

        val actual = repository.requireDestination(expected.id)

        actual shouldBe expected
    }

    @Test
    fun `Given an unknown id - When requireDestination - Then NoSuchElementException is thrown`() {
        shouldThrow<NoSuchElementException> {
            repository.requireDestination("unknown_id")
        }
    }

    @Test
    fun `Given an ascii query - When searchDestinations - Then diacritic destination names match`() {
        val actual = repository.searchDestinations(query = "dobogoko", limit = 10)

        actual.map { it.name } shouldContain "Dobogókő"
    }

    @Test
    fun `Given a query with diacritics and uppercase - When searchDestinations - Then it still matches`() {
        val actual = repository.searchDestinations(query = "DOBOGÓKŐ", limit = 10)

        actual.map { it.name } shouldContain "Dobogókő"
    }

    @Test
    fun `Given a town query - When searchDestinations - Then destinations in that town match`() {
        val actual = repository.searchDestinations(query = "lillafured", limit = 20)

        actual.shouldNotBeEmpty()
        actual.forEach { destination ->
            val matches = NameNormalizer.normalize(destination.name).contains("lillafured") ||
                NameNormalizer.normalize(destination.town).contains("lillafured")
            matches shouldBe true
        }
    }

    @Test
    fun `Given a limit - When searchDestinations - Then at most that many results are returned`() {
        val limit = 3

        val actual = repository.searchDestinations(query = "k", limit = limit)

        actual.size shouldBeLessThanOrEqual limit
    }

    @Test
    fun `When searchDestinations - Then results are sorted by popularity descending`() {
        val actual = repository.searchDestinations(query = "k", limit = 10)

        actual.map { it.popularity } shouldBe actual.map { it.popularity }.sortedDescending()
    }

    @Test
    fun `Given a query with no match - When searchDestinations - Then an empty list is returned`() {
        val actual = repository.searchDestinations(query = "zzqxnomatch", limit = 10)

        actual shouldBe emptyList()
    }

    @Test
    fun `Given a blank query - When searchDestinations - Then an empty list is returned`() {
        val actual = repository.searchDestinations(query = "   ", limit = 10)

        actual shouldBe emptyList()
    }

    @Test
    fun `When getLandscapes - Then all landscapes are returned and each has destinations`() {
        val actual = repository.getLandscapes()

        actual.shouldNotBeEmpty()
        actual.forEach { it.destinations.shouldNotBeEmpty() }
    }

    private companion object {
        data class CategoryTestCase(
            val name: String,
            val type: DestinationType,
            val excludedType: DestinationType,
        )

        fun categoryTestCases() =
            listOf(
                CategoryTestCase("Úrkúti Őskarszt", DestinationType.GEOLOGICAL_SITE, DestinationType.CAVE),
                CategoryTestCase("Körös-torok", DestinationType.RIVER, DestinationType.LAKE),
                CategoryTestCase("Vaskapu", DestinationType.ROCK, DestinationType.VALLEY),
                CategoryTestCase("Kőmorzsoló óriás", DestinationType.STATUE, DestinationType.ROCK),
            )

        const val SEED = 42L
        const val MIN_DISTINCT_TYPES = 6
        const val BUDAPEST_LATITUDE = 47.4979
        const val BUDAPEST_LONGITUDE = 19.0402
        val NORTH_POLE = Location(90.0, 0.0)
    }
}
