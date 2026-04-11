package hu.mostoha.mobile.kmp.huki.logger

import kotlin.text.replace
import kotlin.text.split

/**
 * Trims long lists within a string representation of an object (typically a data class).
 * If a list [a, b, c...] has more than [limit] items, it truncates the rest.
 */
fun Any?.trimLongLists(limit: Int = 10): String {
    if (this == null) return "null"
    val fullString = this.toString()

    // Regex matches content inside brackets: [item1, item2, ...]
    val listRegex = Regex("\\[([^]]+)\\]")
    return fullString.replace(listRegex) { matchResult ->
        val content = matchResult.groupValues[1]
        val items = content.split(", ")

        if (items.size > limit) {
            "[${items.take(limit).joinToString(", ")}, [...trimmed...] (total=${items.size})]"
        } else {
            matchResult.value
        }
    }
}
