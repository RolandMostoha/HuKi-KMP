package hu.mostoha.mobile.kmp.huki.features.gpxguide

import androidx.lifecycle.ViewModel
import co.touchlab.kermit.Logger
import hu.mostoha.mobile.kmp.huki.model.analytics.Screen
import hu.mostoha.mobile.kmp.huki.service.AnalyticsService
import hu.mostoha.mobile.kmp.huki.service.logScreenView

class GpxGuideViewModel(private val analyticsService: AnalyticsService) : ViewModel() {
    fun onEvent(event: GpxGuideUiEvents) {
        Logger.d { "GpxGuideEvent: $event" }
        when (event) {
            GpxGuideUiEvents.ScreenViewed -> analyticsService.logScreenView(Screen.GPX_GUIDE)
        }
    }
}
