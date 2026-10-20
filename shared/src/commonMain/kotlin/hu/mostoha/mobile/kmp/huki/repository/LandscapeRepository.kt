package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.model.domain.LandscapeOutlines

interface LandscapeRepository {
    suspend fun getLandscapeOutlines(): LandscapeOutlines
}
