package hu.mostoha.mobile.kmp.huki.service

class FakeCrashlyticsService : CrashlyticsService {
    val recordedExceptions = mutableListOf<Throwable>()
    val logs = mutableListOf<String>()
    val customKeys = mutableMapOf<String, String>()
    var userId: String? = null
        private set

    override fun recordException(throwable: Throwable) {
        recordedExceptions += throwable
    }

    override fun log(message: String) {
        logs += message
    }

    override fun setCustomKey(key: String, value: String) {
        customKeys[key] = value
    }

    override fun setUserId(userId: String?) {
        this.userId = userId
    }
}
