package hu.mostoha.mobile.kmp.huki.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme

@Composable
fun SecondaryButton(
    @DrawableRes iconResId: Int,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    enabled: Boolean = true,
) {
    BaseButton(
        iconResId = iconResId,
        text = text,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.primary,
        ),
        onClick = onClick,
        modifier = modifier,
        contentDescription = contentDescription,
        enabled = enabled,
    )
}

@Preview
@Composable
private fun SecondaryButtonPreview() {
    HuKiTheme {
        SecondaryButton(
            iconResId = R.drawable.ic_maps,
            text = "Show on map",
            onClick = {},
        )
    }
}
