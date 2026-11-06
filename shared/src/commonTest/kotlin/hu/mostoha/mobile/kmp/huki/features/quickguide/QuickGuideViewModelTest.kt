package hu.mostoha.mobile.kmp.huki.features.quickguide

import hu.mostoha.mobile.kmp.huki.model.analytics.AnalyticsEvent
import hu.mostoha.mobile.kmp.huki.model.analytics.Screen
import hu.mostoha.mobile.kmp.huki.service.FakeAnalyticsService
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class QuickGuideViewModelTest {

    private val analyticsService = FakeAnalyticsService()

    @Test
    fun `Given view model - When screen viewed - Then quick guide screen view is logged`() {
        val viewModel = QuickGuideViewModel(analyticsService)

        viewModel.onEvent(QuickGuideUiEvents.ScreenViewed)

        analyticsService.screenViews shouldBe listOf(AnalyticsEvent.ScreenView(Screen.QUICK_GUIDE))
    }

    @Test
    fun `Given view model - When created - Then no screen view is logged`() {
        QuickGuideViewModel(analyticsService)

        analyticsService.screenViews shouldBe emptyList()
    }
}
