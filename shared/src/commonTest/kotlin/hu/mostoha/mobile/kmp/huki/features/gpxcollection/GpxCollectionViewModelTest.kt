package hu.mostoha.mobile.kmp.huki.features.gpxcollection

import app.cash.turbine.test
import dev.mokkery.answering.returns
import dev.mokkery.answering.returnsBy
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import hu.mostoha.mobile.kmp.huki.model.analytics.AnalyticsEvent
import hu.mostoha.mobile.kmp.huki.model.analytics.GpxShareSource
import hu.mostoha.mobile.kmp.huki.model.analytics.Screen
import hu.mostoha.mobile.kmp.huki.model.domain.GpxFileHeader
import hu.mostoha.mobile.kmp.huki.model.domain.GpxFileItem
import hu.mostoha.mobile.kmp.huki.model.domain.GpxFileSection
import hu.mostoha.mobile.kmp.huki.model.domain.GpxOrigin
import hu.mostoha.mobile.kmp.huki.repository.GpxRepository
import hu.mostoha.mobile.kmp.huki.service.FakeAnalyticsService
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.maplibre.spatialk.units.extensions.kilometers
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class GpxCollectionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val gpxRepository = mock<GpxRepository>()
    private val analyticsService = FakeAnalyticsService()

    private val now = Clock.System.now()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): GpxCollectionViewModel {
        val viewModel = GpxCollectionViewModel(gpxRepository, analyticsService)
        testDispatcher.scheduler.runCurrent()
        return viewModel
    }

    private fun gpxFileItem(fileName: String, lastModified: Instant, lastOpened: Instant? = null): GpxFileItem =
        GpxFileItem(
            fileName = fileName,
            fileUri = "uri/$fileName",
            trackId = "track-$fileName",
            title = "Title of $fileName",
            totalDistance = 10.kilometers,
            travelTime = 2.hours,
            incline = 100,
            decline = 100,
            lastModified = lastModified,
            lastOpened = lastOpened,
            origin = GpxOrigin.EXTERNAL,
        )

    @Test
    fun `Given no gpx files, When view model init, Then uiState is empty and not loading`() {
        everySuspend { gpxRepository.getGpxFiles() } returns emptyList()

        runTest {
            val viewModel = createViewModel()

            val uiState = viewModel.uiState.value

            uiState.isLoading shouldBe false
            uiState.fileCount shouldBe 0
            uiState.sections shouldBe emptyList()
        }
    }

    @Test
    fun `Given gpx files from different days, When view model init, Then sections are grouped by date descending`() {
        val todayFile = gpxFileItem("today.gpx", now)
        val yesterdayFile = gpxFileItem("yesterday.gpx", now.minus(1.days))
        val olderFile = gpxFileItem("older.gpx", now.minus(5.days))

        everySuspend { gpxRepository.getGpxFiles() } returns listOf(olderFile, todayFile, yesterdayFile)

        runTest {
            val viewModel = createViewModel()

            val uiState = viewModel.uiState.value
            val olderLabel = olderFile.lastModified
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
                .toString()
                .replace('-', '.')

            uiState.fileCount shouldBe 3
            uiState.sections shouldBe listOf(
                GpxFileSection(GpxFileHeader.Today, listOf(todayFile)),
                GpxFileSection(GpxFileHeader.Yesterday, listOf(yesterdayFile)),
                GpxFileSection(GpxFileHeader.Date(olderLabel), listOf(olderFile)),
            )
        }
    }

    @Test
    fun `Given files on the same day, When view model init, Then files are sorted by last modified descending`() {
        val earlier = gpxFileItem("earlier.gpx", now.minus(3.hours))
        val later = gpxFileItem("later.gpx", now)

        everySuspend { gpxRepository.getGpxFiles() } returns listOf(earlier, later)

        runTest {
            val viewModel = createViewModel()

            val todaySection = viewModel.uiState.value.sections.single()

            todaySection.header shouldBe GpxFileHeader.Today
            todaySection.files shouldBe listOf(later, earlier)
        }
    }

    @Test
    fun `Given a file, When DeleteClicked event, Then pendingDelete is set`() {
        val file = gpxFileItem("today.gpx", now)
        everySuspend { gpxRepository.getGpxFiles() } returns listOf(file)

        runTest {
            val viewModel = createViewModel()

            viewModel.onEvent(GpxCollectionUiEvents.DeleteClicked(file))

            viewModel.uiState.value.pendingDelete shouldBe file
        }
    }

    @Test
    fun `Given pending delete, When DeleteDismissed event, Then pendingDelete is cleared`() {
        val file = gpxFileItem("today.gpx", now)
        everySuspend { gpxRepository.getGpxFiles() } returns listOf(file)

        runTest {
            val viewModel = createViewModel()
            viewModel.onEvent(GpxCollectionUiEvents.DeleteClicked(file))

            viewModel.onEvent(GpxCollectionUiEvents.DeleteDismissed)

            viewModel.uiState.value.pendingDelete shouldBe null
        }
    }

    @Test
    fun `Given pending delete, When DeleteConfirmed event, Then file is deleted and list refreshes`() {
        val file = gpxFileItem("today.gpx", now)
        var files = listOf(file)
        everySuspend { gpxRepository.getGpxFiles() } returnsBy { files }
        everySuspend { gpxRepository.deleteGpxFile("uri/today.gpx", "track-today.gpx") } returns Unit

        runTest {
            val viewModel = createViewModel()
            viewModel.onEvent(GpxCollectionUiEvents.DeleteClicked(file))
            files = emptyList()

            viewModel.onEvent(GpxCollectionUiEvents.DeleteConfirmed)
            testDispatcher.scheduler.advanceUntilIdle()

            // Keyed on the uri and trackId, since a file name can repeat across origins.
            verifySuspend { gpxRepository.deleteGpxFile("uri/today.gpx", "track-today.gpx") }
            analyticsService.loggedEvents shouldBe listOf(AnalyticsEvent.GpxDeleted)
            val uiState = viewModel.uiState.value
            uiState.pendingDelete shouldBe null
            uiState.fileCount shouldBe 0
            uiState.sections shouldBe emptyList()
        }
    }

    @Test
    fun `Given default state, When BackClicked event, Then NavigateBack effect is emitted`() {
        everySuspend { gpxRepository.getGpxFiles() } returns emptyList()

        runTest {
            val viewModel = createViewModel()

            viewModel.uiEffects.test {
                viewModel.onEvent(GpxCollectionUiEvents.BackClicked)

                awaitItem() shouldBe GpxCollectionUiEffects.NavigateBack
            }
        }
    }

    @Test
    fun `Given a file, When FileClicked event, Then OpenGpx effect with file uri is emitted`() {
        val file = gpxFileItem("today.gpx", now)
        everySuspend { gpxRepository.getGpxFiles() } returns listOf(file)

        runTest {
            val viewModel = createViewModel()

            viewModel.uiEffects.test {
                viewModel.onEvent(GpxCollectionUiEvents.FileClicked(file))

                awaitItem() shouldBe GpxCollectionUiEffects.OpenGpx(file.fileUri)
            }
        }
    }

    @Test
    fun `Given a file, When ShareClicked event, Then ShareGpx effect is emitted and analytics is logged`() {
        val file = gpxFileItem("today.gpx", now)
        everySuspend { gpxRepository.getGpxFiles() } returns listOf(file)

        runTest {
            val viewModel = createViewModel()

            viewModel.uiEffects.test {
                viewModel.onEvent(GpxCollectionUiEvents.ShareClicked(file))

                awaitItem() shouldBe GpxCollectionUiEffects.ShareGpx(
                    fileUri = file.fileUri,
                    fileName = file.fileName,
                )
                analyticsService.loggedEvents shouldBe listOf(
                    AnalyticsEvent.GpxShared(GpxShareSource.COLLECTION),
                )
            }
        }
    }

    @Test
    fun `Given default state, When HelpClicked event, Then NavigateToTutorial effect is emitted`() {
        everySuspend { gpxRepository.getGpxFiles() } returns emptyList()

        runTest {
            val viewModel = createViewModel()

            viewModel.uiEffects.test {
                viewModel.onEvent(GpxCollectionUiEvents.HelpClicked)

                awaitItem() shouldBe GpxCollectionUiEffects.NavigateToTutorial
                analyticsService.loggedEvents shouldBe emptyList()
            }
        }
    }

    @Test
    fun `Given view model init, When created, Then gpx history screen view is logged`() {
        everySuspend { gpxRepository.getGpxFiles() } returns emptyList()

        runTest {
            createViewModel()

            analyticsService.screenViews shouldBe listOf(AnalyticsEvent.ScreenView(Screen.GPX_HISTORY))
        }
    }
}
