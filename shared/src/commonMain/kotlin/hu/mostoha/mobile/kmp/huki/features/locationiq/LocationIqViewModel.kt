package hu.mostoha.mobile.kmp.huki.features.locationiq

import androidx.lifecycle.ViewModel
import co.touchlab.kermit.Logger
import hu.mostoha.mobile.kmp.huki.model.analytics.Screen
import hu.mostoha.mobile.kmp.huki.service.AnalyticsService
import hu.mostoha.mobile.kmp.huki.service.logScreenView

class LocationIqViewModel(private val analyticsService: AnalyticsService) : ViewModel() {
    fun onEvent(event: LocationIqUiEvents) {
        Logger.d { "LocationIqEvent: $event" }
        when (event) {
            LocationIqUiEvents.ScreenViewed -> analyticsService.logScreenView(Screen.LOCATION_IQ)
        }
    }
}
