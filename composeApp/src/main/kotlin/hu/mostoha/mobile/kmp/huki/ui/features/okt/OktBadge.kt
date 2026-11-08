package hu.mostoha.mobile.kmp.huki.ui.features.okt

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme

@Composable
fun OktBadgeText(
    prefix: String,
    number: String?,
    fontSize: TextUnit,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = number?.let { "$prefix\n$it" } ?: prefix,
        modifier = modifier,
        color = color,
        fontWeight = FontWeight.Black,
        textAlign = TextAlign.Center,
        lineHeight = fontSize * 1.05f,
        // A lone prefix must shrink onto one line instead of wrapping mid-word, e.g. "RPDD / K"
        maxLines = if (number == null) 1 else 2,
        autoSize = TextAutoSize.StepBased(minFontSize = 7.sp, maxFontSize = fontSize, stepSize = 0.5.sp),
    )
}

@Preview
@Composable
private fun OktBadgeTextPreview() {
    HuKiTheme {
        OktBadgeText(prefix = "RPDDK", number = "09", fontSize = 12.sp, color = Color.Black)
    }
}

@Preview
@Composable
private fun OktBadgeTextFullTrailPreview() {
    HuKiTheme {
        Box(modifier = Modifier.size(Dimens.OktBadgeSize), contentAlignment = Alignment.Center) {
            OktBadgeText(prefix = "RPDDK", number = null, fontSize = 13.sp, color = Color.Black)
        }
    }
}
