package hu.mostoha.mobile.kmp.huki.analytics

import hu.mostoha.mobile.kmp.huki.model.analytics.AnalyticsEvent
import hu.mostoha.mobile.kmp.huki.model.analytics.GpxSource
import hu.mostoha.mobile.kmp.huki.model.analytics.Layer
import hu.mostoha.mobile.kmp.huki.model.analytics.MenuLink
import hu.mostoha.mobile.kmp.huki.model.analytics.MyLocationMode
import hu.mostoha.mobile.kmp.huki.model.analytics.Screen
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class AnalyticsEventTest {

    @Test
    fun `Given analytics event, When read, Then it has expected name and params`() {
        testCases().forEach { testCase ->
            testCase.event.name shouldBe testCase.name
            testCase.event.params shouldBe testCase.params
        }
    }

    @Test
    fun `Given analytics event, When read, Then name and params obey Firebase limits`() {
        testCases().forEach { testCase ->
            val event = testCase.event
            (event.name.length <= EVENT_NAME_MAX_LENGTH) shouldBe true
            event.params.all { (key, value) ->
                key.length <= PARAM_NAME_MAX_LENGTH && value.length <= PARAM_VALUE_MAX_LENGTH
            } shouldBe true
        }
    }

    companion object {
        private const val EVENT_NAME_MAX_LENGTH = 40
        private const val PARAM_NAME_MAX_LENGTH = 40
        private const val PARAM_VALUE_MAX_LENGTH = 100

        fun testCases() =
            listOf(
                TestCase(AnalyticsEvent.SearchOpened, "search_opened", emptyMap()),
                TestCase(AnalyticsEvent.SearchFailed, "search_failed", emptyMap()),
                TestCase(AnalyticsEvent.SearchEmpty, "search_empty", emptyMap()),
                TestCase(AnalyticsEvent.SearchPlaceSelected, "search_place_selected", emptyMap()),
                TestCase(
                    AnalyticsEvent.SearchPlaceHistorySelected,
                    "search_place_history_selected",
                    emptyMap(),
                ),
                TestCase(AnalyticsEvent.HistoryPlaceSelected, "history_place_selected", emptyMap()),
                TestCase(AnalyticsEvent.HistoryGpxSelected, "history_gpx_selected", emptyMap()),
                TestCase(
                    AnalyticsEvent.DestinationSelected("Dobogókő"),
                    "destination_selected",
                    mapOf("selected_destination" to "Dobogókő"),
                ),
                TestCase(
                    AnalyticsEvent.SearchDestinationSelected("Dobogókő"),
                    "search_destination_selected",
                    mapOf("selected_destination" to "Dobogókő"),
                ),
                TestCase(
                    AnalyticsEvent.ScreenView(Screen.MAP),
                    "screen_view",
                    mapOf("screen_name" to "map"),
                ),
                TestCase(
                    AnalyticsEvent.ScreenView(Screen.SEARCH),
                    "screen_view",
                    mapOf("screen_name" to "search"),
                ),
                TestCase(
                    AnalyticsEvent.ScreenView(Screen.LAYERS),
                    "screen_view",
                    mapOf("screen_name" to "layers"),
                ),
                TestCase(
                    AnalyticsEvent.ScreenView(Screen.GPX_DETAILS),
                    "screen_view",
                    mapOf("screen_name" to "gpx_details"),
                ),
                TestCase(
                    AnalyticsEvent.ScreenView(Screen.WHATS_NEW),
                    "screen_view",
                    mapOf("screen_name" to "whats_new"),
                ),
                TestCase(
                    AnalyticsEvent.ScreenView(Screen.MENU),
                    "screen_view",
                    mapOf("screen_name" to "menu"),
                ),
                TestCase(
                    AnalyticsEvent.ScreenView(Screen.SETTINGS),
                    "screen_view",
                    mapOf("screen_name" to "settings"),
                ),
                TestCase(
                    AnalyticsEvent.ScreenView(Screen.DESTINATIONS),
                    "screen_view",
                    mapOf("screen_name" to "destinations"),
                ),
                TestCase(
                    AnalyticsEvent.ScreenView(Screen.GPX_HISTORY),
                    "screen_view",
                    mapOf("screen_name" to "gpx_history"),
                ),
                TestCase(
                    AnalyticsEvent.ScreenView(Screen.GPX_TUTORIAL),
                    "screen_view",
                    mapOf("screen_name" to "gpx_tutorial"),
                ),
                TestCase(
                    AnalyticsEvent.ScreenView(Screen.PLACE_HISTORY),
                    "screen_view",
                    mapOf("screen_name" to "place_history"),
                ),
                TestCase(
                    AnalyticsEvent.ScreenView(Screen.LOCATION_IQ),
                    "screen_view",
                    mapOf("screen_name" to "location_iq"),
                ),
                TestCase(AnalyticsEvent.LayerSelected(Layer.OUTDOORS), "layer_selected_outdoors", emptyMap()),
                TestCase(AnalyticsEvent.LayerSelected(Layer.STREET), "layer_selected_street", emptyMap()),
                TestCase(AnalyticsEvent.LayerSelected(Layer.SATELLITE), "layer_selected_satellite", emptyMap()),
                TestCase(AnalyticsEvent.LayerSelected(Layer.HIKING), "layer_selected_hiking", emptyMap()),
                TestCase(AnalyticsEvent.MyLocationFollowed(MyLocationMode.FOLLOWING), "my_location_following", emptyMap()),
                TestCase(
                    AnalyticsEvent.MyLocationFollowed(MyLocationMode.LIVE_COMPASS),
                    "my_location_live_compass",
                    emptyMap(),
                ),
                TestCase(AnalyticsEvent.GpxImported(GpxSource.FILES), "gpx_imported_files", emptyMap()),
                TestCase(AnalyticsEvent.GpxImported(GpxSource.LAYERS), "gpx_imported_layers", emptyMap()),
                TestCase(AnalyticsEvent.GpxImportFailed, "gpx_import_failed", emptyMap()),
                TestCase(AnalyticsEvent.GpxNavigationStarted, "gpx_navigation_started", emptyMap()),
                TestCase(AnalyticsEvent.GpxMapsNavigationOpened, "gpx_maps_navigation_opened", emptyMap()),
                TestCase(
                    AnalyticsEvent.GpxRouteVisibilityToggled,
                    "gpx_route_visibility_toggled",
                    emptyMap(),
                ),
                TestCase(AnalyticsEvent.GpxDistancesToggled, "gpx_distances_toggled", emptyMap()),
                TestCase(AnalyticsEvent.GpxOverviewClicked, "gpx_overview_clicked", emptyMap()),
                TestCase(AnalyticsEvent.GpxClosed, "gpx_closed", emptyMap()),
                TestCase(AnalyticsEvent.GpxDeleted, "gpx_deleted", emptyMap()),
                TestCase(AnalyticsEvent.MenuLinkClicked(MenuLink.EMAIL), "menu_link_email", emptyMap()),
                TestCase(AnalyticsEvent.MenuLinkClicked(MenuLink.FACEBOOK), "menu_link_facebook", emptyMap()),
                TestCase(AnalyticsEvent.MenuLinkClicked(MenuLink.GITHUB), "menu_link_github", emptyMap()),
                TestCase(
                    AnalyticsEvent.SettingsZoomControlsToggled,
                    "settings_zoom_controls_toggled",
                    emptyMap(),
                ),
            )
    }

    data class TestCase(
        val event: AnalyticsEvent,
        val name: String,
        val params: Map<String, String>,
    )
}
