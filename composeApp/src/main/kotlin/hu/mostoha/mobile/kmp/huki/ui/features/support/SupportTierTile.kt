package hu.mostoha.mobile.kmp.huki.ui.features.support

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.SupportAnimal
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.theme.LocalIsDarkTheme
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoColor
import hu.mostoha.mobile.kmp.huki.util.mokoString

@Composable
fun SupportTierTile(
    animal: SupportAnimal,
    name: String,
    price: String?,
    billingPeriod: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    scheduledDate: String? = null,
    isPurchasing: Boolean = false,
    isEnabled: Boolean = true,
) {
    val animalColor = mokoColor(animal.colorRes)
    val pillColor = if (LocalIsDarkTheme.current) Color.White.copy(alpha = 0.09f) else Color.White.copy(alpha = 0.8f)
    val activeDescription = mokoString(SharedRes.strings.support_a11y_tier_active)
    val scheduledDescription = mokoString(SharedRes.strings.support_a11y_tier_scheduled)
    Surface(
        onClick = onClick,
        enabled = (price != null || isActive || scheduledDate != null) && isEnabled,
        modifier = modifier
            .testTag(TestTags.supportTier(animal.name))
            .semantics(mergeDescendants = true) {
                when {
                    isActive -> stateDescription = activeDescription
                    scheduledDate != null -> stateDescription = scheduledDescription
                }
            },
        shape = RoundedCornerShape(24.dp),
        color = mokoColor(animal.containerColorRes),
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = Dimens.SmallMedium)
                .padding(top = 20.dp, bottom = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(animal.iconRes.drawableResId),
                contentDescription = null,
                tint = animalColor,
                modifier = Modifier.size(58.dp),
            )
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = animalColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = Dimens.Medium),
            )
            when {
                isActive -> ActivePill(pillColor = pillColor)
                scheduledDate != null -> ScheduledPill(date = scheduledDate, pillColor = pillColor, color = animalColor)
                isPurchasing -> PurchasingPill(pillColor = pillColor, color = animalColor)
                else -> PricePill(price = price, pillColor = pillColor)
            }
            val caption = when {
                isActive || isPurchasing -> null
                scheduledDate != null -> mokoString(SharedRes.strings.support_tier_scheduled)
                price != null -> billingPeriod
                else -> null
            }
            if (caption != null) {
                Text(
                    text = caption,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = Dimens.ExtraSmall),
                )
            }
        }
    }
}

@Composable
private fun PricePill(price: String?, pillColor: Color) {
    Text(
        text = price ?: "–",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        autoSize = TextAutoSize.StepBased(
            minFontSize = 8.sp,
            maxFontSize = MaterialTheme.typography.labelMedium.fontSize,
        ),
        modifier = Modifier
            .padding(top = Dimens.SmallMedium)
            .background(pillColor, CircleShape)
            .padding(horizontal = Dimens.Medium, vertical = Dimens.Small),
    )
}

@Composable
private fun ActivePill(pillColor: Color) {
    val successColor = mokoColor(SharedRes.colors.success)
    Row(
        modifier = Modifier
            .padding(top = Dimens.SmallMedium)
            .background(pillColor, CircleShape)
            .padding(horizontal = Dimens.Medium, vertical = Dimens.Small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.ExtraSmall),
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_check_circle_filled),
            contentDescription = null,
            tint = successColor,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = mokoString(SharedRes.strings.support_tier_active),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = successColor,
            maxLines = 1,
        )
    }
}

@Composable
private fun ScheduledPill(date: String, pillColor: Color, color: Color) {
    Row(
        modifier = Modifier
            .padding(top = Dimens.SmallMedium)
            .background(pillColor, CircleShape)
            .padding(horizontal = Dimens.Medium, vertical = Dimens.Small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.ExtraSmall),
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(SharedRes.images.ic_schedule.drawableResId),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = date,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = color,
            maxLines = 1,
            autoSize = TextAutoSize.StepBased(
                minFontSize = 8.sp,
                maxFontSize = MaterialTheme.typography.labelMedium.fontSize,
            ),
        )
    }
}

@Composable
private fun PurchasingPill(pillColor: Color, color: Color) {
    Box(
        modifier = Modifier
            .padding(top = Dimens.SmallMedium)
            .background(pillColor, CircleShape)
            .padding(horizontal = 20.dp, vertical = Dimens.Small),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            color = color,
            strokeWidth = 2.dp,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Preview
@Composable
private fun SupportTierTilePreview() {
    HuKiTheme {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
                .padding(Dimens.Large),
            horizontalArrangement = Arrangement.spacedBy(Dimens.Medium),
        ) {
            SupportTierTile(
                animal = SupportAnimal.SQUIRREL,
                name = "Mókus",
                price = "499 Ft",
                billingPeriod = null,
                onClick = {},
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            SupportTierTile(
                animal = SupportAnimal.BOAR,
                name = "Vaddisznó",
                price = "499 Ft",
                billingPeriod = "havonta",
                isActive = true,
                onClick = {},
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            SupportTierTile(
                animal = SupportAnimal.DEER,
                name = "Szarvas",
                price = "249 Ft",
                billingPeriod = "havonta",
                scheduledDate = "nov. 3.",
                onClick = {},
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
            SupportTierTile(
                animal = SupportAnimal.BEAR,
                name = "Medve",
                price = "999 Ft",
                isPurchasing = true,
                billingPeriod = "havonta",
                onClick = {},
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }
    }
}
