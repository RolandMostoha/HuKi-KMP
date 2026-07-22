package hu.mostoha.mobile.kmp.huki.features.locationiq

import hu.mostoha.mobile.kmp.huki.model.analytics.AnalyticsEvent
import hu.mostoha.mobile.kmp.huki.model.analytics.Screen
import hu.mostoha.mobile.kmp.huki.service.FakeAnalyticsService
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class LocationIqViewModelTest {

    private val analyticsService = FakeAnalyticsService()

    @Test
    fun `Given view model init, When created, Then location iq screen view is logged`() {
        LocationIqViewModel(analyticsService)

        analyticsService.screenViews shouldBe listOf(AnalyticsEvent.ScreenView(Screen.LOCATION_IQ))
    }
}
