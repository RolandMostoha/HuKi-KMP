package hu.mostoha.mobile.kmp.huki.logger

import co.touchlab.kermit.Logger

/**
 * Swift-friendly error logging that routes through Kermit. Use from iOS instead of `print`,
 * so logs share the Kermit tag and are gated by the release min-severity like everywhere else.
 */
fun logError(message: String) = Logger.e { message }

/**
 * Trims long lists within a string representation of an object (typically a data class).
 * If a list [a, b, c...] has more than [limit] items, it truncates the rest.
 */
fun String.trimLongLists(limit: Int = 10): String {
    // Regex matches content inside brackets: [item1, item2, ...]
    val listRegex = Regex("\\[([^]]+)\\]")

    return this.replace(listRegex) { matchResult ->
        val content = matchResult.groupValues[1]
        val items = content.split(", ")

        if (items.size > limit) {
            "[${items.take(limit).joinToString(", ")}, [...trimmed...] (total=${items.size})]"
        } else {
            matchResult.value
        }
    }
}
