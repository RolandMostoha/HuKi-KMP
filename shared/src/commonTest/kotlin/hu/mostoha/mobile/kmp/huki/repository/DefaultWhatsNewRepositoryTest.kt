package hu.mostoha.mobile.kmp.huki.repository

import androidx.datastore.preferences.core.preferencesOf
import hu.mostoha.mobile.kmp.huki.WhatsNewContent
import hu.mostoha.mobile.kmp.huki.datastore.SettingsPreferenceKeys
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class DefaultWhatsNewRepositoryTest {

    @Test
    fun `Given no last seen version - When shouldShowWhatsNew - Then it returns true`() {
        runTest {
            val repository = DefaultWhatsNewRepository(FakeDataStore())

            val actual = repository.shouldShowWhatsNew()

            actual shouldBe true
        }
    }

    @Test
    fun `Given last seen version equals current - When shouldShowWhatsNew - Then it returns false`() {
        runTest {
            val dataStore = FakeDataStore(
                preferencesOf(SettingsPreferenceKeys.WHATS_NEW_LAST_SEEN_VERSION to WhatsNewContent.currentVersion),
            )
            val repository = DefaultWhatsNewRepository(dataStore)

            val actual = repository.shouldShowWhatsNew()

            actual shouldBe false
        }
    }

    @Test
    fun `Given unseen version - When markCurrentWhatsNewSeen - Then shouldShowWhatsNew becomes false`() {
        runTest {
            val repository = DefaultWhatsNewRepository(FakeDataStore())

            repository.markCurrentWhatsNewSeen()

            repository.shouldShowWhatsNew() shouldBe false
        }
    }
}
