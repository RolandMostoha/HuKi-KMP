package hu.mostoha.mobile.kmp.huki.model.mapper

import hu.mostoha.mobile.kmp.huki.model.analytics.Layer
import hu.mostoha.mobile.kmp.huki.model.analytics.MyLocationMode
import hu.mostoha.mobile.kmp.huki.model.analytics.RouteProfile
import hu.mostoha.mobile.kmp.huki.model.analytics.Screen
import hu.mostoha.mobile.kmp.huki.model.domain.BaseLayer
import hu.mostoha.mobile.kmp.huki.model.domain.MyLocationStatus
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerProfile
import hu.mostoha.mobile.kmp.huki.model.domain.Sheet

fun BaseLayer.toLayer(): Layer =
    when (this) {
        BaseLayer.OUTDOORS -> Layer.OUTDOORS
        BaseLayer.STREET -> Layer.STREET
        BaseLayer.SATELLITE -> Layer.SATELLITE
    }

fun MyLocationStatus.toMyLocationMode(): MyLocationMode? =
    when (this) {
        MyLocationStatus.Following -> MyLocationMode.FOLLOWING
        MyLocationStatus.FollowingLiveCompass -> MyLocationMode.LIVE_COMPASS
        MyLocationStatus.Default, MyLocationStatus.NotAvailable -> null
    }

fun RoutePlannerProfile.toRouteProfile(): RouteProfile =
    when (this) {
        RoutePlannerProfile.ON_TRAILS -> RouteProfile.ON_TRAILS
        RoutePlannerProfile.SHORTEST_ROUTE -> RouteProfile.SHORTEST_ROUTE
        RoutePlannerProfile.BIKE -> RouteProfile.BIKE
    }

fun Sheet?.toScreen(): Screen =
    when (this) {
        null -> Screen.MAP
        Sheet.Search -> Screen.SEARCH
        Sheet.Layers -> Screen.LAYERS
        is Sheet.Gpx -> Screen.GPX_DETAILS
        Sheet.PlaceDetails -> Screen.PLACE_DETAILS
        is Sheet.RoutePlanner -> Screen.ROUTE_PLANNER
        is Sheet.WhatsNew -> Screen.WHATS_NEW
    }
