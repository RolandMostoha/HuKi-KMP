package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.service.CrashlyticsService

object FakeCrashlyticsService : CrashlyticsService {
    override fun recordException(throwable: Throwable) = Unit

    override fun log(message: String) = Unit

    override fun setCustomKey(key: String, value: String) = Unit

    override fun setUserId(userId: String?) = Unit
}
