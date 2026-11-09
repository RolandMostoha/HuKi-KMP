package hu.mostoha.mobile.kmp.huki.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.em
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.util.mokoColor

private const val HEART_ID = "heart"

@Composable
fun TrailingHeartText(
    text: String,
    heartColor: Color,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
) {
    val inlineContent = mapOf(
        HEART_ID to InlineTextContent(
            placeholder = Placeholder(width = 1.em, height = 1.em, PlaceholderVerticalAlign.TextCenter),
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_favorite),
                contentDescription = null,
                tint = heartColor,
            )
        },
    )
    Text(
        text = buildAnnotatedString {
            append(text)
            append(" ")
            appendInlineContent(HEART_ID, "♥")
        },
        inlineContent = inlineContent,
        style = style,
        fontWeight = fontWeight,
        color = color,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun TrailingHeartTextPreview() {
    HuKiTheme {
        TrailingHeartText(
            text = "Thank you for supporting HuKi.",
            heartColor = mokoColor(SharedRes.colors.supportSquirrel),
            modifier = Modifier.padding(Dimens.Large),
        )
    }
}
