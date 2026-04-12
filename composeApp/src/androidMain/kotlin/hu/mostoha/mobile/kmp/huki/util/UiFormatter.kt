package hu.mostoha.mobile.kmp.huki.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

object UiFormatter {

    /**
     * Formats (numbers)(letters) values by applying a small style to the trailing part. E.g. "22.6 km"
     */
    fun formatStatValue(value: String, smallSpanStyle: SpanStyle): AnnotatedString =
        buildAnnotatedString {
            var currentIndex = 0

            PAIR_REGEX.findAll(value).forEach { match ->
                append(value.substring(currentIndex, match.range.first))
                append(match.groupValues[1])
                append(match.groupValues[2])
                withStyle(smallSpanStyle) {
                    append(match.groupValues[3].uppercase())
                }
                currentIndex = match.range.last + 1
            }

            append(value.substring(currentIndex))
        }

    // Matches pairs like "22.6 km" or "7h".
    private val PAIR_REGEX = Regex("""(\d+(?:[.,]\d+)?)(\s*)([A-Za-zÁÉÍÓÖŐÚÜŰáéíóöőúüű]+)""")
}
