package hu.mostoha.mobile.kmp.huki.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hu.mostoha.mobile.kmp.huki.model.domain.DestinationType
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.util.categoryTint
import hu.mostoha.mobile.kmp.huki.util.mokoColor
import hu.mostoha.mobile.kmp.huki.util.mokoImage
import hu.mostoha.mobile.kmp.huki.util.mokoString

@Composable
fun DestinationTypeChip(
    type: DestinationType,
    modifier: Modifier = Modifier,
    contentColor: Color = mokoColor(type.colorRes).categoryTint(),
    containerColor: Color = MaterialTheme.colorScheme.surface,
    textStyle: TextStyle = MaterialTheme.typography.labelMedium,
    contentPadding: PaddingValues = PaddingValues(horizontal = Dimens.MediumLarge, vertical = Dimens.Small),
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(containerColor)
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.Small),
    ) {
        Icon(
            modifier = Modifier.size(16.dp),
            imageVector = mokoImage(type.iconRes),
            contentDescription = null,
            tint = contentColor,
        )
        Text(
            text = mokoString(type.title),
            style = textStyle.copy(fontWeight = FontWeight.SemiBold),
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview
@Composable
private fun DestinationTypeChipPreview() {
    HuKiTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(Dimens.Large),
            verticalArrangement = Arrangement.spacedBy(Dimens.Medium),
        ) {
            DestinationTypeChip(type = DestinationType.PEAK)
            DestinationTypeChip(type = DestinationType.RIVER, textStyle = MaterialTheme.typography.labelSmall)
        }
    }
}
