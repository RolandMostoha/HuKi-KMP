package hu.mostoha.mobile.kmp.huki.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme

private val ImageHeight = 46.dp

@Composable
fun IconTitleCard(
    painter: Painter,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(Dimens.Large),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 0.5.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = Dimens.Small, vertical = Dimens.MediumLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.SmallMedium),
        ) {
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.height(ImageHeight),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 10.sp,
                    maxFontSize = MaterialTheme.typography.titleSmall.fontSize,
                    stepSize = 0.5.sp,
                ),
            )
        }
    }
}

@Preview
@Composable
private fun IconTitleCardPreview() {
    HuKiTheme {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.Large),
            horizontalArrangement = Arrangement.spacedBy(Dimens.Medium),
        ) {
            IconTitleCard(
                painter = painterResource(SharedRes.images.ic_hungary_colored.drawableResId),
                title = "Landscapes",
                onClick = {},
                modifier = Modifier.weight(1f),
            )
            IconTitleCard(
                painter = painterResource(SharedRes.images.ic_destinations_by_category.drawableResId),
                title = "Categories",
                onClick = {},
                modifier = Modifier.weight(1f),
            )
            IconTitleCard(
                painter = painterResource(SharedRes.images.ic_destinations_in_list.drawableResId),
                title = "List",
                onClick = {},
                modifier = Modifier.weight(1f),
            )
        }
    }
}
