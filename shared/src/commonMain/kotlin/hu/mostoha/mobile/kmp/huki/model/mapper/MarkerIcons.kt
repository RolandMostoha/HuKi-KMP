package hu.mostoha.mobile.kmp.huki.model.mapper

import dev.icerock.moko.resources.ImageResource
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.BaseLayer
import hu.mostoha.mobile.kmp.huki.model.domain.JelSymbol
import hu.mostoha.mobile.kmp.huki.model.domain.WaypointType

/**
 * Annotation symbols are color-graded by the Mapbox Outdoors LUT.
 * So dark mode on that style takes a twin painted in the color that *grades into* the wanted one.
 */
object MarkerIcons {

    fun waypointSymbol(type: WaypointType, isDarkMode: Boolean, baseLayer: BaseLayer): ImageResource =
        if (isColorGraded(isDarkMode, baseLayer)) type.outdoorsDarkIcon else type.icon

    /**
     * The trail marking replaces the glyph of the route's edge markers; intermediate waypoints keep theirs.
     */
    fun waypointJelSymbol(type: WaypointType, jelSymbol: JelSymbol?): JelSymbol? =
        jelSymbol.takeIf { type != WaypointType.INTERMEDIATE }

    fun jelMarkerFrame(isDarkMode: Boolean, baseLayer: BaseLayer): ImageResource =
        if (isColorGraded(isDarkMode, baseLayer)) {
            SharedRes.images.ic_gpx_jel_marker_frame_outdoors_dark
        } else {
            SharedRes.images.ic_gpx_jel_marker_frame
        }

    fun placeSymbol(isDarkMode: Boolean, baseLayer: BaseLayer): ImageResource =
        if (isColorGraded(isDarkMode, baseLayer)) {
            SharedRes.images.ic_marker_picker_outdoors_dark
        } else {
            SharedRes.images.ic_marker_picker
        }

    private fun isColorGraded(isDarkMode: Boolean, baseLayer: BaseLayer) = isDarkMode && baseLayer == BaseLayer.OUTDOORS
}
