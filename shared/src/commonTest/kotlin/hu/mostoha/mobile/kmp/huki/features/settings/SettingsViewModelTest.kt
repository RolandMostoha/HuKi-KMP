package hu.mostoha.mobile.kmp.huki.features.settings

import app.cash.turbine.test
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import hu.mostoha.mobile.kmp.huki.model.analytics.AnalyticsEvent
import hu.mostoha.mobile.kmp.huki.model.analytics.Screen
import hu.mostoha.mobile.kmp.huki.model.domain.UserPreferences
import hu.mostoha.mobile.kmp.huki.repository.SettingsRepository
import hu.mostoha.mobile.kmp.huki.service.FakeAnalyticsService
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val settingsRepository = mock<SettingsRepository>(MockMode.autoUnit) {
        every { settings } returns flowOf(UserPreferences.DEFAULTS)
    }
    private val analyticsService = FakeAnalyticsService()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = SettingsViewModel(settingsRepository, analyticsService)

    @Test
    fun `Given default preferences - When observed - Then uiState is default`() {
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.uiState.value shouldBe SettingsUiState.Default
        }
    }

    @Test
    fun `Given stored map zoom controls flag - When observed - Then uiState reflects the flag`() {
        runTest {
            every { settingsRepository.settings } returns
                flowOf(UserPreferences.DEFAULTS.copy(mapZoomControlsVisible = true))
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.uiState.value.mapZoomControlsVisible shouldBe true
        }
    }

    @Test
    fun `Given default state - When MapZoomControlsToggled event - Then repository stores the flag`() {
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(SettingsUiEvents.MapZoomControlsToggled(visible = true))
            advanceUntilIdle()

            verifySuspend { settingsRepository.setMapZoomControlsVisible(true) }
            analyticsService.loggedEvents shouldBe listOf(AnalyticsEvent.SettingsZoomControlsToggled)
        }
    }

    @Test
    fun `Given default state - When BackClicked event - Then NavigateBack effect is emitted`() {
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.uiEffects.test {
                viewModel.onEvent(SettingsUiEvents.BackClicked)

                val actual = awaitItem()

                actual shouldBe SettingsUiEffects.NavigateBack
            }
        }
    }

    @Test
    fun `Given view model init - When created - Then settings screen view is logged`() {
        runTest {
            createViewModel()
            advanceUntilIdle()

            analyticsService.screenViews shouldBe listOf(AnalyticsEvent.ScreenView(Screen.SETTINGS))
        }
    }
}
