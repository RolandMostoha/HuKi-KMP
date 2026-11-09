package hu.mostoha.mobile.kmp.huki.ui.features.support

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.features.support.SupporterChoice
import hu.mostoha.mobile.kmp.huki.model.domain.SupportAnimal
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme
import hu.mostoha.mobile.kmp.huki.theme.LocalIsDarkTheme
import hu.mostoha.mobile.kmp.huki.ui.components.TrailingHeartText
import hu.mostoha.mobile.kmp.huki.util.TestTags
import hu.mostoha.mobile.kmp.huki.util.mokoColor
import hu.mostoha.mobile.kmp.huki.util.mokoString
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private const val HEART_COUNT = 9
private const val MIN_CHOOSER_CHOICES = 2
private const val SILHOUETTE_ALPHA = 0.1f
private val SwitchDuration = 350.milliseconds
private val TextStaggerDelay = 90.milliseconds
private val HeartBurstDistance = 64.dp
private val TextRiseOffset = 12.dp
private val SilhouetteSize = 200.dp
private val SilhouetteOffset = 48.dp
private val SilhouetteSlideOffset = 40.dp
private val CardShape = RoundedCornerShape(26.dp)
private val IconBadgeOffset = 6.dp

@Composable
fun SupporterCard(
    animal: SupportAnimal,
    modifier: Modifier = Modifier,
    celebrate: Boolean = false,
    choices: List<SupporterChoice> = emptyList(),
    onChoose: (SupportAnimal) -> Unit = {},
) {
    val animalColor by animateColorAsState(mokoColor(animal.colorRes), switchTween(), label = "animalColor")
    val containerColor by animateColorAsState(
        targetValue = mokoColor(animal.containerColorRes),
        animationSpec = switchTween(),
        label = "containerColor",
    )
    val iconBackgroundColor = if (LocalIsDarkTheme.current) Color.White.copy(alpha = 0.09f) else Color.White
    val hapticFeedback = LocalHapticFeedback.current
    val iconProgress = remember { Animatable(if (celebrate) 0f else 1f) }
    val burstProgress = remember { Animatable(if (celebrate) 0f else 1f) }
    val textProgresses = remember { List(3) { Animatable(if (celebrate) 0f else 1f) } }
    val silhouetteProgress = remember { Animatable(if (celebrate) 0f else 1f) }
    LaunchedEffect(Unit) {
        if (!celebrate) return@LaunchedEffect
        launch {
            iconProgress.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMediumLow),
            )
        }
        launch {
            delay(120)
            hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
            burstProgress.animateTo(targetValue = 1f, animationSpec = tween(durationMillis = 900))
        }
        launch {
            delay(100)
            silhouetteProgress.animateTo(targetValue = 1f, animationSpec = tween(700, easing = FastOutSlowInEasing))
        }
        textProgresses.forEachIndexed { index, progress ->
            launch {
                delay(200.milliseconds + TextStaggerDelay * index)
                progress.animateTo(targetValue = 1f, animationSpec = tween(400, easing = FastOutSlowInEasing))
            }
        }
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(containerColor, CardShape)
            .testTag(TestTags.SUPPORT_SUPPORTER_CARD),
    ) {
        AnimalSilhouette(animal = animal, progress = silhouetteProgress.value)
        SupporterCardContent(
            animal = animal,
            purchaseCount = choices.firstOrNull { it.animal == animal }?.purchaseCount ?: 1,
            animalColor = animalColor,
            containerColor = containerColor,
            iconBackgroundColor = iconBackgroundColor,
            iconProgress = iconProgress.value,
            burstProgress = burstProgress.value,
            textProgresses = textProgresses.map { it.value },
            chooser = {
                if (choices.size >= MIN_CHOOSER_CHOICES) {
                    SupportAnimalChooser(
                        choices = choices,
                        selectedAnimal = animal,
                        containerColor = containerColor,
                        onChoose = onChoose,
                        modifier = Modifier
                            .padding(top = 18.dp)
                            .riseIn(textProgresses[2].value),
                    )
                }
            },
        )
    }
}

@Composable
private fun BoxScope.AnimalSilhouette(animal: SupportAnimal, progress: Float) {
    Box(
        modifier = Modifier
            .matchParentSize()
            .clip(CardShape),
    ) {
        AnimatedContent(
            targetState = animal,
            transitionSpec = {
                (fadeIn(switchTween()) + slideInHorizontally(switchTween()) { it / 4 })
                    .togetherWith(fadeOut(switchTween(SwitchDuration / 2)))
            },
            modifier = Modifier.align(Alignment.TopEnd),
            label = "silhouette",
        ) { silhouetteAnimal ->
            Icon(
                imageVector = ImageVector.vectorResource(silhouetteAnimal.iconRes.drawableResId),
                contentDescription = null,
                tint = mokoColor(silhouetteAnimal.colorRes).copy(alpha = SILHOUETTE_ALPHA),
                modifier = Modifier
                    .size(SilhouetteSize)
                    .graphicsLayer {
                        // Mirrored to face into the card
                        scaleX = if (silhouetteAnimal.isIconFacingRight) -1f else 1f
                        alpha = progress
                        translationX = SilhouetteOffset.toPx() + (1f - progress) * SilhouetteSlideOffset.toPx()
                    },
            )
        }
    }
}

@Composable
private fun SupporterCardContent(
    animal: SupportAnimal,
    purchaseCount: Int,
    animalColor: Color,
    containerColor: Color,
    iconBackgroundColor: Color,
    iconProgress: Float,
    burstProgress: Float,
    textProgresses: List<Float>,
    chooser: @Composable () -> Unit,
) {
    val animalName = mokoString(animal.nameRes)
    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 22.dp)) {
        Box(contentAlignment = Alignment.Center) {
            HeartBurst(progress = burstProgress, color = animalColor)
            Box(
                modifier = Modifier.graphicsLayer {
                    scaleX = iconProgress
                    scaleY = iconProgress
                    rotationZ = (1f - iconProgress) * -20f
                },
            ) {
                SupporterIcon(animal = animal, backgroundColor = iconBackgroundColor)
                IconPurchaseCountBadge(
                    purchaseCount = purchaseCount,
                    color = animalColor,
                    ringColor = containerColor,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = IconBadgeOffset, y = IconBadgeOffset),
                )
            }
        }
        SwitchingText(
            text = mokoString(SharedRes.strings.support_supporter_label, animalName).uppercase(),
            modifier = Modifier
                .padding(top = Dimens.MediumLarge)
                .riseIn(textProgresses[0]),
        ) { text ->
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = animalColor,
            )
        }
        SwitchingText(
            text = mokoString(SharedRes.strings.support_supporter_title, animalName),
            modifier = Modifier
                .padding(top = Dimens.ExtraSmall)
                .riseIn(textProgresses[1])
                .semantics { heading() },
        ) { text ->
            Text(
                text = text,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        TrailingHeartText(
            text = mokoString(SharedRes.strings.support_supporter_message),
            heartColor = animalColor,
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(top = Dimens.SmallMedium)
                .riseIn(textProgresses[2]),
        )
        chooser()
    }
}

@Composable
private fun SupporterIcon(animal: SupportAnimal, backgroundColor: Color) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .background(backgroundColor, RoundedCornerShape(22.dp)),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = animal,
            transitionSpec = {
                (fadeIn(switchTween()) + scaleIn(switchTween(), initialScale = 0.5f))
                    .togetherWith(fadeOut(switchTween(SwitchDuration / 2)) + scaleOut(targetScale = 0.5f))
            },
            label = "icon",
        ) { iconAnimal ->
            Icon(
                imageVector = ImageVector.vectorResource(iconAnimal.iconRes.drawableResId),
                contentDescription = null,
                tint = mokoColor(iconAnimal.colorRes),
                modifier = Modifier.size(48.dp),
            )
        }
    }
}

@Composable
private fun IconPurchaseCountBadge(
    purchaseCount: Int,
    color: Color,
    ringColor: Color,
    modifier: Modifier = Modifier,
) {
    val description = mokoString(SharedRes.strings.support_a11y_purchase_count, purchaseCount)
    AnimatedVisibility(
        visible = purchaseCount >= MIN_SHOWN_PURCHASE_COUNT,
        enter = scaleIn() + fadeIn(),
        exit = scaleOut() + fadeOut(),
        modifier = modifier,
    ) {
        SupportPurchaseCountBadge(
            purchaseCount = purchaseCount,
            color = color,
            ringColor = ringColor,
            modifier = Modifier
                .clearAndSetSemantics { contentDescription = description }
                .testTag(TestTags.SUPPORT_SUPPORTER_PURCHASE_COUNT),
        )
    }
}

@Composable
private fun SwitchingText(text: String, modifier: Modifier = Modifier, content: @Composable (String) -> Unit) {
    AnimatedContent(
        targetState = text,
        transitionSpec = {
            fadeIn(switchTween())
                .togetherWith(fadeOut(switchTween(SwitchDuration / 2)))
                .using(SizeTransform(clip = false))
        },
        modifier = modifier,
        label = "switchingText",
    ) { targetText ->
        content(targetText)
    }
}

@Composable
private fun HeartBurst(progress: Float, color: Color) {
    if (progress <= 0f || progress >= 1f) return
    val distancePx = with(LocalDensity.current) { HeartBurstDistance.toPx() }
    val heart = ImageVector.vectorResource(R.drawable.ic_favorite)
    repeat(HEART_COUNT) { index ->
        val angle = 2 * PI * index / HEART_COUNT - PI / 2
        val distance = distancePx * FastOutSlowInEasing.transform(progress) * if (index % 2 == 0) 1f else 0.75f
        Icon(
            imageVector = heart,
            contentDescription = null,
            tint = color,
            modifier = Modifier
                .size(if (index % 2 == 0) 14.dp else 10.dp)
                .graphicsLayer {
                    translationX = (cos(angle) * distance).toFloat()
                    translationY = (sin(angle) * distance).toFloat()
                    val scale = if (progress < 0.3f) progress / 0.3f else 1f
                    scaleX = scale
                    scaleY = scale
                    alpha = 1f - progress
                },
        )
    }
}

private fun <T> switchTween(duration: Duration = SwitchDuration) = tween<T>(duration.inWholeMilliseconds.toInt())

private fun Modifier.riseIn(progress: Float): Modifier =
    graphicsLayer {
        alpha = progress
        translationY = (1f - progress) * TextRiseOffset.toPx()
    }

@Preview
@Composable
private fun SupporterCardPreview() {
    HuKiTheme {
        SupporterCard(animal = SupportAnimal.OWL, modifier = Modifier.padding(Dimens.Large))
    }
}

@Preview
@Composable
private fun SupporterCardPurchaseCountPreview() {
    HuKiTheme {
        SupporterCard(
            animal = SupportAnimal.SQUIRREL,
            modifier = Modifier.padding(Dimens.Large),
            choices = listOf(SupporterChoice(SupportAnimal.SQUIRREL, purchaseCount = 3)),
        )
    }
}

@Preview
@Composable
private fun SupporterCardChooserPreview() {
    HuKiTheme {
        SupporterCard(
            animal = SupportAnimal.BOAR,
            modifier = Modifier.padding(Dimens.Large),
            choices = listOf(
                SupporterChoice(SupportAnimal.SQUIRREL, purchaseCount = 2),
                SupporterChoice(SupportAnimal.OWL, purchaseCount = 1),
                SupporterChoice(SupportAnimal.BOAR, purchaseCount = 1),
            ),
        )
    }
}
