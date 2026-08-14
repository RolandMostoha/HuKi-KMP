package hu.mostoha.mobile.kmp.huki.model.domain

import dev.icerock.moko.resources.desc.StringDesc
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class RoutePlannerWaypoint(
    val id: String = Uuid.random().toString(),
    // Can be a placeholder ("My location") or coordinates or reverse-geocoded name
    val name: StringDesc? = null,
    val placeName: String? = null,
    val location: Location? = null,
) {
    val isEmpty: Boolean
        get() = name == null && location == null
}
