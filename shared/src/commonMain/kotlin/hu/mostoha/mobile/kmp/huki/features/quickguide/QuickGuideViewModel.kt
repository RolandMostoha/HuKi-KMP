package hu.mostoha.mobile.kmp.huki.features.quickguide

import androidx.lifecycle.ViewModel
import co.touchlab.kermit.Logger
import hu.mostoha.mobile.kmp.huki.model.analytics.Screen
import hu.mostoha.mobile.kmp.huki.service.AnalyticsService
import hu.mostoha.mobile.kmp.huki.service.logScreenView

class QuickGuideViewModel(private val analyticsService: AnalyticsService) : ViewModel() {
    fun onEvent(event: QuickGuideUiEvents) {
        Logger.d { "QuickGuideEvent: $event" }
        when (event) {
            QuickGuideUiEvents.ScreenViewed -> analyticsService.logScreenView(Screen.QUICK_GUIDE)
        }
    }
}
