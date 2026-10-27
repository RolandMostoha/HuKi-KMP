package hu.mostoha.mobile.kmp.huki.model.mapper

import hu.mostoha.mobile.kmp.huki.model.domain.WaypointType
import io.kotest.matchers.collections.shouldBeSortedBy
import kotlin.test.Test

class WaypointMarkerOrderTest {

    @Test
    fun `Given waypoint types - When sortKey - Then start and end are drawn above intermediate waypoints`() {
        val types = listOf(WaypointType.INTERMEDIATE, WaypointType.START, WaypointType.END)

        val keys = types.map { WaypointMarkerOrder.sortKey(it) }

        keys shouldBeSortedBy { it }
    }
}
