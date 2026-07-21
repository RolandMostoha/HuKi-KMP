package hu.mostoha.mobile.kmp.huki.features.placehistory

import app.cash.turbine.test
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import hu.mostoha.mobile.kmp.huki.model.analytics.AnalyticsEvent
import hu.mostoha.mobile.kmp.huki.model.analytics.Screen
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.OsmType
import hu.mostoha.mobile.kmp.huki.model.domain.Place
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceCategory
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceHistoryHeader
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceHistoryItem
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceHistorySection
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceSource
import hu.mostoha.mobile.kmp.huki.repository.PlaceHistoryRepository
import hu.mostoha.mobile.kmp.huki.service.FakeAnalyticsService
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class PlaceHistoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val placeHistoryRepository = mock<PlaceHistoryRepository>()
    private val analyticsService = FakeAnalyticsService()

    private val timeZone = TimeZone.currentSystemDefault()
    private val now = Clock.System.now()
        .toLocalDateTime(timeZone).date
        .atTime(12, 0)
        .toInstant(timeZone)
    private val clock = object : Clock {
        override fun now(): Instant = now
    }

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): PlaceHistoryViewModel {
        val viewModel = PlaceHistoryViewModel(placeHistoryRepository, clock, analyticsService)
        testDispatcher.scheduler.runCurrent()
        return viewModel
    }

    private fun placeHistoryItem(osmId: String, lastVisited: Instant): PlaceHistoryItem =
        PlaceHistoryItem(
            place = Place(
                osmId = osmId,
                location = Location(47.0, 18.0),
                name = "Place $osmId",
                placeSource = PlaceSource.SEARCH_AUTOCOMPLETE,
                address = "Address $osmId",
                placeCategory = PlaceCategory.PEAK,
                osmType = OsmType.NODE,
            ),
            lastVisited = lastVisited,
        )

    @Test
    fun `Given no places, When view model init, Then uiState is empty and not loading`() {
        everySuspend { placeHistoryRepository.getPlaceHistory() } returns emptyList()

        runTest {
            val viewModel = createViewModel()

            val uiState = viewModel.uiState.value

            uiState.isLoading shouldBe false
            uiState.placeCount shouldBe 0
            uiState.sections shouldBe emptyList()
        }
    }

    @Test
    fun `Given places from different days, When view model init, Then sections are grouped by date descending`() {
        val todayItem = placeHistoryItem("1", now)
        val yesterdayItem = placeHistoryItem("2", now.minus(1.days))
        val olderItem = placeHistoryItem("3", now.minus(5.days))

        everySuspend { placeHistoryRepository.getPlaceHistory() } returns
            listOf(olderItem, todayItem, yesterdayItem)

        runTest {
            val viewModel = createViewModel()

            val uiState = viewModel.uiState.value
            val olderLabel = olderItem.lastVisited
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
                .toString()
                .replace('-', '.')

            uiState.placeCount shouldBe 3
            uiState.sections shouldBe listOf(
                PlaceHistorySection(PlaceHistoryHeader.Today, listOf(todayItem)),
                PlaceHistorySection(PlaceHistoryHeader.Yesterday, listOf(yesterdayItem)),
                PlaceHistorySection(PlaceHistoryHeader.Date(olderLabel), listOf(olderItem)),
            )
        }
    }

    @Test
    fun `Given items on the same day, When view model init, Then items are sorted by last visited descending`() {
        val earlier = placeHistoryItem("1", now.minus(3.hours))
        val later = placeHistoryItem("2", now)

        everySuspend { placeHistoryRepository.getPlaceHistory() } returns listOf(earlier, later)

        runTest {
            val viewModel = createViewModel()

            val todaySection = viewModel.uiState.value.sections.single()

            todaySection.header shouldBe PlaceHistoryHeader.Today
            todaySection.items shouldBe listOf(later, earlier)
        }
    }

    @Test
    fun `Given default state, When BackClicked event, Then NavigateBack effect is emitted`() {
        everySuspend { placeHistoryRepository.getPlaceHistory() } returns emptyList()

        runTest {
            val viewModel = createViewModel()

            viewModel.uiEffects.test {
                viewModel.onEvent(PlaceHistoryUiEvents.BackClicked)

                awaitItem() shouldBe PlaceHistoryUiEffects.NavigateBack
            }
        }
    }

    @Test
    fun `Given a place, When PlaceClicked event, Then OpenPlace effect with osm key is emitted`() {
        val item = placeHistoryItem("123", now)
        everySuspend { placeHistoryRepository.getPlaceHistory() } returns listOf(item)

        runTest {
            val viewModel = createViewModel()

            viewModel.uiEffects.test {
                viewModel.onEvent(PlaceHistoryUiEvents.PlaceClicked(item.place))

                awaitItem() shouldBe PlaceHistoryUiEffects.OpenPlace(OsmType.NODE, "123")
            }
        }
    }

    @Test
    fun `Given view model init, When created, Then place history screen view is logged`() {
        everySuspend { placeHistoryRepository.getPlaceHistory() } returns emptyList()

        runTest {
            createViewModel()

            analyticsService.screenViews shouldBe listOf(AnalyticsEvent.ScreenView(Screen.PLACE_HISTORY))
        }
    }
}
