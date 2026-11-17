package hu.mostoha.mobile.kmp.huki.repository

import androidx.datastore.preferences.core.preferencesOf
import hu.mostoha.mobile.kmp.huki.datastore.HintPreferenceKeys
import hu.mostoha.mobile.kmp.huki.model.domain.Hint
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okio.IOException
import kotlin.test.Test

class DefaultHintRepositoryTest {

    @Test
    fun `Given no stored hints - When dismissedHints - Then it is empty`() {
        runTest {
            val repository = DefaultHintRepository(FakeDataStore(), alwaysShowHints = false)

            val actual = repository.dismissedHints.first()

            actual shouldBe emptySet()
        }
    }

    @Test
    fun `Given a not dismissed hint - When dismissHint - Then dismissedHints contains it`() {
        runTest {
            val repository = DefaultHintRepository(FakeDataStore(), alwaysShowHints = false)

            repository.dismissHint(Hint.ROUTE_PLANNER_LONG_PRESS)

            repository.dismissedHints.first() shouldBe setOf(Hint.ROUTE_PLANNER_LONG_PRESS)
        }
    }

    @Test
    fun `Given a failing store - When dismissHint - Then it does not throw and nothing is stored`() {
        runTest {
            val dataStore = FakeDataStore().apply { writeError = IOException("Disk full") }
            val repository = DefaultHintRepository(dataStore, alwaysShowHints = false)

            repository.dismissHint(Hint.ROUTE_PLANNER_LONG_PRESS)

            repository.dismissedHints.first() shouldBe emptySet()
        }
    }

    @Test
    fun `Given a stored dismissed hint and always show hints - When dismissedHints - Then it is empty`() {
        runTest {
            val dataStore = FakeDataStore(
                preferencesOf(HintPreferenceKeys.DISMISSED_HINTS to setOf("ROUTE_PLANNER_LONG_PRESS")),
            )
            val repository = DefaultHintRepository(dataStore, alwaysShowHints = true)

            val actual = repository.dismissedHints.first()

            actual shouldBe emptySet()
        }
    }

    @Test
    fun `Given always show hints - When dismissHint - Then dismissedHints contains it until restart`() {
        runTest {
            val dataStore = FakeDataStore()
            val repository = DefaultHintRepository(dataStore, alwaysShowHints = true)

            repository.dismissHint(Hint.ROUTE_PLANNER_LONG_PRESS)

            repository.dismissedHints.first() shouldBe setOf(Hint.ROUTE_PLANNER_LONG_PRESS)
            DefaultHintRepository(dataStore, alwaysShowHints = true).dismissedHints.first() shouldBe emptySet()
        }
    }

    @Test
    fun `Given an unknown stored hint name - When dismissedHints - Then it is ignored`() {
        runTest {
            val dataStore = FakeDataStore(
                preferencesOf(HintPreferenceKeys.DISMISSED_HINTS to setOf("REMOVED_HINT", "ROUTE_PLANNER_LONG_PRESS")),
            )
            val repository = DefaultHintRepository(dataStore, alwaysShowHints = false)

            val actual = repository.dismissedHints.first()

            actual shouldBe setOf(Hint.ROUTE_PLANNER_LONG_PRESS)
        }
    }
}
