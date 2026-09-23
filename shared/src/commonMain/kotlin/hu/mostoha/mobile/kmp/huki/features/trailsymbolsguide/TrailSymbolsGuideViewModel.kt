package hu.mostoha.mobile.kmp.huki.features.trailsymbolsguide

import androidx.lifecycle.ViewModel
import co.touchlab.kermit.Logger
import hu.mostoha.mobile.kmp.huki.model.analytics.Screen
import hu.mostoha.mobile.kmp.huki.service.AnalyticsService
import hu.mostoha.mobile.kmp.huki.service.logScreenView

class TrailSymbolsGuideViewModel(private val analyticsService: AnalyticsService) : ViewModel() {
    fun onEvent(event: TrailSymbolsGuideUiEvents) {
        Logger.d { "TrailSymbolsGuideEvent: $event" }
        when (event) {
            TrailSymbolsGuideUiEvents.ScreenViewed -> analyticsService.logScreenView(Screen.TRAIL_SYMBOLS_GUIDE)
        }
    }
}
