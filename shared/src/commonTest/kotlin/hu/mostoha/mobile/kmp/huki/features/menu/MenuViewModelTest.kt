package hu.mostoha.mobile.kmp.huki.features.menu

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
class MenuViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var menuViewModel: MenuViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        menuViewModel = MenuViewModel()
        testDispatcher.scheduler.runCurrent()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Given view model init, When observed, Then uiState is default`() {
        runTest {
            menuViewModel.uiState.value shouldBe MenuUiState.Default
        }
    }

    @Test
    fun `Given default state, When BackClicked event, Then NavigateBack effect is emitted`() {
        runTest {
            menuViewModel.menuUiEffects.test {
                menuViewModel.onEvent(MenuUiEvents.BackClicked)

                val actual = awaitItem()

                actual shouldBe MenuUiEffects.NavigateBack
            }
        }
    }

    @Test
    fun `Given default state, When PlaceHistoryClicked event, Then NavigateToPlaceHistory effect is emitted`() {
        runTest {
            menuViewModel.menuUiEffects.test {
                menuViewModel.onEvent(MenuUiEvents.PlaceHistoryClicked)

                val actual = awaitItem()

                actual shouldBe MenuUiEffects.NavigateToPlaceHistory
            }
        }
    }

    @Test
    fun `Given default state, When GpxCollectionClicked event, Then NavigateToGpxCollection effect is emitted`() {
        runTest {
            menuViewModel.menuUiEffects.test {
                menuViewModel.onEvent(MenuUiEvents.GpxCollectionClicked)

                val actual = awaitItem()

                actual shouldBe MenuUiEffects.NavigateToGpxCollection
            }
        }
    }

    @Test
    fun `Given default state, When EmailClicked event, Then SendEmail effect with email and subject is emitted`() {
        runTest {
            menuViewModel.menuUiEffects.test {
                menuViewModel.onEvent(MenuUiEvents.EmailClicked)

                val actual = awaitItem()

                actual shouldBe MenuUiEffects.SendEmail(
                    emailRes = SharedRes.strings.menu_contact_email,
                    subjectRes = SharedRes.strings.menu_email_subject,
                )
            }
        }
    }

    @Test
    fun `Given default state, When FacebookClicked event, Then OpenUrl effect with Facebook url is emitted`() {
        runTest {
            menuViewModel.menuUiEffects.test {
                menuViewModel.onEvent(MenuUiEvents.FacebookClicked)

                val actual = awaitItem()

                actual shouldBe MenuUiEffects.OpenUrl(SharedRes.strings.menu_facebook_url)
            }
        }
    }

    @Test
    fun `Given default state, When GithubClicked event, Then OpenUrl effect with GitHub url is emitted`() {
        runTest {
            menuViewModel.menuUiEffects.test {
                menuViewModel.onEvent(MenuUiEvents.GithubClicked)

                val actual = awaitItem()

                actual shouldBe MenuUiEffects.OpenUrl(SharedRes.strings.menu_github_url)
            }
        }
    }

    @Test
    fun `Given default state, When LocationIqClicked event, Then NavigateToLocationIq effect is emitted`() {
        runTest {
            menuViewModel.menuUiEffects.test {
                menuViewModel.onEvent(MenuUiEvents.LocationIqClicked)

                val actual = awaitItem()

                actual shouldBe MenuUiEffects.NavigateToLocationIq
            }
        }
    }
}
