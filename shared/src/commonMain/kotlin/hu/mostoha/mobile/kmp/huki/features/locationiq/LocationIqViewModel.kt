package hu.mostoha.mobile.kmp.huki.features.locationiq

import androidx.lifecycle.ViewModel
import hu.mostoha.mobile.kmp.huki.model.analytics.AnalyticsEvent
import hu.mostoha.mobile.kmp.huki.model.analytics.Screen
import hu.mostoha.mobile.kmp.huki.service.AnalyticsService

class LocationIqViewModel(analyticsService: AnalyticsService) : ViewModel() {
    init {
        analyticsService.logEvent(AnalyticsEvent.ScreenView(Screen.LOCATION_IQ))
    }
}
