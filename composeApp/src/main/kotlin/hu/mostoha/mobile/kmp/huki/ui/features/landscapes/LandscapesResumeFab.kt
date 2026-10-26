package hu.mostoha.mobile.kmp.huki.ui.features.landscapes

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.features.map.LandscapesGrouping
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoString
import hu.mostoha.mobile.kmp.huki.util.testTagAsResourceId

private val ImageWidth = 34.dp

@Composable
fun LandscapesResumeFab(grouping: LandscapesGrouping, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val isCategories = grouping == LandscapesGrouping.CATEGORY
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier
            .size(Dimens.OktResumeFabSize)
            .testTagAsResourceId(TestTags.LANDSCAPES_RESUME_FAB),
        containerColor = MaterialTheme.colorScheme.surfaceBright,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = Dimens.FloatingActionElevation),
        shape = CircleShape,
    ) {
        Image(
            painter = painterResource(
                if (isCategories) {
                    SharedRes.images.ic_destinations_by_category.drawableResId
                } else {
                    SharedRes.images.ic_hungary_colored.drawableResId
                },
            ),
            contentDescription = mokoString(
                if (isCategories) {
                    SharedRes.strings.categories_a11y_resume
                } else {
                    SharedRes.strings.landscapes_a11y_resume
                },
            ),
            modifier = Modifier.width(ImageWidth),
        )
    }
}

@Preview
@Composable
private fun LandscapesResumeFabPreview() {
    HuKiTheme {
        Row(
            modifier = Modifier.padding(Dimens.Large),
            horizontalArrangement = Arrangement.spacedBy(Dimens.Large),
        ) {
            LandscapesResumeFab(grouping = LandscapesGrouping.LANDSCAPE, onClick = {})
            LandscapesResumeFab(grouping = LandscapesGrouping.CATEGORY, onClick = {})
        }
    }
}
