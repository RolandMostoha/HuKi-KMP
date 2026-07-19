package hu.mostoha.mobile.kmp.huki.model.mapper

import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.StringDesc
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.WhatsNewContent
import hu.mostoha.mobile.kmp.huki.model.domain.WhatsNew
import hu.mostoha.mobile.kmp.huki.model.domain.WhatsNewMessage
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlin.test.Test

class WhatsNewMapperTest {

    @Test
    fun `Given entry without message, When toWhatsNew, Then it maps fields and message is null`() {
        val entry = WhatsNewContent.Entry(
            version = "1.0",
            releaseDate = "2026-08-01",
            notes = SharedRes.strings.a11y_close,
            message = null,
        )

        val actual = entry.toWhatsNew()

        actual shouldBe WhatsNew(
            version = "1.0",
            releaseDate = LocalDate(2026, 8, 1),
            releaseNotes = StringDesc.Resource(SharedRes.strings.a11y_close),
            message = null,
        )
    }

    @Test
    fun `Given entry with message, When toWhatsNew, Then the message is wrapped as StringDesc`() {
        val entry = WhatsNewContent.Entry(
            version = "1.0",
            releaseDate = "2026-08-01",
            notes = SharedRes.strings.a11y_close,
            message = WhatsNewContent.Message(
                title = SharedRes.strings.a11y_menu,
                body = SharedRes.strings.a11y_close,
            ),
        )

        val actual = entry.toWhatsNew()

        actual.message shouldBe WhatsNewMessage(
            title = StringDesc.Resource(SharedRes.strings.a11y_menu),
            body = StringDesc.Resource(SharedRes.strings.a11y_close),
        )
    }

    @Test
    fun `Given markdown notes, When toReleaseNoteLines, Then bullet markers and blank lines are stripped`() {
        val notes = "- Map, GPS navigation\n\n* Hungarian trails\n  - GPX import\n"

        val actual = notes.toReleaseNoteLines()

        actual shouldBe listOf("Map, GPS navigation", "Hungarian trails", "GPX import")
    }

    @Test
    fun `Given generated content, When toCurrentWhatsNew, Then the current version entry is returned`() {
        val actual = WhatsNewContent.toCurrentWhatsNew()

        actual.version shouldBe WhatsNewContent.currentVersion
    }

    @Test
    fun `Given generated content, When toWhatsNewHistory, Then every release is mapped`() {
        val actual = WhatsNewContent.toWhatsNewHistory()

        actual.map { it.version } shouldBe WhatsNewContent.releases.map { it.version }
    }
}
