package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.model.domain.Location

interface LandscapeLocator {
    /**
     * Returns the [hu.mostoha.mobile.kmp.huki.model.domain.Landscape.id] that holds the largest share of
     * [locations], or null when no landscape holds a meaningful share of them.
     */
    suspend fun findLandscapeId(locations: List<Location>): String?
}
