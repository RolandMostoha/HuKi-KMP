package hu.mostoha.mobile.kmp.huki.service

import hu.mostoha.mobile.kmp.huki.model.analytics.AnalyticsEvent
import hu.mostoha.mobile.kmp.huki.model.analytics.UserProperty

/**
 * [AnalyticsService] decorator that mirrors every [AnalyticsEvent.ScreenView] into Crashlytics as a
 * breadcrumb and a custom key attached to any crash report.
 */
class CrashlyticsDecoratorService(
    private val delegate: AnalyticsService,
    private val crashlyticsService: CrashlyticsService,
) : AnalyticsService {
    override fun logEvent(event: AnalyticsEvent) {
        if (event is AnalyticsEvent.ScreenView) {
            val screen = event.screen.value
            crashlyticsService.log("screen: $screen")
            crashlyticsService.setCustomKey(CrashlyticsService.CRASH_KEY_LAST_SCREEN, screen)
        }
        delegate.logEvent(event)
    }

    override fun setUserProperty(property: UserProperty) = delegate.setUserProperty(property)

    override fun setUserId(userId: String?) = delegate.setUserId(userId)
}
