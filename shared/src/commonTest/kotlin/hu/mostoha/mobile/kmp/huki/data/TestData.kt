package hu.mostoha.mobile.kmp.huki.data

import hu.mostoha.mobile.kmp.huki.model.domain.GpxDetails
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import org.maplibre.spatialk.units.extensions.kilometers
import kotlin.time.Duration.Companion.hours

val TEST_GPX_WAY_CLOSED = listOf(
    Location(47.6848363, 18.9194592, 300.0),
    Location(47.68592367426645, 18.90795119564933, 400.0),
    Location(47.68143363252469, 18.89978418689257, 350.0),
    Location(47.6848363, 18.9194592, 300.0),
)

val TEST_GPX_DETAILS = GpxDetails(
    fileName = "dera_szurdok.gpx",
    fileUri = "file://dera_szurdok.gpx",
    title = "OKT 5 - Rozália téglagyár - Dobogókő",
    locations = TEST_GPX_WAY_CLOSED,
    waypoints = emptyList(),
    bounds = TEST_GPX_WAY_CLOSED,
    totalDistance = 15.kilometers,
    travelTime = 2.hours,
    altitudeRange = 300 to 800,
    incline = 500,
    decline = 300,
)
