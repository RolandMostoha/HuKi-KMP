package hu.mostoha.mobile.kmp.huki.util

object NameNormalizer {
    private val diacritics = mapOf(
        'á' to 'a',
        'é' to 'e',
        'í' to 'i',
        'ó' to 'o',
        'ö' to 'o',
        'ő' to 'o',
        'ú' to 'u',
        'ü' to 'u',
        'ű' to 'u',
    )

    fun normalize(value: String): String = value.lowercase().map { diacritics[it] ?: it }.joinToString("")
}
