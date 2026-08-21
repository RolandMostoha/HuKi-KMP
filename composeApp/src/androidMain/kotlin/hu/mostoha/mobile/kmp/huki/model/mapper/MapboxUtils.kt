package hu.mostoha.mobile.kmp.huki.model.mapper

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.MapAnimationOwnerRegistry
import com.mapbox.maps.plugin.viewport.ViewportStatus
import com.mapbox.maps.plugin.viewport.data.DefaultViewportTransitionOptions
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateBearing
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateOptions
import com.mapbox.maps.plugin.viewport.data.OverviewViewportStateOptions
import com.mapbox.maps.plugin.viewport.state.FollowPuckViewportState
import com.mapbox.maps.plugin.viewport.state.OverviewViewportState
import hu.mostoha.mobile.kmp.huki.features.map.MapUiEffects
import hu.mostoha.mobile.kmp.huki.model.domain.CameraTarget
import hu.mostoha.mobile.kmp.huki.model.domain.MyLocationStatus
import hu.mostoha.mobile.kmp.huki.util.AnimationConstants
import hu.mostoha.mobile.kmp.huki.util.AnimationConstants.MAP_FOLLOW_ANIM_DURATION
import hu.mostoha.mobile.kmp.huki.util.MapConstants
import kotlin.time.Duration

fun ViewportStatus.isIdle(): Boolean = this is ViewportStatus.Idle

/**
 * The follow-puck state the viewport is in or transitioning towards, or null when not following.
 */
private fun ViewportStatus.followPuckStateOrNull(): FollowPuckViewportState? =
    (this as? ViewportStatus.State)?.state as? FollowPuckViewportState
        ?: (this as? ViewportStatus.Transition)?.toState as? FollowPuckViewportState

fun ViewportStatus.isFollow(): Boolean = followPuckStateOrNull() != null

fun ViewportStatus.isOverview(): Boolean =
    (this as? ViewportStatus.State)?.state is OverviewViewportState ||
        (this as? ViewportStatus.Transition)?.toState is OverviewViewportState

fun Boolean.toDuration(duration: Duration): Long = if (this) duration.inWholeMilliseconds else 0

/**
 * Zooms the map in or out by one step, clamped to the min/max zoom.
 * Keeps the follow-puck state while following, otherwise eases the free camera.
 */
internal fun MapViewportState.zoom(zoomIn: Boolean) {
    val step = if (zoomIn) MapConstants.MAP_ZOOM_STEP else -MapConstants.MAP_ZOOM_STEP
    val currentZoom = cameraState?.zoom ?: return
    val newZoom = (currentZoom + step).coerceIn(MapConstants.MAP_MIN_ZOOM, MapConstants.MAP_MAX_ZOOM)
    val followState = mapViewportStatus?.followPuckStateOrNull()
    if (followState != null) {
        transitionToFollowPuckState(
            followPuckViewportStateOptions = followState.options.toBuilder()
                .zoom(newZoom)
                .build(),
            defaultTransitionOptions = DefaultViewportTransitionOptions.Builder()
                .maxDurationMs(MAP_FOLLOW_ANIM_DURATION.inWholeMilliseconds)
                .build(),
        )
    } else {
        easeTo(
            cameraOptions = CameraOptions.Builder()
                .zoom(newZoom)
                .build(),
            animationOptions = MapAnimationOptions.mapAnimationOptions {
                duration(MAP_FOLLOW_ANIM_DURATION.inWholeMilliseconds)
            },
        )
    }
}

/**
 * Transitions the camera to follow the location puck for the given status (north-up or live compass).
 * Preserves the current zoom while already following, otherwise applies at least the default follow zoom.
 */
internal fun MapViewportState.followLocation(myLocationStatus: MyLocationStatus, animated: Boolean) {
    val alreadyFollowing = mapViewportStatus?.isFollow() == true
    val currentZoom = cameraState?.zoom ?: MapConstants.FOLLOW_LOCATION_ZOOM_LEVEL
    val zoom = if (alreadyFollowing) {
        currentZoom
    } else {
        currentZoom.coerceAtLeast(MapConstants.FOLLOW_LOCATION_ZOOM_LEVEL)
    }
    val transitionOptions = DefaultViewportTransitionOptions.Builder()
        .maxDurationMs(animated.toDuration(MAP_FOLLOW_ANIM_DURATION))
        .build()
    when (myLocationStatus) {
        MyLocationStatus.Following -> {
            this.transitionToFollowPuckState(
                followPuckViewportStateOptions = FollowPuckViewportStateOptions.Builder()
                    .zoom(zoom)
                    .pitch(0.0)
                    .bearing(FollowPuckViewportStateBearing.Constant(0.0))
                    .build(),
                defaultTransitionOptions = transitionOptions,
            )
        }
        MyLocationStatus.FollowingLiveCompass -> {
            this.transitionToFollowPuckState(
                followPuckViewportStateOptions = FollowPuckViewportStateOptions.Builder()
                    .zoom(zoom)
                    .pitch(MapConstants.FOLLOW_LOCATION_LIVE_COMPASS_PITCH)
                    .bearing(FollowPuckViewportStateBearing.SyncWithLocationPuck)
                    .build(),
                defaultTransitionOptions = transitionOptions,
            )
        }
        MyLocationStatus.Default, MyLocationStatus.NotAvailable -> Unit
    }
}

/**
 * Eases the camera back to north-up, keeping the center and zoom.
 * Uses the GESTURES animation owner so it can interrupt an active viewport state.
 */
internal fun MapViewportState.resetBearing() {
    easeTo(
        cameraOptions = CameraOptions.Builder()
            .bearing(0.0)
            .build(),
        animationOptions = MapAnimationOptions.mapAnimationOptions {
            duration(MAP_FOLLOW_ANIM_DURATION.inWholeMilliseconds)
            owner(MapAnimationOwnerRegistry.GESTURES)
        },
    )
}

/**
 * Moves the camera to a single point (flyTo) or fits a bounds/route with padding (overview).
 */
internal fun MapViewportState.moveCamera(
    density: Density,
    effect: MapUiEffects.UpdateCamera,
    isLandscape: Boolean,
    routePlannerBottomInset: Dp? = null,
) {
    when (val target = effect.target) {
        is CameraTarget.Center -> this.flyTo(
            cameraOptions = CameraOptions.Builder()
                .apply {
                    center(target.location.toPoint())
                    target.zoom?.let { zoom(it) }
                    effect.bearing?.let { bearing(it) }
                    effect.pitch?.let { pitch(it) }
                }
                .build(),
            animationOptions = MapAnimationOptions.mapAnimationOptions {
                duration(AnimationConstants.MAP_CAMERA_ANIM_DURATION.inWholeMilliseconds)
            },
        )
        is CameraTarget.Bounds -> {
            val transitionOptions = DefaultViewportTransitionOptions.Builder()
                .maxDurationMs(AnimationConstants.MAP_CAMERA_ANIM_DURATION.inWholeMilliseconds)
                .build()
            this.transitionToOverviewState(
                overviewViewportStateOptions = OverviewViewportStateOptions.Builder()
                    .geometry(target.locations.toLineString())
                    .apply {
                        effect.bearing?.let { bearing(it) }
                        effect.pitch?.let { pitch(it) }
                        effect.contentPadding?.let {
                            padding(it.toEdgeInset(density, isLandscape, routePlannerBottomInset))
                        }
                        target.maxZoom?.let { maxZoom(it) }
                    }
                    .build(),
                defaultTransitionOptions = transitionOptions,
            )
        }
    }
}
