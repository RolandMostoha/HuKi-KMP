package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.model.domain.GpxDetails
import hu.mostoha.mobile.kmp.huki.model.domain.GpxFileItem

interface GpxRepository {
    suspend fun readGpxFile(uri: String): GpxDetails

    suspend fun getGpxFiles(): List<GpxFileItem>

    suspend fun deleteGpxFile(fileName: String)
}
