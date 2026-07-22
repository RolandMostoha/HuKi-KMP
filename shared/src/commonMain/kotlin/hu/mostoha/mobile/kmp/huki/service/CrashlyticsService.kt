package hu.mostoha.mobile.kmp.huki.service

/**
 * Shared crash reporting interface, the concrete implementation is provided per platform.
 *
 * Uncaught crashes are collected automatically; use these functions to record non-fatal
 * exceptions and attach context (breadcrumbs, keys) that shows up on the crash report.
 */
interface CrashlyticsService {
    fun recordException(throwable: Throwable)

    fun log(message: String)

    fun setCustomKey(key: String, value: String)

    fun setUserId(userId: String?)

    companion object {
        const val CRASH_KEY_LAST_SCREEN = "last_screen"
    }
}
