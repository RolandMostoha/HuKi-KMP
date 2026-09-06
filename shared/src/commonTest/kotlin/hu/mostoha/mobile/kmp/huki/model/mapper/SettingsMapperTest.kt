package hu.mostoha.mobile.kmp.huki.model.mapper

import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.preferencesOf
import hu.mostoha.mobile.kmp.huki.datastore.SettingsPreferenceKeys
import hu.mostoha.mobile.kmp.huki.model.domain.UserPreferences
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SettingsMapperTest {

    @Test
    fun `Given empty preferences - When toUserPreferences - Then default preferences are returned`() {
        val preferences = emptyPreferences()

        val actual = preferences.toUserPreferences()

        actual shouldBe UserPreferences.DEFAULTS
    }

    @Test
    fun `Given stored map zoom controls flag - When toUserPreferences - Then the flag is returned`() {
        val preferences = preferencesOf(SettingsPreferenceKeys.MAP_ZOOM_CONTROLS_VISIBLE to true)

        val actual = preferences.toUserPreferences()

        actual.mapZoomControlsVisible shouldBe true
    }
}
