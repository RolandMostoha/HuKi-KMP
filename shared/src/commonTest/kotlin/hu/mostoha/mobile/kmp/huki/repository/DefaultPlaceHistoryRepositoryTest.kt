package hu.mostoha.mobile.kmp.huki.repository

import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.database.PlaceHistoryDao
import hu.mostoha.mobile.kmp.huki.model.db.PlaceHistoryEntity
import hu.mostoha.mobile.kmp.huki.model.domain.Destination
import hu.mostoha.mobile.kmp.huki.model.domain.DestinationType
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.OsmType
import hu.mostoha.mobile.kmp.huki.model.domain.Place
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceCategory
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceHistoryItem
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceSource
import hu.mostoha.mobile.kmp.huki.model.mapper.toPlace
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.Instant

class DefaultPlaceHistoryRepositoryTest {

    private val dao = mock<PlaceHistoryDao>(MockMode.autoUnit)
    private val fixedClock = object : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(VISITED_AT)
    }
    private val repository = DefaultPlaceHistoryRepository(dao, fixedClock)

    @Test
    fun `Given place, When recordVisit, Then dao upsert is called with the mapped entity`() {
        runTest {
            val place = Place(
                osmId = "123",
                location = Location(47.7181, 18.8948),
                name = "Dobogókő",
                placeSource = PlaceSource.SEARCH_AUTOCOMPLETE,
                osmType = OsmType.NODE,
            )

            repository.recordVisit(place)

            verifySuspend {
                dao.upsert(
                    PlaceHistoryEntity(
                        osmType = OsmType.NODE,
                        osmId = "123",
                        name = "Dobogókő",
                        nameNormalized = "dobogoko",
                        address = null,
                        latitude = 47.7181,
                        longitude = 18.8948,
                        placeCategory = null,
                        placeSource = PlaceSource.SEARCH_AUTOCOMPLETE,
                        boundingBox = null,
                        lastVisited = VISITED_AT,
                    ),
                )
            }
        }
    }

    @Test
    fun `Given destination place, When recordVisit, Then dao upsert is called with the mapped entity`() {
        runTest {
            val destination = Destination(
                osmId = "456",
                name = "Kékestető",
                town = "Mátraszentimre",
                type = DestinationType.HIGHEST_PEAK,
                location = Location(47.8721, 20.0102),
                description = SharedRes.strings.destinations_type_peak,
                popularity = 10,
            )

            repository.recordVisit(destination.toPlace())

            verifySuspend {
                dao.upsert(
                    PlaceHistoryEntity(
                        osmType = OsmType.NODE,
                        osmId = "456",
                        name = "Kékestető",
                        nameNormalized = "kekesteto",
                        address = "Mátraszentimre",
                        latitude = 47.8721,
                        longitude = 20.0102,
                        placeCategory = PlaceCategory.PEAK,
                        placeSource = PlaceSource.DESTINATIONS,
                        boundingBox = null,
                        lastVisited = VISITED_AT,
                    ),
                )
            }
        }
    }

    @Test
    fun `Given stored entities, When getRecentPlaces, Then dao recent entities are mapped to places`() {
        runTest {
            val entity = PlaceHistoryEntity(
                osmType = OsmType.WAY,
                osmId = "789",
                name = "Visegrádi vár",
                nameNormalized = "visegradi var",
                address = "Visegrád",
                latitude = 47.79,
                longitude = 18.97,
                placeCategory = PlaceCategory.CASTLE,
                placeSource = PlaceSource.SEARCH_AUTOCOMPLETE,
                boundingBox = null,
                lastVisited = VISITED_AT,
            )
            everySuspend { dao.getRecent(3) } returns listOf(entity)

            val recentPlaces = repository.getRecentPlaces(3)

            recentPlaces shouldBe listOf(
                Place(
                    osmId = "789",
                    location = Location(47.79, 18.97),
                    name = "Visegrádi vár",
                    placeSource = PlaceSource.SEARCH_AUTOCOMPLETE,
                    address = "Visegrád",
                    placeCategory = PlaceCategory.CASTLE,
                    osmType = OsmType.WAY,
                    boundingBox = null,
                ),
            )
        }
    }

    @Test
    fun `Given stored entities, When getPlaceHistory, Then dao entities are mapped to history items`() {
        runTest {
            val entity = PlaceHistoryEntity(
                osmType = OsmType.WAY,
                osmId = "789",
                name = "Visegrádi vár",
                nameNormalized = "visegradi var",
                address = "Visegrád",
                latitude = 47.79,
                longitude = 18.97,
                placeCategory = PlaceCategory.CASTLE,
                placeSource = PlaceSource.SEARCH_AUTOCOMPLETE,
                boundingBox = null,
                lastVisited = VISITED_AT,
            )
            everySuspend { dao.getAll() } returns listOf(entity)

            val history = repository.getPlaceHistory()

            history shouldBe listOf(
                PlaceHistoryItem(
                    place = Place(
                        osmId = "789",
                        location = Location(47.79, 18.97),
                        name = "Visegrádi vár",
                        placeSource = PlaceSource.SEARCH_AUTOCOMPLETE,
                        address = "Visegrád",
                        placeCategory = PlaceCategory.CASTLE,
                        osmType = OsmType.WAY,
                        boundingBox = null,
                    ),
                    lastVisited = Instant.fromEpochMilliseconds(VISITED_AT),
                ),
            )
        }
    }

    @Test
    fun `Given a stored entity, When getPlace by key, Then the mapped place is returned`() {
        runTest {
            val entity = PlaceHistoryEntity(
                osmType = OsmType.NODE,
                osmId = "123",
                name = "Dobogókő",
                nameNormalized = "dobogoko",
                address = null,
                latitude = 47.7181,
                longitude = 18.8948,
                placeCategory = PlaceCategory.PEAK,
                placeSource = PlaceSource.SEARCH_AUTOCOMPLETE,
                boundingBox = null,
                lastVisited = VISITED_AT,
            )
            everySuspend { dao.getByKey(OsmType.NODE, "123") } returns entity

            val place = repository.getPlace(OsmType.NODE, "123")

            place shouldBe Place(
                osmId = "123",
                location = Location(47.7181, 18.8948),
                name = "Dobogókő",
                placeSource = PlaceSource.SEARCH_AUTOCOMPLETE,
                address = null,
                placeCategory = PlaceCategory.PEAK,
                osmType = OsmType.NODE,
                boundingBox = null,
            )
        }
    }

    @Test
    fun `Given a query, When searchPlaces, Then dao is queried with the normalized query and entities are mapped`() {
        runTest {
            val entity = PlaceHistoryEntity(
                osmType = OsmType.NODE,
                osmId = "123",
                name = "Dobogókő",
                nameNormalized = "dobogoko",
                address = null,
                latitude = 47.7181,
                longitude = 18.8948,
                placeCategory = PlaceCategory.PEAK,
                placeSource = PlaceSource.SEARCH_AUTOCOMPLETE,
                boundingBox = null,
                lastVisited = VISITED_AT,
            )
            everySuspend { dao.searchByName("dobogo", 5) } returns listOf(entity)

            val actual = repository.searchPlaces("DOBOGÓ", 5)

            actual shouldBe listOf(
                Place(
                    osmId = "123",
                    location = Location(47.7181, 18.8948),
                    name = "Dobogókő",
                    placeSource = PlaceSource.SEARCH_AUTOCOMPLETE,
                    address = null,
                    placeCategory = PlaceCategory.PEAK,
                    osmType = OsmType.NODE,
                    boundingBox = null,
                ),
            )
        }
    }

    @Test
    fun `Given a blank query, When searchPlaces, Then an empty list is returned`() {
        runTest {
            val actual = repository.searchPlaces("   ", 5)

            actual shouldBe emptyList()
        }
    }

    @Test
    fun `Given a query with LIKE wildcards, When searchPlaces, Then the wildcards are escaped`() {
        runTest {
            everySuspend { dao.searchByName(any(), any()) } returns emptyList()

            repository.searchPlaces("a%b_c", 5)

            verifySuspend { dao.searchByName("a\\%b\\_c", 5) }
        }
    }

    @Test
    fun `Given no stored entity, When getPlace by key, Then null is returned`() {
        runTest {
            everySuspend { dao.getByKey(OsmType.NODE, "missing") } returns null

            val place = repository.getPlace(OsmType.NODE, "missing")

            place shouldBe null
        }
    }

    companion object {
        private const val VISITED_AT = 1_700_000_000_000
    }
}
