package hu.mostoha.mobile.kmp.huki.features.locationiq

import hu.mostoha.mobile.kmp.huki.model.analytics.AnalyticsEvent
import hu.mostoha.mobile.kmp.huki.model.analytics.Screen
import hu.mostoha.mobile.kmp.huki.service.FakeAnalyticsService
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class LocationIqViewModelTest {

    private val analyticsService = FakeAnalyticsService()

    @Test
    fun `Given view model - When screen viewed - Then location iq screen view is logged`() {
        val viewModel = LocationIqViewModel(analyticsService)

        viewModel.onEvent(LocationIqUiEvents.ScreenViewed)

        analyticsService.screenViews shouldBe listOf(AnalyticsEvent.ScreenView(Screen.LOCATION_IQ))
    }

    @Test
    fun `Given view model - When created - Then no screen view is logged`() {
        LocationIqViewModel(analyticsService)

        analyticsService.screenViews shouldBe emptyList()
    }
}
