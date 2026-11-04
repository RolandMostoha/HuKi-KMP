package hu.mostoha.mobile.kmp.huki.ui.features.support

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.features.support.SupporterChoice
import hu.mostoha.mobile.kmp.huki.model.domain.SupportAnimal
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.theme.LocalIsDarkTheme
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoColor
import hu.mostoha.mobile.kmp.huki.util.mokoString

private val ChoiceSize = 64.dp
private val ChoiceRingWidth = 2.5.dp

@Composable
fun SupportAnimalChooser(
    choices: List<SupporterChoice>,
    selectedAnimal: SupportAnimal,
    containerColor: Color,
    onChoose: (SupportAnimal) -> Unit,
    modifier: Modifier = Modifier,
) {
    val panelColor = if (LocalIsDarkTheme.current) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.6f)
    val badgeRingColor = panelColor.compositeOver(containerColor)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(panelColor, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .padding(vertical = Dimens.Large)
            .testTag(TestTags.SUPPORT_ANIMAL_CHOOSER),
    ) {
        Text(
            text = mokoString(SharedRes.strings.support_chooser_title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = Dimens.Large),
        )
        Row(
            modifier = Modifier
                .padding(top = Dimens.MediumLarge)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = Dimens.Large)
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.Small),
        ) {
            choices.forEach { choice ->
                AnimalChoice(
                    choice = choice,
                    isSelected = choice.animal == selectedAnimal,
                    badgeRingColor = badgeRingColor,
                    onClick = { onChoose(choice.animal) },
                )
            }
        }
    }
}

@Composable
private fun AnimalChoice(
    choice: SupporterChoice,
    isSelected: Boolean,
    badgeRingColor: Color,
    onClick: () -> Unit,
) {
    val animalColor = mokoColor(choice.animal.colorRes)
    val name = mokoString(choice.animal.nameRes)
    val isCountShown = choice.purchaseCount >= MIN_SHOWN_PURCHASE_COUNT
    val purchaseCountDescription = mokoString(SharedRes.strings.support_a11y_purchase_count, choice.purchaseCount)
    val ringColor by animateColorAsState(if (isSelected) animalColor else Color.Transparent, label = "choiceRing")
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(76.dp)
            .selectable(
                selected = isSelected,
                interactionSource = interactionSource,
                indication = null,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .semantics(mergeDescendants = true) {
                contentDescription = if (isCountShown) "$name, $purchaseCountDescription" else name
            }
            .padding(vertical = Dimens.ExtraSmall)
            .testTag(TestTags.supportAnimalChoice(choice.animal.name)),
    ) {
        Box(modifier = Modifier.size(ChoiceSize)) {
            Box(
                modifier = Modifier
                    .size(ChoiceSize)
                    .border(ChoiceRingWidth, ringColor, CircleShape)
                    .padding(ChoiceRingWidth + 2.dp)
                    .clip(CircleShape)
                    .background(mokoColor(choice.animal.containerColorRes))
                    .indication(interactionSource, ripple()),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(choice.animal.iconRes.drawableResId),
                    contentDescription = null,
                    tint = animalColor,
                    modifier = Modifier.size(32.dp),
                )
            }
            if (isCountShown) {
                SupportPurchaseCountBadge(
                    purchaseCount = choice.purchaseCount,
                    color = animalColor,
                    ringColor = badgeRingColor,
                    modifier = Modifier.align(Alignment.BottomStart),
                )
            }
            SelectedBadge(
                isSelected = isSelected,
                color = animalColor,
                ringColor = badgeRingColor,
                modifier = Modifier.align(Alignment.BottomEnd),
            )
        }
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

@Composable
private fun SelectedBadge(
    isSelected: Boolean,
    color: Color,
    ringColor: Color,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = isSelected,
        enter = scaleIn() + fadeIn(),
        exit = scaleOut() + fadeOut(),
        modifier = modifier,
    ) {
        SupportBadge(color = color, ringColor = ringColor) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_check),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

@Preview
@Composable
private fun SupportAnimalChooserPreview() {
    HuKiTheme {
        val containerColor = mokoColor(SupportAnimal.BOAR.containerColorRes)
        Box(modifier = Modifier.background(containerColor).padding(Dimens.Large)) {
            SupportAnimalChooser(
                choices = listOf(
                    SupporterChoice(SupportAnimal.SQUIRREL, purchaseCount = 3),
                    SupporterChoice(SupportAnimal.OWL, purchaseCount = 1),
                    SupporterChoice(SupportAnimal.BOAR, purchaseCount = 1),
                ),
                selectedAnimal = SupportAnimal.BOAR,
                containerColor = containerColor,
                onChoose = {},
            )
        }
    }
}
