import FirebaseAnalytics
import Shared

class IosAnalyticsService: AnalyticsService {
    init() {
        #if DEBUG
        Analytics.setAnalyticsCollectionEnabled(FeatureFlags.shared.DEBUG_ENABLE_ANALYTICS)
        #endif
    }

    func logEvent(event: AnalyticsEvent) {
        Analytics.logEvent(event.name, parameters: event.params)
    }

    func setUserProperty(property: UserProperty) {
        Analytics.setUserProperty(property.value, forName: property.name)
    }

    func setUserId(userId: String?) {
        Analytics.setUserID(userId)
    }
}
