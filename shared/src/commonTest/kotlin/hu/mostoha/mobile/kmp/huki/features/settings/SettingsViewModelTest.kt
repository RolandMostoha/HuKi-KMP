package hu.mostoha.mobile.kmp.huki.features.settings

import app.cash.turbine.test
import hu.mostoha.mobile.huki.shared.SharedRes
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var settingsViewModel: SettingsViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        settingsViewModel = SettingsViewModel()
        testDispatcher.scheduler.runCurrent()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Given view model init, When observed, Then uiState is default`() {
        runTest {
            settingsViewModel.uiState.value shouldBe SettingsUiState.Default
        }
    }

    @Test
    fun `Given default state, When BackClicked event, Then NavigateBack effect is emitted`() {
        runTest {
            settingsViewModel.settingsUiEffects.test {
                settingsViewModel.onEvent(SettingsUiEvents.BackClicked)

                val actual = awaitItem()

                actual shouldBe SettingsUiEffects.NavigateBack
            }
        }
    }

    @Test
    fun `Given default state, When EmailClicked event, Then SendEmail effect with email and subject is emitted`() {
        runTest {
            settingsViewModel.settingsUiEffects.test {
                settingsViewModel.onEvent(SettingsUiEvents.EmailClicked)

                val actual = awaitItem()

                actual shouldBe SettingsUiEffects.SendEmail(
                    emailRes = SharedRes.strings.settings_contact_email,
                    subjectRes = SharedRes.strings.settings_email_subject,
                )
            }
        }
    }

    @Test
    fun `Given default state, When FacebookClicked event, Then OpenUrl effect with Facebook url is emitted`() {
        runTest {
            settingsViewModel.settingsUiEffects.test {
                settingsViewModel.onEvent(SettingsUiEvents.FacebookClicked)

                val actual = awaitItem()

                actual shouldBe SettingsUiEffects.OpenUrl(SharedRes.strings.settings_facebook_url)
            }
        }
    }

    @Test
    fun `Given default state, When GithubClicked event, Then OpenUrl effect with GitHub url is emitted`() {
        runTest {
            settingsViewModel.settingsUiEffects.test {
                settingsViewModel.onEvent(SettingsUiEvents.GithubClicked)

                val actual = awaitItem()

                actual shouldBe SettingsUiEffects.OpenUrl(SharedRes.strings.settings_github_url)
            }
        }
    }

    @Test
    fun `Given default state, When LocationIqClicked event, Then NavigateToLocationIq effect is emitted`() {
        runTest {
            settingsViewModel.settingsUiEffects.test {
                settingsViewModel.onEvent(SettingsUiEvents.LocationIqClicked)

                val actual = awaitItem()

                actual shouldBe SettingsUiEffects.NavigateToLocationIq
            }
        }
    }
}
