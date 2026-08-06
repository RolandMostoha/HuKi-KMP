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
fun PrimaryButton(
    @DrawableRes iconResId: Int,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    BaseButton(
        iconResId = iconResId,
        text = text,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        onClick = onClick,
        modifier = modifier,
        contentDescription = contentDescription,
    )
}

@Preview
@Composable
private fun PrimaryButtonPreview() {
    HuKiTheme {
        PrimaryButton(
            iconResId = R.drawable.ic_fab_my_location_live_compass,
            text = "Show on map",
            onClick = {},
        )
    }
}
