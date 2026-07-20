package hu.mostoha.mobile.kmp.huki.model.mapper

import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.StringDesc
import hu.mostoha.mobile.kmp.huki.WhatsNewContent
import hu.mostoha.mobile.kmp.huki.model.domain.WhatsNew
import hu.mostoha.mobile.kmp.huki.model.domain.WhatsNewMessage
import kotlinx.datetime.LocalDate

fun WhatsNewContent.toWhatsNewHistory(): List<WhatsNew> = releases.map { it.toWhatsNew() }

fun WhatsNewContent.toCurrentWhatsNew(): WhatsNew = releases.first { it.version == currentVersion }.toWhatsNew()

fun WhatsNewContent.Entry.toWhatsNew(): WhatsNew =
    WhatsNew(
        version = version,
        releaseDate = LocalDate.parse(releaseDate),
        releaseNotes = StringDesc.Resource(notes),
        message = message?.toWhatsNewMessage(),
    )

/**
 * Splits the resolved release notes markdown into the bullet lines the UI renders.
 */
fun String.toReleaseNoteLines(): List<String> =
    lineSequence()
        .map { it.trim().removePrefix("-").removePrefix("*").trim() }
        .filter { it.isNotEmpty() }
        .toList()

private fun WhatsNewContent.Message.toWhatsNewMessage(): WhatsNewMessage =
    WhatsNewMessage(
        title = StringDesc.Resource(title),
        body = StringDesc.Resource(body),
    )
