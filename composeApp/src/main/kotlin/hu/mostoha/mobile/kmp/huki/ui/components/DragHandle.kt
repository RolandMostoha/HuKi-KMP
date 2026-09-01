package hu.mostoha.mobile.kmp.huki.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme

@Composable
fun DragHandle(verticalPadding: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(vertical = verticalPadding)
            .size(width = 42.dp, height = 4.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)),
    )
}

@Preview
@Composable
private fun DragHandlePreview() {
    HuKiTheme {
        DragHandle(modifier = Modifier.padding(Dimens.Large), verticalPadding = Dimens.MediumLarge)
    }
}
