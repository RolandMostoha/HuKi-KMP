package hu.mostoha.mobile.kmp.huki.features.trailsymbolsguide

import hu.mostoha.mobile.kmp.huki.model.analytics.AnalyticsEvent
import hu.mostoha.mobile.kmp.huki.model.analytics.Screen
import hu.mostoha.mobile.kmp.huki.service.FakeAnalyticsService
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class TrailSymbolsGuideViewModelTest {

    private val analyticsService = FakeAnalyticsService()

    @Test
    fun `Given view model - When screen viewed - Then trail symbols guide screen view is logged`() {
        val viewModel = TrailSymbolsGuideViewModel(analyticsService)

        viewModel.onEvent(TrailSymbolsGuideUiEvents.ScreenViewed)

        analyticsService.screenViews shouldBe listOf(AnalyticsEvent.ScreenView(Screen.TRAIL_SYMBOLS_GUIDE))
    }

    @Test
    fun `Given view model - When created - Then no screen view is logged`() {
        TrailSymbolsGuideViewModel(analyticsService)

        analyticsService.screenViews shouldBe emptyList()
    }
}
