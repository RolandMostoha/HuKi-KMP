package hu.mostoha.mobile.kmp.huki.ui.features.quickguide

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.KeyframesSpec
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.kmp.huki.theme.Dimens
import hu.mostoha.mobile.kmp.huki.theme.HuKiTheme

@Composable
fun GestureDemoFab(@DrawableRes iconResId: Int, gesture: DemoGesture, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "GestureDemoFab")
    val timing = GestureTiming.of(gesture)
    val fabScale by transition.animateKeyframes(cycleMillis = timing.cycle, restValue = 1f) {
        1f at PRESS_START_MS
        PRESSED_SCALE at PRESS_START_MS + PRESS_IN_MS using FastOutSlowInEasing
        PRESSED_SCALE at timing.release
        1f at timing.release + RELEASE_MS using FastOutSlowInEasing
    }
    val touchAlpha by transition.animateKeyframes(cycleMillis = timing.cycle, restValue = 0f) {
        0f at PRESS_START_MS
        TOUCH_ALPHA at PRESS_START_MS + PRESS_IN_MS
        timing.holdAlpha at timing.release
        0f at timing.release + RELEASE_MS
    }
    val touchScale by transition.animateKeyframes(cycleMillis = timing.cycle, restValue = 1f) {
        1f at PRESS_START_MS + PRESS_IN_MS
        timing.holdScale at timing.release using FastOutSlowInEasing
        timing.holdScale at timing.release + RELEASE_MS
        1f at timing.release + RELEASE_MS + 1
    }
    val ringProgress by transition.animateKeyframes(cycleMillis = timing.cycle, restValue = 0f) {
        0f at timing.release
        1f at timing.release + RIPPLE_MS using FastOutSlowInEasing
        0f at timing.release + RIPPLE_MS + 1
    }
    val ringColor = MaterialTheme.colorScheme.primary
    Box(
        modifier = modifier.size(DEMO_SIZE.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(DEMO_SIZE.dp)) {
            val fabRadius = FAB_SIZE.dp.toPx() / 2
            if (ringProgress in 0.001f..0.999f) {
                drawCircle(
                    color = ringColor.copy(alpha = RIPPLE_ALPHA * (1f - ringProgress)),
                    radius = fabRadius + (size.minDimension / 2 - fabRadius) * ringProgress,
                    style = Stroke(width = RING_STROKE.dp.toPx()),
                )
            }
        }
        Surface(
            modifier = Modifier
                .size(FAB_SIZE.dp)
                .graphicsLayer {
                    scaleX = fabScale
                    scaleY = fabScale
                },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shadowElevation = Dimens.FloatingActionElevation,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = ImageVector.vectorResource(iconResId),
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.IconMedium),
                )
            }
        }
        Box(
            modifier = Modifier
                .size(TOUCH_SIZE.dp)
                .graphicsLayer {
                    scaleX = touchScale
                    scaleY = touchScale
                    alpha = touchAlpha
                }
                .background(Color.White, CircleShape),
        )
    }
}

@Composable
private fun InfiniteTransition.animateKeyframes(
    cycleMillis: Int,
    restValue: Float,
    keyframes: KeyframesSpec.KeyframesSpecConfig<Float>.() -> Unit,
): State<Float> =
    animateFloat(
        initialValue = restValue,
        targetValue = restValue,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = cycleMillis
                keyframes()
            },
        ),
        label = "GestureDemoFabKeyframes",
    )

private data class GestureTiming(
    val release: Int,
    val cycle: Int,
    val holdScale: Float = 1f,
    val holdAlpha: Float = TOUCH_ALPHA,
) {
    companion object {
        fun of(gesture: DemoGesture): GestureTiming =
            when (gesture) {
                DemoGesture.CLICK -> GestureTiming(release = CLICK_RELEASE_MS, cycle = CLICK_CYCLE_MS)
                DemoGesture.LONG_CLICK -> GestureTiming(
                    release = LONG_CLICK_RELEASE_MS,
                    cycle = LONG_CLICK_CYCLE_MS,
                    holdScale = HOLD_TOUCH_SCALE,
                    holdAlpha = HOLD_TOUCH_ALPHA,
                )
            }
    }
}

private const val FAB_SIZE = 52
private const val DEMO_SIZE = 64
private const val TOUCH_SIZE = 28
private const val TOUCH_ALPHA = 0.55f
private const val HOLD_TOUCH_ALPHA = 0.35f
private const val HOLD_TOUCH_SCALE = 1.7f
private const val RING_STROKE = 3
private const val RIPPLE_ALPHA = 0.6f
private const val PRESSED_SCALE = 0.9f
private const val PRESS_START_MS = 400
private const val PRESS_IN_MS = 120
private const val RELEASE_MS = 180
private const val RIPPLE_MS = 600
private const val CLICK_RELEASE_MS = 650
private const val CLICK_CYCLE_MS = 2200
private const val LONG_CLICK_RELEASE_MS = 1400
private const val LONG_CLICK_CYCLE_MS = 2800

@Preview
@Composable
private fun GestureDemoFabClickPreview() {
    HuKiTheme {
        GestureDemoFab(iconResId = R.drawable.ic_fab_my_location_following, gesture = DemoGesture.CLICK)
    }
}

@Preview
@Composable
private fun GestureDemoFabLongClickPreview() {
    HuKiTheme {
        GestureDemoFab(iconResId = R.drawable.ic_fab_my_location_live_compass, gesture = DemoGesture.LONG_CLICK)
    }
}
