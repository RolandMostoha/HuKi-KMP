package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.model.domain.GpxDetails
import hu.mostoha.mobile.kmp.huki.model.domain.GpxFileItem
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlan
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerProfile

interface GpxRepository {
    suspend fun readGpxFile(uri: String): GpxDetails

    suspend fun saveRoutePlan(
        routePlan: RoutePlan,
        stopPlaceNames: List<String?>,
        routeProfile: RoutePlannerProfile,
    ): String

    suspend fun getGpxFiles(): List<GpxFileItem>

    suspend fun getRecentGpxFiles(limit: Int): List<GpxFileItem>

    /**
     * Deletes a sandbox GPX and its cached attributes. Keyed on the uri and [trackId] rather than the file
     * name, which can repeat across origins.
     */
    suspend fun deleteGpxFile(fileUri: String, trackId: String)
}
