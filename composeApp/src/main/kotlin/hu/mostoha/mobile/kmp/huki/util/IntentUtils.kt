package hu.mostoha.mobile.kmp.huki.util

import android.content.ContentResolver
import android.content.Intent
import hu.mostoha.mobile.kmp.huki.model.domain.IncomingLink

private const val MIME_TYPE_TEXT = "text/plain"

/**
 * Null for a launch from Recents, which replays the intent of a link that was already opened.
 */
fun Intent.toIncomingLink(): IncomingLink? {
    if (flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY != 0) return null
    val data = data
    return when {
        action == Intent.ACTION_VIEW && data?.scheme == ContentResolver.SCHEME_CONTENT -> {
            IncomingLink.GpxFile(data.toString())
        }
        action == Intent.ACTION_VIEW -> dataString?.toTextLink()
        action == Intent.ACTION_SEND && type == MIME_TYPE_TEXT -> getStringExtra(Intent.EXTRA_TEXT)?.toTextLink()
        else -> null
    }
}

private fun String.toTextLink(): IncomingLink? = takeIf { it.isNotBlank() }?.let(IncomingLink::Text)
