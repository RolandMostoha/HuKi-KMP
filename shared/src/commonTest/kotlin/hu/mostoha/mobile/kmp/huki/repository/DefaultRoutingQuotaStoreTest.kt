package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.model.domain.RoutingQuotaState
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

class DefaultRoutingQuotaStoreTest {

    private val store = DefaultRoutingQuotaStore(FakeDataStore())

    @Test
    fun `Given empty store - When getQuotaState - Then no limit returns`() {
        runTest {
            val actual = store.getQuotaState(NOW)

            actual shouldBe RoutingQuotaState(isReserveReached = false, isBlocked = false)
        }
    }

    @Test
    fun `Given limits until the future - When getQuotaState - Then limits return`() {
        runTest {
            store.setReserveReachedUntil(NOW + 1.milliseconds)
            store.setBlockedUntil(NOW + 1.milliseconds)

            val actual = store.getQuotaState(NOW)

            actual shouldBe RoutingQuotaState(isReserveReached = true, isBlocked = true)
        }
    }

    @Test
    fun `Given expired limits - When getQuotaState - Then no limit returns`() {
        runTest {
            store.setReserveReachedUntil(NOW)
            store.setBlockedUntil(NOW)

            val actual = store.getQuotaState(NOW)

            actual shouldBe RoutingQuotaState(isReserveReached = false, isBlocked = false)
        }
    }

    companion object {
        private val NOW = Instant.parse("2026-10-10T14:00:00Z")
    }
}
