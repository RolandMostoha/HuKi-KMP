package hu.mostoha.mobile.kmp.huki.ui.features.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.SupportAnimal
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoColor
import hu.mostoha.mobile.kmp.huki.util.mokoString
import hu.mostoha.mobile.kmp.huki.util.rememberLastNonNull

@Composable
fun SearchBar(
    supporterAnimal: SupportAnimal?,
    onSearchClick: () -> Unit,
    onMenuClick: () -> Unit,
    onSupportClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Keeps the last animal so the button can still animate out
    val shownAnimal = rememberLastNonNull(supporterAnimal)
    Surface(
        modifier = modifier
            .testTag(TestTags.MAIN_SEARCH_BAR)
            .height(65.dp)
            .fillMaxWidth(),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = Dimens.FloatingActionElevation,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedVisibility(
                visible = supporterAnimal != null,
                enter = fadeIn() + scaleIn() + expandHorizontally(),
                exit = fadeOut() + scaleOut() + shrinkHorizontally(),
            ) {
                shownAnimal?.let { animal ->
                    SupporterButton(
                        animal = animal,
                        onClick = onSupportClick,
                        modifier = Modifier.padding(end = Dimens.SmallMedium),
                    )
                }
            }
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                onClick = {
                    onSearchClick()
                },
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.Medium),
                ) {
                    Icon(
                        modifier = Modifier.size(Dimens.IconSmall),
                        imageVector = ImageVector.vectorResource(R.drawable.ic_search),
                        contentDescription = null,
                        tint = mokoColor(SharedRes.colors.iconStrong),
                    )
                    Text(
                        text = mokoString(SharedRes.strings.search_input_placeholder),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    )
                }
            }
            IconButton(
                onClick = {
                    onMenuClick.invoke()
                },
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_menu),
                    contentDescription = mokoString(SharedRes.strings.a11y_menu),
                )
            }
        }
    }
}

@Composable
private fun SupporterButton(animal: SupportAnimal, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val openSupportLabel = mokoString(SharedRes.strings.menu_a11y_open_support)
    val supporterLabel = mokoString(SharedRes.strings.support_supporter_label, mokoString(animal.nameRes))
    Surface(
        modifier = modifier
            .size(43.dp)
            .testTag(TestTags.MAIN_SEARCH_BAR_SUPPORT_BUTTON)
            .semantics {
                contentDescription = openSupportLabel
                stateDescription = supporterLabel
            },
        shape = CircleShape,
        color = mokoColor(animal.containerColorRes),
        onClick = onClick,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = ImageVector.vectorResource(animal.iconRes.drawableResId),
                contentDescription = null,
                tint = mokoColor(animal.colorRes),
                modifier = Modifier.size(27.dp),
            )
        }
    }
}

@Preview
@Composable
private fun SearchBarPreview() {
    HuKiTheme {
        SearchBar(
            supporterAnimal = null,
            onSearchClick = {},
            onMenuClick = {},
            onSupportClick = {},
            modifier = Modifier.padding(Dimens.Medium),
        )
    }
}

@Preview
@Composable
private fun SearchBarSupporterPreview() {
    HuKiTheme {
        SearchBar(
            supporterAnimal = SupportAnimal.HEDGEHOG,
            onSearchClick = {},
            onMenuClick = {},
            onSupportClick = {},
            modifier = Modifier.padding(Dimens.Medium),
        )
    }
}
