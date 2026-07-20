package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.model.domain.WhatsNew

interface WhatsNewRepository {
    val currentWhatsNew: WhatsNew

    val whatsNewHistory: List<WhatsNew>

    suspend fun shouldShowWhatsNew(): Boolean

    suspend fun markCurrentWhatsNewSeen()
}
