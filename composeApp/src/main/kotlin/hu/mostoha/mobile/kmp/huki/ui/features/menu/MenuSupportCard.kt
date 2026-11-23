package hu.mostoha.mobile.kmp.huki.ui.features.menu

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.ui.components.SupportAnimalBadges
import hu.mostoha.mobile.kmp.huki.util.mokoColor

@Composable
fun MenuSupportCard(
    title: String,
    message: String,
    contentDescription: String,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cardColor = MaterialTheme.colorScheme.surface
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag)
            .clearAndSetSemantics {
                this.contentDescription = "$contentDescription, $message"
                role = Role.Button
            },
        shape = RoundedCornerShape(26.dp),
        color = cardColor,
        shadowElevation = 0.5.dp,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(Dimens.Large),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 14.dp),
            ) {
                // Pulls the invisible card-colored ring out so the circles line up with the text
                SupportAnimalBadges(ringColor = cardColor, size = 32.dp, modifier = Modifier.offset(x = (-3).dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 2.dp),
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = mokoColor(SharedRes.colors.supportRed),
                modifier = Modifier.size(Dimens.IconExtraSmall),
            )
        }
    }
}

@Preview
@Composable
private fun MenuSupportCardPreview() {
    HuKiTheme {
        MenuSupportCard(
            title = "HuKi támogatása",
            message = "Segíti a HuKi fejlesztését és fenntartását.",
            contentDescription = "Támogatás megnyitása",
            testTag = "",
            onClick = {},
            modifier = Modifier.padding(Dimens.Large),
        )
    }
}
