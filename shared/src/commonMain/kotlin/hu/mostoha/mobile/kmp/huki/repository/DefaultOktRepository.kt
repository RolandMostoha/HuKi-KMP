package hu.mostoha.mobile.kmp.huki.repository

import co.touchlab.kermit.Logger
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.OktSection
import hu.mostoha.mobile.kmp.huki.model.domain.OktSectionGeometry
import hu.mostoha.mobile.kmp.huki.model.domain.OktStamp
import hu.mostoha.mobile.kmp.huki.model.domain.OktTrail
import hu.mostoha.mobile.kmp.huki.model.domain.OktType
import hu.mostoha.mobile.kmp.huki.model.mapper.toMarkerPositions
import hu.mostoha.mobile.kmp.huki.model.mapper.toOktStamp
import hu.mostoha.mobile.kmp.huki.util.TrackProgressIndex
import hu.mostoha.mobile.kmp.huki.util.nearestIndexTo
import hu.mostoha.mobile.kmp.huki.util.simplify
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.maplibre.spatialk.gpx.Gpx
import org.maplibre.spatialk.units.extensions.meters
import kotlin.math.max
import kotlin.math.min
import kotlin.time.measureTimedValue

class DefaultOktRepository(
    private val bundledFileReader: BundledFileReader,
    private val dispatcher: CoroutineDispatcher,
) : OktRepository {

    private val mutex = Mutex()
    private val trails = mutableMapOf<OktType, OktTrail>()

    override suspend fun getOktTrail(type: OktType): OktTrail =
        mutex.withLock {
            trails.getOrPut(type) {
                withContext(dispatcher) {
                    val (trail, duration) = measureTimedValue { loadTrail(type) }
                    Logger.i { "Okt: loaded $trail in $duration" }
                    trail
                }
            }
        }

    private fun loadTrail(type: OktType): OktTrail {
        val gpx = Gpx.decodeFromString(bundledFileReader.readText(type.gpxFile))
        val locations = gpx.tracks
            .flatMap { it.segments }
            .flatMap { it.points }
            .map { Location(it.latitude, it.longitude, it.elevation) }
        val stamps = gpx.waypoints
            .mapNotNull { it.toOktStamp(type) }
            .sortedWith(compareBy({ it.number }, { it.tag }))
        if (stamps.size != gpx.waypoints.size) {
            Logger.w { "Okt: ${gpx.waypoints.size - stamps.size} waypoints without a stamp code in $type" }
        }

        val sections = type.sections.map { section ->
            if (section.id == type.fullTrailId) {
                OktSectionGeometry(section, locations, emptyList())
            } else {
                section.toGeometry(locations, stamps)
            }
        }
        val progressIndex = TrackProgressIndex(locations)

        return OktTrail(
            type = type,
            locations = locations,
            sections = sections,
            baseLine = locations.simplify(BASE_LINE_TOLERANCE),
            progressIndex = progressIndex,
            markerPositions = sections.toMarkerPositions(progressIndex),
        )
    }

    private fun OktSection.toGeometry(trailLocations: List<Location>, stamps: List<OktStamp>): OktSectionGeometry {
        val startIndex = trailLocations.nearestIndexTo(start)
        val endIndex = trailLocations.nearestIndexTo(end)

        return OktSectionGeometry(
            section = this,
            locations = trailLocations.subList(min(startIndex, endIndex), max(startIndex, endIndex) + 1),
            stamps = stamps.filter { it.number in stampRange },
        )
    }

    private companion object {
        val BASE_LINE_TOLERANCE = 10.meters
    }
}
