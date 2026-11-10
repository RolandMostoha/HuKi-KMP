package hu.mostoha.mobile.kmp.huki.ui.features.support

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.SupportAnimal
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.util.mokoColor
import hu.mostoha.mobile.kmp.huki.util.mokoString

/**
 * A single purchase isn't counted, the badge appears from the second one.
 */
const val MIN_SHOWN_PURCHASE_COUNT = 2

@Composable
fun SupportBadge(
    color: Color,
    ringColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .background(ringColor, CircleShape)
            .padding(2.dp)
            .background(color, CircleShape)
            .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
fun SupportPurchaseCountBadge(
    purchaseCount: Int,
    color: Color,
    ringColor: Color,
    modifier: Modifier = Modifier,
) {
    SupportBadge(color = color, ringColor = ringColor, modifier = modifier) {
        Text(
            text = mokoString(SharedRes.strings.support_purchase_count, purchaseCount),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 5.dp),
        )
    }
}

@Preview
@Composable
private fun SupportPurchaseCountBadgePreview() {
    HuKiTheme {
        val containerColor = mokoColor(SupportAnimal.SQUIRREL.containerColorRes)
        Row(modifier = Modifier.background(containerColor).padding(Dimens.Large)) {
            SupportPurchaseCountBadge(
                purchaseCount = 3,
                color = mokoColor(SupportAnimal.SQUIRREL.colorRes),
                ringColor = containerColor,
            )
        }
    }
}
