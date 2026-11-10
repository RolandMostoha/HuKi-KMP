package hu.mostoha.mobile.kmp.huki.ui.features.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.SupportAnimal
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoColor
import hu.mostoha.mobile.kmp.huki.util.mokoString

@Composable
fun MenuSupporterCard(animal: SupportAnimal, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val animalColor = mokoColor(animal.colorRes)
    val animalIcon = ImageVector.vectorResource(animal.iconRes.drawableResId)
    val supporterLabel = mokoString(SharedRes.strings.support_supporter_label, mokoString(animal.nameRes))
    val openSupportLabel = mokoString(SharedRes.strings.menu_a11y_open_support)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag(TestTags.MENU_ROW_SUPPORT)
            .clearAndSetSemantics {
                contentDescription = openSupportLabel
                stateDescription = supporterLabel
                role = Role.Button
            },
        shape = RoundedCornerShape(26.dp),
        color = mokoColor(animal.containerColorRes),
        onClick = onClick,
    ) {
        Box {
            // matchParentSize + requiredSize keep the oversized silhouette from growing the card
            Box(modifier = Modifier.matchParentSize()) {
                Icon(
                    imageVector = animalIcon,
                    contentDescription = null,
                    tint = animalColor.copy(alpha = 0.1f),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .offset(x = 24.dp, y = 12.dp)
                        .requiredSize(110.dp)
                        .graphicsLayer { scaleX = if (animal.isIconFacingRight) -1f else 1f },
                )
            }
            Row(
                modifier = Modifier.padding(horizontal = Dimens.Large, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                            RoundedCornerShape(14.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = animalIcon,
                        contentDescription = null,
                        tint = animalColor,
                        modifier = Modifier.size(28.dp),
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 14.dp),
                ) {
                    Text(
                        text = supporterLabel.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.4.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = mokoString(SharedRes.strings.menu_supporter_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = mokoString(SharedRes.strings.menu_supporter_message),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                    tint = animalColor,
                    modifier = Modifier.size(Dimens.IconExtraSmall),
                )
            }
        }
    }
}

@Preview
@Composable
private fun MenuSupporterCardPreview() {
    HuKiTheme {
        MenuSupporterCard(
            animal = SupportAnimal.OWL,
            onClick = {},
            modifier = Modifier.padding(Dimens.Large),
        )
    }
}
