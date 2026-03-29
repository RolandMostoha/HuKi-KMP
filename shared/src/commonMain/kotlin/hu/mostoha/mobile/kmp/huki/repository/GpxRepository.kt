package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.model.domain.GpxDetails

interface GpxRepository {
    suspend fun readGpxFile(uri: String): GpxDetails
}
