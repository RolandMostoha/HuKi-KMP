package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.model.domain.IncomingLink
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface IncomingLinkStore {
    /**
     * The link waiting to be opened, kept until consumed so a link arriving before the map exists is not lost.
     */
    val pendingLink: StateFlow<IncomingLink?>

    /**
     * Emits once a link resolved to a location, so the UI can bring the map to the front.
     */
    val linkOpened: SharedFlow<Unit>

    fun push(link: IncomingLink)

    fun consume()

    fun markOpened()
}
