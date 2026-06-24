package hu.mostoha.mobile.kmp.huki.model.db

import androidx.room.Embedded
import androidx.room.Entity
import hu.mostoha.mobile.kmp.huki.model.domain.BoundingBox
import hu.mostoha.mobile.kmp.huki.model.domain.OsmType
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceCategory
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceSource

@Entity(tableName = "place_history", primaryKeys = ["osmType", "osmId"])
data class PlaceHistoryEntity(
    val osmType: OsmType,
    val osmId: String,
    val name: String,
    val nameNormalized: String,
    val address: String?,
    val latitude: Double,
    val longitude: Double,
    val placeCategory: PlaceCategory?,
    val placeSource: PlaceSource,
    @Embedded(prefix = "bounding_box_")
    val boundingBox: BoundingBox?,
    val lastVisited: Long,
)
