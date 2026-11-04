package hu.mostoha.mobile.kmp.huki.util

import android.content.Intent
import hu.mostoha.mobile.kmp.huki.model.domain.IncomingLink

/**
 * Null for a launch from Recents, which replays the intent of a link that was already opened.
 */
fun Intent.toIncomingLink(): IncomingLink? {
    if (flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY != 0) return null
    val text = dataString.takeIf { action == Intent.ACTION_VIEW }
    return text?.takeIf { it.isNotBlank() }?.let(::IncomingLink)
}
