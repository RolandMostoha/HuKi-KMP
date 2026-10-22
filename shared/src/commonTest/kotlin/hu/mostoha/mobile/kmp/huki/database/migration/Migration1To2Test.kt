package hu.mostoha.mobile.kmp.huki.database.migration

import hu.mostoha.mobile.kmp.huki.data.ALL_DESTINATIONS
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class Migration1To2Test {

    @Test
    fun `Given legacy destination ids - When mapped - Then every new id is unique`() {
        val ids = Migration1To2.LEGACY_DESTINATION_IDS.values

        val actual = ids.toSet().size

        actual shouldBe ids.size
    }

    @Test
    fun `Given legacy destination ids - When mapped - Then every legacy id is numeric and every new id is a slug`() {
        val invalid = Migration1To2.LEGACY_DESTINATION_IDS.filter { (legacyId, id) ->
            !legacyId.matches(NUMERIC_ID) || !id.matches(SLUG_ID)
        }

        invalid.entries.shouldBeEmpty()
    }

    @Test
    fun `Given current destinations - When checking ids - Then none of them uses a legacy id`() {
        val actual = ALL_DESTINATIONS.map { it.id }.filter { it in Migration1To2.LEGACY_DESTINATION_IDS.keys }

        actual.shouldBeEmpty()
    }

    @Test
    fun `Given current destinations - When checking ids - Then every id is a unique slug`() {
        val ids = ALL_DESTINATIONS.map { it.id }

        ids.filterNot { it.matches(SLUG_ID) }.shouldBeEmpty()
        ids.toSet().size shouldBe ids.size
    }

    companion object {
        private val NUMERIC_ID = Regex("[0-9]+")
        private val SLUG_ID = Regex("[a-z0-9]+(_[a-z0-9]+)*")
    }
}
