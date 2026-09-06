package hu.mostoha.mobile.kmp.huki.analytics

import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import com.google.firebase.crashlytics.setCustomKeys
import hu.mostoha.mobile.android.huki.BuildConfig
import hu.mostoha.mobile.kmp.huki.service.CrashlyticsService
import hu.mostoha.mobile.kmp.huki.util.FeatureFlags

class AndroidCrashlyticsService : CrashlyticsService {
    private val firebaseCrashlytics = Firebase.crashlytics.apply {
        isCrashlyticsCollectionEnabled = !BuildConfig.DEBUG || FeatureFlags.DEBUG_ENABLE_CRASHLYTICS
    }

    override fun recordException(throwable: Throwable) {
        firebaseCrashlytics.recordException(throwable)
    }

    override fun log(message: String) {
        firebaseCrashlytics.log(message)
    }

    override fun setCustomKey(key: String, value: String) {
        firebaseCrashlytics.setCustomKeys { key(key, value) }
    }

    override fun setUserId(userId: String?) {
        firebaseCrashlytics.setUserId(userId ?: "")
    }
}
