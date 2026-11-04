package hu.mostoha.mobile.kmp.huki.datastore

import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.preferencesOf
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class LegacySupportMigrationTest {

    @Test
    fun `Given legacy purchases - When migrated - Then the records and tokens are moved`() =
        runTest {
            val legacy = preferencesOf(
                SupportPreferenceKeys.PURCHASED_ONE_TIME_PRODUCTS to setOf("huki_support_one_time_level_1;3;1000"),
                SupportPreferenceKeys.RECORDED_PURCHASE_TOKENS to setOf("a", "b", "c"),
            )
            val migration = LegacySupportMigration { legacy }

            val actual = migration.migrate(emptyPreferences())

            actual shouldBe preferencesOf(
                SupportPreferenceKeys.PURCHASED_ONE_TIME_PRODUCTS to setOf("huki_support_one_time_level_1;3;1000"),
                SupportPreferenceKeys.RECORDED_PURCHASE_TOKENS to setOf("a", "b", "c"),
                SupportPreferenceKeys.LEGACY_MIGRATED to true,
            )
        }

    @Test
    fun `Given purchases in both apps - When migrated - Then counts are summed and the latest time is kept`() =
        runTest {
            val legacy = preferencesOf(
                SupportPreferenceKeys.PURCHASED_ONE_TIME_PRODUCTS to setOf(
                    "huki_support_one_time_level_1;2;1000",
                    "huki_support_one_time_level_2;1;500",
                ),
                SupportPreferenceKeys.RECORDED_PURCHASE_TOKENS to setOf("a", "b", "c"),
            )
            val current = preferencesOf(
                SupportPreferenceKeys.PURCHASED_ONE_TIME_PRODUCTS to setOf("huki_support_one_time_level_1;1;2000"),
                SupportPreferenceKeys.RECORDED_PURCHASE_TOKENS to setOf("d"),
            )
            val migration = LegacySupportMigration { legacy }

            val actual = migration.migrate(current)

            actual[SupportPreferenceKeys.PURCHASED_ONE_TIME_PRODUCTS] shouldBe setOf(
                "huki_support_one_time_level_1;3;2000",
                "huki_support_one_time_level_2;1;500",
            )
            actual[SupportPreferenceKeys.RECORDED_PURCHASE_TOKENS] shouldBe setOf("a", "b", "c", "d")
        }

    @Test
    fun `Given no legacy file - When migrated - Then only the migration is marked`() =
        runTest {
            val migration = LegacySupportMigration { null }

            val actual = migration.migrate(emptyPreferences())

            actual shouldBe preferencesOf(SupportPreferenceKeys.LEGACY_MIGRATED to true)
        }

    @Test
    fun `Given unreadable legacy file - When migrated - Then the migration is marked and nothing is lost`() =
        runTest {
            val current = preferencesOf(SupportPreferenceKeys.RECORDED_PURCHASE_TOKENS to setOf("d"))
            val migration = LegacySupportMigration { throw IllegalStateException("Corrupted") }

            val actual = migration.migrate(current)

            actual shouldBe preferencesOf(
                SupportPreferenceKeys.RECORDED_PURCHASE_TOKENS to setOf("d"),
                SupportPreferenceKeys.LEGACY_MIGRATED to true,
            )
        }

    @Test
    fun `Given migrated preferences - When checked - Then no migration is needed`() =
        runTest {
            val migration = LegacySupportMigration { null }

            val actual = migration.shouldMigrate(preferencesOf(SupportPreferenceKeys.LEGACY_MIGRATED to true))

            actual shouldBe false
        }
}
