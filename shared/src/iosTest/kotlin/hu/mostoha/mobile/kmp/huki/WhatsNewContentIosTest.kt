package hu.mostoha.mobile.kmp.huki

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class WhatsNewContentIosTest {

    @Test
    fun `Given the iOS build - When reading its releases - Then only iOS release notes are bundled`() {
        val actual = WhatsNewContent.releases.map { it.notes.resourceId }

        actual.filterNot { it.startsWith("whatsnew_ios_") } shouldHaveSize 0
    }

    @Test
    fun `Given the iOS build - When reading its releases - Then the current version is one of them`() {
        val actual = WhatsNewContent.releases.map { it.version }

        actual shouldContain WhatsNewContent.currentVersion
        actual.distinct() shouldBe actual
    }
}
