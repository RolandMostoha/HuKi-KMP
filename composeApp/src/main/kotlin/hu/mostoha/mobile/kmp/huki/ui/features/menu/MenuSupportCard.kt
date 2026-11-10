package hu.mostoha.mobile.kmp.huki.ui.features.menu

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.ui.components.BaseButton
import hu.mostoha.mobile.kmp.huki.ui.components.SupportAnimalBadges
import hu.mostoha.mobile.kmp.huki.util.mokoColor

@Composable
fun MenuSupportCard(
    title: String,
    message: String,
    buttonText: String,
    buttonContentDescription: String,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cardColor = MaterialTheme.colorScheme.surface
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = cardColor,
        shadowElevation = 0.5.dp,
    ) {
        Column(modifier = Modifier.padding(Dimens.Large)) {
            SupportAnimalBadges(ringColor = cardColor, size = 36.dp)
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 14.dp),
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Dimens.ExtraSmall),
            )
            BaseButton(
                iconResId = R.drawable.ic_favorite,
                text = buttonText,
                colors = ButtonDefaults.buttonColors(
                    containerColor = mokoColor(SharedRes.colors.supportRed),
                    contentColor = Color.White,
                ),
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimens.Large)
                    .testTag(testTag)
                    .semantics { contentDescription = buttonContentDescription },
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
            message = "Támogatásod hozzájárul a HuKi fenntartásához és fejlesztéséhez.",
            buttonText = "Támogatom",
            buttonContentDescription = "Támogatás megnyitása",
            testTag = "",
            onClick = {},
            modifier = Modifier.padding(Dimens.Large),
        )
    }
}
