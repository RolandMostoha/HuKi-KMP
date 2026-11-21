package hu.mostoha.mobile.kmp.huki.model.domain

import dev.icerock.moko.resources.desc.Raw
import dev.icerock.moko.resources.desc.StringDesc
import kotlin.uuid.Uuid

data class RoutePlannerWaypoint(
    val id: String = Uuid.random().toString(),
    // Can be a placeholder ("My location") or coordinates or reverse-geocoded name
    val name: StringDesc? = null,
    val placeName: String? = null,
    val location: Location? = null,
    val customName: String? = null,
    val comment: String? = null,
) {
    val isEmpty: Boolean
        get() = name == null && location == null

    /**
     * A user given name wins over the resolved one, which stays in [placeName] as the address.
     */
    val displayName: StringDesc?
        get() = customName?.let { StringDesc.Raw(it) } ?: name

    /**
     * The name a saved GPX file uses for this stop.
     */
    val gpxName: String?
        get() = customName ?: placeName
}
