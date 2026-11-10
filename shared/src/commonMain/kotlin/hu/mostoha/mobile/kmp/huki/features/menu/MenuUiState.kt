package hu.mostoha.mobile.kmp.huki.features.menu

import hu.mostoha.mobile.kmp.huki.WhatsNewContent
import hu.mostoha.mobile.kmp.huki.model.domain.SupportAnimal

data class MenuUiState(
    val versionName: String = WhatsNewContent.currentVersion,
    val supporterAnimal: SupportAnimal? = null,
) {
    companion object {
        val Default = MenuUiState()
    }
}
