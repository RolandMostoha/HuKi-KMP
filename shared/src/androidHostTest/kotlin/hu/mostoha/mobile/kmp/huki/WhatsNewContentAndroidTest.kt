package hu.mostoha.mobile.kmp.huki

import hu.mostoha.mobile.huki.shared.SharedRes
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class WhatsNewContentAndroidTest {

    @Test
    fun `Given the Android build - When reading its releases - Then only Android release notes are bundled`() {
        // An Android StringResource only carries an Int id, so its name comes from the generated SharedRes fields
        val names = SharedRes.strings::class.java.declaredFields.associate { field ->
            field.isAccessible = true
            field.get(SharedRes.strings) to field.name
        }

        val actual = WhatsNewContent.releases.map { names[it.notes].orEmpty() }

        actual.filterNot { it.startsWith("whatsnew_android_") } shouldHaveSize 0
    }

    @Test
    fun `Given the Android build - When reading its releases - Then the current version is one of them`() {
        val actual = WhatsNewContent.releases.map { it.version }

        actual shouldContain WhatsNewContent.currentVersion
        actual.distinct() shouldBe actual
    }
}
