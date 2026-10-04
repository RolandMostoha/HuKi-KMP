package hu.mostoha.mobile.kmp.huki.data

import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.OktSectionGeometry
import hu.mostoha.mobile.kmp.huki.model.domain.OktStamp
import hu.mostoha.mobile.kmp.huki.model.domain.OktTrail
import hu.mostoha.mobile.kmp.huki.model.domain.OktType
import hu.mostoha.mobile.kmp.huki.model.mapper.toMarkerPositions
import hu.mostoha.mobile.kmp.huki.util.TrackProgressIndex

val TEST_OKT_FULL_TRAIL = OKT_SECTIONS.first { it.id == OKT_FULL_TRAIL_ID }
val TEST_OKT_SECTION_01 = OKT_SECTIONS.first { it.id == "OKT-01" }
val TEST_OKT_SECTION_02 = OKT_SECTIONS.first { it.id == "OKT-02" }

val TEST_OKT_LOCATIONS = listOf(
    TEST_OKT_SECTION_01.start,
    midpoint(TEST_OKT_SECTION_01.start, TEST_OKT_SECTION_01.end),
    TEST_OKT_SECTION_01.end,
    midpoint(TEST_OKT_SECTION_02.start, TEST_OKT_SECTION_02.end),
    TEST_OKT_SECTION_02.end,
)

val TEST_OKT_STAMP = OktStamp(
    title = "Hét-forrás",
    description = "Hét-forrás - A forrás melletti esőbeálló bejárati oszlopán. (OKTPH_02)",
    location = TEST_OKT_LOCATIONS[1],
    tag = "OKTPH_02",
    number = 2.0,
)

private val TEST_OKT_SECTIONS = listOf(
    OktSectionGeometry(TEST_OKT_FULL_TRAIL, TEST_OKT_LOCATIONS, emptyList()),
    OktSectionGeometry(TEST_OKT_SECTION_01, TEST_OKT_LOCATIONS.subList(0, 3), listOf(TEST_OKT_STAMP)),
    OktSectionGeometry(TEST_OKT_SECTION_02, TEST_OKT_LOCATIONS.subList(2, 5), emptyList()),
)

private val TEST_OKT_PROGRESS_INDEX = TrackProgressIndex(TEST_OKT_LOCATIONS)

val TEST_OKT_TRAIL = OktTrail(
    type = OktType.OKT,
    locations = TEST_OKT_LOCATIONS,
    sections = TEST_OKT_SECTIONS,
    baseLine = TEST_OKT_LOCATIONS,
    progressIndex = TEST_OKT_PROGRESS_INDEX,
    markerPositions = TEST_OKT_SECTIONS.toMarkerPositions(TEST_OKT_PROGRESS_INDEX),
)

private fun midpoint(start: Location, end: Location): Location =
    Location(
        latitude = (start.latitude + end.latitude) / 2,
        longitude = (start.longitude + end.longitude) / 2,
        altitude = 300.0,
    )
