package hu.mostoha.mobile.kmp.huki.model.domain

import dev.icerock.moko.resources.desc.StringDesc
import kotlinx.datetime.LocalDate

data class WhatsNew(
    val version: String,
    val releaseDate: LocalDate,
    val releaseNotes: StringDesc,
    val message: WhatsNewMessage?,
)
