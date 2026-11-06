package hu.mostoha.mobile.kmp.huki.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.theme.dividerColor
import hu.mostoha.mobile.kmp.huki.util.mokoColor

@Composable
fun GuideSection(title: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .padding(horizontal = Dimens.Large)
            .padding(top = Dimens.ExtraLarge),
        verticalArrangement = Arrangement.spacedBy(Dimens.Small),
    ) {
        GuideSectionTitle(text = title)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(Dimens.Large),
                ),
            shape = RoundedCornerShape(Dimens.Large),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun GuideSectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.8.sp),
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
        modifier = modifier
            .fillMaxWidth()
            .padding(start = Dimens.ExtraSmall, bottom = Dimens.ExtraSmall),
    )
}

@Composable
fun GuideStep(
    title: String,
    message: String,
    leading: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {},
) {
    Row(
        modifier = modifier.padding(Dimens.MediumLarge),
        horizontalArrangement = Arrangement.spacedBy(Dimens.Medium),
        verticalAlignment = Alignment.Top,
    ) {
        leading()
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.ExtraSmall)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            content()
        }
    }
}

@Composable
fun GuideStepBadge(number: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(mokoColor(SharedRes.colors.primary)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = number.toString(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = mokoColor(SharedRes.colors.onPrimary),
        )
    }
}

@Composable
fun GuideStepDivider(modifier: Modifier = Modifier, startIndent: Int = GUIDE_STEP_DIVIDER_INDENT) {
    HorizontalDivider(
        modifier = modifier.padding(start = startIndent.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.dividerColor,
    )
}

private const val GUIDE_STEP_DIVIDER_INDENT = 50

@Preview
@Composable
private fun GuideSectionPreview() {
    HuKiTheme {
        GuideSection(title = "How to import GPX into HuKi") {
            GuideStep(
                title = "Browse a hike collection website",
                message = "Look for a GPX export option near the route map.",
                leading = { GuideStepBadge(number = 1) },
            )
            GuideStepDivider()
            GuideStep(
                title = "Open the file with HuKi",
                message = "Tap the GPX file, choose Share, then pick HuKi.",
                leading = { GuideStepBadge(number = 2) },
            )
        }
    }
}
