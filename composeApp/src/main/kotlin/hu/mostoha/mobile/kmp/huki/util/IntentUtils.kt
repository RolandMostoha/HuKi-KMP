package hu.mostoha.mobile.kmp.huki.util

import android.content.Intent
import hu.mostoha.mobile.kmp.huki.model.domain.IncomingLink

private const val MIME_TYPE_TEXT = "text/plain"

/**
 * Null for a launch from Recents, which replays the intent of a link that was already opened.
 */
fun Intent.toIncomingLink(): IncomingLink? {
    if (flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY != 0) return null
    val text = when (action) {
        Intent.ACTION_VIEW -> dataString
        Intent.ACTION_SEND -> getStringExtra(Intent.EXTRA_TEXT).takeIf { type == MIME_TYPE_TEXT }
        else -> null
    }
    return text?.takeIf { it.isNotBlank() }?.let(::IncomingLink)
}
