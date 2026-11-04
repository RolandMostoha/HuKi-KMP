package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.model.domain.IncomingLink
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class DefaultIncomingLinkStore : IncomingLinkStore {
    private val _pendingLink = MutableStateFlow<IncomingLink?>(null)
    override val pendingLink: StateFlow<IncomingLink?> = _pendingLink.asStateFlow()

    private val _linkOpened = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    override val linkOpened: SharedFlow<Unit> = _linkOpened.asSharedFlow()

    override fun push(link: IncomingLink) {
        _pendingLink.value = link
    }

    override fun consume() {
        _pendingLink.value = null
    }

    override fun markOpened() {
        _linkOpened.tryEmit(Unit)
    }
}
