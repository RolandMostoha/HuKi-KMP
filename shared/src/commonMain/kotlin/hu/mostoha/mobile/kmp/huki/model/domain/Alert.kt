package hu.mostoha.mobile.kmp.huki.model.domain

import dev.icerock.moko.resources.StringResource

data class Alert(
    val title: StringResource,
    val message: StringResource,
)
