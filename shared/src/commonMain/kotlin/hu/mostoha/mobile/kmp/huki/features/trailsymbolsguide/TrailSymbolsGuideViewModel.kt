package hu.mostoha.mobile.kmp.huki.features.trailsymbolsguide

import androidx.lifecycle.ViewModel
import hu.mostoha.mobile.kmp.huki.model.analytics.AnalyticsEvent
import hu.mostoha.mobile.kmp.huki.model.analytics.Screen
import hu.mostoha.mobile.kmp.huki.service.AnalyticsService

class TrailSymbolsGuideViewModel(analyticsService: AnalyticsService) : ViewModel() {
    init {
        analyticsService.logEvent(AnalyticsEvent.ScreenView(Screen.TRAIL_SYMBOLS_GUIDE))
    }
}
