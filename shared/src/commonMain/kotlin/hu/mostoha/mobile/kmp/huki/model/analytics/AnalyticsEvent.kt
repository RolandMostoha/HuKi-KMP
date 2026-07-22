package hu.mostoha.mobile.kmp.huki.model.analytics

/**
 * Analytics event, following the Google Analytics way of sending a [name] and optional [params].
 *
 * Convention: low-cardinality, enumerable variants are flattened into the event [name]
 * (e.g. `layer_selected_satellite`) so they are filterable by event name with no console setup.
 * Reserve [params] for high-cardinality or continuous values (distances, counts, free identifiers).
 */
sealed interface AnalyticsEvent {
    val name: String
    val params: Map<String, String> get() = emptyMap()

    data object SearchOpened : AnalyticsEvent {
        override val name = "search_opened"
    }

    data object SearchFailed : AnalyticsEvent {
        override val name = "search_failed"
    }

    data object SearchRateLimited : AnalyticsEvent {
        override val name = "search_rate_limited"
    }

    data object SearchNoInternet : AnalyticsEvent {
        override val name = "search_no_internet"
    }

    data object SearchEmpty : AnalyticsEvent {
        override val name = "search_empty"
    }

    data object SearchPlaceSelected : AnalyticsEvent {
        override val name = "search_place_selected"
    }

    data object SearchPlaceHistorySelected : AnalyticsEvent {
        override val name = "search_place_history_selected"
    }

    data object HistoryPlaceSelected : AnalyticsEvent {
        override val name = "history_place_selected"
    }

    data object HistoryGpxSelected : AnalyticsEvent {
        override val name = "history_gpx_selected"
    }

    data class DestinationSelected(val destinationName: String) : AnalyticsEvent {
        override val name = "destination_selected"
        override val params = mapOf("selected_destination" to destinationName)
    }

    data class SearchDestinationSelected(val destinationName: String) : AnalyticsEvent {
        override val name = "search_destination_selected"
        override val params = mapOf("selected_destination" to destinationName)
    }

    data class ScreenView(val screen: Screen) : AnalyticsEvent {
        override val name = "screen_view"
        override val params = mapOf("screen_name" to screen.value)
    }

    data class LayerSelected(val layer: Layer) : AnalyticsEvent {
        override val name = "layer_selected_${layer.value}"
    }

    data class MyLocationFollowed(val mode: MyLocationMode) : AnalyticsEvent {
        override val name = "my_location_${mode.value}"
    }

    data object LocationPermissionGranted : AnalyticsEvent {
        override val name = "location_permission_granted"
    }

    data class LocationPermissionDenied(val deniedAlways: Boolean) : AnalyticsEvent {
        override val name = if (deniedAlways) "location_permission_denied_always" else "location_permission_denied"
    }

    data class GpxImported(val gpxSource: GpxSource) : AnalyticsEvent {
        override val name = "gpx_imported_${gpxSource.value}"
    }

    data object GpxImportFailed : AnalyticsEvent {
        override val name = "gpx_import_failed"
    }

    data object GpxNavigationStarted : AnalyticsEvent {
        override val name = "gpx_navigation_started"
    }

    data object GpxMapsNavigationOpened : AnalyticsEvent {
        override val name = "gpx_maps_navigation_opened"
    }

    data object GpxRouteVisibilityToggled : AnalyticsEvent {
        override val name = "gpx_route_visibility_toggled"
    }

    data object GpxDistancesToggled : AnalyticsEvent {
        override val name = "gpx_distances_toggled"
    }

    data object GpxOverviewClicked : AnalyticsEvent {
        override val name = "gpx_overview_clicked"
    }

    data object GpxClosed : AnalyticsEvent {
        override val name = "gpx_closed"
    }

    data object GpxDeleted : AnalyticsEvent {
        override val name = "gpx_deleted"
    }

    data class MenuLinkClicked(val link: MenuLink) : AnalyticsEvent {
        override val name = "menu_link_${link.value}"
    }

    data object SettingsZoomControlsToggled : AnalyticsEvent {
        override val name = "settings_zoom_controls_toggled"
    }
}
