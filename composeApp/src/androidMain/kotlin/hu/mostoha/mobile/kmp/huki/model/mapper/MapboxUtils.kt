package hu.mostoha.mobile.kmp.huki.model.mapper

import com.mapbox.maps.plugin.viewport.ViewportStatus
import com.mapbox.maps.plugin.viewport.state.FollowPuckViewportState
import com.mapbox.maps.plugin.viewport.state.OverviewViewportState
import kotlin.time.Duration

fun ViewportStatus.isIdle(): Boolean = this is ViewportStatus.Idle

fun ViewportStatus.isFollow(): Boolean = (this as? ViewportStatus.State)?.state is FollowPuckViewportState

fun ViewportStatus.isOverview(): Boolean =
    (this as? ViewportStatus.State)?.state is OverviewViewportState ||
        (this as? ViewportStatus.Transition)?.toState is OverviewViewportState

fun Boolean.toDuration(duration: Duration): Long = if (this) duration.inWholeMilliseconds else 0
