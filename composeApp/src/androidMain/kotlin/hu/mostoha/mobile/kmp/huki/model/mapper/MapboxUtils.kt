package hu.mostoha.mobile.kmp.huki.model.mapper

import com.mapbox.maps.plugin.viewport.ViewportStatus
import com.mapbox.maps.plugin.viewport.state.FollowPuckViewportState
import kotlin.time.Duration

fun ViewportStatus.isFollow(): Boolean = (this as? ViewportStatus.State)?.state is FollowPuckViewportState

fun Boolean.toDuration(duration: Duration): Long = if (this) duration.inWholeMilliseconds else 0
