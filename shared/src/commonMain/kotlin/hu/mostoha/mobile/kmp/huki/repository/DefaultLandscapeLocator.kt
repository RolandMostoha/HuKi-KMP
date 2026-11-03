package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.model.domain.BoundingBox
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.contains
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.PolygonGeometry
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.turf.booleans.contains

private const val MAX_SAMPLED_LOCATIONS = 200
private const val MIN_LANDSCAPE_SHARE = 1.0 / 3

/**
 * Finds the landscape of a track by a majority vote of its points, not by an exact match:
 * - Loads the bundled landscape outlines once and keeps them in memory.
 * - Samples every Nth location, so roughly [MAX_SAMPLED_LOCATIONS] points are checked even on long tracks.
 * - Checks each sample against the outlines: a bounding box prefilter, then the exact point-in-polygon test.
 * - The landscape holding the most samples wins, if it holds at least [MIN_LANDSCAPE_SHARE] of them, so a route
 *   that only touches a landscape's edge stays outside of it.
 */
class DefaultLandscapeLocator(private val landscapeRepository: LandscapeRepository) : LandscapeLocator {

    private val mutex = Mutex()
    private var areas: List<LandscapeArea>? = null

    override suspend fun findLandscapeId(locations: List<Location>): String? {
        if (locations.isEmpty()) return null

        val areas = loadAreas()
        val samples = locations.sampled()
        val (landscapeId, count) = samples
            .mapNotNull { location -> areas.firstOrNull { it.contains(location) }?.id }
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }
            ?: return null

        return landscapeId.takeIf { count >= samples.size * MIN_LANDSCAPE_SHARE }
    }

    private suspend fun loadAreas(): List<LandscapeArea> =
        mutex.withLock {
            areas ?: landscapeRepository.getLandscapeOutlines().let { outlines ->
                FeatureCollection.fromJson<Geometry, JsonObject>(outlines.geoJson).features.mapNotNull { feature ->
                    val id = feature.properties["id"]?.jsonPrimitive?.content ?: return@mapNotNull null
                    val polygon = feature.geometry as? PolygonGeometry ?: return@mapNotNull null
                    val bounds = outlines.bounds[id] ?: return@mapNotNull null
                    LandscapeArea(id = id, bounds = bounds, polygon = polygon)
                }
            }.also { areas = it }
        }

    private fun List<Location>.sampled(): List<Location> {
        val step = (size / MAX_SAMPLED_LOCATIONS).coerceAtLeast(1)
        return filterIndexed { index, _ -> index % step == 0 }
    }

    private class LandscapeArea(
        val id: String,
        val bounds: BoundingBox,
        val polygon: PolygonGeometry,
    ) {
        fun contains(location: Location): Boolean =
            bounds.contains(location) && polygon.contains(Position(location.longitude, location.latitude))
    }
}
