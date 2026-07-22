package hu.mostoha.mobile.kmp.huki.analytics

import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import hu.mostoha.mobile.android.huki.BuildConfig
import hu.mostoha.mobile.kmp.huki.model.analytics.AnalyticsEvent
import hu.mostoha.mobile.kmp.huki.model.analytics.UserProperty
import hu.mostoha.mobile.kmp.huki.service.AnalyticsService
import hu.mostoha.mobile.kmp.huki.util.FeatureFlags

class AndroidAnalyticsService : AnalyticsService {
    private val firebaseAnalytics: FirebaseAnalytics = Firebase.analytics.apply {
        setAnalyticsCollectionEnabled(!BuildConfig.DEBUG || FeatureFlags.DEBUG_ENABLE_ANALYTICS)
    }

    override fun logEvent(event: AnalyticsEvent) {
        firebaseAnalytics.logEvent(event.name) {
            event.params.forEach { (key, value) -> param(key, value) }
        }
    }

    override fun setUserProperty(property: UserProperty) {
        firebaseAnalytics.setUserProperty(property.name, property.value)
    }

    override fun setUserId(userId: String?) {
        firebaseAnalytics.setUserId(userId)
    }
}
