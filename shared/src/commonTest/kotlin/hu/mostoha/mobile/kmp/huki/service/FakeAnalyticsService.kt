package hu.mostoha.mobile.kmp.huki.service

import hu.mostoha.mobile.kmp.huki.model.analytics.AnalyticsEvent
import hu.mostoha.mobile.kmp.huki.model.analytics.UserProperty

class FakeAnalyticsService : AnalyticsService {
    val loggedEvents = mutableListOf<AnalyticsEvent>()
    val screenViews = mutableListOf<AnalyticsEvent.ScreenView>()
    val userProperties = mutableListOf<UserProperty>()
    var userId: String? = null
        private set

    override fun logEvent(event: AnalyticsEvent) {
        when (event) {
            is AnalyticsEvent.ScreenView -> screenViews += event
            else -> loggedEvents += event
        }
    }

    override fun setUserProperty(property: UserProperty) {
        userProperties += property
    }

    override fun setUserId(userId: String?) {
        this.userId = userId
    }
}
