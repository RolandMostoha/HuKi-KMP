package hu.mostoha.mobile.kmp.huki.util

/**
 * Escapes SQL LIKE wildcards (`%`, `_`) and the escape char itself so user input is matched
 * literally. Pair with an `ESCAPE '\'` clause in the query.
 */
fun String.escapeLikeWildcards(): String =
    replace("\\", "\\\\")
        .replace("%", "\\%")
        .replace("_", "\\_")
