import FirebaseCrashlytics
import Shared

class IosCrashlyticsService: CrashlyticsService {
    init() {
        #if DEBUG
        Crashlytics.crashlytics().setCrashlyticsCollectionEnabled(FeatureFlags.shared.DEBUG_ENABLE_CRASHLYTICS)
        #endif
    }

    func recordException(throwable: KotlinThrowable) {
        // Group by the Kotlin exception type so distinct exceptions land in distinct Crashlytics issues
        let exceptionName = String(describing: type(of: throwable))
        let error = NSError(
            domain: exceptionName,
            code: 0,
            userInfo: [
                NSLocalizedDescriptionKey: throwable.message ?? exceptionName
            ]
        )
        Crashlytics.crashlytics().record(error: error)
    }

    func log(message: String) {
        Crashlytics.crashlytics().log(message)
    }

    func setCustomKey(key: String, value: String) {
        Crashlytics.crashlytics().setCustomValue(value, forKey: key)
    }

    func setUserId(userId: String?) {
        Crashlytics.crashlytics().setUserID(userId ?? "")
    }
}
