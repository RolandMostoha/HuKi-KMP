package hu.mostoha.mobile.kmp.huki.service

import hu.mostoha.mobile.kmp.huki.model.analytics.AnalyticsEvent
import hu.mostoha.mobile.kmp.huki.model.analytics.UserProperty

/**
 * Shared analytics interface, the concrete implementation is provided per platform.
 */
interface AnalyticsService {
    fun logEvent(event: AnalyticsEvent)

    fun setUserProperty(property: UserProperty)

    fun setUserId(userId: String?)
}
