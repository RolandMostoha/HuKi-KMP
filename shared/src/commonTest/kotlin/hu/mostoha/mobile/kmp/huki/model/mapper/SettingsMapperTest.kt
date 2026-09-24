package hu.mostoha.mobile.kmp.huki.model.mapper

import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.preferencesOf
import hu.mostoha.mobile.kmp.huki.datastore.SettingsPreferenceKeys
import hu.mostoha.mobile.kmp.huki.model.domain.ThemeMode
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

    @Test
    fun `Given stored theme mode - When toUserPreferences - Then the theme mode is returned`() {
        testCases().forEach { testCase ->
            val preferences = if (testCase.input != null) {
                preferencesOf(SettingsPreferenceKeys.THEME_MODE to testCase.input)
            } else {
                emptyPreferences()
            }

            val actual = preferences.toUserPreferences()

            actual.themeMode shouldBe testCase.expected
        }
    }

    companion object {
        fun testCases() =
            listOf(
                TestCase(input = "SYSTEM", expected = ThemeMode.SYSTEM),
                TestCase(input = "LIGHT", expected = ThemeMode.LIGHT),
                TestCase(input = "DARK", expected = ThemeMode.DARK),
                TestCase(input = "light", expected = ThemeMode.SYSTEM),
                TestCase(input = "REMOVED_MODE", expected = ThemeMode.SYSTEM),
                TestCase(input = "", expected = ThemeMode.SYSTEM),
                TestCase(input = null, expected = ThemeMode.SYSTEM),
            )
    }

    data class TestCase(
        val input: String?,
        val expected: ThemeMode,
    )
}
