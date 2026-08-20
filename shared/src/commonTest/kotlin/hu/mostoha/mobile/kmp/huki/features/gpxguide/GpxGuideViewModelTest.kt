package hu.mostoha.mobile.kmp.huki.features.gpxguide

import hu.mostoha.mobile.kmp.huki.model.analytics.AnalyticsEvent
import hu.mostoha.mobile.kmp.huki.model.analytics.Screen
import hu.mostoha.mobile.kmp.huki.service.FakeAnalyticsService
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class GpxGuideViewModelTest {

    private val analyticsService = FakeAnalyticsService()

    @Test
    fun `Given view model init - When created - Then gpx tutorial screen view is logged`() {
        GpxGuideViewModel(analyticsService)

        analyticsService.screenViews shouldBe listOf(AnalyticsEvent.ScreenView(Screen.GPX_GUIDE))
    }
}
